package com.app.parser;

import com.app.model.AWSResource;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parser for AWS CDK code (TypeScript, Python, Java, Go)
 */
@Component
public class CDKParser {
    
    /**
     * Parse AWS CDK TypeScript code
     */
    public List<AWSResource> parseCDKTypeScript(String content, String sourceFile) {
        List<AWSResource> resources = new ArrayList<>();
        
        // Parse EC2 instances
        resources.addAll(parseCDKEC2(content, sourceFile));
        
        // Parse RDS instances
        resources.addAll(parseCDKRDS(content, sourceFile));
        
        // Parse S3 buckets
        resources.addAll(parseCDKS3(content, sourceFile));
        
        // Parse Lambda functions
        resources.addAll(parseCDKLambda(content, sourceFile));
        
        // Parse Load Balancers
        resources.addAll(parseCDKLoadBalancer(content, sourceFile));
        
        return resources;
    }
    
    /**
     * Parse AWS CDK Python code
     */
    public List<AWSResource> parseCDKPython(String content, String sourceFile) {
        List<AWSResource> resources = new ArrayList<>();
        
        // Parse EC2 instances
        resources.addAll(parsePythonEC2(content, sourceFile));
        
        // Parse RDS instances
        resources.addAll(parsePythonRDS(content, sourceFile));
        
        // Parse S3 buckets
        resources.addAll(parsePythonS3(content, sourceFile));
        
        // Parse Lambda functions
        resources.addAll(parsePythonLambda(content, sourceFile));
        
        return resources;
    }
    
    /**
     * Parse EC2 instances in CDK TypeScript
     */
    private List<AWSResource> parseCDKEC2(String content, String sourceFile) {
        List<AWSResource> ec2Resources = new ArrayList<>();
        
        // Match patterns like:
        // new ec2.Instance(this, 'MyInstance', { instanceType: InstanceType.of(...) })
        // new aws_cdk_lib.aws_ec2.Instance(...)
        Pattern pattern = Pattern.compile(
            "new\\s+ec2\\.Instance\\(.*?instanceType\\s*:\\s*InstanceType\\.of\\(.*?InstanceClass\\.(\\w+)\\s*,\\s*InstanceSize\\.(\\w+)",
            Pattern.DOTALL
        );
        
        Matcher matcher = pattern.matcher(content);
        while (matcher.find()) {
            String instanceClass = matcher.group(1);
            String instanceSize = matcher.group(2);
            String instanceType = "t3." + instanceSize.toLowerCase();
            
            ec2Resources.add(AWSResource.builder()
                .type("ec2")
                .instanceType(instanceType)
                .count(1)
                .sourceFile(sourceFile)
                .build());
        }
        
        // Also match direct Instance creation
        Pattern directPattern = Pattern.compile("new\\s+ec2\\.Instance\\(.*?\\{");
        Matcher directMatcher = directPattern.matcher(content);
        
        while (directMatcher.find() && ec2Resources.isEmpty()) {
            ec2Resources.add(AWSResource.builder()
                .type("ec2")
                .instanceType("t3.micro")
                .count(1)
                .sourceFile(sourceFile)
                .build());
        }
        
        return ec2Resources;
    }
    
    /**
     * Parse RDS instances in CDK TypeScript
     */
    private List<AWSResource> parseCDKRDS(String content, String sourceFile) {
        List<AWSResource> rdsResources = new ArrayList<>();
        
        // Match patterns like:
        // new rds.DatabaseInstance(this, 'MyDB', { instanceType: ... })
        Pattern pattern = Pattern.compile(
            "new\\s+rds\\.DatabaseInstance\\(.*?instanceType\\s*:\\s*InstanceType\\.of\\(.*?InstanceClass\\.(\\w+).*?InstanceSize\\.(\\w+)",
            Pattern.DOTALL
        );
        
        Matcher matcher = pattern.matcher(content);
        while (matcher.find()) {
            String instanceClass = matcher.group(1);
            String instanceSize = matcher.group(2);
            String dbInstanceClass = "db.t3." + instanceSize.toLowerCase();
            
            rdsResources.add(AWSResource.builder()
                .type("rds")
                .instanceType(dbInstanceClass)
                .count(1)
                .sourceFile(sourceFile)
                .build());
        }
        
        // Fallback pattern
        if (rdsResources.isEmpty()) {
            Pattern fallbackPattern = Pattern.compile("new\\s+rds\\.DatabaseInstance\\(");
            Matcher fallbackMatcher = fallbackPattern.matcher(content);
            
            if (fallbackMatcher.find()) {
                rdsResources.add(AWSResource.builder()
                    .type("rds")
                    .instanceType("db.t3.micro")
                    .count(1)
                    .sourceFile(sourceFile)
                    .build());
            }
        }
        
        return rdsResources;
    }
    
