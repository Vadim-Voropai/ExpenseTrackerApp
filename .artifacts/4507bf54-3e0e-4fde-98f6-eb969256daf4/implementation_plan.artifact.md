# Refactor Sync Feedback: Snackbar and Lottie Animation

This plan replaces the intrusive sync alert dialogs with non-intrusive Snackbar messages for errors and adds a Lottie animation to indicate sync progress.

## User Review Required

> [!IMPORTANT]
> - **Lottie Dependency**: I will add `io.github.alexzhirkevich:compottie:2.2.4` to support Lottie in Compose Multiplatform.
> - **Animation File**: Since I cannot create a complex Lottie JSON file from scratch, I will set up the code to use a file named `sync_animation.json` in the `composeResources/files` directory. **You will need to provide this Lottie JSON file.**
> - **Feedback Logic**:
>     - **Success**: No visual feedback other than the animation stopping.
>     - **Error**: A Snackbar will appear at the bottom of the screen with the error message.

## Proposed Changes

### [Infrastructure]

#### [MODIFY] [libs.versions.toml](file:///Users/vadim/OutsourceProjects/Android/learning/ExpenseTrackerApp/gradle/libs.versions.toml)
- Add `compottie = "2.2.4"`.
- Add `compottie-library = { module = "io.github.alexzhirkevich:compottie", version.ref = "compottie" }`.

#### [MODIFY] [shared/build.gradle.kts](file:///Users/vadim/OutsourceProjects/Android/learning/ExpenseTrackerApp/shared/build.gradle.kts)
- Add `libs.compottie.library` to `commonMain` dependencies.

### [Presentation Layer - ViewModels]

#### [MODIFY] [SettingsViewModel.kt](file:///Users/vadim/OutsourceProjects/Android/learning/ExpenseTrackerApp/shared/src/commonMain/kotlin/com/vvv/openexpensetracker/presentation/screens/settings/SettingsViewModel.kt)
- Update `syncNow()`: Only set `_syncMessage` on failure.

#### [MODIFY] [ExpenseListViewModel.kt](file:///Users/vadim/OutsourceProjects/Android/learning/ExpenseTrackerApp/shared/src/commonMain/kotlin/com/vvv/openexpensetracker/presentation/screens/expenses/ExpenseListViewModel.kt)
- Update `syncExpenses()`: Only set `_syncMessage` on failure.

### [Presentation Layer - Screens]

#### [MODIFY] [SettingsScreen.kt](file:///Users/vadim/OutsourceProjects/Android/learning/ExpenseTrackerApp/shared/src/commonMain/kotlin/com/vvv/openexpensetracker/presentation/screens/settings/SettingsScreen.kt)
- Remove `AlertDialog` logic.
- Add `SnackbarHost` and `SnackbarHostState` to the `Scaffold`.
- Add `LaunchedEffect` to trigger `snackbarHostState.showSnackbar()` on errors.
- Replace the `Refresh` icon/progress indicator with a `LottieAnimation` component when `uiState.isSyncing` is true.

#### [MODIFY] [ExpenseListScreen.kt](file:///Users/vadim/OutsourceProjects/Android/learning/ExpenseTrackerApp/shared/src/commonMain/kotlin/com/vvv/openexpensetracker/presentation/screens/expenses/ExpenseListScreen.kt)
- Remove `AlertDialog` logic.
- Add `SnackbarHost` and `SnackbarHostState`.
- Add `LaunchedEffect` for error Snackbars.
- Replace the `Refresh` icon with a `LottieAnimation` component when `uiState.isRefreshing` is true.

## Verification Plan

### Manual Verification
1.  **Trigger Sync**: Tap the sync button.
2.  **Verify Progress**: Ensure the Lottie animation plays (once the JSON file is provided).
3.  **Verify Success**: Ensure NO dialog or snackbar appears on success.
4.  **Verify Error**: Simulate an error and verify the Snackbar appears.
