# Quick Start Guide - AWS Cost Intelligence Analyzer

## ⚡ 5-Minute Quick Start

### Option 1: Using Docker (Easiest)

```bash
# From project root
docker-compose up --build

# Open in browser:
# Frontend: http://localhost:3000
# Backend API: http://localhost:8080/api/health
```

### Option 2: Manual Setup

#### Terminal 1 - Backend
```bash
# From project root
mvn clean install
mvn spring-boot:run

# Backend will start on http://localhost:8080
```

#### Terminal 2 - Frontend
```bash
# From project root
cd frontend
npm install
npm start

# Frontend will open at http://localhost:3000
```

## 🧪 Test the Application

### 1. Using the UI
1. Open http://localhost:3000
2. Paste this example repository URL:
   ```
   https://github.com/hashicorp/terraform-aws-modules
   ```
3. Click "Analyze"
4. Wait for results (30-60 seconds)

### 2. Using API directly
```bash
curl -X POST http://localhost:8080/api/analyze \
  -H "Content-Type: application/json" \
  -d '{
    "repositoryUrl": "https://github.com/hashicorp/terraform-aws-modules"
  }'
```

### 3. Health Check
```bash
curl http://localhost:8080/api/health
```

## 📁 Expected Output

When analyzing a repository with Terraform code, you should see:

```json
{
  "repositoryUrl": "...",
  "resources": [
    {
      "type": "ec2",
      "instanceType": "t3.large",
      "count": 2
    }
  ],
  "costEstimate": {
    "low_traffic": {
      "monthly_cost": 120.50,
      "yearly_cost": 1446.00
    },
    "medium_traffic": {
      "monthly_cost": 380.75,
      "yearly_cost": 4569.00
    },
    "high_traffic": {
      "monthly_cost": 1200.25,
      "yearly_cost": 14403.00
    }
  },
  "optimizations": [...],
  "totalResourcesFound": 5
}
```

## 🎨 What You'll See in the Dashboard

1. **Input Section**: Enter GitLab/GitHub repository URL
2. **Cost Summary**: Cards showing costs for Low/Medium/High traffic
3. **Cost Breakdown**: Pie chart showing costs by resource type
4. **Detected Resources**: List of EC2, RDS, S3, Lambda, LB instances found
5. **Optimization Suggestions**: 
   - Suggested improvements
   - Estimated savings
   - Trade-offs for each recommendation

## 🛠️ Troubleshooting

### Port Already in Use
```bash
# Kill process on port 8080 (backend)
lsof -ti:8080 | xargs kill -9

# Kill process on port 3000 (frontend)
lsof -ti:3000 | xargs kill -9
```

### Maven Build Fails
```bash
# Clear Maven cache
mvn clean
mvn -U install
```

### Frontend Build Issues
```bash
# Clear npm cache
npm cache clean --force
rm -rf node_modules package-lock.json
npm install
```

### Git Clone Permission Denied
The application needs to clone repositories. Make sure:
- Git is installed
- You have internet access
- For private repos, consider generating a GitLab/GitHub token

## 📊 Example Repositories to Analyze

Here are some public repositories you can test with:

1. **Terraform AWS Modules**
   ```
   https://github.com/hashicorp/terraform-aws-modules
   ```

2. **AWS Well-Architected Framework**
   ```
   https://github.com/aws/aws-architecture-icons
   ```

3. **CloudFormation Examples**
   ```
   https://github.com/aws-cloudformation/aws-cloudformation-user-guide
   ```

## 🔧 Development Tips

### Adding New Resource Types
1. Edit `TerraformParser.java` or `CloudFormationParser.java`
2. Add parsing method for new resource
3. Add pricing in `PricingConfig.java`
4. Update `OptimizationEngine.java` for recommendations

### Updating AWS Pricing
Edit `PricingConfig.java` with latest pricing from AWS pricing pages

### Customizing Optimizations
Edit `OptimizationEngine.java` methods:
- `analyzeEC2()`
- `analyzeRDS()`
- `analyzeS3()`
- etc.

## 📈 Performance Tips

For large repositories:
- Smaller repos analyze faster (< 100 IaC files)
- Public repos typically clone in 10-30 seconds
- Complex parsing can take 20-40 seconds

## 🚀 Production Deployment

For production, consider:

1. **Use Environment Variables**
   ```bash
   export SPRING_PROFILES_ACTIVE=prod
   export SERVER_PORT=8080
   ```

2. **Scale Horizontally**
   - Run multiple backend instances
   - Use load balancer (ALB/NLB)
   - Add caching layer (Redis)

3. **Security**
   - Add authentication
   - Use HTTPS/TLS
   - Implement rate limiting
   - Add API key management

4. **Database**
   - Move from H2 to PostgreSQL/MySQL
   - Add persistent storage

## 📞 Need Help?

1. Check the main [README.md](../README.md)
2. Review API documentation in README.md
3. Check project architecture section
4. Review test examples in this document

---

Happy analyzing! 🎉

