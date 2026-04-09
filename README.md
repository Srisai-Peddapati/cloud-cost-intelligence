# AWS Cost Intelligence Analyzer

A web-based dashboard that analyzes Infrastructure-as-Code (IaC) repositories and estimates AWS infrastructure costs under different traffic scenarios, while suggesting optimizations and explaining trade-offs.

## 🎯 Features

### Core Features
- **Repository Analysis**: Parse GitLab/GitHub repositories containing IaC code
- **IaC Support**: 
  - Terraform (`.tf` files)
  - CloudFormation (`.yaml`, `.yml`, `.json`)
  - Basic AWS CDK support
  
- **Resource Detection**: Automatically extract AWS resources:
  - EC2 Instances
  - RDS Databases
  - S3 Buckets
  - Lambda Functions
  - Load Balancers

- **Cost Estimation**: Calculate AWS infrastructure costs under three traffic profiles:
  - **Low Traffic**: 100 requests/minute
  - **Medium Traffic**: 1,000 requests/minute
  - **High Traffic**: 10,000 requests/minute

- **Cost Reports**: 
  - Monthly and yearly cost estimates
  - Cost breakdown by resource type (Compute, Database, Storage, Load Balancer)
  - Visual charts and graphs

- **Optimization Suggestions**:
  - Smart recommendations based on detected infrastructure
  - Estimated savings per optimization
  - Trade-off analysis for each suggestion
  - Priority-based recommendations

## 🏗️ Project Architecture

### Backend (Spring Boot 3.2)
```
src/main/java/com/app/
├── Main.java                          # Spring Boot entry point
├── controller/
│   └── AnalysisController.java       # REST API endpoints
├── service/
│   ├── AnalysisService.java          # Orchestration logic
│   └── RepositoryService.java        # Repository cloning & parsing
├── engine/
│   ├── CostEstimationEngine.java     # Cost calculation logic
│   └── OptimizationEngine.java       # Optimization suggestions
├── parser/
│   ├── TerraformParser.java          # Terraform parsing
│   └── CloudFormationParser.java     # CloudFormation parsing
├── model/
│   ├── AWSResource.java
│   ├── CostEstimate.java
│   ├── InfrastructureAnalysis.java
│   ├── Optimization.java
│   └── AnalysisRequest.java
└── config/
    └── PricingConfig.java            # AWS pricing configuration
```

### Frontend (React 18)
```
frontend/
├── public/
│   └── index.html
├── src/
│   ├── App.js
│   ├── index.js
│   ├── components/
│   │   ├── AnalysisInput.js
│   │   ├── CostSummary.js
│   │   ├── CostBreakdown.js
│   │   ├── OptimizationsList.js
│   │   └── ResourcesList.js
│   └── styles/
│       ├── App.css
│       ├── CostSummary.css
│       ├── CostBreakdown.css
│       ├── OptimizationsList.css
│       └── ResourcesList.css
└── package.json
```

## 🚀 Getting Started

### Prerequisites
- Java 21+
- Node.js 16+
- Maven 3.8+
- Git

### Backend Setup

1. **Navigate to project directory**:
```bash
cd /Users/srisaipeddapati/Desktop/cost-intelligence/cost-intelligence
```

2. **Install dependencies**:
```bash
mvn clean install
```

3. **Run the application**:
```bash
mvn spring-boot:run
```

The backend will start on `http://localhost:8080`

4. **Health Check**:
```bash
curl http://localhost:8080/api/health
```

### Frontend Setup

1. **Navigate to frontend directory**:
```bash
cd frontend
```

2. **Install dependencies**:
```bash
npm install
```

3. **Start development server**:
```bash
npm start
```

The frontend will open at `http://localhost:3000`

## 📡 API Endpoints

### POST /api/analyze
Analyzes a repository for AWS infrastructure costs.

**Request**:
```json
{
  "repositoryUrl": "https://github.com/user/repo",
  "gitlabToken": "optional-token-for-private-repos"
}
```

**Response**:
```json
{
  "repositoryUrl": "https://github.com/user/repo",
  "resources": [
    {
      "type": "ec2",
      "instanceType": "t3.large",
      "count": 2,
      "sourceFile": "path/to/main.tf"
    }
  ],
  "costEstimate": {
    "low_traffic": {
      "requests_per_min": 100,
      "monthly_cost": 120.00,
      "yearly_cost": 1440.00
    },
    "medium_traffic": {
      "requests_per_min": 1000,
      "monthly_cost": 380.00,
      "yearly_cost": 4560.00
    },
    "high_traffic": {
      "requests_per_min": 10000,
      "monthly_cost": 1200.00,
      "yearly_cost": 14400.00
    },
    "breakdown": {
      "compute_cost": 135.52,
      "database_cost": 0.00,
      "storage_cost": 2.30,
      "load_balancer_cost": 0.00,
      "total_cost": 137.82
    }
  },
  "optimizations": [
    {
      "suggestion": "Use Reserved Instances",
      "description": "Commit to 1-3 year terms for consistent workloads",
      "savingsPercentage": "30-60%",
      "monthlySavings": 1000.00,
      "yearlySavings": 12000.00,
      "tradeoff": "Requires upfront commitment; less flexibility",
      "category": "compute",
      "priority": 5,
      "complexity": "medium"
    }
  ],
  "analysisTimestamp": 1712651234000,
  "totalFilesScanned": 15,
  "totalResourcesFound": 8
}
```

