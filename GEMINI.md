# Repository Guidelines & Rules: NeXT

## Room Persistence & KSP (Kotlin Symbol Processing)

1. **Room Compiler Dependency**:
   - Always configure the Room annotation processor using KSP (`ksp(libs.androidx.room.compiler)`) in Kotlin projects.
   - **Never** use `annotationProcessor(libs.androidx.room.compiler)` for Kotlin entities/DAOs. `annotationProcessor` targets only Java sources in AGP and leaves Kotlin Room entities unprocessed, causing runtime `java.lang.RuntimeException: AppDatabase_Impl does not exist` crashes upon `AppDatabase.getInstance(context)`.

2. **DAO Method Return Types**:
   - Explicitly specify integer return types (`: Int`) for `@Query` suspend update/delete methods in Room DAOs (e.g., `suspend fun deleteTask(id: String): Int`). Leaving return types ambiguous can cause JVM signature mismatches during Room KSP code generation.

3. **Gradle Properties for KSP & Room**:
   - Include `android.disallowKotlinSourceSets=false` in `gradle.properties` to support generated KSP source sets in AGP 9+.
   - Set `ksp.useK2=false` in `gradle.properties` for stable Room symbol processing with Kotlin 2.x.
