package com.app.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonProperty;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CostEstimate {
    @JsonProperty("low_traffic")
    private TrafficCost lowTraffic;
    
    @JsonProperty("medium_traffic")
    private TrafficCost mediumTraffic;
    
    @JsonProperty("high_traffic")
    private TrafficCost highTraffic;
    
    private CostBreakdown breakdown;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TrafficCost {
        @JsonProperty("requests_per_min")
        private int requestsPerMin;
        
        @JsonProperty("monthly_cost")
        private double monthlyCost;
        
        @JsonProperty("yearly_cost")
        private double yearlyCost;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CostBreakdown {
        @JsonProperty("compute_cost")
        private double computeCost;
        
        @JsonProperty("database_cost")
        private double databaseCost;
        
        @JsonProperty("storage_cost")
        private double storageCost;
        
        @JsonProperty("load_balancer_cost")
        private double loadBalancerCost;
        
        @JsonProperty("total_cost")
        private double totalCost;
    }
}

