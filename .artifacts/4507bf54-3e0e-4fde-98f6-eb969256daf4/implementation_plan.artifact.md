# Align Save Action with Material 3 Guidelines

This plan improves the "Add/Edit Expense" user experience by moving the primary "Save" action to the `TopAppBar`, following standard Material 3 patterns for data-entry screens. This also fixes the issue of the button being positioned "too low" on the screen.

## Proposed Changes

### [Resources]

#### [MODIFY] [strings.xml](file:///Users/vadim/OutsourceProjects/Android/learning/ExpenseTrackerApp/shared/src/commonMain/composeResources/values/strings.xml)
- Re-add a generic `action_save` string: "Save".

### [Presentation Layer - UI]

#### [MODIFY] [AddExpenseScreen.kt](file:///Users/vadim/OutsourceProjects/Android/learning/ExpenseTrackerApp/shared/src/commonMain/kotlin/com/vvv/openexpensetracker/presentation/screens/add_expense/AddExpenseScreen.kt)
- **TopAppBar**: Add an `actions` block containing a `TextButton` that triggers `AddExpenseIntent.SaveExpense`. This makes the action always visible and easily accessible.
- **Main Layout**:
    - Remove the large `Button` from the bottom of the screen.
    - Add `navigationBarsPadding()` to the root container to ensure the UI respects system bars.
    - Wrap the content in a `verticalScroll` to ensure usability on smaller screens or when the keyboard is open.
    - Adjust the `Column` arrangement from `SpaceBetween` to a standard top-aligned flow with spacing.

## Verification Plan

### Manual Verification
1.  **UI Check**: Open the "Add Expense" screen. Verify that the "Save" button is now in the top right corner of the app bar.
2.  **Functionality**: Input data and tap the "Save" button in the app bar. Ensure the expense is saved and you are navigated back to the list.
3.  **Keyboard Handling**: Open the keyboard and verify that you can still scroll through the category grid if needed.
4.  **Edge-to-Edge**: Verify that the bottom of the screen (the category grid) has proper padding and is not clipped by the system navigation bar.
