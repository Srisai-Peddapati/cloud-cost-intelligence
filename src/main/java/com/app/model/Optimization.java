package com.app.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Optimization {
    private String suggestion;
    private String description;
    private String savingsPercentage;
    private double monthlySavings;
    private double yearlySavings;
    private String tradeoff;
    private String category; // compute, storage, database, architecture
    private int priority; // 1-5, higher is more important
    private String complexity; // low, medium, high
}

