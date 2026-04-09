# 🔧 AWS SDK DEPENDENCY FIX

## Problem

IDE shows compilation errors because AWS SDK dependencies haven't been downloaded:
```
package software.amazon.awssdk.services.pricing does not exist
cannot find symbol: class PricingClient
```

## ✅ Solution

The dependencies are declared in `pom.xml`, but Maven needs to download them. Follow these steps:

---

## 🚀 STEP 1: Force Maven to Download Dependencies

Run this command to force Maven to download and cache all dependencies:

```bash
cd /Users/srisaipeddapati/Desktop/cost-intelligence/cost-intelligence

# Download all dependencies
mvn dependency:resolve

# Or full build which also downloads
mvn clean install -DskipTests
```

This will:
- ✅ Download AWS SDK v2.20.0
- ✅ Download all related dependencies
- ✅ Build the entire project
- ✅ Create JAR file in target/

**Time**: 2-5 minutes (first time only)

---

## 🔄 STEP 2: Refresh IDE Cache

### For IntelliJ IDEA:

1. File → Invalidate Caches → Invalidate and Restart
2. After restart, Maven will refresh
3. Right-click project → Maven → Reload Projects
4. Wait for "Downloading sources..." to complete

### For Eclipse:

1. Right-click project → Maven → Update Project
2. Check "Force Update of Snapshots/Releases"
3. Click OK

---

## 📋 Verify Installation

After Maven finishes, verify AWS SDK is downloaded:

```bash
ls -la ~/.m2/repository/software/amazon/awssdk/pricing/2.20.0/

# Should show:
# pricing-2.20.0.jar
# pricing-2.20.0.pom
```

---

## ✅ After Dependencies Are Downloaded

Your IDE will automatically resolve:
- ✅ `software.amazon.awssdk.services.pricing.*`
- ✅ `PricingClient` class
- ✅ `GetProductsRequest` and other classes
- ✅ All compilation errors disappear

---

## 🎯 QUICK FIX COMMAND

```bash
cd /Users/srisaipeddapati/Desktop/cost-intelligence/cost-intelligence

# One command to download everything
mvn clean install -DskipTests

# Then refresh your IDE
```

---

## 💡 Why This Happens

1. You edited `pom.xml` and added AWS SDK dependencies
2. IDE shows red squiggles but hasn't downloaded yet
3. Maven downloads from internet on first use
4. Once downloaded and cached, IDE finds them automatically

---

## ⚠️ Common Issues

**Issue**: "Still can't find AWS SDK after maven build"
**Solution**: 
```bash
# Clear Maven cache and rebuild
rm -rf ~/.m2/repository/software/amazon/awssdk
mvn clean install -DskipTests
```

**Issue**: "Build hangs during dependency download"
**Solution**: Check internet connection and wait (can take 3-5 minutes for first build)

---

## 🚀 NEXT STEPS

After Maven finishes:

1. **Build succeeds** ✅
2. **IDE errors disappear** ✅
3. **Run the application**:
   ```bash
   # Set AWS credentials
   export AWS_ACCESS_KEY_ID="your-key"
   export AWS_SECRET_ACCESS_KEY="your-secret"
   
   # Run
   mvn spring-boot:run
   ```

---

## 📊 Expected Maven Output

```
[INFO] Downloading from central: https://repo.maven.apache.org/maven2/...
[INFO] Downloaded from central: ... (X KB)
...
[INFO] BUILD SUCCESS
```

**Duration**: 2-5 minutes (network dependent)

---

## ✨ Summary

| Step | Command | Time |
|------|---------|------|
| 1. Download deps | `mvn clean install -DskipTests` | 2-5 min |
| 2. Refresh IDE | File → Invalidate Caches | 1 min |
| 3. Verify | Check IDE - no red errors | Instant |
| 4. Run app | `mvn spring-boot:run` | 10 sec |

---

**The AWS SDK will be downloaded automatically during the Maven build. After that, your IDE will find all the classes!** ✅

