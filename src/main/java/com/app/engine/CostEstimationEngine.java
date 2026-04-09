package com.app.engine;

import com.app.config.PricingConfig;
import com.app.model.AWSResource;
import com.app.model.CostEstimate;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class CostEstimationEngine {
    
    private final PricingConfig pricingConfig;
    
    // Traffic profiles: requests per minute
    private static final int LOW_TRAFFIC = 100;
    private static final int MEDIUM_TRAFFIC = 1000;
    private static final int HIGH_TRAFFIC = 10000;
    
    // Performance constants
    private static final int REQUESTS_PER_SECOND_PER_EC2 = 50;
    private static final int REQUESTS_PER_SECOND_PER_LAMBDA = 100;
    private static final int DB_CONNECTIONS_PER_EC2 = 20;
    
    public CostEstimationEngine(PricingConfig pricingConfig) {
        this.pricingConfig = pricingConfig;
    }
    
    public CostEstimate calculateCosts(List<AWSResource> resources) {
        return CostEstimate.builder()
            .lowTraffic(calculateTrafficCost(resources, LOW_TRAFFIC))
            .mediumTraffic(calculateTrafficCost(resources, MEDIUM_TRAFFIC))
            .highTraffic(calculateTrafficCost(resources, HIGH_TRAFFIC))
            .breakdown(calculateBreakdown(resources))
            .build();
    }
    
    private CostEstimate.TrafficCost calculateTrafficCost(List<AWSResource> resources, int requestsPerMin) {
        double monthlyCost = calculateMonthlyCost(resources, requestsPerMin);
        double yearlyCost = monthlyCost * 12;
        
        return CostEstimate.TrafficCost.builder()
            .requestsPerMin(requestsPerMin)
            .monthlyCost(Math.round(monthlyCost * 100.0) / 100.0)
            .yearlyCost(Math.round(yearlyCost * 100.0) / 100.0)
            .build();
    }
    
    private double calculateMonthlyCost(List<AWSResource> resources, int requestsPerMin) {
        double totalCost = 0;
        
        int ec2Count = 0;
        int rdsCount = 0;
        double s3Size = 0;
        int lbCount = 0;
        int lambdaCount = 0;
        
        // Aggregate resources
        for (AWSResource resource : resources) {
            if ("ec2".equals(resource.getType())) {
                ec2Count += resource.getCount();
            } else if ("rds".equals(resource.getType())) {
                rdsCount += resource.getCount();
            } else if ("s3".equals(resource.getType())) {
                s3Size += parseSize(resource.getSize());
            } else if ("load_balancer".equals(resource.getType())) {
                lbCount += resource.getCount();
            } else if ("lambda".equals(resource.getType())) {
                lambdaCount += resource.getCount();
            }
        }
        
        // Calculate compute costs
        totalCost += calculateEC2Cost(resources, ec2Count);
        
        // Calculate RDS costs
        totalCost += calculateRDSCost(resources, rdsCount);
        
        // Calculate S3 costs
        totalCost += calculateS3Cost(resources, s3Size);
        
        // Calculate Load Balancer costs
        totalCost += calculateLoadBalancerCost(lbCount);
        
        // Calculate Lambda costs (if present)
        if (lambdaCount > 0) {
            totalCost += calculateLambdaCost(requestsPerMin, lambdaCount);
        }
        
        // Add data transfer costs based on traffic
        totalCost += calculateDataTransferCost(requestsPerMin);
        
        return totalCost;
    }
    
    private double calculateEC2Cost(List<AWSResource> resources, int count) {
        double cost = 0;
        int countProcessed = 0;
        
        for (AWSResource resource : resources) {
            if ("ec2".equals(resource.getType())) {
                double unitPrice = pricingConfig.getEC2Price(resource.getInstanceType());
                cost += unitPrice * resource.getCount();
                countProcessed += resource.getCount();
                
                if (countProcessed >= count) break;
            }
        }
        
        return cost;
    }
    
    private double calculateRDSCost(List<AWSResource> resources, int count) {
        double cost = 0;
        int countProcessed = 0;
        
        for (AWSResource resource : resources) {
            if ("rds".equals(resource.getType())) {
                double unitPrice = pricingConfig.getRDSPrice(resource.getInstanceType());
                cost += unitPrice * resource.getCount();
                countProcessed += resource.getCount();
                
                if (countProcessed >= count) break;
            }
        }
        
        return cost;
    }
    
    private double calculateS3Cost(List<AWSResource> resources, double totalSizeGB) {
        double cost = 0;
        
        for (AWSResource resource : resources) {
            if ("s3".equals(resource.getType())) {
                double price = pricingConfig.getS3Price(resource.getTier());
                double sizeGB = parseSize(resource.getSize());
                cost += price * sizeGB;
            }
        }
        
        return cost;
    }
    
    private double calculateLoadBalancerCost(int count) {
        return PricingConfig.ALB_PRICE_PER_MONTH * count;
    }
    
    private double calculateLambdaCost(int requestsPerMin, int lambdaCount) {
        // Calculate based on requests per month
        long requestsPerMonth = (long) requestsPerMin * 60 * 24 * 30;
        double costPerRequest = PricingConfig.LAMBDA_PRICE_PER_MILLION_REQUESTS / 1_000_000;
        double estimatedDuration = 300; // 300ms average execution time
        double costPerSecond = PricingConfig.LAMBDA_PRICE_PER_GB_SECOND;
        
        // Cost based on invocations
        double invocationCost = (requestsPerMonth / 1_000_000) * PricingConfig.LAMBDA_PRICE_PER_MILLION_REQUESTS;
        
        // Cost based on duration (assuming 512MB memory = 0.5GB)
        double executionTimeSeconds = (requestsPerMonth * estimatedDuration) / 1000;
        double durationCost = executionTimeSeconds * 0.5 * costPerSecond;
        
        return (invocationCost + durationCost) * lambdaCount;
    }
    
    private double calculateDataTransferCost(int requestsPerMin) {
        // Estimate 1KB per request
        long requestsPerMonth = (long) requestsPerMin * 60 * 24 * 30;
        double dataTransferGB = (requestsPerMonth * 1) / (1024 * 1024); // Convert KB to GB
        
        return dataTransferGB * PricingConfig.DATA_TRANSFER_OUT_PRICE;
    }
    
    private CostEstimate.CostBreakdown calculateBreakdown(List<AWSResource> resources) {
        double computeCost = 0;
        double databaseCost = 0;
        double storageCost = 0;
        double loadBalancerCost = 0;
        
        for (AWSResource resource : resources) {
            if ("ec2".equals(resource.getType())) {
                computeCost += pricingConfig.getEC2Price(resource.getInstanceType()) * resource.getCount();
            } else if ("rds".equals(resource.getType())) {
                databaseCost += pricingConfig.getRDSPrice(resource.getInstanceType()) * resource.getCount();
            } else if ("s3".equals(resource.getType())) {
                double sizeGB = parseSize(resource.getSize());
                double price = pricingConfig.getS3Price(resource.getTier());
                storageCost += price * sizeGB;
            } else if ("load_balancer".equals(resource.getType())) {
                loadBalancerCost += pricingConfig.getLoadBalancerPrice(resource.getInstanceType());
            }
        }
        
        double totalCost = computeCost + databaseCost + storageCost + loadBalancerCost;
        
        return CostEstimate.CostBreakdown.builder()
            .computeCost(Math.round(computeCost * 100.0) / 100.0)
            .databaseCost(Math.round(databaseCost * 100.0) / 100.0)
            .storageCost(Math.round(storageCost * 100.0) / 100.0)
            .loadBalancerCost(Math.round(loadBalancerCost * 100.0) / 100.0)
            .totalCost(Math.round(totalCost * 100.0) / 100.0)
            .build();
    }
    
    private double parseSize(String sizeStr) {
        if (sizeStr == null || sizeStr.isEmpty()) {
            return 100; // Default to 100GB
        }
        
        String normalized = sizeStr.toUpperCase();
        double size = Double.parseDouble(normalized.replaceAll("[^0-9.]", ""));
        
        if (normalized.contains("TB")) {
            size *= 1024;
        } else if (normalized.contains("MB")) {
            size /= 1024;
        }
        
        return size;
    }
}

