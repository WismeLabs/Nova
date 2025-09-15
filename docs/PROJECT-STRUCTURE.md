# 📁 Nova Project Structure

> **Clean, organized codebase for the Nova Android app**

## 🏗️ **Overall Structure**

```
Nova/
├── 📱 app/                          # Android application code
│   ├── src/main/java/com/wisme/nova/
│   │   ├── ui/                      # UI screens (Compose)
│   │   ├── viewmodel/               # ViewModels (MVVM)
│   │   ├── domain/                  # Business logic
│   │   ├── data/                    # Data layer (API, DB)
│   │   ├── di/                      # Dependency injection
│   │   └── utils/                   # Helpers & constants
│   ├── src/main/res/                # Android resources
│   └── build.gradle.kts             # App dependencies
│
├── 📋 docs/                         # Documentation
│   └── ci-cd/                       # CI/CD documentation
│       ├── README.md                # CI/CD overview
│       ├── workflow-guide.md        # How CI works
│       └── branch-protection.md     # GitHub settings
│
├── ⚙️ .github/                      # GitHub workflows & templates
│   ├── workflows/
│   │   └── quality-check.yml        # Main CI workflow
│   ├── ISSUE_TEMPLATE/
│   │   ├── bug_report.yml           # Bug report template
│   │   └── feature_request.yml      # Feature request template
│   └── pull_request_template.md     # PR template
│
├── 🔧 gradle/                       # Gradle wrapper
├── 📄 build.gradle.kts              # Project-level build config
├── 📄 settings.gradle.kts           # Gradle settings
├── 📄 local.properties              # Local environment config
├── 📄 SETUP.md                      # Development setup guide
└── 📄 README.md                     # Main project documentation
```

---

## 🎯 **Key Organization Principles**

### **📁 Grouped by Purpose**
- **`app/`** - All Android code
- **`docs/`** - All documentation  
- **`.github/`** - All GitHub automation

### **📚 Clear Documentation Structure**
- **Main README** - Project overview and quick start
- **SETUP.md** - Detailed development setup
- **docs/ci-cd/** - All CI/CD documentation in one place

### **🤖 Automated Workflows**
- **`quality-check.yml`** - Prevents broken code from merging
- **Templates** - Consistent issue reporting and PRs

---

## 🚀 **For Developers**

### **Quick Navigation:**
- 🏠 **Start here:** [README.md](../README.md)
- 🛠️ **Setup:** [SETUP.md](../SETUP.md)  
- 🔄 **CI/CD:** [docs/ci-cd/README.md](./ci-cd/README.md)

### **Common Tasks:**
- **Add new feature:** Follow the `app/src/main/java/com/wisme/nova/` structure
- **Update CI:** Edit `.github/workflows/quality-check.yml`
- **Document changes:** Update relevant files in `docs/`

---

## 🎯 **Benefits of This Structure**

✅ **Easy to find things** - Logical grouping by purpose  
✅ **Scalable** - Add new docs/workflows easily  
✅ **Clean root directory** - No scattered config files  
✅ **Self-documenting** - Clear folder names and structure  
✅ **Team-friendly** - New developers can navigate easily  

---

## 📋 **Next Steps**

1. **Explore the app structure** in `app/src/main/java/com/wisme/nova/`
2. **Read the setup guide** in `SETUP.md`
3. **Understand CI/CD** in `docs/ci-cd/README.md`
4. **Create your first feature** following the architecture patterns

**Happy coding!** 🚀