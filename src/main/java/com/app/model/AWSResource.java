package com.app.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AWSResource {
    private String type; // ec2, rds, s3, lambda, elb, etc.
    private String instanceType; // t3.large, db.m5.large, etc.
    private int count; // number of instances
    private String size; // for storage: 100GB, etc.
    private String region; // us-east-1, etc.
    private String tier; // for databases: standard, high-performance, etc.

    // Additional metadata
    private String sourceFile; // where this resource was found
    private int lineNumber; // line number in the source file
}

