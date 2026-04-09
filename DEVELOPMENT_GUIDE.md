# Development Guide - AWS Cost Intelligence Analyzer

## 🛠️ Development Environment Setup

### Prerequisites
- Java 21 JDK
- Node.js 16+ LTS
- Maven 3.8+
- Git
- IDE (IntelliJ IDEA recommended)

### Installation

#### Java & Maven
```bash
# Install Homebrew (if not installed)
/bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"

# Install Java 21
brew install openjdk@21
export JAVA_HOME=/usr/libexec/java_home -v 21

# Install Maven
brew install maven

# Verify installation
java -version
mvn -version
```

#### Node.js
```bash
# Install Node.js
brew install node

# Verify installation
node -v
npm -v
```

---

## 🚀 Quick Start for Development

### Terminal 1: Backend
```bash
cd /Users/srisaipeddapati/Desktop/cost-intelligence/cost-intelligence

# First time only
mvn clean install

# Start development server
mvn spring-boot:run
```

Backend runs with:
- API: http://localhost:8080/api
- Health: http://localhost:8080/api/health
- Hot reload: Enabled via Spring DevTools

### Terminal 2: Frontend
```bash
cd /Users/srisaipeddapati/Desktop/cost-intelligence/cost-intelligence/frontend

# First time only
npm install

# Start development server
npm start
```

Frontend runs on: http://localhost:3000

---

## 📝 Code Structure & Best Practices

### Backend Structure

#### Controllers (`controller/`)
- Handle HTTP requests/responses
- Input validation
- CORS configuration
- Error handling

**Pattern**:
```java
@RestController
@RequestMapping("/api")
public class MyController {
    @PostMapping("/endpoint")
    public ResponseEntity<?> myEndpoint(@RequestBody MyRequest request) {
        // Handle request
        // Return response
    }
}
```

#### Services (`service/`)
- Business logic orchestration
- Repository management
- Coordination between layers

**Pattern**:
```java
@Service
public class MyService {
    private final DependencyA depA;
    private final DependencyB depB;
    
    public MyService(DependencyA depA, DependencyB depB) {
        this.depA = depA;
        this.depB = depB;
    }
    
    public ResultType doSomething(InputType input) {
        // Orchestrate operations
    }
}
```

#### Engines (`engine/`)
- Complex algorithms
- Cost calculation
- Optimization logic

**Pattern**:
```java
@Component
public class CalculationEngine {
    public Result calculate(Input input) {
        // Algorithm implementation
        return result;
    }
}
```

#### Parsers (`parser/`)
- IaC file parsing
- Regex-based or structured parsing
- Resource extraction

**Pattern**:
```java
@Component
public class TerraformParser {
    public List<AWSResource> parse(String content, String sourceFile) {
        // Parse content
        // Extract resources
        return resources;
    }
}
```

#### Models (`model/`)
- Data transfer objects
- API request/response bodies
- Internal data structures

**Pattern**:
```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MyModel {
    private String field1;
    private int field2;
    // Lombok generates getters, setters, equals, hashCode, toString
}
```

### Frontend Structure

#### Components (`components/`)
- Functional React components
- Props for configuration
- State management with hooks
- Event handlers

**Pattern**:
```javascript
export function MyComponent({ prop1, prop2, onEvent }) {
    const [state, setState] = useState(initialValue);
    
    const handleClick = (event) => {
        // Handle event
        setState(newValue);
        onEvent?.(data);
    };
    
    return (
        <div>
            {/* JSX */}
        </div>
    );
}
```

#### Styling (`styles/`)
- Component-scoped CSS
- Mobile-responsive design
- CSS custom properties for theming
- BEM naming convention (optional)

**Pattern**:
```css
.component-name {
    /* Styles */
}

.component-name__child {
    /* Child styles */
}

.component-name--variant {
    /* Variant styles */
}

@media (max-width: 768px) {
    /* Responsive styles */
}
```

---

## 🔄 Development Workflow

### Adding a New AWS Resource Type

#### Step 1: Update Terraform Parser
```java
// parser/TerraformParser.java
private List<AWSResource> parseNewResource(String content, String sourceFile) {
    Pattern pattern = Pattern.compile("resource\\s+\"aws_new_resource\"\\s+\"([^\"]+)\"\\s*\\{([^}]+)\\}");
    Matcher matcher = pattern.matcher(content);
    
    while (matcher.find()) {
        String config = matcher.group(2);
        // Extract properties
        AWSResource resource = AWSResource.builder()
            .type("new_resource")
            // ... other properties
            .build();
        resources.add(resource);
    }
    return resources;
}
```

#### Step 2: Update CloudFormation Parser
```java
// parser/CloudFormationParser.java
} else if (type.contains("AWS::NewResource")) {
    resources.add(parseNewResource(propertiesNode, sourceFile));
}
```

