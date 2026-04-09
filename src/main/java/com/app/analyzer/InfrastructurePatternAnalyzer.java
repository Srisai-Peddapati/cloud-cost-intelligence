package com.app.analyzer;

import com.app.model.AWSResource;
import org.springframework.stereotype.Component;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Analyzes detected infrastructure patterns and generates intelligent summaries
 */
@Component
public class InfrastructurePatternAnalyzer {
    
    public static class InfrastructurePattern {
        public String name;
        public String category;
        public double confidence; // 0.0 to 1.0
        public String description;
        public String[] characteristics;
        public String[] recommendations;
        
        public InfrastructurePattern(String name, String category, double confidence, 
                                    String description, String[] characteristics, String[] recommendations) {
            this.name = name;
            this.category = category;
            this.confidence = confidence;
            this.description = description;
            this.characteristics = characteristics;
            this.recommendations = recommendations;
        }
    }
    
    public static class ArchitectureSummary {
        public String dominantPattern;
        public List<InfrastructurePattern> detectedPatterns;
        public Map<String, Integer> resourceDistribution;
        public String maturityLevel; // "Development", "Staging", "Production"
        public String scalabilityAssessment;
        public String securityPosture;
        public double estimatedComplexity; // 0.0 to 1.0
        public List<String> keyInsights;
    }
    
    /**
     * Analyze infrastructure and detect patterns
     */
    public ArchitectureSummary analyzeInfrastructure(List<AWSResource> resources) {
        ArchitectureSummary summary = new ArchitectureSummary();
        
        // Detect patterns
        List<InfrastructurePattern> patterns = detectPatterns(resources);
        summary.detectedPatterns = patterns;
        
        // Find dominant pattern
        if (!patterns.isEmpty()) {
            summary.dominantPattern = patterns.get(0).name;
        } else {
            summary.dominantPattern = "Unknown";
        }
        
        // Resource distribution
        summary.resourceDistribution = getResourceDistribution(resources);
        
        // Assessments
        summary.maturityLevel = assessMaturityLevel(resources);
        summary.scalabilityAssessment = assessScalability(resources);
        summary.securityPosture = assessSecurity(resources);
        summary.estimatedComplexity = calculateComplexity(resources);
        
        // Key insights
        summary.keyInsights = generateKeyInsights(resources, patterns);
        
        return summary;
    }
    
    /**
     * Detect infrastructure patterns
     */
    private List<InfrastructurePattern> detectPatterns(List<AWSResource> resources) {
        List<InfrastructurePattern> patterns = new ArrayList<>();
        
        // Count resources by type
        Map<String, Long> resourceCounts = resources.stream()
                .collect(Collectors.groupingBy(r -> r.getType(), Collectors.counting()));
        
        int ec2Count = (int) (long) resourceCounts.getOrDefault("ec2", 0L);
        int rdsCount = (int) (long) resourceCounts.getOrDefault("rds", 0L);
        int lambdaCount = (int) (long) resourceCounts.getOrDefault("lambda", 0L);
        int s3Count = (int) (long) resourceCounts.getOrDefault("s3", 0L);
        int lbCount = (int) (long) resourceCounts.getOrDefault("load_balancer", 0L);
        
        // Pattern 1: Traditional Web Application
        if (ec2Count >= 2 && rdsCount >= 1 && lbCount >= 1) {
            patterns.add(new InfrastructurePattern(
                    "Traditional Web Application",
                    "Monolithic",
                    0.95,
                    "Classic 3-tier architecture with load balancer, compute, and database",
                    new String[]{"Multiple EC2s", "RDS database", "Load balancer"},
                    new String[]{"Consider microservices", "Implement auto-scaling", "Use Reserved Instances"}
            ));
        }
        
        // Pattern 2: Serverless Application
        if (lambdaCount >= 3 && ec2Count == 0 && rdsCount == 0) {
            patterns.add(new InfrastructurePattern(
                    "Serverless Architecture",
                    "Event-Driven",
                    0.9,
                    "Serverless-first architecture using Lambda without compute instances",
                    new String[]{"Multiple Lambda functions", "No EC2 instances", "Minimal provisioned resources"},
                    new String[]{"Optimize Lambda memory allocation", "Use concurrency controls", "Implement cost monitoring"}
            ));
        }
        
        // Pattern 3: Microservices Architecture
        if (ec2Count >= 3 && lambdaCount >= 2 && rdsCount >= 1) {
            patterns.add(new InfrastructurePattern(
                    "Microservices Architecture",
                    "Distributed",
                    0.85,
                    "Hybrid microservices with both containers and serverless components",
                    new String[]{"Multiple EC2s", "Multiple Lambda functions", "Separate databases"},
                    new String[]{"Implement service mesh", "Use containerization", "Optimize inter-service communication"}
            ));
        }
        
        // Pattern 4: Data Processing Pipeline
        if (s3Count >= 2 && lambdaCount >= 2 && rdsCount == 0) {
            patterns.add(new InfrastructurePattern(
                    "Data Processing Pipeline",
                    "Batch Processing",
                    0.8,
                    "Data processing workflow using S3 and Lambda",
                    new String[]{"S3 storage", "Lambda processors", "Event-driven"},
                    new String[]{"Implement data lifecycle policies", "Optimize Lambda memory", "Use S3 Intelligent-Tiering"}
            ));
        }
        
        // Pattern 5: Development/Test Environment
        if (ec2Count <= 2 && rdsCount <= 1 && lambdaCount == 0 && lbCount == 0) {
            patterns.add(new InfrastructurePattern(
                    "Development/Test Environment",
                    "Non-Production",
                    0.85,
                    "Small-scale environment for development and testing",
                    new String[]{"Limited resources", "Minimal redundancy", "Basic configuration"},
                    new String[]{"Clean up unused resources", "Use smaller instance types", "Consider on-demand only"}
            ));
        }
        
        // Pattern 6: High-Availability Production
        if (ec2Count >= 4 && rdsCount >= 1 && lbCount >= 1 && s3Count >= 1) {
            patterns.add(new InfrastructurePattern(
                    "High-Availability Production",
                    "Enterprise",
                    0.9,
                    "Production-grade infrastructure with redundancy and multiple layers",
                    new String[]{"Multiple EC2 instances", "RDS with backup", "Load balancer", "Storage"},
                    new String[]{"Implement Reserved Instances", "Enable Multi-AZ", "Use read replicas"}
            ));
        }
        
        // Sort by confidence
        patterns.sort((p1, p2) -> Double.compare(p2.confidence, p1.confidence));
        
        return patterns;
    }
    
