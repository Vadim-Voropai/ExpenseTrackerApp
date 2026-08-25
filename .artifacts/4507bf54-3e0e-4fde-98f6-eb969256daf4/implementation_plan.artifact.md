# Move Performance Calculation to Receipt Extraction

This plan refactors the LLM performance measurement to happen automatically during receipt extraction. This removes the need for a separate manual benchmark and provides real-world performance data to the user.

## Proposed Changes

### [Domain Layer]

#### [MODIFY] [LlmRepository.kt](file:///Users/vadim/OutsourceProjects/Android/learning/ExpenseTrackerApp/shared/src/commonMain/kotlin/com/vvv/openexpensetracker/domain/repository/LlmRepository.kt)
- Remove `suspend fun runBenchmark(): LlmBenchmarkResult`.

#### [DELETE] [RunLlmBenchmarkUseCase](file:///Users/vadim/OutsourceProjects/Android/learning/ExpenseTrackerApp/shared/src/commonMain/kotlin/com/vvv/openexpensetracker/domain/usecase/LlmUseCases.kt)
- Remove `RunLlmBenchmarkUseCase` and its `initialize` method. Initialization will be handled by `AnalyzeReceiptLlmUseCase`.

### [Data Layer]

#### [MODIFY] [LlmRepositoryImpl.kt](file:///Users/vadim/OutsourceProjects/Android/learning/ExpenseTrackerApp/shared/src/commonMain/kotlin/com/vvv/openexpensetracker/data/repository/LlmRepositoryImpl.kt)
- **Remove `runBenchmark()`**: Delete the dedicated benchmarking method.
- **Update `extractReceiptData()`**:
    - Switch implementation from `LlamaBridge.generate()` to **`LlamaBridge.generateStream()`**.
    - Capture the **start time** (when prompt is sent).
    - Capture the **first token time** (when decoding starts).
    - Capture the **end time** (when generation completes).
    - Count the total number of tokens (deltas) generated.
    - Calculate **TPS** (Tokens Per Second) and **Total Duration**.
    - Automatically call `preferencesRepository.setLlmBenchmarkResult()` with the results of every successful scan.

### [Presentation Layer]

#### [MODIFY] [SettingsIntent.kt](file:///Users/vadim/OutsourceProjects/Android/learning/ExpenseTrackerApp/shared/src/commonMain/kotlin/com/vvv/openexpensetracker/presentation/screens/settings/SettingsIntent.kt)
- Remove `RunBenchmark`.

#### [MODIFY] [SettingsViewModel.kt](file:///Users/vadim/OutsourceProjects/Android/learning/ExpenseTrackerApp/shared/src/commonMain/kotlin/com/vvv/openexpensetracker/presentation/screens/settings/SettingsViewModel.kt)
- Remove benchmark-related states and logic (`isBenchmarking`, `runBenchmark()`, etc.).
- Update `startLlmDownload()` to only initialize the engine after download, without triggering a manual benchmark.

#### [MODIFY] [SettingsScreen.kt](file:///Users/vadim/OutsourceProjects/Android/learning/ExpenseTrackerApp/shared/src/commonMain/kotlin/com/vvv/openexpensetracker/presentation/screens/settings/SettingsScreen.kt)
- Update the "Performance" section:
    - Remove the "Run Performance Benchmark" button.
    - Rename labels to "Last Scan Speed" and "Last Scan Time".
    - Show the results only if a scan has been performed (stored in preferences).

### [Dependency Injection]

#### [MODIFY] [Koin.kt](file:///Users/vadim/OutsourceProjects/Android/learning/ExpenseTrackerApp/shared/src/commonMain/kotlin/com/vvv/openexpensetracker/di/Koin.kt)
- Remove `RunLlmBenchmarkUseCase` registration.

## Verification Plan

### Manual Verification
1.  **Benchmarking**: Scan a receipt in the Add Expense screen.
2.  **Verify Results**: Go to Settings and verify that the "Last Scan Speed" and "Last Scan Time" have been updated with the metrics from that specific scan.
3.  **Persistence**: Relaunch the app and verify the metrics from the last scan are still visible.
4.  **UI Cleanup**: Verify that the "Run Benchmark" button is no longer present in Settings.