#### Step 3: Add Pricing
```java
// config/PricingConfig.java
public static final Map<String, Double> RESOURCE_PRICING = new HashMap<>();
static {
    RESOURCE_PRICING.put("type1", 100.00);
    RESOURCE_PRICING.put("type2", 200.00);
}
```

#### Step 4: Update Cost Engine
```java
// engine/CostEstimationEngine.java
for (AWSResource resource : resources) {
    if ("new_resource".equals(resource.getType())) {
        totalCost += calculateNewResourceCost(resource);
    }
}
```

#### Step 5: Add Optimization
```java
// engine/OptimizationEngine.java
private void analyzeNewResource(List<AWSResource> resources, List<Optimization> optimizations) {
    // Add recommendations
}
```

### Adding a New Frontend Component

#### Step 1: Create Component
```javascript
// components/NewComponent.js
export function NewComponent({ data }) {
    return (
        <div className="new-component">
            {/* JSX */}
        </div>
    );
}
```

#### Step 2: Add Styling
```css
/* styles/NewComponent.css */
.new-component {
    /* Styles */
}
```

#### Step 3: Import & Use
```javascript
// App.js
import { NewComponent } from './components/NewComponent';

export function App() {
    return <NewComponent data={analysisData} />;
}
```

---

## 🧪 Testing

### Backend Testing

#### Unit Tests
```java
// src/test/java/com/app/engine/CostEstimationEngineTest.java
@SpringBootTest
public class CostEstimationEngineTest {
    @Autowired
    private CostEstimationEngine engine;
    
    @Test
    public void testCostCalculation() {
        List<AWSResource> resources = // Create test data
        CostEstimate result = engine.calculateCosts(resources);
        assertEquals(expectedValue, result.getMonthly());
    }
}
```

#### Manual Testing with curl
```bash
# Test health endpoint
curl http://localhost:8080/api/health

# Test analysis endpoint
curl -X POST http://localhost:8080/api/analyze \
  -H "Content-Type: application/json" \
  -d '{
    "repositoryUrl": "https://github.com/example/repo"
  }'
```

### Frontend Testing

#### Component Testing
```javascript
// Test with React Testing Library
import { render, screen } from '@testing-library/react';
import { MyComponent } from './MyComponent';

test('renders component', () => {
    render(<MyComponent />);
    expect(screen.getByText('Expected Text')).toBeInTheDocument();
});
```

#### Manual Testing
1. Start frontend: `npm start`
2. Open DevTools (F12)
3. Check Network tab for API calls
4. Check Console for errors
5. Test with different repository URLs

---

## 📊 Debugging

### Backend Debugging

#### Enable Debug Logging
```properties
# src/main/resources/application.properties
logging.level.com.app=DEBUG
```

#### Using IDE Debugger
1. Set breakpoints in IDE
2. Run: `mvn spring-boot:run` (with debug flag)
3. Step through code

#### Log Statements
```java
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

private static final Logger logger = LoggerFactory.getLogger(MyClass.class);

logger.debug("Debug message: {}", variable);
logger.info("Info message");
logger.warn("Warning: {}", variable);
logger.error("Error: {}", variable, exception);
```

### Frontend Debugging

#### React DevTools
```bash
# Install React DevTools browser extension
# Available for Chrome and Firefox
```

#### Console Logging
```javascript
console.log('Value:', variable);
console.error('Error:', error);
console.warn('Warning:', message);
console.table(arrayOfObjects); // Pretty print arrays
```

#### Network Tab
- Check API requests/responses
- Verify CORS headers
- Monitor request size and time

---

## 🔄 Common Development Tasks

### Update AWS Pricing
```java
// src/main/java/com/app/config/PricingConfig.java
EC2_PRICING.put("instance_type", newPrice);
RDS_PRICING.put("instance_class", newPrice);
```

### Add New Optimization Suggestion
```java
// src/main/java/com/app/engine/OptimizationEngine.java
optimizations.add(Optimization.builder()
    .suggestion("Your suggestion")
    .description("Detailed description")
    .savingsPercentage("X-Y%")
    .monthlySavings(amount)
    .yearlySavings(amount * 12)
    .tradeoff("Trade-off explanation")
    .category("category")
    .priority(5)
    .complexity("low|medium|high")
    .build());
```

### Customize UI Theme
```css
/* src/styles/App.css */
:root {
  --primary-color: #3b82f6;
  --secondary-color: #ef4444;
  /* Update color values */
}
```

### Add New API Endpoint
```java
@PostMapping("/new-endpoint")
public ResponseEntity<?> newEndpoint(@RequestBody InputType input) {
    // Implementation
    return ResponseEntity.ok(response);
}
```

---

## 📦 Dependency Management

### Adding Backend Dependencies
```xml
<!-- pom.xml -->
<dependency>
    <groupId>org.example</groupId>
    <artifactId>artifact-name</artifactId>
    <version>1.0.0</version>
</dependency>
```