    /**
     * Get resource distribution summary
     */
    private Map<String, Integer> getResourceDistribution(List<AWSResource> resources) {
        return resources.stream()
                .collect(Collectors.groupingBy(
                        r -> r.getType(),
                        Collectors.collectingAndThen(Collectors.toList(), List::size)
                ));
    }
    
    /**
     * Assess maturity level based on infrastructure
     */
    private String assessMaturityLevel(List<AWSResource> resources) {
        Map<String, Long> counts = resources.stream()
                .collect(Collectors.groupingBy(r -> r.getType(), Collectors.counting()));
        
        int lbCount = (int) (long) counts.getOrDefault("load_balancer", 0L);
        int ec2Count = (int) (long) counts.getOrDefault("ec2", 0L);
        
        if (lbCount >= 1 && ec2Count >= 3) {
            return "Production-Ready";
        } else if (ec2Count >= 2) {
            return "Staging";
        } else {
            return "Development";
        }
    }
    
    /**
     * Assess scalability characteristics
     */
    private String assessScalability(List<AWSResource> resources) {
        Map<String, Long> counts = resources.stream()
                .collect(Collectors.groupingBy(r -> r.getType(), Collectors.counting()));
        
        int ec2Count = (int) (long) counts.getOrDefault("ec2", 0L);
        int lambdaCount = (int) (long) counts.getOrDefault("lambda", 0L);
        int lbCount = (int) (long) counts.getOrDefault("load_balancer", 0L);
        
        if (lambdaCount > 0 || lbCount > 0) {
            if (ec2Count > 3) {
                return "High - Auto-scaling enabled with load balancing";
            } else {
                return "Medium - Has load balancing, can auto-scale";
            }
        } else if (ec2Count > 0) {
            return "Low - Manual scaling required";
        } else {
            return "Unknown - Limited infrastructure visibility";
        }
    }
    
    /**
     * Assess security posture
     */
    private String assessSecurity(List<AWSResource> resources) {
        Map<String, Long> counts = resources.stream()
                .collect(Collectors.groupingBy(r -> r.getType(), Collectors.counting()));
        
        int lbCount = (int) (long) counts.getOrDefault("load_balancer", 0L);
        int ec2Count = (int) (long) counts.getOrDefault("ec2", 0L);
        
        if (lbCount > 0 && ec2Count >= 2) {
            return "Medium - Has load balancer and distributed compute";
        } else if (ec2Count >= 1) {
            return "Low - Needs additional security layers";
        } else {
            return "Managed - Serverless/managed services provide built-in security";
        }
    }
    
    /**
     * Calculate overall complexity score
     */
    private double calculateComplexity(List<AWSResource> resources) {
        int uniqueTypes = (int) resources.stream()
                .map(r -> r.getType())
                .distinct()
                .count();
        
        int totalResources = resources.size();
        
        // Complexity = (unique types + total resources) / 20
        // Capped at 1.0
        double complexity = (uniqueTypes + totalResources) / 20.0;
        return Math.min(complexity, 1.0);
    }
    
    /**
     * Generate key insights about the infrastructure
     */
    private List<String> generateKeyInsights(List<AWSResource> resources, List<InfrastructurePattern> patterns) {
        List<String> insights = new ArrayList<>();
        
        Map<String, Long> counts = resources.stream()
                .collect(Collectors.groupingBy(r -> r.getType(), Collectors.counting()));
        
        // Insight 1: Dominant resource type
        counts.entrySet().stream()
                .max(Comparator.comparingLong(Map.Entry::getValue))
                .ifPresent(e -> insights.add(String.format("Primary resource: %s (%d instances)", e.getKey(), e.getValue())));
        
        // Insight 2: Architecture pattern
        if (!patterns.isEmpty()) {
            insights.add(String.format("Architecture: %s", patterns.get(0).name));
        }
        
        // Insight 3: Cost optimization opportunities
        if (counts.getOrDefault("ec2", 0L) > 2) {
            insights.add("High compute costs - consider Reserved Instances or Auto-scaling");
        }
        
        if (counts.getOrDefault("rds", 0L) > 0 && counts.getOrDefault("load_balancer", 0L) == 0) {
            insights.add("Database without load balancing - may need horizontal scaling");
        }
        
        // Insight 4: Scalability readiness
        if (counts.getOrDefault("load_balancer", 0L) > 0) {
            insights.add("Infrastructure supports load balancing and auto-scaling");
        } else if (counts.getOrDefault("lambda", 0L) > 0) {
            insights.add("Serverless workloads - automatically scalable");
        } else {
            insights.add("Limited auto-scaling capabilities - manual scaling required");
        }
        
        // Insight 5: Storage usage
        if (counts.getOrDefault("s3", 0L) > 0) {
            insights.add("Using S3 for storage - implement lifecycle policies for cost optimization");
        }
        
        return insights;
    }
}

