package com.app.config;

import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.Map;

@Component
public class PricingConfig {
    
    // EC2 Instance Pricing (per month in USD)
    public static final Map<String, Double> EC2_PRICING = new HashMap<>();
    static {
        EC2_PRICING.put("t3.micro", 8.47);
        EC2_PRICING.put("t3.small", 16.94);
        EC2_PRICING.put("t3.medium", 33.88);
        EC2_PRICING.put("t3.large", 67.76);
        EC2_PRICING.put("t3.xlarge", 135.52);
        EC2_PRICING.put("t3.2xlarge", 271.04);
        EC2_PRICING.put("m5.large", 96.00);
        EC2_PRICING.put("m5.xlarge", 192.00);
        EC2_PRICING.put("m5.2xlarge", 384.00);
        EC2_PRICING.put("c5.large", 85.00);
        EC2_PRICING.put("c5.xlarge", 170.00);
        EC2_PRICING.put("c5.2xlarge", 340.00);
        EC2_PRICING.put("default", 50.00); // fallback price
    }
    
    // RDS Instance Pricing (per month in USD)
    public static final Map<String, Double> RDS_PRICING = new HashMap<>();
    static {
        RDS_PRICING.put("db.t3.micro", 19.70);
        RDS_PRICING.put("db.t3.small", 39.40);
        RDS_PRICING.put("db.t3.medium", 78.80);
        RDS_PRICING.put("db.m5.large", 200.00);
        RDS_PRICING.put("db.m5.xlarge", 400.00);
        RDS_PRICING.put("db.m5.2xlarge", 800.00);
        RDS_PRICING.put("db.r5.large", 550.00);
        RDS_PRICING.put("db.r5.xlarge", 1100.00);
        RDS_PRICING.put("default", 100.00); // fallback price
    }
    
    // S3 Pricing (per GB per month in USD)
    public static final double S3_STANDARD_PRICING = 0.023;
    public static final double S3_IA_PRICING = 0.0125;
    public static final double S3_GLACIER_PRICING = 0.004;
    
    // Lambda Pricing (per million requests)
    public static final double LAMBDA_PRICE_PER_MILLION_REQUESTS = 0.20;
    public static final double LAMBDA_PRICE_PER_GB_SECOND = 0.0000166667;
    
    // Load Balancer Pricing (per month)
    public static final double ALB_PRICE_PER_MONTH = 20.00;
    public static final double NLB_PRICE_PER_MONTH = 32.50;
    
    // Data Transfer Pricing (per GB)
    public static final double DATA_TRANSFER_OUT_PRICE = 0.09;
    public static final double DATA_TRANSFER_TO_INTERNET = 0.09;
    
    public Double getEC2Price(String instanceType) {
        return EC2_PRICING.getOrDefault(instanceType.toLowerCase(), EC2_PRICING.get("default"));
    }
    
    public Double getRDSPrice(String instanceType) {
        return RDS_PRICING.getOrDefault(instanceType.toLowerCase(), RDS_PRICING.get("default"));
    }
    
    public double getS3Price(String tier) {
        return switch (tier.toLowerCase()) {
            case "ia", "infrequent_access" -> S3_IA_PRICING;
            case "glacier" -> S3_GLACIER_PRICING;
            default -> S3_STANDARD_PRICING;
        };
    }
    
    public Double getLoadBalancerPrice(String type) {
        return switch (type.toLowerCase()) {
            case "nlb", "network" -> NLB_PRICE_PER_MONTH;
            default -> ALB_PRICE_PER_MONTH; // Default to ALB
        };
    }
}

