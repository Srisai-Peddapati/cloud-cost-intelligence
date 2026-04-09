package com.app.pricing;

import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.pricing.PricingClient;
import software.amazon.awssdk.services.pricing.model.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.*;
import java.time.LocalDateTime;

/**
 * AWS Pricing Service - Fetches REAL pricing from AWS Pricing API
 * NO FALLBACK - Uses real AWS pricing only
 */
@Service
public class AWSPricingService {
    
    private final PricingClient pricingClient;
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    private Map<String, RegionalPricing> priceCache = new HashMap<>();
    private LocalDateTime lastUpdated = LocalDateTime.now();
    private static final long CACHE_DURATION_HOURS = 24;
    
    public AWSPricingService() {
        try {
            this.pricingClient = PricingClient.builder().region(
                software.amazon.awssdk.regions.Region.US_EAST_1
            ).build();
            System.out.println("✓ AWS Pricing Client initialized - REAL AWS PRICING ENABLED (NO FALLBACK)");
        } catch (Exception e) {
            throw new RuntimeException(
                "CRITICAL: Failed to initialize AWS Pricing Client.\n" +
                "Set AWS credentials:\n" +
                "export AWS_ACCESS_KEY_ID=\"your-key\"\n" +
                "export AWS_SECRET_ACCESS_KEY=\"your-secret\"\n" +
                "NO FALLBACK MODE - Application requires real AWS credentials", e
            );
        }
    }
    
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
    
    private String generateCacheKey(PricingQuery query) {
        return String.format("%s_%s_%s", query.service, query.resourceType, query.region);
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
     * Fetch real pricing from AWS Pricing API - NO FALLBACK
     */
    private RegionalPricing fetchPriceFromAWS(PricingQuery query) {
        try {
            String serviceCode = getServiceCode(query.service);
            String regionName = getRegionName(query.region);
            
            List<Filter> filters = new ArrayList<>();
            filters.add(Filter.builder()
                .type(FilterType.TERM_MATCH)
                .field("location")
                .value(regionName)
                .build());
            
            if ("EC2".equalsIgnoreCase(query.service)) {
                filters.add(Filter.builder()
                    .type(FilterType.TERM_MATCH)
                    .field("operatingSystem")
                    .value("Linux")
                    .build());
                
                filters.add(Filter.builder()
                    .type(FilterType.TERM_MATCH)
                    .field("instanceType")
                    .value(query.resourceType)
                    .build());
            } else if ("RDS".equalsIgnoreCase(query.service)) {
                filters.add(Filter.builder()
                    .type(FilterType.TERM_MATCH)
                    .field("instanceType")
                    .value(query.resourceType)
                    .build());
            }
            
            GetProductsRequest request = GetProductsRequest.builder()
                .serviceCode(serviceCode)
                .filters(filters)
                .maxResults(1)
                .build();
            
            GetProductsResponse response = pricingClient.getProducts(request);
            
            if (response.priceList().isEmpty()) {
                throw new RuntimeException(
                    "NO PRICING FOUND: " + query.service + " " + query.resourceType + 
                    " in " + query.region
                );
            }
            
            String priceJson = response.priceList().get(0);
            double hourlyPrice = parsePrice(priceJson);
            
            System.out.println("✓ Real AWS: $" + String.format("%.6f", hourlyPrice) + "/hr ($" + 
                String.format("%.2f", hourlyPrice * 730) + "/mo)");
            
            return new RegionalPricing(query.service, query.resourceType, query.region, hourlyPrice);
            
        } catch (Exception e) {
            String error = "\n❌ FATAL: Real AWS Pricing API failed (NO FALLBACK)\n" +
                "Service: " + query.service + "\n" +
                "Resource: " + query.resourceType + "\n" +
                "Error: " + e.getMessage();
            System.err.println(error);
            throw new RuntimeException(error, e);
        }
    }
    
    
    /**
     * Parse price from AWS Pricing API JSON response
     */
    private double parsePrice(String priceJson) throws Exception {
        JsonNode root = objectMapper.readTree(priceJson);
        JsonNode termsNode = root.path("terms");
        JsonNode onDemandNode = termsNode.path("OnDemand");
        JsonNode skuNode = onDemandNode.elements().next();
        JsonNode priceDimensionsNode = skuNode.path("priceDimensions");
        JsonNode priceNode = priceDimensionsNode.elements().next()
            .path("pricePerUnit")
            .path("USD");
        
        return Double.parseDouble(priceNode.asText());
    }
    
    private String getServiceCode(String service) {
        switch(service.toUpperCase()) {
            case "EC2": return "AmazonEC2";
            case "RDS": return "AmazonRDS";
            case "S3": return "AmazonS3";
            case "LAMBDA": return "AWSLambda";
            case "ALB":
            case "ELB": return "ElasticLoadBalancing";
            default: throw new RuntimeException("Unsupported service: " + service);
        }
    }
    
    private String getRegionName(String region) {
        Map<String, String> regionMap = new HashMap<>();
        regionMap.put("us-east-1", "US East (N. Virginia)");
        regionMap.put("us-east-2", "US East (Ohio)");
        regionMap.put("us-west-1", "US West (N. California)");
        regionMap.put("us-west-2", "US West (Oregon)");
        regionMap.put("eu-west-1", "EU (Ireland)");
        regionMap.put("eu-central-1", "EU (Frankfurt)");
        regionMap.put("eu-west-2", "EU (London)");
        regionMap.put("ap-southeast-1", "Asia Pacific (Singapore)");
        regionMap.put("ap-northeast-1", "Asia Pacific (Tokyo)");
        regionMap.put("ap-south-1", "Asia Pacific (Mumbai)");
        
        String regionName = regionMap.get(region);
        if (regionName == null) {
            throw new RuntimeException("Unsupported region: " + region);
        }
        return regionName;
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

