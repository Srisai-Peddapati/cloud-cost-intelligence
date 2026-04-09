package com.app.parser;

import com.app.model.AWSResource;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class TerraformParser {
    
    public List<AWSResource> parseResources(String tfContent, String sourceFile) {
        List<AWSResource> resources = new ArrayList<>();
        
        // Parse EC2 instances
        resources.addAll(parseEC2(tfContent, sourceFile));
        
        // Parse RDS instances
        resources.addAll(parseRDS(tfContent, sourceFile));
        
        // Parse S3 buckets
        resources.addAll(parseS3(tfContent, sourceFile));
        
        // Parse Load Balancers
        resources.addAll(parseLoadBalancer(tfContent, sourceFile));
        
        // Parse Lambda functions
        resources.addAll(parseLambda(tfContent, sourceFile));
        
        return resources;
    }
    
    private List<AWSResource> parseEC2(String content, String sourceFile) {
        List<AWSResource> ec2Resources = new ArrayList<>();
        
        // Match aws_instance blocks
        Pattern instancePattern = Pattern.compile(
            "resource\\s+\"aws_instance\"\\s+\"([^\"]+)\"\\s*\\{([^}]+)\\}",
            Pattern.DOTALL
        );
        
        Matcher instanceMatcher = instancePattern.matcher(content);
        while (instanceMatcher.find()) {
            String blockContent = instanceMatcher.group(2);
            
            // Extract instance type
            Pattern typePattern = Pattern.compile("instance_type\\s*=\\s*[\"']([^\"']+)[\"']");
            Matcher typeMatcher = typePattern.matcher(blockContent);
            String instanceType = typeMatcher.find() ? typeMatcher.group(1) : "t3.micro";
            
            // Extract count
            Pattern countPattern = Pattern.compile("count\\s*=\\s*(\\d+)");
            Matcher countMatcher = countPattern.matcher(blockContent);
            int count = countMatcher.find() ? Integer.parseInt(countMatcher.group(1)) : 1;
            
            ec2Resources.add(AWSResource.builder()
                .type("ec2")
                .instanceType(instanceType)
                .count(count)
                .sourceFile(sourceFile)
                .build());
        }
        
        // Also match aws_launch_template for EC2
        Pattern launchTemplatePattern = Pattern.compile(
            "resource\\s+\"aws_launch_template\"\\s+\"([^\"]+)\"\\s*\\{([^}]+)\\}",
            Pattern.DOTALL
        );
        
        Matcher launchTemplateMatcher = launchTemplatePattern.matcher(content);
        while (launchTemplateMatcher.find()) {
            String blockContent = launchTemplateMatcher.group(2);
            
            Pattern typePattern = Pattern.compile("instance_type\\s*=\\s*[\"']([^\"']+)[\"']");
            Matcher typeMatcher = typePattern.matcher(blockContent);
            String instanceType = typeMatcher.find() ? typeMatcher.group(1) : "t3.micro";
            
            ec2Resources.add(AWSResource.builder()
                .type("ec2")
                .instanceType(instanceType)
                .count(1)
                .sourceFile(sourceFile)
                .build());
        }
        
        return ec2Resources;
    }
    
    private List<AWSResource> parseRDS(String content, String sourceFile) {
        List<AWSResource> rdsResources = new ArrayList<>();
        
        Pattern rdsPattern = Pattern.compile(
            "resource\\s+\"aws_db_instance\"\\s+\"([^\"]+)\"\\s*\\{([^}]+)\\}",
            Pattern.DOTALL
        );
        
        Matcher rdsMatcher = rdsPattern.matcher(content);
        while (rdsMatcher.find()) {
            String blockContent = rdsMatcher.group(2);
            
            Pattern classPattern = Pattern.compile("instance_class\\s*=\\s*[\"']([^\"']+)[\"']");
            Matcher classMatcher = classPattern.matcher(blockContent);
            String instanceClass = classMatcher.find() ? classMatcher.group(1) : "db.t3.micro";
            
            rdsResources.add(AWSResource.builder()
                .type("rds")
                .instanceType(instanceClass)
                .count(1)
                .sourceFile(sourceFile)
                .build());
        }
        
        return rdsResources;
    }
    
    private List<AWSResource> parseS3(String content, String sourceFile) {
        List<AWSResource> s3Resources = new ArrayList<>();
        
        Pattern s3Pattern = Pattern.compile(
            "resource\\s+\"aws_s3_bucket\"\\s+\"([^\"]+)\"\\s*\\{([^}]*?)\\}",
            Pattern.DOTALL
        );
        
        Matcher s3Matcher = s3Pattern.matcher(content);
        while (s3Matcher.find()) {
            // Extract bucket name for size estimation (default to 100GB)
            s3Resources.add(AWSResource.builder()
                .type("s3")
                .size("100GB")
                .tier("standard")
                .sourceFile(sourceFile)
                .build());
        }
        
        return s3Resources;
    }
    
    private List<AWSResource> parseLoadBalancer(String content, String sourceFile) {
        List<AWSResource> lbResources = new ArrayList<>();
        
        // Parse ALB
        Pattern albPattern = Pattern.compile(
            "resource\\s+\"aws_lb\"\\s+\"([^\"]+)\"\\s*\\{([^}]*?)\\}",
            Pattern.DOTALL
        );
        
        Matcher albMatcher = albPattern.matcher(content);
        while (albMatcher.find()) {
            String blockContent = albMatcher.group(2);
            
            Pattern typePattern = Pattern.compile("load_balancer_type\\s*=\\s*[\"']([^\"']+)[\"']");
            Matcher typeMatcher = typePattern.matcher(blockContent);
            String lbType = typeMatcher.find() ? typeMatcher.group(1) : "application";
            
            lbResources.add(AWSResource.builder()
                .type("load_balancer")
                .instanceType(lbType)
                .count(1)
                .sourceFile(sourceFile)
                .build());
        }
        
        return lbResources;
    }
    
    private List<AWSResource> parseLambda(String content, String sourceFile) {
        List<AWSResource> lambdaResources = new ArrayList<>();
        
        Pattern lambdaPattern = Pattern.compile(
            "resource\\s+\"aws_lambda_function\"\\s+\"([^\"]+)\"\\s*\\{([^}]*?)\\}",
            Pattern.DOTALL
        );
        
        Matcher lambdaMatcher = lambdaPattern.matcher(content);
        while (lambdaMatcher.find()) {
            String blockContent = lambdaMatcher.group(2);
            
            // Extract memory (default 128MB)
            Pattern memoryPattern = Pattern.compile("memory_size\\s*=\\s*(\\d+)");
            Matcher memoryMatcher = memoryPattern.matcher(blockContent);
            String memory = memoryMatcher.find() ? memoryMatcher.group(1) + "MB" : "128MB";
            
            lambdaResources.add(AWSResource.builder()
                .type("lambda")
                .size(memory)
                .count(1)
                .sourceFile(sourceFile)
                .build());
        }
        
        return lambdaResources;
    }
}

