# AWS Cost Intelligence Analyzer - API Documentation

## Base URL
```
http://localhost:8080/api
```

## Endpoints

### 1. POST /analyze
Analyzes a repository for AWS infrastructure costs.

**Description**: 
- Clones the specified Git repository
- Scans for IaC files (Terraform, CloudFormation)
- Extracts AWS resources
- Calculates costs under different traffic profiles
- Generates optimization recommendations

**Request**:
```http
POST /api/analyze
Content-Type: application/json

{
  "repositoryUrl": "https://github.com/user/repo",
  "gitlabToken": "optional-personal-access-token"
}
```

**Parameters**:
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| repositoryUrl | string | Yes | GitHub or GitLab repository URL (HTTPS format) |
| gitlabToken | string | No | Personal access token for private repositories |

**Response** (200 OK):
```json
{
  "repositoryUrl": "https://github.com/user/repo",
  "resources": [
    {
      "type": "ec2",
      "instanceType": "t3.large",
      "count": 2,
      "region": "us-east-1",
      "sourceFile": "/path/to/main.tf",
      "lineNumber": 15
    },
    {
      "type": "rds",
      "instanceType": "db.m5.large",
      "count": 1,
      "sourceFile": "/path/to/database.tf"
    },
    {
      "type": "s3",
      "size": "100GB",
      "tier": "standard",
      "sourceFile": "/path/to/storage.tf"
    },
    {
      "type": "load_balancer",
      "instanceType": "application",
      "count": 1,
      "sourceFile": "/path/to/networking.tf"
    },
    {
      "type": "lambda",
      "size": "256MB",
      "count": 1,
      "sourceFile": "/path/to/lambda.tf"
    }
  ],
  "costEstimate": {
    "low_traffic": {
      "requests_per_min": 100,
      "monthly_cost": 120.50,
      "yearly_cost": 1446.00
    },
    "medium_traffic": {
      "requests_per_min": 1000,
      "monthly_cost": 380.75,
      "yearly_cost": 4569.00
    },
    "high_traffic": {
      "requests_per_min": 10000,
      "monthly_cost": 1200.25,
      "yearly_cost": 14403.00
    },
    "breakdown": {
      "compute_cost": 135.52,
      "database_cost": 200.00,
      "storage_cost": 2.30,
      "load_balancer_cost": 20.00,
      "total_cost": 357.82
    }
  },
  "optimizations": [
    {
      "suggestion": "Use Reserved Instances (RI) or Savings Plans",
      "description": "Commit to 1-3 year terms for consistent workloads",
      "savingsPercentage": "30-60%",
      "monthlySavings": 1000.00,
      "yearlySavings": 12000.00,
      "tradeoff": "Requires upfront commitment; less flexibility for sudden scaling down",
      "category": "compute",
      "priority": 5,
      "complexity": "medium"
    },
    {
      "suggestion": "Enable Auto-Scaling for EC2 instances",
      "description": "Use Auto Scaling Groups to dynamically adjust instance count based on demand",
      "savingsPercentage": "20-50%",
      "monthlySavings": 800.00,
      "yearlySavings": 9600.00,
      "tradeoff": "Slight latency during scaling events (typically 1-2 minutes)",
      "category": "compute",
      "priority": 4,
      "complexity": "medium"
    }
  ],
  "analysisTimestamp": 1712651234567,
  "totalFilesScanned": 15,
  "totalResourcesFound": 8
}
```

**Error Responses**:

400 Bad Request:
```json
{
  "error": "Repository URL is required",
  "timestamp": "1712651234567"
}
```

500 Internal Server Error:
```json
{
  "error": "Error analyzing repository: Repository not found",
  "timestamp": "1712651234567"
}
```

---

### 2. GET /health
Health check endpoint.

**Description**: Returns the status of the application

**Request**:
```http
GET /api/health
```

**Response** (200 OK):
```json
{
  "status": "UP",
  "service": "AWS Cost Intelligence Analyzer",
  "version": "1.0"
}
```

---

## Data Models

### AWSResource
Represents a detected AWS resource in the IaC code.

```json
{
  "type": "ec2|rds|s3|lambda|load_balancer",
  "instanceType": "t3.large|db.m5.large|etc",
  "count": 1,
  "size": "100GB|256MB|etc",
  "tier": "standard|infrequent_access|glacier",
  "region": "us-east-1",
  "sourceFile": "/path/to/file.tf",
  "lineNumber": 15
}
```

### CostEstimate
Contains cost calculations for different traffic profiles.

```json
{
  "low_traffic": {
    "requests_per_min": 100,
    "monthly_cost": 120.50,
    "yearly_cost": 1446.00
  },
  "medium_traffic": {
    "requests_per_min": 1000,
    "monthly_cost": 380.75,
    "yearly_cost": 4569.00
  },
  "high_traffic": {
    "requests_per_min": 10000,
    "monthly_cost": 1200.25,
    "yearly_cost": 14403.00
  },
  "breakdown": {
    "compute_cost": 135.52,
    "database_cost": 200.00,
    "storage_cost": 2.30,
    "load_balancer_cost": 20.00,
    "total_cost": 357.82
  }
}
```

### Optimization
Represents an optimization recommendation.

```json
{
  "suggestion": "Use Reserved Instances",
  "description": "Commit to 1-3 year terms for consistent workloads",
  "savingsPercentage": "30-60%",
  "monthlySavings": 1000.00,
  "yearlySavings": 12000.00,
  "tradeoff": "Requires upfront commitment",
  "category": "compute|storage|database|architecture|operations",
  "priority": 5,
  "complexity": "low|medium|high"
}
```