    /**
     * Parse S3 buckets in CDK TypeScript
     */
    private List<AWSResource> parseCDKS3(String content, String sourceFile) {
        List<AWSResource> s3Resources = new ArrayList<>();
        
        Pattern pattern = Pattern.compile("new\\s+s3\\.Bucket\\(");
        Matcher matcher = pattern.matcher(content);
        
        while (matcher.find()) {
            s3Resources.add(AWSResource.builder()
                .type("s3")
                .size("100GB")
                .tier("standard")
                .sourceFile(sourceFile)
                .build());
        }
        
        return s3Resources;
    }
    
    /**
     * Parse Lambda functions in CDK TypeScript
     */
    private List<AWSResource> parseCDKLambda(String content, String sourceFile) {
        List<AWSResource> lambdaResources = new ArrayList<>();
        
        // Match lambda.Function
        Pattern pattern = Pattern.compile("new\\s+lambda\\.Function\\(.*?\\{");
        Matcher matcher = pattern.matcher(content);
        
        int count = 0;
        while (matcher.find()) {
            count++;
        }
        
        // Check for memory configuration
        Pattern memoryPattern = Pattern.compile("memorySize\\s*:\\s*(\\d+)");
        Matcher memoryMatcher = memoryPattern.matcher(content);
        
        String memory = "128MB";
        if (memoryMatcher.find()) {
            memory = memoryMatcher.group(1) + "MB";
        }
        
        for (int i = 0; i < count; i++) {
            lambdaResources.add(AWSResource.builder()
                .type("lambda")
                .size(memory)
                .count(1)
                .sourceFile(sourceFile)
                .build());
        }
        
        return lambdaResources;
    }
    
    /**
     * Parse Load Balancers in CDK TypeScript
     */
    private List<AWSResource> parseCDKLoadBalancer(String content, String sourceFile) {
        List<AWSResource> lbResources = new ArrayList<>();
        
        // Match ApplicationLoadBalancer
        Pattern albPattern = Pattern.compile("new\\s+elbv2\\.ApplicationLoadBalancer\\(");
        Matcher albMatcher = albPattern.matcher(content);
        
        while (albMatcher.find()) {
            lbResources.add(AWSResource.builder()
                .type("load_balancer")
                .instanceType("application")
                .count(1)
                .sourceFile(sourceFile)
                .build());
        }
        
        // Match NetworkLoadBalancer
        Pattern nlbPattern = Pattern.compile("new\\s+elbv2\\.NetworkLoadBalancer\\(");
        Matcher nlbMatcher = nlbPattern.matcher(content);
        
        while (nlbMatcher.find()) {
            lbResources.add(AWSResource.builder()
                .type("load_balancer")
                .instanceType("network")
                .count(1)
                .sourceFile(sourceFile)
                .build());
        }
        
        return lbResources;
    }
    
    // Python CDK parsers
    
    private List<AWSResource> parsePythonEC2(String content, String sourceFile) {
        List<AWSResource> ec2Resources = new ArrayList<>();
        
        Pattern pattern = Pattern.compile("ec2\\.Instance\\(");
        Matcher matcher = pattern.matcher(content);
        
        while (matcher.find()) {
            ec2Resources.add(AWSResource.builder()
                .type("ec2")
                .instanceType("t3.medium")
                .count(1)
                .sourceFile(sourceFile)
                .build());
        }
        
        return ec2Resources;
    }
    
    private List<AWSResource> parsePythonRDS(String content, String sourceFile) {
        List<AWSResource> rdsResources = new ArrayList<>();
        
        Pattern pattern = Pattern.compile("rds\\.DatabaseInstance\\(");
        Matcher matcher = pattern.matcher(content);
        
        while (matcher.find()) {
            rdsResources.add(AWSResource.builder()
                .type("rds")
                .instanceType("db.t3.micro")
                .count(1)
                .sourceFile(sourceFile)
                .build());
        }
        
        return rdsResources;
    }
    
    private List<AWSResource> parsePythonS3(String content, String sourceFile) {
        List<AWSResource> s3Resources = new ArrayList<>();
        
        Pattern pattern = Pattern.compile("s3\\.Bucket\\(");
        Matcher matcher = pattern.matcher(content);
        
        while (matcher.find()) {
            s3Resources.add(AWSResource.builder()
                .type("s3")
                .size("100GB")
                .tier("standard")
                .sourceFile(sourceFile)
                .build());
        }
        
        return s3Resources;
    }
    
    private List<AWSResource> parsePythonLambda(String content, String sourceFile) {
        List<AWSResource> lambdaResources = new ArrayList<>();
        
        Pattern pattern = Pattern.compile("lambda\\.Function\\(");
        Matcher matcher = pattern.matcher(content);
        
        while (matcher.find()) {
            lambdaResources.add(AWSResource.builder()
                .type("lambda")
                .size("128MB")
                .count(1)
                .sourceFile(sourceFile)
                .build());
        }
        
        return lambdaResources;
    }
}