Then: `mvn clean install`

### Adding Frontend Dependencies
```bash
cd frontend
npm install package-name
# or
npm install --save-dev package-name
```

---

## 🚀 Performance Optimization

### Backend
- Add caching for repeated analyses
- Implement async processing
- Use connection pooling
- Optimize regex patterns in parsers

### Frontend
- Code splitting with React.lazy()
- Memoization with React.memo()
- Virtual scrolling for large lists
- Image optimization

---

## 🔐 Security Considerations

### Input Validation
```java
if (repositoryUrl == null || repositoryUrl.trim().isEmpty()) {
    throw new IllegalArgumentException("Repository URL is required");
}
```

### Safe Git Operations
```java
// Validate URL format
if (!repositoryUrl.matches("https?://.*\\.git")) {
    throw new IllegalArgumentException("Invalid repository URL");
}
```

### Resource Cleanup
```java
try {
    // Use resource
} finally {
    // Always cleanup
    repositoryService.cleanupRepository(path);
}
```

---

## 📚 Documentation Standards

### JavaDoc
```java
/**
 * Calculates the cost of AWS infrastructure.
 *
 * @param resources List of AWS resources
 * @return Cost estimate for different traffic profiles
 * @throws IllegalArgumentException if resources list is empty
 */
public CostEstimate calculateCosts(List<AWSResource> resources) {
    // Implementation
}
```

### Comments
```java
// Bad: obvious comment
int count = 0; // Set count to zero

// Good: explains WHY, not WHAT
// We count instances separately to support heterogeneous clusters
int instanceCount = 0;
```

---

## 🔄 Git Workflow

### Create Feature Branch
```bash
git checkout -b feature/new-feature
git add .
git commit -m "Add: description of changes"
git push origin feature/new-feature
```

### Create Pull Request
1. Push to feature branch
2. Open GitHub/GitLab
3. Create PR with description
4. Request review
5. Merge after approval

### Commit Message Convention
```
feat: Add new resource parsing for RDS clusters
fix: Resolve cost calculation error for storage
docs: Update API documentation
style: Format code according to conventions
refactor: Extract common parsing logic
test: Add unit tests for optimization engine
```

---

## 📈 Version Management

### Backend Version
Update in `pom.xml`:
```xml
<version>1.0.1</version>
```

### Frontend Version
Update in `package.json`:
```json
{
  "version": "1.0.1"
}
```

---

## 🎯 Code Quality

### Naming Conventions

**Java**:
- Classes: `PascalCase` (CostEstimationEngine)
- Methods: `camelCase` (calculateCosts)
- Constants: `UPPER_SNAKE_CASE` (MAX_INSTANCES)
- Variables: `camelCase` (instanceCount)

**JavaScript**:
- Components: `PascalCase` (CostSummary)
- Functions: `camelCase` (handleAnalyze)
- Constants: `UPPER_SNAKE_CASE` (API_BASE_URL)
- Variables: `camelCase` (userData)

### Code Organization

- Keep files focused on single responsibility
- Maximum file length: ~300 lines
- Maximum method length: ~50 lines
- Maximum class size: 10-15 methods

---

## 🆘 Troubleshooting

### Backend Issues

**Build fails**:
```bash
mvn clean -U install
```

**Port in use**:
```bash
lsof -ti:8080 | xargs kill -9
```

**Dependency conflicts**:
```bash
mvn dependency:tree
mvn dependency:resolve
```

### Frontend Issues

**NPM fails**:
```bash
rm -rf node_modules package-lock.json
npm cache clean --force
npm install
```

**Port in use**:
```bash
lsof -ti:3000 | xargs kill -9
```

**Module not found**:
```bash
npm install
npm list
```

---

## 📞 Getting Help

1. **Check logs**: Console output and IDE debugger
2. **Review documentation**: README, QUICKSTART, API_DOCUMENTATION
3. **Check examples**: Look at existing code patterns
4. **Search online**: Stack Overflow, GitHub issues
5. **Ask community**: Discussion forums, Slack channels

---

## 💡 Tips & Tricks

### IDE Shortcuts (IntelliJ IDEA)
- `Cmd + J`: Complete statement
- `Cmd + Shift + Return`: Complete method
- `Cmd + B`: Go to definition
- `Cmd + Shift + B`: Go to implementation
- `Cmd + /`: Comment line
- `Cmd + Shift + /`: Comment block

### npm Scripts
```json
{
  "scripts": {
    "start": "react-scripts start",
    "build": "react-scripts build",
    "test": "react-scripts test",
    "eject": "react-scripts eject"
  }
}
```

### Maven Commands
```bash
# Compile
mvn compile

# Run tests
mvn test

# Create JAR
mvn package

# Install locally
mvn install

# Clean build
mvn clean package

# Skip tests
mvn package -DskipTests
```

---

**Happy Coding!** 🚀

Last Updated: April 9, 2026

