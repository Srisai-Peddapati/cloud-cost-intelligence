package com.app.detector;

import org.springframework.stereotype.Component;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Pattern;

@Component
public class LanguageDetector {

    public enum IaCType {
        TERRAFORM("terraform", "Terraform"),
        CLOUDFORMATION("cloudformation", "CloudFormation"),
        AWS_CDK_TYPESCRIPT("aws_cdk_typescript", "AWS CDK (TypeScript)"),
        AWS_CDK_PYTHON("aws_cdk_python", "AWS CDK (Python)"),
        AWS_CDK_JAVA("aws_cdk_java", "AWS CDK (Java)"),
        AWS_CDK_GO("aws_cdk_go", "AWS CDK (Go)"),
        SERVERLESS("serverless", "Serverless Framework"),
        PULUMI_TYPESCRIPT("pulumi_typescript", "Pulumi (TypeScript)"),
        PULUMI_PYTHON("pulumi_python", "Pulumi (Python)"),
        UNKNOWN("unknown", "Unknown");

        public final String id;
        public final String label;

        IaCType(String id, String label) {
            this.id = id;
            this.label = label;
        }
    }

    public static class DetectionResult {
        public IaCType type;
        public String language; // typescript, python, java, go, etc.
        public double confidence; // 0.0 to 1.0
        public String description;
        public String[] indicators; // Indicators that led to this detection

        public DetectionResult(IaCType type, String language, double confidence, String description, String[] indicators) {
            this.type = type;
            this.language = language;
            this.confidence = confidence;
            this.description = description;
            this.indicators = indicators;
        }
    }

    /**
     * Detect the IaC type from file path and content
     */
    public DetectionResult detectType(String filePath, String content) {
        String fileName = extractFileName(filePath);
        String extension = extractExtension(fileName);

        // Check file extension first
        if (isTerraformFile(fileName)) {
            return new DetectionResult(IaCType.TERRAFORM, "hcl", 1.0, "Terraform file", new String[]{"*.tf extension"});
        }

        if (isCloudFormationFile(fileName)) {
            return detectCloudFormationType(content, fileName);
        }

        if (isAWSCDKFile(fileName, content)) {
            return detectCDKVariant(fileName, content);
        }

        if (isServerlessFile(fileName)) {
            return new DetectionResult(IaCType.SERVERLESS, detectServerlessLanguage(content), 0.8, "Serverless Framework", new String[]{"serverless.yml/yaml"});
        }

        if (isPulumiFile(fileName)) {
            return detectPulumiVariant(fileName, content);
        }

        // Content-based detection
        return detectByContent(fileName, content);
    }

    private boolean isTerraformFile(String fileName) {
        return fileName.endsWith(".tf");
    }

    private boolean isCloudFormationFile(String fileName) {
        String lower = fileName.toLowerCase();
        return lower.endsWith(".yaml") || lower.endsWith(".yml") || lower.endsWith(".json") ||
               lower.contains("template") || lower.contains("cloudformation");
    }

    private DetectionResult detectCloudFormationType(String content, String fileName) {
        List<String> indicators = new ArrayList<>();

        if (content.contains("AWSTemplateFormatVersion")) {
            indicators.add("AWSTemplateFormatVersion");
        }
        if (content.contains("Resources") && content.contains("Properties")) {
            indicators.add("Resources and Properties sections");
        }
        if (content.contains("AWS::")) {
            indicators.add("AWS:: resource types");
        }

        if (indicators.size() >= 2) {
            return new DetectionResult(IaCType.CLOUDFORMATION, "yaml/json", 1.0, "CloudFormation template",
                    indicators.toArray(new String[0]));
        }

        return new DetectionResult(IaCType.CLOUDFORMATION, "yaml/json", 0.7, "Possible CloudFormation",
                new String[]{"YAML/JSON structure"});
    }

    private boolean isAWSCDKFile(String fileName, String content) {
        String lower = fileName.toLowerCase();
        if (lower.contains("cdk") || lower.contains("app.ts") || lower.contains("stack")) {
            return true;
        }

        // Check for CDK imports
        return content.contains("from 'aws-cdk-lib") ||
               content.contains("from aws_cdk import") ||
               content.contains("import software.amazon.awscdk") ||
               content.contains("github.com/aws/aws-cdk-go");
    }

