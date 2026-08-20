# Move Hardcoded Strings to Resources

This plan aims to centralize all hardcoded UI strings into the `strings.xml` resource file to support future localization and improve maintainability using Compose Multiplatform Resources.

## Proposed Changes

### [Resources]

#### [MODIFY] [strings.xml](file:///Users/vadim/OutsourceProjects/Android/learning/ExpenseTrackerApp/shared/src/commonMain/composeResources/values/strings.xml)
- Clean up irrelevant strings (Artist, Dimensions, etc. if they are indeed not used).
- Add strings for all screens:
    - **General**: App Name, OK, Back, Save, Never, at.
    - **Home**: Tab labels (Expenses, Analytics, Settings), FAB content description (Add Expense).
    - **Expense List**: Title, Sync Now, Search placeholder, "All" filter, empty state messages, "Delete" action.
    - **Add/Edit Expense**: Titles, "Amount" label and placeholder, "Description" label and placeholder, "Date" label and "Set Today" button, "Select Category" label, "Save Expense" button.
    - **Settings**: Title, User Account content description, Guest Mode messages, Currency label, Backup & Sync section, "Sync with Google Drive", "Last synced" label, Sign Out, Sign In with Google, Version info.
    - **Analytics**: Title, empty state message, "Total Spent", "Category Breakdown".
    - **Categories**: Food, Transport, Utilities, Entertainment, Health, Shopping, Others.

### [UI Screens]

#### [MODIFY] [HomeScreen.kt](file:///Users/vadim/OutsourceProjects/Android/learning/ExpenseTrackerApp/shared/src/commonMain/kotlin/com/vvv/openexpensetracker/presentation/screens/home/HomeScreen.kt)
- Use `stringResource(Res.string.key)` for tab labels and FAB.

#### [MODIFY] [ExpenseListScreen.kt](file:///Users/vadim/OutsourceProjects/Android/learning/ExpenseTrackerApp/shared/src/commonMain/kotlin/com/vvv/openexpensetracker/presentation/screens/expenses/ExpenseListScreen.kt)
- Use `stringResource` for title, search, empty states, etc.
- Update `formatDate` if necessary (though it seems to use platform-agnostic date formatting).

#### [MODIFY] [AddExpenseScreen.kt](file:///Users/vadim/OutsourceProjects/Android/learning/ExpenseTrackerApp/shared/src/commonMain/kotlin/com/vvv/openexpensetracker/presentation/screens/add_expense/AddExpenseScreen.kt)
- Use `stringResource` for all labels, placeholders, and buttons.

#### [MODIFY] [SettingsScreen.kt](file:///Users/vadim/OutsourceProjects/Android/learning/ExpenseTrackerApp/shared/src/commonMain/kotlin/com/vvv/openexpensetracker/presentation/screens/settings/SettingsScreen.kt)
- Use `stringResource` for all settings labels, profile info, and sync details.

#### [MODIFY] [StatsScreen.kt](file:///Users/vadim/OutsourceProjects/Android/learning/ExpenseTrackerApp/shared/src/commonMain/kotlin/com/vvv/openexpensetracker/presentation/screens/stats/StatsScreen.kt)
- Use `stringResource` for titles and labels.

### [Domain Model]

#### [MODIFY] [Category.kt](file:///Users/vadim/OutsourceProjects/Android/learning/ExpenseTrackerApp/shared/src/commonMain/kotlin/com/vvv/openexpensetracker/domain/model/Category.kt)
- Keep constants for keys, but consider how to display them in the UI.
- I will add a helper function or mapping in the UI layer to translate these category keys using `stringResource`.

## Verification Plan

### Automated Tests
- Run `:androidApp:assembleDebug` to ensure all `stringResource` calls are valid and the `Res` class is re-generated correctly.

### Manual Verification
1.  Navigate through all screens (Home, List, Add/Edit, Stats, Settings).
2.  Verify that all text is still correctly displayed.
3.  Check both Signed-in and Guest modes in Settings to verify conditional strings.
