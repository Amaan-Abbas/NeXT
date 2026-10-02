---
name: room-ksp-setup
description: >-
  Troubleshoot and fix Room database code generation failures, AppDatabase_Impl missing launch crashes,
  and KSP Gradle plugin configuration in Kotlin Android projects.
---

# Room Database & KSP Setup Runbook

Use this skill when encountering `AppDatabase_Impl does not exist` crashes, Room compilation errors, or when configuring Room with KSP in Kotlin Android projects.

## Checklist & Resolution Steps

### 1. Version Catalog Setup (`gradle/libs.versions.toml`)
Ensure KSP plugin and compatible Room dependencies are declared:
```toml
[versions]
kotlin = "2.0.21"
ksp = "2.0.21-1.0.28"
room = "2.8.5"

[plugins]
ksp = { id = "com.google.devtools.ksp", version.ref = "ksp" }
```

### 2. Plugin Application in Gradle Scripts
- Root `build.gradle.kts`:
  ```kotlin
  plugins {
      alias(libs.plugins.ksp) apply false
  }
  ```
- Application `app/build.gradle.kts`:
  ```kotlin
  plugins {
      alias(libs.plugins.ksp)
  }

  dependencies {
      implementation(libs.androidx.room.runtime)
      implementation(libs.androidx.room.ktx)
      ksp(libs.androidx.room.compiler)
  }
  ```

### 3. DAO Method Signatures
Ensure suspend `@Query` delete/update operations explicitly state their return type (`: Int`):
```kotlin
@Query("DELETE FROM tasks WHERE id = :id")
suspend fun deleteTask(id: String): Int
```

### 4. Required Gradle Properties
In `gradle.properties`:
```properties
android.disallowKotlinSourceSets=false
ksp.useK2=false
```

### 5. Verification
Run clean build and assemble:
```pwsh
.\gradlew.bat :app:assembleDebug
```
Verify that `AppDatabase_Impl.kt` is generated under `app/build/generated/ksp/debug/kotlin/...`.
