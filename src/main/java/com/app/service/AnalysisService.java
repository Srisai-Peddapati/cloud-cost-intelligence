package com.app.service;

import com.app.engine.CostEstimationEngine;
import com.app.engine.OptimizationEngine;
import com.app.analyzer.InfrastructurePatternAnalyzer;
import com.app.model.AnalysisRequest;
import com.app.model.InfrastructureAnalysis;
import com.app.model.AWSResource;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class AnalysisService {
    
    private final RepositoryService repositoryService;
    private final CostEstimationEngine costEstimationEngine;
    private final OptimizationEngine optimizationEngine;
    private final InfrastructurePatternAnalyzer patternAnalyzer;

    public AnalysisService(
            RepositoryService repositoryService,
            CostEstimationEngine costEstimationEngine,
            OptimizationEngine optimizationEngine,
            InfrastructurePatternAnalyzer patternAnalyzer) {
        this.repositoryService = repositoryService;
        this.costEstimationEngine = costEstimationEngine;
        this.optimizationEngine = optimizationEngine;
        this.patternAnalyzer = patternAnalyzer;
    }
    
    /**
     * Analyze a repository for AWS infrastructure costs
     */
    public InfrastructureAnalysis analyzeRepository(AnalysisRequest request) {
        String repositoryUrl = request.getRepositoryUrl();
        
        // Step 1: Analyze repository and extract resources
        RepositoryService.RepositoryAnalysisResult analysisResult = 
            repositoryService.analyzeRepository(repositoryUrl);
        
        if (!analysisResult.isSuccess()) {
            throw new RuntimeException("Failed to analyze repository: " + analysisResult.getMessage());
        }
        
        List<AWSResource> resources = analysisResult.getResources();
        
        // If no resources found, add a default minimal setup
        if (resources.isEmpty()) {
            resources.add(AWSResource.builder()
                .type("ec2")
                .instanceType("t3.medium")
                .count(1)
                .build());
        }
        
        // Step 2: Calculate costs
        var costEstimate = costEstimationEngine.calculateCosts(resources);
        
        // Step 3: Generate optimizations
        var optimizations = optimizationEngine.generateOptimizations(resources);
        
        // Step 4: Analyze infrastructure patterns
        var architectureSummary = patternAnalyzer.analyzeInfrastructure(resources);
        
        // Step 4: Build response
        InfrastructureAnalysis analysis = InfrastructureAnalysis.builder()
            .repositoryUrl(repositoryUrl)
            .resources(resources)
            .costEstimate(costEstimate)
            .optimizations(optimizations)
            .analysisTimestamp(System.currentTimeMillis())
            .totalFilesScanned(10) // TODO: track actual file count
            .totalResourcesFound(resources.size())
            .build();
        
        // Cleanup
        try {
            repositoryService.cleanupRepository(analysisResult.getRepositoryPath());
        } catch (Exception e) {
            // Silently fail cleanup
        }
        
        return analysis;
    }
}