    private DetectionResult detectCDKVariant(String fileName, String content) {
        List<String> indicators = new ArrayList<>();

        if ((fileName.endsWith(".ts") || fileName.endsWith(".tsx")) &&
            (content.contains("from 'aws-cdk-lib") || content.contains("Stack extends"))) {
            indicators.add("TypeScript AWS CDK syntax");
            return new DetectionResult(IaCType.AWS_CDK_TYPESCRIPT, "typescript", 1.0,
                    "AWS CDK (TypeScript)", indicators.toArray(new String[0]));
        }

        if (fileName.endsWith(".py") && content.contains("from aws_cdk import")) {
            indicators.add("Python AWS CDK syntax");
            return new DetectionResult(IaCType.AWS_CDK_PYTHON, "python", 1.0,
                    "AWS CDK (Python)", indicators.toArray(new String[0]));
        }

        if (fileName.endsWith(".java") && content.contains("import software.amazon.awscdk")) {
            indicators.add("Java AWS CDK syntax");
            return new DetectionResult(IaCType.AWS_CDK_JAVA, "java", 1.0,
                    "AWS CDK (Java)", indicators.toArray(new String[0]));
        }

        if (fileName.endsWith(".go") && content.contains("github.com/aws/aws-cdk-go")) {
            indicators.add("Go AWS CDK syntax");
            return new DetectionResult(IaCType.AWS_CDK_GO, "go", 1.0,
                    "AWS CDK (Go)", indicators.toArray(new String[0]));
        }

        // Fallback to TypeScript if CDK indicators present
        return new DetectionResult(IaCType.AWS_CDK_TYPESCRIPT, "typescript", 0.8,
                "AWS CDK (likely TypeScript)", new String[]{"CDK-like structure"});
    }

    private boolean isServerlessFile(String fileName) {
        String lower = fileName.toLowerCase();
        return lower.equals("serverless.yml") || lower.equals("serverless.yaml");
    }

    private String detectServerlessLanguage(String content) {
        if (content.contains("runtime: python")) return "python";
        if (content.contains("runtime: nodejs")) return "javascript";
        return "mixed";
    }

    private boolean isPulumiFile(String fileName) {
        String lower = fileName.toLowerCase();
        return lower.equals("pulumi.yaml") || lower.equals("pulumi.yml");
    }

    private DetectionResult detectPulumiVariant(String fileName, String content) {
        if (content.contains("nodejs")) {
            return new DetectionResult(IaCType.PULUMI_TYPESCRIPT, "typescript", 0.9,
                    "Pulumi (TypeScript)", new String[]{"nodejs runtime"});
        }
        if (content.contains("python")) {
            return new DetectionResult(IaCType.PULUMI_PYTHON, "python", 0.9,
                    "Pulumi (Python)", new String[]{"python runtime"});
        }
        return new DetectionResult(IaCType.PULUMI_TYPESCRIPT, "typescript", 0.7,
                "Pulumi (unknown variant)", new String[]{"pulumi.yaml"});
    }

    private DetectionResult detectByContent(String fileName, String content) {
        List<String> indicators = new ArrayList<>();

        // Check for AWS infrastructure patterns
        int awsResourceCount = countMatches(content, "aws_");
        int cfResourceCount = countMatches(content, "AWS::");

        if (awsResourceCount > cfResourceCount && awsResourceCount > 0) {
            indicators.add(awsResourceCount + " AWS resource references");
            return new DetectionResult(IaCType.TERRAFORM, "hcl", 0.6,
                    "Likely Terraform", indicators.toArray(new String[0]));
        }

        if (cfResourceCount > 0) {
            indicators.add(cfResourceCount + " CloudFormation resources");
            return new DetectionResult(IaCType.CLOUDFORMATION, "yaml/json", 0.7,
                    "Likely CloudFormation", indicators.toArray(new String[0]));
        }

        // Check for CDK patterns
        if (content.contains("new ") && content.contains("Stack") && fileName.endsWith(".ts")) {
            indicators.add("CDK-like construct pattern");
            return new DetectionResult(IaCType.AWS_CDK_TYPESCRIPT, "typescript", 0.7,
                    "Possible AWS CDK", indicators.toArray(new String[0]));
        }

        return new DetectionResult(IaCType.UNKNOWN, extractLanguage(fileName), 0.2,
                "Unable to determine IaC type", new String[]{"No clear indicators"});
    }

    private int countMatches(String content, String pattern) {
        int count = 0;
        int index = 0;
        while ((index = content.indexOf(pattern, index)) != -1) {
            count++;
            index += pattern.length();
        }
        return count;
    }

    private String extractLanguage(String fileName) {
        if (fileName.endsWith(".tf")) return "hcl";
        if (fileName.endsWith(".yaml") || fileName.endsWith(".yml")) return "yaml";
        if (fileName.endsWith(".json")) return "json";
        if (fileName.endsWith(".ts") || fileName.endsWith(".tsx")) return "typescript";
        if (fileName.endsWith(".js") || fileName.endsWith(".jsx")) return "javascript";
        if (fileName.endsWith(".py")) return "python";
        if (fileName.endsWith(".java")) return "java";
        if (fileName.endsWith(".go")) return "go";
        if (fileName.endsWith(".rs")) return "rust";
        return "unknown";
    }

    private String extractFileName(String filePath) {
        return filePath.substring(filePath.lastIndexOf('/') + 1).toLowerCase();
    }

    private String extractExtension(String fileName) {
        int lastDot = fileName.lastIndexOf('.');
        return lastDot > 0 ? fileName.substring(lastDot + 1) : "";
    }
}

