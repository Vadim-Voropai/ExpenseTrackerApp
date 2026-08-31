# Walkthrough - Fixing `displayTitle` and UI Cleanup

I have resolved the compilation issue caused by the `displayTitle` property in the `Expense` domain model and performed a general cleanup of the category mapping logic.

## Changes Made

### Domain Layer
- **[Expense.kt](file:///Users/vadim/OutsourceProjects/Android/learning/ExpenseTrackerApp/shared/src/commonMain/kotlin/com/vvv/openexpensetracker/domain/model/Expense.kt)**: Removed `displayTitle` property. Domain models should remain pure and not depend on UI resources.
- **[Category.kt](file:///Users/vadim/OutsourceProjects/Android/learning/ExpenseTrackerApp/shared/src/commonMain/kotlin/com/vvv/openexpensetracker/domain/model/Category.kt)**: Ensured category constants and resource mapping are centralized.

### Presentation Layer
- **[ExpenseListScreen.kt](file:///Users/vadim/OutsourceProjects/Android/learning/ExpenseTrackerApp/shared/src/commonMain/kotlin/com/vvv/openexpensetracker/presentation/screens/expenses/ExpenseListScreen.kt)**:
    - Updated `ExpenseItemRow` to handle localized category names and descriptions directly.
    - Improved layout to separate category name and description for better readability.
    - Set `maxLines` and `overflow` for long descriptions.
- **[AddExpenseScreen.kt](file:///Users/vadim/OutsourceProjects/Android/learning/ExpenseTrackerApp/shared/src/commonMain/kotlin/com/vvv/openexpensetracker/presentation/screens/add_expense/AddExpenseScreen.kt)** & **[StatsScreen.kt](file:///Users/vadim/OutsourceProjects/Android/learning/ExpenseTrackerApp/shared/src/commonMain/kotlin/com/vvv/openexpensetracker/presentation/screens/stats/StatsScreen.kt)**: Updated imports to use the centralized `getCategoryNameResource`.

### Cleanup
- **[CategoryMapper.kt](file:///Users/vadim/OutsourceProjects/Android/learning/ExpenseTrackerApp/shared/src/commonMain/kotlin/com/vvv/openexpensetracker/presentation/theme/CategoryMapper.kt)**: Deleted this redundant file as its functionality is now handled within the `Category` object and direct resource calls.

## Verification Results

### Automated Tests
- The project builds successfully (`gradle :shared:assembleDebug` passed).

### Manual Verification
- Verified that the expense list correctly displays:
    - Bolded category name on the first line.
    - Description (if available) on the second line with secondary color.
    - Correct localized strings for all categories.

## Git Status
- Changes committed and pushed to `main` branch.
