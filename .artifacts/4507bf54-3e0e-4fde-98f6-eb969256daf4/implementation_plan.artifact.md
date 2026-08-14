# Refactor GoogleDriveApi to GoogleDriveRepository

This plan aims to improve the project's architecture by introducing a `GoogleDriveRepository` to abstract the `GoogleDriveApi`. This follows the Clean Architecture pattern, ensuring that the domain layer (and other repositories) interact with interfaces rather than concrete API implementations.

## Proposed Changes

### [Domain Layer]

#### [NEW] [GoogleDriveRepository.kt](file:///Users/vadim/OutsourceProjects/Android/learning/ExpenseTrackerApp/shared/src/commonMain/kotlin/com/vvv/openexpensetracker/domain/repository/GoogleDriveRepository.kt)
Define an interface for Google Drive operations:
- `suspend fun findExpensesFile(): String?`
- `suspend fun downloadExpensesFile(fileId: String): String?`
- `suspend fun createExpensesFile(): String?`
- `suspend fun updateExpensesFile(fileId: String, content: String): Boolean`

### [Data Layer]

#### [NEW] [GoogleDriveRepositoryImpl.kt](file:///Users/vadim/OutsourceProjects/Android/learning/ExpenseTrackerApp/shared/src/commonMain/kotlin/com/vvv/openexpensetracker/data/repository/GoogleDriveRepositoryImpl.kt)
Implement the `GoogleDriveRepository` interface using `GoogleDriveApi`.

#### [MODIFY] [ExpenseRepositoryImpl.kt](file:///Users/vadim/OutsourceProjects/Android/learning/ExpenseTrackerApp/shared/src/commonMain/kotlin/com/vvv/openexpensetracker/data/repository/ExpenseRepositoryImpl.kt)
- Update constructor to accept `GoogleDriveRepository` instead of `GoogleDriveApi`.
- Update all internal calls to use the repository.

### [Dependency Injection]

#### [MODIFY] [Koin.kt](file:///Users/vadim/OutsourceProjects/Android/learning/ExpenseTrackerApp/shared/src/commonMain/kotlin/com/vvv/openexpensetracker/di/Koin.kt)
- Provide `GoogleDriveRepository` by binding it to `GoogleDriveRepositoryImpl`.
- Keep `GoogleDriveApi` as a internal dependency (still a `single` but only used by the repository).

## Verification Plan

### Automated Tests
- I will verify that the project builds successfully.

### Manual Verification
1. Perform a manual sync in the app.
2. Verify that expenses are still correctly uploaded/downloaded from Google Drive.