### InfrastructureAnalysis
Complete analysis result.

```json
{
  "repositoryUrl": "https://github.com/user/repo",
  "resources": [],
  "costEstimate": {},
  "optimizations": [],
  "analysisTimestamp": 1712651234567,
  "totalFilesScanned": 15,
  "totalResourcesFound": 8
}
```

---

## Supported IaC Formats

### Terraform (.tf files)
Supported resources:
- `aws_instance` → EC2 instance
- `aws_db_instance` → RDS database
- `aws_s3_bucket` → S3 bucket
- `aws_lb` → Load balancer
- `aws_lambda_function` → Lambda function

Example parsing:
```hcl
resource "aws_instance" "web" {
  instance_type = "t3.large"
  count         = 2
}
```

### CloudFormation (YAML/JSON)
Supported resource types:
- `AWS::EC2::Instance` → EC2 instance
- `AWS::RDS::DBInstance` → RDS database
- `AWS::S3::Bucket` → S3 bucket
- `AWS::ElasticLoadBalancingV2::LoadBalancer` → Load balancer
- `AWS::Lambda::Function` → Lambda function

Example parsing:
```yaml
Resources:
  WebServer:
    Type: AWS::EC2::Instance
    Properties:
      InstanceType: t3.large
```

---

## Traffic Profiles

The analyzer estimates costs under three traffic profiles:

| Profile | Requests/Min | Use Case |
|---------|-------------|----------|
| Low | 100 | Development, Testing |
| Medium | 1,000 | Standard Production |
| High | 10,000 | Peak Load, High Traffic |

The cost scaling considers:
- EC2 auto-scaling requirements
- Lambda invocation counts
- Database connection load
- Data transfer costs
- Load balancer utilization

---

## Optimization Categories

### Compute
- Downsize instances
- Use Reserved Instances
- Enable Auto-Scaling
- Use Spot Instances
- Migrate to serverless

### Database
- Review Multi-AZ configuration
- Purchase Reserved Instances
- Implement caching (ElastiCache)
- Use read replicas

### Storage
- S3 Intelligent-Tiering
- Move to Glacier
- Lifecycle policies

### Architecture
- Serverless migration (Lambda + DynamoDB)
- NoSQL alternatives (DynamoDB vs RDS)
- Monitoring and right-sizing

### Operations
- CloudWatch monitoring
- Resource utilization tracking
- Continuous optimization

---

## Example Requests and Responses

### Example 1: Basic Terraform Repository

**Request**:
```bash
curl -X POST http://localhost:8080/api/analyze \
  -H "Content-Type: application/json" \
  -d '{
    "repositoryUrl": "https://github.com/example/terraform-aws"
  }'
```

**Response**:
```json
{
  "repositoryUrl": "https://github.com/example/terraform-aws",
  "resources": [
    {
      "type": "ec2",
      "instanceType": "t3.medium",
      "count": 1
    }
  ],
  "costEstimate": {
    "low_traffic": {
      "requests_per_min": 100,
      "monthly_cost": 33.88,
      "yearly_cost": 406.56
    },
    "medium_traffic": {
      "requests_per_min": 1000,
      "monthly_cost": 50.25,
      "yearly_cost": 603.00
    },
    "high_traffic": {
      "requests_per_min": 10000,
      "monthly_cost": 150.75,
      "yearly_cost": 1809.00
    },
    "breakdown": {
      "compute_cost": 33.88,
      "database_cost": 0.00,
      "storage_cost": 0.00,
      "load_balancer_cost": 0.00,
      "total_cost": 33.88
    }
  },
  "optimizations": [
    {
      "suggestion": "Use Reserved Instances (RI) or Savings Plans",
      "description": "Commit to 1-3 year terms for consistent workloads",
      "savingsPercentage": "30-60%",
      "monthlySavings": 20.33,
      "yearlySavings": 243.96,
      "tradeoff": "Requires upfront commitment",
      "category": "compute",
      "priority": 5,
      "complexity": "medium"
    }
  ],
  "analysisTimestamp": 1712651234567,
  "totalFilesScanned": 3,
  "totalResourcesFound": 1
}
```

---

## Rate Limiting

Currently, there is no rate limiting. For production, implement:
- API key authentication
- Rate limiting (e.g., 10 requests/minute per user)
- Request throttling

---

## Error Handling

The API returns appropriate HTTP status codes:

| Status | Meaning |
|--------|---------|
| 200 | Success |
| 400 | Bad Request (invalid input) |
| 500 | Internal Server Error |

All errors include a timestamp and error message for debugging.

---

## Performance Expectations

| Factor | Typical Time |
|--------|-------------|
| Repository clone | 10-30 seconds |
| File scanning | 5-15 seconds |
| Cost calculation | 1-3 seconds |
| **Total** | **20-50 seconds** |

Factors affecting performance:
- Repository size
- Number of IaC files
- Internet connection speed
- Server resources

---

## Best Practices

1. **Use HTTPS URLs**: Always use `https://` for repository URLs
2. **Valid Repositories**: Ensure the repository URL is publicly accessible or provide valid token
3. **Timeout**: Set client timeout to at least 60 seconds
4. **Caching**: Cache results to avoid re-analyzing the same repository
5. **Error Handling**: Implement retry logic with exponential backoff

---

## Support

For API issues or questions:
1. Check this documentation
2. Review the README.md
3. Check the Quick Start guide
4. Review the GitHub issues

---

Last Updated: April 9, 2026
Version: 1.0