### GET /api/health
Health check endpoint.

**Response**:
```json
{
  "status": "UP",
  "service": "AWS Cost Intelligence Analyzer",
  "version": "1.0"
}
```

## 💰 Pricing Configuration

The application includes hardcoded AWS pricing (as of April 2024):

### EC2 Instance Pricing (Monthly)
- t3.micro: $8.47
- t3.small: $16.94
- t3.medium: $33.88
- t3.large: $67.76
- m5.large: $96.00
- c5.large: $85.00

### RDS Instance Pricing (Monthly)
- db.t3.micro: $19.70
- db.t3.small: $39.40
- db.m5.large: $200.00
- db.r5.large: $550.00

### S3 Pricing (Per GB/Month)
- Standard: $0.023
- Infrequent Access: $0.0125
- Glacier: $0.004

### Load Balancers (Monthly)
- ALB: $20.00
- NLB: $32.50

### Lambda
- Per Million Requests: $0.20
- Per GB-Second: $0.0000166667

## 🔍 Supported IaC Formats

### Terraform
Supports parsing of:
- `aws_instance` - EC2 instances
- `aws_db_instance` - RDS databases
- `aws_s3_bucket` - S3 buckets
- `aws_lb` - Load balancers
- `aws_lambda_function` - Lambda functions

### CloudFormation
Supports YAML and JSON templates with:
- AWS::EC2::Instance
- AWS::RDS::DBInstance
- AWS::S3::Bucket
- AWS::ElasticLoadBalancingV2::LoadBalancer
- AWS::Lambda::Function

## 📊 Cost Estimation Logic

The cost estimation engine:
1. Aggregates all detected resources
2. Calculates base infrastructure cost
3. Adjusts for traffic profiles (EC2 auto-scaling, Lambda invocations, etc.)
4. Includes data transfer costs
5. Returns monthly and yearly estimates

### Traffic Profile Assumptions
- **Low Traffic**: Minimal resource utilization, suitable for dev/test
- **Medium Traffic**: Normal production load, cost-optimized setup
- **High Traffic**: Peak load handling, may require scaling

## 🎯 Optimization Recommendations

The engine generates recommendations in several categories:

### Compute Optimizations
- Downsize instances
- Use Reserved Instances (RI)
- Enable Auto-Scaling
- Use Spot Instances for non-critical workloads
- Migrate to serverless (Lambda)

### Database Optimizations
- Review Multi-AZ necessity
- Purchase RDS Reserved Instances
- Implement ElastiCache or read replicas

### Storage Optimizations
- Use S3 Intelligent-Tiering
- Move to Glacier for archive
- Enable lifecycle policies

### Architecture-level
- Migrate to serverless
- Evaluate DynamoDB for NoSQL
- Monitor CloudWatch metrics

## 🛠️ Configuration

### Backend Configuration
Edit `src/main/resources/application.properties`:
```properties
server.port=8080
spring.application.name=AWS Cost Intelligence Analyzer
logging.level.com.app=DEBUG
```

### Frontend Configuration
Set API base URL in `frontend/src/App.js`:
```javascript
const API_BASE = 'http://localhost:8080/api';
```

## 📈 Limitations & Future Improvements

### Current Limitations
- Basic parsing (regex-based, not full IaC DSL parsing)
- Hardcoded AWS pricing (not real-time)
- No authentication required
- Single repository analysis per request
- Limited CDK support

### Planned Enhancements
- Real-time AWS pricing integration
- Database persistence for historical analysis
- User authentication and authorization
- Cost comparison (before/after optimization)
- Integration with AWS Cost Explorer
- Support for more IaC tools (CDK, Pulumi)
- Cost trends and forecasting

## 🧪 Testing

### Manual Testing

1. **Test with public repository**:
```bash
curl -X POST http://localhost:8080/api/analyze \
  -H "Content-Type: application/json" \
  -d '{"repositoryUrl":"https://github.com/hashicorp/terraform-provider-aws"}'
```

2. **Test UI**:
- Open `http://localhost:3000`
- Enter a repository URL
- Click "Analyze"
- Review cost estimates and optimizations

## 📝 License

MIT License - Feel free to use and modify for your purposes.

## 👥 Contributing

Contributions are welcome! Please follow these steps:
1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Submit a pull request

## 📞 Support

For issues or questions:
1. Check the GitHub Issues page
2. Review the architecture documentation
3. Check the API response examples

## 🎓 Educational Notes

This project demonstrates:
- **Spring Boot 3.2**: Modern Spring framework patterns
- **REST API Design**: Clean, RESTful endpoint design
- **React 18**: Component-based frontend architecture
- **IaC Parsing**: Regex-based parsing of Terraform and CloudFormation
- **Cost Calculation**: Algorithm for infrastructure cost estimation
- **UI/UX**: Clean dashboard design with charts and visualizations

---

**Last Updated**: April 9, 2026
**Version**: 1.0-SNAPSHOT

