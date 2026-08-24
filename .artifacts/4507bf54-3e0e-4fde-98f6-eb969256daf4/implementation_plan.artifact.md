# Fix Hash Verification and Model Integrity

This plan addresses the issue where the model download completes but fails the hash verification ("skips equity"). It adds robust logging and error handling to identify the exact cause of the mismatch.

## Proposed Changes

### [Data Layer - LLM]

#### [MODIFY] [LlmRepositoryImpl.kt](file:///Users/vadim/OutsourceProjects/Android/learning/ExpenseTrackerApp/shared/src/commonMain/kotlin/com/vvv/openexpensetracker/data/repository/LlmRepositoryImpl.kt)
- **Enhanced Verification**:
    - Wrap the entire download and hashing logic in a `try-catch` block.
    - Log the calculated hash to the console (or include it in the exception) to help debug if the file is slightly different from expected.
    - Ensure `HashingSink` is correctly finalized before the value is read.
- **Improved Streaming**:
    - Add a check to ensure `calculatedHash` is not empty before verification.
    - If an error occurs during the stream, ensure the partial file is deleted.
- **Detailed Error Reporting**: Throw an exception that includes both the expected and actual hash for easier troubleshooting.

## Verification Plan

### Manual Verification
1.  **Monitor Logs**: Run the app and monitor Logcat for "Calculated Hash: [value]".
2.  **Verify Integrity**: If the hashes still don't match, compare the logged hash with the one on Hugging Face to see if we are downloading a different version (e.g., LFS pointer vs actual file).
3.  **Success Path**: Verify that upon a successful match, the model correctly initializes and scanning becomes available.
