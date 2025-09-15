# 🛡️ Branch Protection Setup

> **How to prevent broken code from reaching your main branches**

## 🎯 **Why You Need This**
- Prevents developers from pushing broken code directly to `main` or `beta`
- Forces all changes to go through PRs and quality checks
- Keeps your main branches stable and deployable

---

## ⚙️ **GitHub Settings to Configure**

### 1. **Go to Repository Settings**
- Navigate to your Nova repository on GitHub
- Click **Settings** tab
- Click **Branches** in the left sidebar

### 2. **Protect `main` Branch**
Click **Add rule** and configure:

```
Branch name pattern: main

✅ Require a pull request before merging
  ✅ Require approvals: 1
  ✅ Dismiss stale PR approvals when new commits are pushed

✅ Require status checks to pass before merging
  ✅ Require branches to be up to date before merging
  Search for: "Error Prevention Check" (from quality-check.yml)

✅ Require conversation resolution before merging
✅ Include administrators (recommended for small teams)
```

### 3. **Protect `beta` Branch**
Repeat the same for `beta` branch with slightly relaxed rules:

```
Branch name pattern: beta

✅ Require a pull request before merging
  ✅ Require approvals: 1 (can be 0 for faster development)

✅ Require status checks to pass before merging
  ✅ Require branches to be up to date before merging
  Search for: "Error Prevention Check"

✅ Include administrators
```

---

## 🚀 **Result: Protected Workflow**

### **What Developers Can Do:**
```bash
✅ Create feature branches from beta
✅ Push to feature branches
✅ Open PRs to beta or main
```

### **What Developers CANNOT Do:**
```bash
❌ Push directly to main
❌ Push directly to beta  
❌ Merge PR without quality checks passing
❌ Merge PR without approval
```

### **Typical Workflow:**
```bash
1. git checkout beta
2. git checkout -b feat/my-feature
3. # Make changes
4. git push origin feat/my-feature
5. # Open PR to beta
6. # Wait for quality check ✅
7. # Get approval from teammate
8. # Merge PR
```

---

## 🔧 **Small Team Recommendations**

### **For Development Speed:**
- `beta` branch: Require quality checks but allow self-merge
- `main` branch: Require quality checks + 1 approval

### **For Extra Safety:**
- Both branches: Require quality checks + 1 approval
- Dismiss stale approvals on new commits

### **Quality Check Status:**
Make sure "Error Prevention Check" is required:
- This is the job name from `quality-check.yml`
- It ensures code compiles, tests pass, and lint passes
- No PR can merge without this passing

---

## 🎯 **Benefits for Your Team**

✅ **Prevents Broken Builds**
- No more "who broke the build?" moments
- Quality checks catch errors before merge

✅ **Maintains Code Quality**
- Lint checks prevent sloppy code
- Test requirements catch bugs early

✅ **Encourages Code Review**
- Required approvals mean fresh eyes on code
- Knowledge sharing between team members

✅ **Safe Deployment**
- `main` branch is always deployable
- `beta` branch is stable for testing

---

## 🚨 **Emergency Override (Use Sparingly)**

As admin, you can still:
- Force push to protected branches (if enabled)
- Merge without status checks (in emergency)
- Bypass approval requirements

**When to use:**
- Hotfix for critical production bug
- CI system is down but code is verified safe
- Emergency deployment needed

**Remember:** Use these overrides only when absolutely necessary!

---

## 📋 **Quick Setup Checklist**

- [ ] Go to Repository Settings > Branches
- [ ] Add protection rule for `main` branch
- [ ] Add protection rule for `beta` branch  
- [ ] Require "Error Prevention Check" status
- [ ] Set approval requirements (1 person recommended)
- [ ] Test with a dummy PR to verify it works

**That's it!** Your branches are now protected and your code quality is automatically enforced. 🛡️