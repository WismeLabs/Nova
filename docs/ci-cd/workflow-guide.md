# 🛡️ GitHub Workflow Guide

> **Simple quality checks to prevent buggy code from being merged**

## 🎯 What We Have

### ✅ **Quality Check Workflow** (Required for all PRs)
**File:** `.github/workflows/quality-check.yml`

**What it does:**
- 🔨 **Builds your code** - Catches compilation errors
- 🧪 **Runs tests** - Catches logic bugs  
- 📊 **Runs lint** - Catches code quality issues
- 💬 **Comments on PR** - Tells you if everything is good

**When it runs:**
- ✅ Every time you open a PR
- ✅ Every time you push to a PR
- ✅ When code is pushed to `beta` branch

---

## 🚀 **How It Works for Developers**

### 1. **Create a PR**
```bash
git checkout -b feat/my-feature
git push origin feat/my-feature
# Open PR on GitHub
```

### 2. **Automatic Quality Check**
- GitHub automatically runs the quality check
- You'll see a ✅ or ❌ status on your PR
- If ❌, you CANNOT merge until fixed

### 3. **Fix Issues (if any)**
```bash
# Fix the errors shown in the workflow
git add .
git commit -m "fix: resolve build errors"
git push
# Quality check runs again automatically
```

### 4. **Merge When Green**
- Only merge when you see ✅ "All checks have passed"
- This ensures no broken code gets into `beta` or `main`

---

## 🔍 **What Gets Checked**

| Check | What it catches | Example |
|-------|----------------|---------|
| **Build** | Compilation errors | Missing imports, syntax errors |
| **Tests** | Logic bugs | Functions returning wrong values |
| **Lint** | Code quality | Unused variables, formatting |

---

## 🚨 **Common Issues & Fixes**

### **Build Fails** 🔨
```
Error: Cannot find symbol: class MyClass
```
**Fix:** Add missing imports or fix typos

### **Tests Fail** 🧪  
```
Expected: true, Actual: false
```
**Fix:** Fix the logic in your code or update the test

### **Lint Issues** 📊
```
Unused variable 'result'
```
**Fix:** Remove unused code or use the variable

---

## 📋 **PR Template Guide**

When you create a PR, GitHub will show a template. Fill it out to help reviewers:

- **What does this PR do?** - Brief description
- **How to test it?** - Steps for reviewers to test
- **Screenshots** - Show UI changes
- **Checklist** - Quick self-review

---

## 🎯 **Best Practices for Small Teams**

### **Before Creating PR:**
```bash
# Test locally first
./gradlew assembleDebug
./gradlew test
./gradlew lintDebug
```

### **PR Size:**
- ✅ Keep PRs small (< 400 lines changed)
- ✅ One feature per PR
- ❌ Don't bundle multiple features

### **Commit Messages:**
```bash
# Good
feat(auth): add login validation
fix(player): resolve crash on pause
docs(readme): update setup instructions

# Bad  
update stuff
fix bug
wip
```

---

## 🛠️ **Troubleshooting**

### **Workflow Taking Too Long?**
- Normal time: 5-10 minutes
- If > 15 minutes, check if tests are hanging

### **Workflow Fails on Setup?**
- Usually a temporary GitHub issue
- Click "Re-run jobs" to try again

### **Local Works, CI Fails?**
- Check you committed all files
- Verify `local.properties` isn't needed for CI

---

## 🎉 **That's It!**

The workflow is designed to be **simple and helpful**:
- Catches errors before they reach other developers
- Gives clear feedback on what to fix
- Doesn't slow down development with complex rules

**Remember:** The goal is to catch obvious bugs, not be perfect. Keep it simple for your small team! 🚀