# Disable Noisy Logs and Support Auto-Resume Scanning

This plan silences repetitive OCR logs and ensures that scanning automatically resumes if AI data extraction fails. We will achieve this by dynamically managing the camera's analyzer based on the app's processing state.

## Proposed Changes

### [Presentation Layer - Components]

#### [MODIFY] [TextRecognitionCamera.android.kt](file:///Users/vadim/OutsourceProjects/Android/learning/ExpenseTrackerApp/shared/src/androidMain/kotlin/com/vvv/openexpensetracker/presentation/components/TextRecognitionCamera.android.kt)
- **Dynamic Analyzer Lifecycle**:
    - Move the `controller.setImageAnalysisAnalyzer` logic into a `LaunchedEffect(isPaused)`.
    - **When `isPaused` is true**: Call `controller.clearImageAnalysisAnalyzer()`. This stops the VisionKit pipeline, saving battery and silencing "OCR process succeeded" logs.
    - **When `isPaused` is false**: Re-attach the `MlKitAnalyzer`. This ensures that if the AI finishes (successfully or with an error), the camera immediately starts looking for text again.
- **Clean Logging**: Remove the `Log.e("TAG", ...)` debug statement.

### [Presentation Layer - Scan Receipt]

#### [MODIFY] [ScanReceiptViewModel.kt](file:///Users/vadim/OutsourceProjects/Android/learning/ExpenseTrackerApp/shared/src/commonMain/kotlin/com/vvv/openexpensetracker/presentation/screens/scan_receipt/ScanReceiptViewModel.kt)
- Ensure the `isProcessing = false` update happens in a `finally` block or at all exit points of the extraction coroutine to guarantee the camera resumes after a failure.

## Verification Plan

### Manual Verification
1.  **Silence Verification**: Point the camera at a receipt. Once processing starts, verify that "OCR process succeeded via visionkit pipeline" logs stop in the console.
2.  **Failure Recovery**:
    - Trigger a scan on "nonsense" text that passes the local check but might fail LLM extraction.
    - Verify that once the spinner disappears, the camera immediately resumes highlighting text.
3.  **Success Path**: Verify that successful extraction still navigates back as expected.
