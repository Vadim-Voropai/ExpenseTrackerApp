# Code Cleanup: Unused Imports, Strings, and Values

This plan focuses on cleaning up the codebase by removing unused imports, redundant string resources, and resolving minor linting warnings to improve code quality and maintainability.

## Proposed Changes

### [Resources]

#### [MODIFY] [strings.xml](file:///Users/vadim/OutsourceProjects/Android/learning/ExpenseTrackerApp/shared/src/commonMain/composeResources/values/strings.xml)
- Remove unused strings: `ok`, `save`.

### [UI Screens]

#### [MODIFY] [ExpenseListScreen.kt](file:///Users/vadim/OutsourceProjects/Android/learning/ExpenseTrackerApp/shared/src/commonMain/kotlin/com/vvv/openexpensetracker/presentation/screens/expenses/ExpenseListScreen.kt)
- Remove unused imports (e.g., `AnimatedVisibility`, `tween`, `fadeOut`, `shrinkVertically` if not used).
- Fix `dayOfMonth` deprecation warning by using `day`.
- Remove unused parameter `e` in catch blocks.

#### [MODIFY] [SettingsScreen.kt](file:///Users/vadim/OutsourceProjects/Android/learning/ExpenseTrackerApp/shared/src/commonMain/kotlin/com/vvv/openexpensetracker/presentation/screens/settings/SettingsScreen.kt)
- Fix `outlinedButtonBorder` deprecation warning.
- Fix `Instant` deprecation warning by using `kotlin.time.Instant`.
- Remove unused parameter `e` in catch blocks.
- Add missing trailing commas for consistency.

#### [MODIFY] [AddExpenseScreen.kt](file:///Users/vadim/OutsourceProjects/Android/learning/ExpenseTrackerApp/shared/src/commonMain/kotlin/com/vvv/openexpensetracker/presentation/screens/add_expense/AddExpenseScreen.kt)
- Remove unused imports.

### [Repositories]

#### [MODIFY] [GoogleAuthRepositoryImpl.kt](file:///Users/vadim/OutsourceProjects/Android/learning/ExpenseTrackerApp/shared/src/androidMain/kotlin/com/vvv/openexpensetracker/domain/repository/GoogleAuthRepositoryImpl.kt)
- Add missing trailing commas.
- Add clarifying parentheses for complex conditions.
- Simplify `if (newToken != null)` where applicable.

#### [MODIFY] [GoogleDriveApi.kt](file:///Users/vadim/OutsourceProjects/Android/learning/ExpenseTrackerApp/shared/src/commonMain/kotlin/com/vvv/openexpensetracker/data/source/remote/GoogleDriveApi.kt)
- Remove redundant null checks (`if (fileId != null)` when it's always true).
- Add clarifying parentheses.

## Verification Plan

### Automated Tests
- Run `:androidApp:assembleDebug` to ensure the project still builds correctly.
- Run `analyze_file` on modified files to verify that the identified warnings have been resolved.

### Manual Verification
- Briefly verify that the UI still behaves as expected and no strings are missing.
