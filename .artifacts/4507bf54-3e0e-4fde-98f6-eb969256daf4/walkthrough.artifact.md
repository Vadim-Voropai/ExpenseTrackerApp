# Move Hardcoded Strings to Resources Walkthrough

I have moved all hardcoded UI strings to the centralized `strings.xml` resource file. This change ensures better maintainability and enables future localization (i18n) support across Android and iOS using the Compose Multiplatform resource system.

## Changes Made

### 1. Centralized String Resources
- **[MODIFY] [strings.xml](file:///Users/vadim/OutsourceProjects/Android/learning/ExpenseTrackerApp/shared/src/commonMain/composeResources/values/strings.xml)**:
    - Added over 50 unique string keys covering all screens: Home, Expense List, Add/Edit Expense, Analytics, and Settings.
    - Included localized strings for all expense categories (Food, Transport, Health, etc.).
    - Cleaned up unused template strings.

### 2. Refactored UI Screens
Updated all major Composable screens to use the `stringResource` API:
- **[MODIFY] [HomeScreen.kt](file:///Users/vadim/OutsourceProjects/Android/learning/ExpenseTrackerApp/shared/src/commonMain/kotlin/com/vvv/openexpensetracker/presentation/screens/home/HomeScreen.kt)**: Tab labels and FAB content descriptions.
- **[MODIFY] [ExpenseListScreen.kt](file:///Users/vadim/OutsourceProjects/Android/learning/ExpenseTrackerApp/shared/src/commonMain/kotlin/com/vvv/openexpensetracker/presentation/screens/expenses/ExpenseListScreen.kt)**: Title, search placeholders, empty state messages, and action buttons.
- **[MODIFY] [AddExpenseScreen.kt](file:///Users/vadim/OutsourceProjects/Android/learning/ExpenseTrackerApp/shared/src/commonMain/kotlin/com/vvv/openexpensetracker/presentation/screens/add_expense/AddExpenseScreen.kt)**: Input labels, placeholders, validation errors, and the save button.
- **[MODIFY] [SettingsScreen.kt](file:///Users/vadim/OutsourceProjects/Android/learning/ExpenseTrackerApp/shared/src/commonMain/kotlin/com/vvv/openexpensetracker/presentation/screens/settings/SettingsScreen.kt)**: Profile details, currency labels, sync status, and version info.
- **[MODIFY] [StatsScreen.kt](file:///Users/vadim/OutsourceProjects/Android/learning/ExpenseTrackerApp/shared/src/commonMain/kotlin/com/vvv/openexpensetracker/presentation/screens/stats/StatsScreen.kt)**: Analytics titles, labels, and breakdown categories.

### 3. Category Localization Helper
- **[NEW] [CategoryMapper.kt](file:///Users/vadim/OutsourceProjects/Android/learning/ExpenseTrackerApp/shared/src/commonMain/kotlin/com/vvv/openexpensetracker/presentation/theme/CategoryMapper.kt)**: Created a helper function `getCategoryNameResource(category: String)` to map internal data keys to localized string resources.

### 4. Logic & State Improvements
- **[MODIFY] [StatsViewModel.kt](file:///Users/vadim/OutsourceProjects/Android/learning/ExpenseTrackerApp/shared/src/commonMain/kotlin/com/vvv/openexpensetracker/presentation/screens/stats/StatsViewModel.kt)**: Injected `PreferencesRepository` to expose the preferred currency in the Analytics screen, ensuring consistent symbol display ($ vs €).

## Verification Results

### Build Verification
- **Success**: The project was built successfully with `:androidApp:assembleDebug`. This confirms that the `Res` class was correctly generated and all `stringResource` references are valid.

### UI Consistency
- Verified that all previous hardcoded text remains visible and correctly formatted in the UI.
- Verified that the "Last synced" time in Settings correctly uses the localized "at" connector.

> [!TIP]
> To support a new language, you can now simply create a new directory (e.g., `values-es/strings.xml`) and translate the keys. The app will automatically switch based on the device's system settings.
