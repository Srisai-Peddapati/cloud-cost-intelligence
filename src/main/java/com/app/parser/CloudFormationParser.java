package com.app.parser;

import com.app.model.AWSResource;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class CloudFormationParser {
    
    private final ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
    private final ObjectMapper jsonMapper = new ObjectMapper();
    
    public List<AWSResource> parseResources(String cfContent, String sourceFile) {
        List<AWSResource> resources = new ArrayList<>();
        
        try {
            JsonNode rootNode;
            
            // Try to parse as YAML first, then JSON
            try {
                rootNode = yamlMapper.readTree(cfContent);
            } catch (Exception e) {
                try {
                    rootNode = jsonMapper.readTree(cfContent);
                } catch (Exception e2) {
                    return resources; // Return empty list if parsing fails
                }
            }
            
            if (rootNode.has("Resources")) {
                JsonNode resourcesNode = rootNode.get("Resources");
                resourcesNode.fields().forEachRemaining(entry -> {
                    String resourceName = entry.getKey();
                    JsonNode resourceNode = entry.getValue();
                    
                    if (resourceNode.has("Type")) {
                        String type = resourceNode.get("Type").asText();
                        JsonNode propertiesNode = resourceNode.get("Properties");
                        
                        if (type.contains("EC2::Instance")) {
                            resources.add(parseEC2Instance(propertiesNode, sourceFile));
                        } else if (type.contains("RDS::DBInstance")) {
                            resources.add(parseRDSInstance(propertiesNode, sourceFile));
                        } else if (type.contains("S3::Bucket")) {
                            resources.add(parseS3Bucket(propertiesNode, sourceFile));
                        } else if (type.contains("ElasticLoadBalancing")) {
                            resources.add(parseLoadBalancer(propertiesNode, type, sourceFile));
                        } else if (type.contains("Lambda::Function")) {
                            resources.add(parseLambdaFunction(propertiesNode, sourceFile));
                        }
                    }
                });
            }
        } catch (Exception e) {
            // Silently fail for malformed templates
        }
        
        return resources;
    }
    
    private AWSResource parseEC2Instance(JsonNode properties, String sourceFile) {
        if (properties == null) {
            return AWSResource.builder()
                .type("ec2")
                .instanceType("t3.micro")
                .count(1)
                .sourceFile(sourceFile)
                .build();
        }
        
        String instanceType = properties.has("InstanceType") 
            ? properties.get("InstanceType").asText() 
            : "t3.micro";
        
        return AWSResource.builder()
            .type("ec2")
            .instanceType(instanceType)
            .count(1)
            .sourceFile(sourceFile)
            .build();
    }
    
    private AWSResource parseRDSInstance(JsonNode properties, String sourceFile) {
        if (properties == null) {
            return AWSResource.builder()
                .type("rds")
                .instanceType("db.t3.micro")
                .count(1)
                .sourceFile(sourceFile)
                .build();
        }
        
        String instanceClass = properties.has("DBInstanceClass") 
            ? properties.get("DBInstanceClass").asText() 
            : "db.t3.micro";
        
        return AWSResource.builder()
            .type("rds")
            .instanceType(instanceClass)
            .count(1)
            .sourceFile(sourceFile)
            .build();
    }
    
    private AWSResource parseS3Bucket(JsonNode properties, String sourceFile) {
        String tier = "standard";
        
        // Try to detect S3 storage class from properties
        if (properties != null && properties.has("VersioningConfiguration")) {
            tier = "standard"; // Could add more sophisticated detection
        }
        
        return AWSResource.builder()
            .type("s3")
            .size("100GB")
            .tier(tier)
            .sourceFile(sourceFile)
            .build();
    }
    
    private AWSResource parseLoadBalancer(JsonNode properties, String type, String sourceFile) {
        String lbType = "application"; // Default to ALB
        
        if (type.contains("LoadBalancer") && type.contains("Network")) {
            lbType = "network";
        }
        
        return AWSResource.builder()
            .type("load_balancer")
            .instanceType(lbType)
            .count(1)
            .sourceFile(sourceFile)
            .build();
    }
    
    private AWSResource parseLambdaFunction(JsonNode properties, String sourceFile) {
        if (properties == null) {
            return AWSResource.builder()
                .type("lambda")
                .size("128MB")
                .count(1)
                .sourceFile(sourceFile)
                .build();
        }
        
        int memory = properties.has("MemorySize") 
            ? properties.get("MemorySize").asInt() 
            : 128;
        
        return AWSResource.builder()
            .type("lambda")
            .size(memory + "MB")
            .count(1)
            .sourceFile(sourceFile)
            .build();
    }
}

