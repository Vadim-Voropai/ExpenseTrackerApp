# Simplify Expense Display Title Walkthrough

I have moved the logic for the expense display title directly into the `Expense` domain model as a calculated property. This simplifies the architecture by removing the need for intermediate UI models or complex extension mappings.

## Changes Made

### 1. Domain Model Enhancement
- **[MODIFY] [Expense.kt](file:///Users/vadim/OutsourceProjects/Android/learning/ExpenseTrackerApp/shared/src/commonMain/kotlin/com/vvv/openexpensetracker/domain/model/Expense.kt)**:
    - Added a calculated `val displayTitle: String` property to the `Expense` data class.
    - Implemented the logic: `description.ifEmpty { category }`. This ensures that every expense has a meaningful title in the list without requiring extra processing in the ViewModel or UI layer.

### 2. UI Simplification
- **[MODIFY] [ExpenseListScreen.kt](file:///Users/vadim/OutsourceProjects/Android/learning/ExpenseTrackerApp/shared/src/commonMain/kotlin/com/vvv/openexpensetracker/presentation/screens/expenses/ExpenseListScreen.kt)**:
    - Updated the `ExpenseItemRow` to use `expense.displayTitle` directly.
    - Removed the dependence on `UiText` and its `.asString()` resolver, making the Composable code cleaner and more standard.

### 3. Code Cleanup
- **[DELETE] `ExpenseExtensions.kt`**: Removed the extension property that was previously used for this logic.
- **[DELETE] `ExpenseUiModel.kt`**: Removed the redundant UI-specific model.
- **[DELETE] `UiText.kt`**: Removed the utility class as it is no longer required for this specific display logic.

## Verification Results

### Build Verification
- **Success**: The project was built successfully with `:androidApp:assembleDebug`.

### UI Behavior
- **Description Present**: Items show their user-entered description.
- **Empty Description**: Items automatically show their category name (e.g., "Food", "Shopping") as the primary title.
- **Performance**: Calculated properties in data classes are highly efficient and reduce the overhead of mapping lists in the ViewModel.
