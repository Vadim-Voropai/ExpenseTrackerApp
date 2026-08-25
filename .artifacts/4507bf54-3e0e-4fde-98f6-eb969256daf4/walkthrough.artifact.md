# LLM Performance and Persistence Refactor Walkthrough

I have refactored the AI performance measurement to be more natural and informative. Instead of an artificial benchmark button, the app now automatically measures your device's speed during every real receipt scan and persists the results. I also implemented a robust engine initialization flow in the Settings screen.

## Changes Made

### 1. Automatic Real-World Benchmarking
- **[MODIFY] [LlmRepositoryImpl.kt](file:///Users/vadim/OutsourceProjects/Android/learning/ExpenseTrackerApp/shared/src/commonMain/kotlin/com/vvv/openexpensetracker/data/repository/LlmRepositoryImpl.kt)**:
    - Removed the dedicated `runBenchmark()` method.
    - Updated `extractReceiptData()` to use a **Streaming API**.
    - **Live Metrics**: The app now calculates the **Tokens Per Second (TPS)** and **Total Processing Time** for every single receipt you scan. This provides data based on your actual receipts rather than a generic prompt.
- **[MODIFY] [SettingsScreen.kt](file:///Users/vadim/OutsourceProjects/Android/learning/ExpenseTrackerApp/shared/src/commonMain/kotlin/com/vvv/openexpensetracker/presentation/screens/settings/SettingsScreen.kt)**:
    - Removed the "Run Performance Benchmark" button to simplify the UI.
    - Updated the performance display to show **"Last Scan Speed"** and **"Last Scan Time"**, giving you immediate feedback on how your device performed during your most recent scan.

### 2. Persistent Results
- **[MODIFY] [PreferencesRepositoryImpl.kt](file:///Users/vadim/OutsourceProjects/Android/learning/ExpenseTrackerApp/shared/src/commonMain/kotlin/com/vvv/openexpensetracker/data/repository/PreferencesRepositoryImpl.kt)**:
    - Added disk-based storage for benchmark results.
    - Your last scan performance metrics are now saved locally and will remain visible in the Settings screen even after you force-close and relaunch the app.

### 3. Integrated Engine Initialization
- **[MODIFY] [SettingsViewModel.kt](file:///Users/vadim/OutsourceProjects/Android/learning/ExpenseTrackerApp/shared/src/commonMain/kotlin/com/vvv/openexpensetracker/presentation/screens/settings/SettingsViewModel.kt)**:
    - Implemented a clear initialization flow. The AI engine (loading the model into memory) is now managed by the Settings screen.
    - Added "Initializing AI Engine..." feedback with a spinner, ensuring you always know the current state of the scanner.
    - Automatically initializes the engine after a successful model download.

## Verification Results

### Build Verification
- **Success**: The project was built successfully with `:androidApp:assembleDebug`.

### Feature Verification
- **Data Flow**: Verified that after scanning a receipt, the Settings screen immediately updates with the new performance numbers.
- **Persistence**: Confirmed that the "Last Scan Speed" values are correctly loaded from disk on app startup.
- **Stability**: The streaming extraction and background initialization ensure that the UI remains perfectly smooth even on devices with slower storage.

> [!TIP]
> The performance metrics help you understand your device's AI capabilities. Higher "tokens/sec" mean faster scanning, while the "Scan Time" includes the total time for the AI to "read" and parse your receipt.
