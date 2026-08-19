# Cache Google Drive Folder and File IDs

This plan implements local caching of Google Drive folder and file IDs to reduce API calls and improve synchronization performance.

## Proposed Changes

### [Core]

#### [MODIFY] [Constants.kt](file:///Users/vadim/OutsourceProjects/Android/learning/ExpenseTrackerApp/shared/src/commonMain/kotlin/com/vvv/openexpensetracker/core/Constants.kt)
- Add `KEY_FOLDER_ID = "google_drive_folder_id"`
- Add `KEY_FILE_ID = "google_drive_file_id"`

### [Remote Data Source]

#### [MODIFY] [GoogleDriveApi.kt](file:///Users/vadim/OutsourceProjects/Android/learning/ExpenseTrackerApp/shared/src/commonMain/kotlin/com/vvv/openexpensetracker/data/source/remote/GoogleDriveApi.kt)
- Make `getAppFolder(createIfMissing: Boolean)` public so the repository can manage it.
- Update `findExpensesFile(folderId: String? = null)` to use the provided `folderId` if available, bypassing folder discovery.
- Update `createExpensesFile(folderId: String)` to require a `folderId`, ensuring the repository has already resolved it.

### [Data Layer]

#### [MODIFY] [GoogleDriveRepositoryImpl.kt](file:///Users/vadim/OutsourceProjects/Android/learning/ExpenseTrackerApp/shared/src/commonMain/kotlin/com/vvv/openexpensetracker/data/repository/GoogleDriveRepositoryImpl.kt)
- Inject `LocalStorage`.
- **`findExpensesFile()`**:
    1. Check `LocalStorage` for `KEY_FILE_ID`. If found, return it.
    2. If not, check `LocalStorage` for `KEY_FOLDER_ID`.
    3. Call `api.findExpensesFile(folderId)`.
    4. If a file is found, save its ID to `LocalStorage`.
    5. (Optional) If the folder was discovered during this call, we might need a way to capture it. I'll update `findExpensesFile` to return both or just ensure `getAppFolder` is called first if needed.
- **`createExpensesFile()`**:
    1. Get `folderId` (from cache or `api.getAppFolder(true)`).
    2. Call `api.createExpensesFile(folderId)`.
    3. Save both IDs to `LocalStorage`.
- **Error Handling**: Catch `ClientRequestException` with `404 Not Found` in `downloadExpensesFile` and `updateExpensesFile`. If this happens, it means the cached ID is dead. Clear the cache and throw an exception to trigger a retry/re-discovery.

### [Domain Layer]

#### [MODIFY] [GoogleDriveRepository.kt](file:///Users/vadim/OutsourceProjects/Android/learning/ExpenseTrackerApp/shared/src/commonMain/kotlin/com/vvv/openexpensetracker/domain/repository/GoogleDriveRepository.kt)
- Add `fun clearCache()`.

## Verification Plan

### Automated Tests
- Verify that the project builds.
- I will check the logs to ensure that after the first sync, subsequent syncs do not call the "list files" or "search folder" endpoints if the IDs are cached.

### Manual Verification
1. Perform a sync. Verify in logs that folder/file discovery happens.
2. Perform another sync. Verify that it uses the cached IDs directly.
3. Manually delete the file on Google Drive. Perform a sync. Verify that the app handles the 404, clears the cache, and re-creates/re-discovers the file.
