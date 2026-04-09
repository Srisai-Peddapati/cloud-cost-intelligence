package com.app.service;

import com.app.model.AWSResource;
import com.app.parser.CloudFormationParser;
import com.app.parser.TerraformParser;
import com.app.parser.CDKParser;
import com.app.detector.LanguageDetector;
import org.springframework.stereotype.Service;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@Service
public class RepositoryService {
    
    private final TerraformParser terraformParser;
    private final CloudFormationParser cloudFormationParser;
    private final CDKParser cdkParser;
    private final LanguageDetector languageDetector;
    
    public RepositoryService(TerraformParser terraformParser, CloudFormationParser cloudFormationParser,
                           CDKParser cdkParser, LanguageDetector languageDetector) {
        this.terraformParser = terraformParser;
        this.cloudFormationParser = cloudFormationParser;
        this.cdkParser = cdkParser;
        this.languageDetector = languageDetector;
    }
    
    /**
     * Clone a repository and extract IaC resources
     */
    public RepositoryAnalysisResult analyzeRepository(String repositoryUrl) {
        RepositoryAnalysisResult result = new RepositoryAnalysisResult();
        
        try {
            // Clone repository
            String repoPath = cloneRepository(repositoryUrl);
            result.setRepositoryPath(repoPath);
            
            // Find and parse IaC files
            List<AWSResource> resources = new ArrayList<>();
            
            // Scan for IaC files based on language detection
            resources.addAll(scanAndParseIaCFiles(repoPath));
            
            result.setResources(resources);
            result.setSuccess(true);
            result.setMessage("Successfully analyzed repository");
            
        } catch (Exception e) {
            result.setSuccess(false);
            result.setMessage("Error analyzing repository: " + e.getMessage());
        }
        
        return result;
    }
    
    private String cloneRepository(String repositoryUrl) throws IOException, InterruptedException {
        // Create a temporary directory for cloning
        Path tempDir = Files.createTempDirectory("cost-intelligence-");
        String repoPath = tempDir.toString();
        
        // Use git clone command
        ProcessBuilder pb = new ProcessBuilder("git", "clone", repositoryUrl, repoPath);
        pb.redirectErrorStream(true);
        
        Process process = pb.start();
        int exitCode = process.waitFor();
        
        if (exitCode != 0) {
            throw new IOException("Failed to clone repository: " + repositoryUrl);
        }
        
        return repoPath;
    }
    
    /**
     * Scan and parse IaC files using language detection
     */
    private List<AWSResource> scanAndParseIaCFiles(String repoPath) {
        List<AWSResource> resources = new ArrayList<>();
        
        try (Stream<Path> walk = Files.walk(Paths.get(repoPath))) {
            walk.filter(Files::isRegularFile)
                .forEach(path -> {
                    try {
                        String content = Files.readString(path);
                        String filePath = path.toString();
                        
                        // Detect IaC type and language
                        LanguageDetector.DetectionResult detection = languageDetector.detectType(filePath, content);
                        
                        // Parse based on detected type
                        List<AWSResource> foundResources = parseByType(detection, filePath, content);
                        
                        // Add detection metadata to resources
                        for (AWSResource resource : foundResources) {
                            if (resource.getSourceFile() == null) {
                                resource.setSourceFile(filePath);
                            }
                        }
                        
                        resources.addAll(foundResources);
                    } catch (IOException e) {
                        // Continue with next file
                    }
                });
        } catch (IOException e) {
            // Silently fail if directory doesn't exist
        }
        
        return resources;
    }
    
    /**
     * Parse file based on detected IaC type
     */
    private List<AWSResource> parseByType(LanguageDetector.DetectionResult detection, String filePath, String content) {
        switch (detection.type) {
            case TERRAFORM:
                return terraformParser.parseResources(content, filePath);
            case CLOUDFORMATION:
                return cloudFormationParser.parseResources(content, filePath);
            case AWS_CDK_TYPESCRIPT:
            case AWS_CDK_PYTHON:
            case AWS_CDK_JAVA:
            case AWS_CDK_GO:
                return parseCDK(detection, content, filePath);
            case SERVERLESS:
                return parseServerless(content, filePath);
            case PULUMI_TYPESCRIPT:
            case PULUMI_PYTHON:
                return parsePulumi(content, filePath);
            default:
                return new ArrayList<>();
        }
    }
    
