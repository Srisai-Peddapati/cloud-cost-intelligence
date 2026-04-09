package com.app.pricing;

import org.springframework.stereotype.Service;
import java.util.*;
import java.time.LocalDateTime;

/**
 * Dynamic AWS Pricing Service that fetches real pricing from AWS
 * In production, integrate with AWS Pricing API
 * For now, provides caching and regional pricing support
 */
@Service
public class AWSPricingService {
    
    private Map<String, RegionalPricing> priceCache = new HashMap<>();
    private LocalDateTime lastUpdated = LocalDateTime.now();
    private static final long CACHE_DURATION_HOURS = 24;
    
    public static class PricingQuery {
        public String service; // "EC2", "RDS", "S3", etc.
        public String resourceType; // "t3.large", "db.m5.large", etc.
        public String region; // "us-east-1", "eu-west-1", etc.
        public String osType; // "Linux", "Windows", etc.
        public String tenancy; // "default", "dedicated", etc.
        
        public PricingQuery(String service, String resourceType, String region) {
            this.service = service;
            this.resourceType = resourceType;
            this.region = region != null ? region : "us-east-1";
        }
    }
    
    public static class RegionalPricing {
        public String service;
        public String resource;
        public String region;
        public double hourlyPrice;
        public double monthlyPrice; // Calculated: hourlyPrice * 730 (avg hours/month)
        public double yearlyPrice; // monthlyPrice * 12
        public String currency;
        public LocalDateTime fetchedAt;
        
        public RegionalPricing(String service, String resource, String region, double hourlyPrice) {
            this.service = service;
            this.resource = resource;
            this.region = region;
            this.hourlyPrice = hourlyPrice;
            this.monthlyPrice = hourlyPrice * 730;
            this.yearlyPrice = monthlyPrice * 12;
            this.currency = "USD";
            this.fetchedAt = LocalDateTime.now();
        }
    }
    
    /**
     * Get pricing for an AWS resource
     * In production, this would call AWS Pricing API
     */
    public RegionalPricing getPrice(PricingQuery query) {
        String cacheKey = generateCacheKey(query);
        
        // Check cache
        if (priceCache.containsKey(cacheKey) && !isCacheExpired()) {
            return priceCache.get(cacheKey);
        }
        
        // Fetch from AWS Pricing API (or use hardcoded fallback)
        RegionalPricing pricing = fetchPriceFromAWS(query);
        
        // Cache the result
        priceCache.put(cacheKey, pricing);
        lastUpdated = LocalDateTime.now();
        
        return pricing;
    }
    
    /**
     * Get prices for multiple resources at once
     */
    public Map<String, RegionalPricing> getPrices(List<PricingQuery> queries) {
        Map<String, RegionalPricing> results = new HashMap<>();
        for (PricingQuery query : queries) {
            results.put(generateCacheKey(query), getPrice(query));
        }
        return results;
    }
    
    /**
     * Fetch price from AWS Pricing API
     * TODO: Integrate with actual AWS Pricing API
     */
    private RegionalPricing fetchPriceFromAWS(PricingQuery query) {
        // In production, call AWS Pricing API
        // For now, use hardcoded fallback with regional adjustments
        
        double basePrice = getBasePriceFromFallback(query);
        double regionalMultiplier = getRegionalMultiplier(query.region);
        double finalHourlyPrice = basePrice * regionalMultiplier;
        
        return new RegionalPricing(query.service, query.resourceType, query.region, finalHourlyPrice);
    }
    
    /**
     * Hardcoded fallback pricing (as in original PricingConfig)
     */
    private double getBasePriceFromFallback(PricingQuery query) {
        if ("EC2".equalsIgnoreCase(query.service)) {
            return getEC2Price(query.resourceType);
        }
        if ("RDS".equalsIgnoreCase(query.service)) {
            return getRDSPrice(query.resourceType);
        }
        if ("S3".equalsIgnoreCase(query.service)) {
            return getS3Price(query.resourceType);
        }
        if ("Lambda".equalsIgnoreCase(query.service)) {
            return 0.0000002; // Per millisecond
        }
        if ("ALB".equalsIgnoreCase(query.service) || "ELB".equalsIgnoreCase(query.service)) {
            return getLoadBalancerPrice(query.resourceType);
        }
        return 0.0;
    }
    
    /**
     * Regional price multiplier (1.0 = us-east-1)
     */
    private double getRegionalMultiplier(String region) {
        Map<String, Double> multipliers = new HashMap<>();
        multipliers.put("us-east-1", 1.0);
        multipliers.put("us-east-2", 0.95);
        multipliers.put("us-west-1", 1.1);
        multipliers.put("us-west-2", 0.95);
        multipliers.put("eu-west-1", 1.15);
        multipliers.put("eu-central-1", 1.2);
        multipliers.put("ap-southeast-1", 1.15);
        multipliers.put("ap-northeast-1", 1.25);
        multipliers.put("ap-south-1", 0.9);
        
        return multipliers.getOrDefault(region, 1.0);
    }
    
    private double getEC2Price(String instanceType) {
        Map<String, Double> pricing = new HashMap<>();
        pricing.put("t3.micro", 0.0104);
        pricing.put("t3.small", 0.0208);
        pricing.put("t3.medium", 0.0416);
        pricing.put("t3.large", 0.0832);
        pricing.put("t3.xlarge", 0.1664);
        pricing.put("t3.2xlarge", 0.3328);
        pricing.put("m5.large", 0.096);
        pricing.put("m5.xlarge", 0.192);
        pricing.put("m5.2xlarge", 0.384);
        pricing.put("c5.large", 0.085);
        pricing.put("c5.xlarge", 0.17);
        pricing.put("c5.2xlarge", 0.34);
        return pricing.getOrDefault(instanceType.toLowerCase(), 0.05);
    }
    
    private double getRDSPrice(String instanceClass) {
        Map<String, Double> pricing = new HashMap<>();
        pricing.put("db.t3.micro", 0.017);
        pricing.put("db.t3.small", 0.034);
        pricing.put("db.t3.medium", 0.068);
        pricing.put("db.m5.large", 0.203);
        pricing.put("db.m5.xlarge", 0.406);
        pricing.put("db.m5.2xlarge", 0.812);
        pricing.put("db.r5.large", 0.58);
        pricing.put("db.r5.xlarge", 1.16);
        return pricing.getOrDefault(instanceClass.toLowerCase(), 0.1);
    }
    
    private double getS3Price(String tier) {
        // Price per GB per month
        Map<String, Double> pricing = new HashMap<>();
        pricing.put("standard", 0.023);
        pricing.put("infrequent_access", 0.0125);
        pricing.put("glacier", 0.004);
        return pricing.getOrDefault(tier.toLowerCase(), 0.023);
    }
    
    private double getLoadBalancerPrice(String type) {
        // Price per hour
        return "nlb".equalsIgnoreCase(type) ? 0.0351 : 0.0252; // ALB default
    }
    
    private String generateCacheKey(PricingQuery query) {
        return String.format("%s_%s_%s", query.service, query.resourceType, query.region);
    }
    
    private boolean isCacheExpired() {
        return LocalDateTime.now().minusHours(CACHE_DURATION_HOURS).isAfter(lastUpdated);
    }
    
    /**
     * Clear the cache (for testing or manual refresh)
     */
    public void clearCache() {
        priceCache.clear();
        lastUpdated = LocalDateTime.now();
    }
    
    /**
     * Get cache statistics
     */
    public Map<String, Object> getCacheStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("size", priceCache.size());
        stats.put("lastUpdated", lastUpdated);
        stats.put("expired", isCacheExpired());
        return stats;
    }
}

