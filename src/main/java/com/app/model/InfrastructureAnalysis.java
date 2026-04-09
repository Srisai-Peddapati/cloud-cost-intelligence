package com.app.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InfrastructureAnalysis {
    private String repositoryUrl;
    private List<AWSResource> resources;
    private CostEstimate costEstimate;
    private List<Optimization> optimizations;
    
    // Metadata
    private long analysisTimestamp;
    private int totalFilesScanned;
    private int totalResourcesFound;
}

