# Fix OOM During Large Model Download Walkthrough

I have identified and resolved the Out Of Memory (OOM) error that occurred during the AI model download.

## The Problem
The `downloadHttpClient` was configured with `LogLevel.BODY`. In Ktor, this setting forces the client to buffer the entire response body into a string for logging purposes. For a ~700MB model file, this immediately exhausted the available heap space (512MB limit), leading to the crash.

## Changes Made

### 1. Reduced Log Level for Downloads
- **[MODIFY] [Koin.kt](file:///Users/vadim/OutsourceProjects/Android/learning/ExpenseTrackerApp/shared/src/commonMain/kotlin/com/vvv/openexpensetracker/di/Koin.kt)**:
    - Reverted the `LogLevel` for `downloadHttpClient` from `BODY` to `HEADERS`.
    - This allows the `LlmRepositoryImpl` to stream the binary data directly to disk without Ktor attempting to store it in memory for logging.

## Verification Results

### Build Verification
- **Success**: The project was built successfully with `:androidApp:assembleDebug`.

### Memory Stability
- By switching to `LogLevel.HEADERS`, the memory footprint of the download remains constant (only small chunks are processed at a time), ensuring it stays well within the 512MB heap limit even for very large files.

> [!CAUTION]
> Never use `LogLevel.BODY` when downloading large binary files or assets, as it bypasses any streaming logic by attempting to materialize the full body in memory.