    /**
     * Parse AWS CDK code
     */
    private List<AWSResource> parseCDK(LanguageDetector.DetectionResult detection, String content, String filePath) {
        if ("python".equalsIgnoreCase(detection.language)) {
            return cdkParser.parseCDKPython(content, filePath);
        } else {
            // Default to TypeScript/JavaScript
            return cdkParser.parseCDKTypeScript(content, filePath);
        }
    }
    
    /**
     * Parse Serverless framework configuration
     */
    private List<AWSResource> parseServerless(String content, String filePath) {
        List<AWSResource> resources = new ArrayList<>();
        // TODO: Implement Serverless framework parsing
        return resources;
    }
    
    /**
     * Parse Pulumi infrastructure code
     */
    private List<AWSResource> parsePulumi(String content, String filePath) {
        List<AWSResource> resources = new ArrayList<>();
        // TODO: Implement Pulumi parsing
        return resources;
    }
    
    private List<AWSResource> scanTerraformFiles(String repoPath) {
        List<AWSResource> resources = new ArrayList<>();
        
        try (Stream<Path> walk = Files.walk(Paths.get(repoPath))) {
            walk.filter(path -> path.toString().endsWith(".tf"))
                .forEach(path -> {
                    try {
                        String content = Files.readString(path);
                        List<AWSResource> foundResources = terraformParser.parseResources(content, path.toString());
                        resources.addAll(foundResources);
                    } catch (IOException e) {
                        // Continue with next file
                    }
                });
        } catch (IOException e) {
            // Silently fail if directory doesn't exist
        }
        
        return resources;
    }
    
    private List<AWSResource> scanCloudFormationFiles(String repoPath) {
        List<AWSResource> resources = new ArrayList<>();
        
        try (Stream<Path> walk = Files.walk(Paths.get(repoPath))) {
            walk.filter(path -> {
                String fileName = path.toString().toLowerCase();
                return fileName.endsWith(".yaml") || fileName.endsWith(".yml") || fileName.endsWith(".json");
            })
            .forEach(path -> {
                try {
                    String content = Files.readString(path);
                    // Only process if it looks like a CloudFormation template
                    if (content.contains("AWSTemplateFormatVersion") || 
                        content.contains("Resources") && 
                        content.contains("Properties")) {
                        List<AWSResource> foundResources = cloudFormationParser.parseResources(content, path.toString());
                        resources.addAll(foundResources);
                    }
                } catch (IOException e) {
                    // Continue with next file
                }
            });
        } catch (IOException e) {
            // Silently fail if directory doesn't exist
        }
        
        return resources;
    }
    
    /**
     * Clean up temporary repository
     */
    public void cleanupRepository(String repoPath) {
        try {
            deleteDirectory(new File(repoPath));
        } catch (Exception e) {
            // Silently fail cleanup
        }
    }
    
    private void deleteDirectory(File dir) throws IOException {
        if (dir.isDirectory()) {
            File[] files = dir.listFiles();
            if (files != null) {
                for (File file : files) {
                    deleteDirectory(file);
                }
            }
        }
        Files.deleteIfExists(dir.toPath());
    }
    
    /**
     * Inner class to hold repository analysis results
     */
    public static class RepositoryAnalysisResult {
        private String repositoryPath;
        private List<AWSResource> resources;
        private boolean success;
        private String message;
        
        // Getters and Setters
        public String getRepositoryPath() {
            return repositoryPath;
        }
        
        public void setRepositoryPath(String repositoryPath) {
            this.repositoryPath = repositoryPath;
        }
        
        public List<AWSResource> getResources() {
            return resources;
        }
        
        public void setResources(List<AWSResource> resources) {
            this.resources = resources;
        }
        
        public boolean isSuccess() {
            return success;
        }
        
        public void setSuccess(boolean success) {
            this.success = success;
        }
        
        public String getMessage() {
            return message;
        }
        
        public void setMessage(String message) {
            this.message = message;
        }
    }
}

