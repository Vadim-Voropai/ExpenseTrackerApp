# Simplify Expense Display Logic (Remove ExpenseUiModel)

This plan simplifies the UI logic by removing the `ExpenseUiModel` and instead using an extension property on the domain `Expense` model to calculate the display title. This reduces boilerplate while maintaining a clean separation of concerns.

## Proposed Changes

### [Presentation Layer - Common]

#### [MODIFY] [UiText.kt](file:///Users/vadim/OutsourceProjects/Android/learning/ExpenseTrackerApp/shared/src/commonMain/kotlin/com/vvv/openexpensetracker/presentation/util/UiText.kt)
- No changes needed, keeping the existing `UiText` utility.

### [Presentation Layer - Expense List]

#### [DELETE] [ExpenseUiModel.kt](file:///Users/vadim/OutsourceProjects/Android/learning/ExpenseTrackerApp/shared/src/commonMain/kotlin/com/vvv/openexpensetracker/presentation/screens/expenses/ExpenseUiModel.kt)
- Remove this file as it's no longer necessary.

#### [NEW] [ExpenseExtensions.kt](file:///Users/vadim/OutsourceProjects/Android/learning/ExpenseTrackerApp/shared/src/commonMain/kotlin/com/vvv/openexpensetracker/presentation/screens/expenses/ExpenseExtensions.kt)
- Implement an extension property `val Expense.displayTitle: UiText`.
- Move the logic: `if (description.isEmpty()) UiText.StringRes(...) else UiText.DynamicString(description)`.

#### [MODIFY] [ExpenseListViewModel.kt](file:///Users/vadim/OutsourceProjects/Android/learning/ExpenseTrackerApp/shared/src/commonMain/kotlin/com/vvv/openexpensetracker/presentation/screens/expenses/ExpenseListViewModel.kt)
- Revert `ExpenseListUIState` to hold `List<Expense>` instead of `ExpenseUiModel`.
- Remove the mapping logic from the `combine` block.

#### [MODIFY] [ExpenseListScreen.kt](file:///Users/vadim/OutsourceProjects/Android/learning/ExpenseTrackerApp/shared/src/commonMain/kotlin/com/vvv/openexpensetracker/presentation/screens/expenses/ExpenseListScreen.kt)
- Revert `ExpenseItemRow` and list items to use `Expense` directly.
- Call the extension property `expense.displayTitle.asString()`.

## Verification Plan

### Automated Tests
- Run `:androidApp:assembleDebug` to ensure compilation is successful.

### Manual Verification
- Verify that the expense list still correctly displays the description or the category name fallback.
- Ensure that clicking/swiping items still works as expected with the simplified model.
