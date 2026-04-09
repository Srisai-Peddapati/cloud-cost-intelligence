package com.app.controller;

import com.app.model.AnalysisRequest;
import com.app.model.InfrastructureAnalysis;
import com.app.service.AnalysisService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class AnalysisController {
    
    private final AnalysisService analysisService;
    
    public AnalysisController(AnalysisService analysisService) {
        this.analysisService = analysisService;
    }
    
    /**
     * POST /api/analyze
     * Analyzes a GitLab repository for AWS infrastructure costs
     */
    @PostMapping("/analyze")
    public ResponseEntity<?> analyzeRepository(@RequestBody AnalysisRequest request) {
        try {
            // Validate request
            if (request.getRepositoryUrl() == null || request.getRepositoryUrl().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(
                    createErrorResponse("Repository URL is required")
                );
            }
            
            // Analyze repository
            InfrastructureAnalysis analysis = analysisService.analyzeRepository(request);
            
            return ResponseEntity.ok(analysis);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                createErrorResponse("Error analyzing repository: " + e.getMessage())
            );
        }
    }
    
    /**
     * GET /api/health
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<?> health() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "AWS Cost Intelligence Analyzer");
        response.put("version", "1.0");
        return ResponseEntity.ok(response);
    }
    
    /**
     * Helper method to create error response
     */
    private Map<String, String> createErrorResponse(String message) {
        Map<String, String> error = new HashMap<>();
        error.put("error", message);
        error.put("timestamp", String.valueOf(System.currentTimeMillis()));
        return error;
    }
}

