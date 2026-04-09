package com.app.engine;

import com.app.model.AWSResource;
import com.app.model.Optimization;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;

@Component
public class OptimizationEngine {
    
    public List<Optimization> generateOptimizations(List<AWSResource> resources) {
        List<Optimization> optimizations = new ArrayList<>();
        
        // Analyze EC2 instances
        analyzeEC2(resources, optimizations);
        
        // Analyze RDS instances
        analyzeRDS(resources, optimizations);
        
        // Analyze S3 buckets
        analyzeS3(resources, optimizations);
        
        // Analyze Load Balancers
        analyzeLoadBalancers(resources, optimizations);
        
        // Analyze Lambda usage
        analyzeLambda(resources, optimizations);
        
        // Architecture-level optimizations
        addArchitectureOptimizations(resources, optimizations);
        
        // Sort by priority
        optimizations.sort((o1, o2) -> Integer.compare(o2.getPriority(), o1.getPriority()));
        
        return optimizations;
    }
    
    private void analyzeEC2(List<AWSResource> resources, List<Optimization> optimizations) {
        int largeInstances = 0;
        int totalEC2 = 0;
        
        for (AWSResource resource : resources) {
            if ("ec2".equals(resource.getType())) {
                totalEC2 += resource.getCount();
                
                String type = resource.getInstanceType().toLowerCase();
                if (type.contains("large") || type.contains("xlarge") || type.contains("2xlarge")) {
                    largeInstances += resource.getCount();
                }
            }
        }
        
        // Optimization: Downsize large instances
        if (largeInstances > 0) {
            optimizations.add(Optimization.builder()
                .suggestion("Downsize large EC2 instances")
                .description("Consider using smaller instance types (t3.medium or t3.large) during low traffic periods")
                .savingsPercentage("30-40%")
                .monthlySavings(500.0) // Estimated
                .yearlySavings(6000.0)
                .tradeoff("May experience slower performance under peak loads if not paired with auto-scaling")
                .category("compute")
                .priority(4)
                .complexity("low")
                .build());
        }
        
        // Optimization: Use Reserved Instances
        if (totalEC2 > 0) {
            optimizations.add(Optimization.builder()
                .suggestion("Use Reserved Instances (RI) or Savings Plans")
                .description("Commit to 1-3 year terms for consistent workloads")
                .savingsPercentage("30-60%")
                .monthlySavings(1000.0)
                .yearlySavings(12000.0)
                .tradeoff("Requires upfront commitment; less flexibility for sudden scaling down")
                .category("compute")
                .priority(5)
                .complexity("medium")
                .build());
        }
        
        // Optimization: Enable Auto-Scaling
        if (totalEC2 > 2) {
            optimizations.add(Optimization.builder()
                .suggestion("Enable Auto-Scaling for EC2 instances")
                .description("Use Auto Scaling Groups to dynamically adjust instance count based on demand")
                .savingsPercentage("20-50%")
                .monthlySavings(800.0)
                .yearlySavings(9600.0)
                .tradeoff("Slight latency during scaling events (typically 1-2 minutes)")
                .category("compute")
                .priority(4)
                .complexity("medium")
                .build());
        }
        
        // Optimization: Use Spot Instances
        optimizations.add(Optimization.builder()
            .suggestion("Use Spot Instances for non-critical workloads")
            .description("Run fault-tolerant applications on Spot Instances for up to 90% discount")
            .savingsPercentage("70-90%")
            .monthlySavings(1500.0)
            .yearlySavings(18000.0)
            .tradeoff("Can be interrupted with 2-minute notice; not suitable for critical applications")
            .category("compute")
            .priority(3)
            .complexity("high")
            .build());
    }
    
    private void analyzeRDS(List<AWSResource> resources, List<Optimization> optimizations) {
        int totalRDS = 0;
        
        for (AWSResource resource : resources) {
            if ("rds".equals(resource.getType())) {
                totalRDS += resource.getCount();
            }
        }
        
        if (totalRDS > 0) {
            // Optimization: Multi-AZ
            optimizations.add(Optimization.builder()
                .suggestion("Review Multi-AZ deployment necessity")
                .description("Only use Multi-AZ for production databases requiring high availability")
                .savingsPercentage("50%")
                .monthlySavings(200.0)
                .yearlySavings(2400.0)
                .tradeoff("Reduced availability and redundancy; not suitable for production")
                .category("database")
                .priority(3)
                .complexity("medium")
                .build());
            
            // Optimization: RDS Reserved Instances
            optimizations.add(Optimization.builder()
                .suggestion("Purchase RDS Reserved Instances")
                .description("Get 1-3 year reserved capacity at significant discount")
                .savingsPercentage("30-65%")
                .monthlySavings(400.0)
                .yearlySavings(4800.0)
                .tradeoff("Upfront payment required; less flexibility for downsizing")
                .category("database")
                .priority(4)
                .complexity("low")
                .build());
            
            // Optimization: Read Replicas and Caching
            optimizations.add(Optimization.builder()
                .suggestion("Implement ElastiCache or read replicas")
                .description("Offload read queries to cache layer or read replicas to reduce primary DB load")
                .savingsPercentage("15-30%")
                .monthlySavings(300.0)
                .yearlySavings(3600.0)
                .tradeoff("Added complexity; eventual consistency issues possible with read replicas")
                .category("database")
                .priority(3)
                .complexity("high")
                .build());
        }
    }
    
