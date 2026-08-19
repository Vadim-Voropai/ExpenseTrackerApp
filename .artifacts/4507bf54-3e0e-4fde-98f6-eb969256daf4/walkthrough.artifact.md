# Google Drive ID Caching Walkthrough

I have implemented a caching mechanism for Google Drive folder and file IDs. This significantly improves synchronization speed by bypassing expensive "search by name" operations on subsequent syncs.

## Changes Made

### 1. Caching Infrastructure
- **[MODIFY] [Constants.kt](file:///Users/vadim/OutsourceProjects/Android/learning/ExpenseTrackerApp/shared/src/commonMain/kotlin/com/vvv/openexpensetracker/core/Constants.kt)**: Added `KEY_FOLDER_ID` and `KEY_FILE_ID` for local persistence.
- **[MODIFY] [GoogleDriveRepositoryImpl.kt](file:///Users/vadim/OutsourceProjects/Android/learning/ExpenseTrackerApp/shared/src/commonMain/kotlin/com/vvv/openexpensetracker/data/repository/GoogleDriveRepositoryImpl.kt)**:
    - Integrated `LocalStorage` to save and retrieve cached IDs.
    - **`findExpensesFile`**: Now checks the cache first. If a cache miss occurs, it performs the full discovery and then populates the cache.
    - **`createExpensesFile`**: Automatically caches the IDs of the newly created folder and file.

### 2. Resilience and Error Recovery
- **Automatic Cache Invalidation**: I added 404 (Not Found) error handling to `downloadExpensesFile` and `updateExpensesFile`.
- If a cached ID is no longer valid (e.g., the user deleted the file directly from the Drive web UI), the app will:
    1. Catch the 404 error.
    2. Call `clearCache()`.
    3. On the next sync attempt, it will perform a fresh re-discovery and re-creation if necessary.

### 3. API and Dependency Updates
- **[MODIFY] [GoogleDriveApi.kt](file:///Users/vadim/OutsourceProjects/Android/learning/ExpenseTrackerApp/shared/src/commonMain/kotlin/com/vvv/openexpensetracker/data/source/remote/GoogleDriveApi.kt)**: Refactored to allow the repository to provide specific IDs, making the API calls more direct and efficient.
- **[MODIFY] [Koin.kt](file:///Users/vadim/OutsourceProjects/Android/learning/ExpenseTrackerApp/shared/src/commonMain/kotlin/com/vvv/openexpensetracker/di/Koin.kt)**: Updated dependency injection to provide `LocalStorage` to the `GoogleDriveRepository`.

## Verification Results

### Build Verification
- **Success**: The project was built successfully with `:androidApp:assembleDebug`.

### Performance Gains
> [!TIP]
> Subsequent sync operations will now skip the folder search and file list queries, directly accessing the `expenses.json` file by its unique ID. This reduces the number of API round-trips from 3-4 down to just 1 per sync in the steady state.

### Next Steps
1. **Sync**: Perform a sync to populate the cache.
2. **Verification**: Check your local logs (if enabled) to see that the second sync is much faster and bypasses the search logic.
