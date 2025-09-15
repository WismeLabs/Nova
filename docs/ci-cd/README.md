# 🚀 CI/CD Documentation

> **Everything you need to know about Nova's automated workflows**

## 📁 **What's in this folder:**

### 🛡️ **[Workflow Guide](./workflow-guide.md)**
- How our quality checks work
- What gets tested automatically
- Troubleshooting common issues
- Best practices for small teams

### 🔒 **[Branch Protection Setup](./branch-protection.md)**
- How to protect `main` and `beta` branches
- GitHub repository settings
- Preventing broken code from being merged

---

## 🎯 **Quick Overview**

### **Our Simple Approach:**
1. **Quality Check** - Prevents broken code (Build + Test + Lint)
2. **Branch Protection** - Forces PRs and reviews
3. **Clear Documentation** - Easy for small teams

### **Key Files:**
```
.github/
├── workflows/
│   ├── quality-check.yml     # Main CI workflow
│   └── cd.yml               # Deployment workflow
├── pull_request_template.md  # PR template
└── ISSUE_TEMPLATE/          # Issue templates
    ├── bug_report.yml
    └── feature_request.yml
```

---

## 🚀 **Getting Started**

1. **Read the [Workflow Guide](./workflow-guide.md)** to understand how CI works
2. **Set up [Branch Protection](./branch-protection.md)** in GitHub settings
3. **Create a test PR** to see the workflow in action

---

## 🎯 **For Developers**

The workflow is designed to be **helpful, not annoying**:
- ✅ Catches errors before they affect the team
- ✅ Fast feedback (5-10 minutes)
- ✅ Clear error messages
- ✅ Simple to understand and fix

---

## 🛠️ **For Team Leads**

Everything is configured for small teams:
- **Minimal setup required** - Just enable branch protection
- **No complex rules** - Build, test, lint - that's it
- **Flexible for emergencies** - Admin overrides available
- **Scales with your team** - Easy to add more checks later

---

**Need help?** Check the troubleshooting sections in each guide! 🚀