    private void analyzeS3(List<AWSResource> resources, List<Optimization> optimizations) {
        boolean hasS3 = resources.stream().anyMatch(r -> "s3".equals(r.getType()));
        
        if (hasS3) {
            // Optimization: S3 Tiering
            optimizations.add(Optimization.builder()
                .suggestion("Use S3 Intelligent-Tiering or Glacier for infrequent access")
                .description("Automatically move data to cheaper tiers or use Glacier for archive")
                .savingsPercentage("50-87%")
                .monthlySavings(100.0)
                .yearlySavings(1200.0)
                .tradeoff("Slightly higher retrieval latency for older data; may not be suitable for frequently accessed data")
                .category("storage")
                .priority(3)
                .complexity("medium")
                .build());
            
            // Optimization: Lifecycle Policies
            optimizations.add(Optimization.builder()
                .suggestion("Enable S3 Lifecycle policies")
                .description("Automatically delete old versions and expire incomplete uploads")
                .savingsPercentage("10-20%")
                .monthlySavings(50.0)
                .yearlySavings(600.0)
                .tradeoff("Deleted data cannot be recovered; requires careful policy setup")
                .category("storage")
                .priority(2)
                .complexity("low")
                .build());
        }
    }
    
    private void analyzeLoadBalancers(List<AWSResource> resources, List<Optimization> optimizations) {
        int totalLB = 0;
        
        for (AWSResource resource : resources) {
            if ("load_balancer".equals(resource.getType())) {
                totalLB += resource.getCount();
            }
        }
        
        if (totalLB > 1) {
            optimizations.add(Optimization.builder()
                .suggestion("Consolidate multiple load balancers")
                .description("Combine multiple ALBs under one using path-based or host-based routing")
                .savingsPercentage("50-80%")
                .monthlySavings(300.0)
                .yearlySavings(3600.0)
                .tradeoff("More complex routing rules; potential single point of failure")
                .category("compute")
                .priority(2)
                .complexity("medium")
                .build());
        }
    }
    
    private void analyzeLambda(List<AWSResource> resources, List<Optimization> optimizations) {
        boolean hasLambda = resources.stream().anyMatch(r -> "lambda".equals(r.getType()));
        
        if (hasLambda) {
            optimizations.add(Optimization.builder()
                .suggestion("Optimize Lambda memory and timeout")
                .description("Right-size memory allocation; Lambda CPU scales with memory")
                .savingsPercentage("20-40%")
                .monthlySavings(150.0)
                .yearlySavings(1800.0)
                .tradeoff("May reduce execution speed if memory is too low")
                .category("compute")
                .priority(3)
                .complexity("low")
                .build());
            
            optimizations.add(Optimization.builder()
                .suggestion("Implement Lambda provisioned concurrency carefully")
                .description("Use provisioned concurrency only for critical, high-traffic functions")
                .savingsPercentage("0-30%")
                .monthlySavings(200.0)
                .yearlySavings(2400.0)
                .tradeoff("Added cost for guaranteed concurrency; only cost-effective for specific use cases")
                .category("compute")
                .priority(2)
                .complexity("high")
                .build());
        }
    }
    
    private void addArchitectureOptimizations(List<AWSResource> resources, List<Optimization> optimizations) {
        boolean hasEC2 = resources.stream().anyMatch(r -> "ec2".equals(r.getType()));
        boolean hasRDS = resources.stream().anyMatch(r -> "rds".equals(r.getType()));
        
        if (hasEC2) {
            optimizations.add(Optimization.builder()
                .suggestion("Consider migrating to serverless (Lambda + DynamoDB)")
                .description("Eliminate server management overhead; pay only for what you use")
                .savingsPercentage("40-70%")
                .monthlySavings(1200.0)
                .yearlySavings(14400.0)
                .tradeoff("Cold start latency; potential complexity in rewriting application logic")
                .category("architecture")
                .priority(3)
                .complexity("high")
                .build());
        }
        
        if (hasRDS) {
            optimizations.add(Optimization.builder()
                .suggestion("Evaluate DynamoDB for high-throughput, low-latency needs")
                .description("NoSQL alternative for certain use cases; better scaling characteristics")
                .savingsPercentage("20-50%")
                .monthlySavings(300.0)
                .yearlySavings(3600.0)
                .tradeoff("Different data model; less suitable for complex relational queries")
                .category("architecture")
                .priority(2)
                .complexity("high")
                .build());
        }
        
        // General recommendations
        optimizations.add(Optimization.builder()
            .suggestion("Monitor CloudWatch metrics for right-sizing")
            .description("Review CPU, memory, and network utilization to identify over-provisioned resources")
            .savingsPercentage("10-30%")
            .monthlySavings(200.0)
            .yearlySavings(2400.0)
            .tradeoff("Requires ongoing monitoring and adjustment; risk of undersizing critical resources")
            .category("operations")
            .priority(2)
            .complexity("low")
            .build());
    }
}

