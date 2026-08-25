# Fix String Resource Placeholder Replacement

This plan fixes the issue where `%s` and `%d` placeholders in `strings.xml` are not being correctly replaced by values in Compose Multiplatform. We will update the placeholders to use the required positional format (e.g., `%1$s`).

## Proposed Changes

### [Resources]

#### [MODIFY] [strings.xml](file:///Users/vadim/OutsourceProjects/Android/learning/ExpenseTrackerApp/shared/src/commonMain/composeResources/values/strings.xml)
Update all string templates to use positional placeholders:
- `settings_ai_tps_label`: change `%s` to `%1$s`.
- `settings_ai_time_label`: change `%s` to `%1$s`.
- `settings_ai_download_progress`: change `%d%%` to `%1$d%%`.

### [Presentation Layer - Settings]

#### [MODIFY] [SettingsScreen.kt](file:///Users/vadim/OutsourceProjects/Android/learning/ExpenseTrackerApp/shared/src/commonMain/kotlin/com/vvv/openexpensetracker/presentation/screens/settings/SettingsScreen.kt)
- Ensure that `stringResource` is called with the arguments correctly after the XML change.

## Verification Plan

### Automated Tests
- Run `:androidApp:assembleDebug` to ensure resources are correctly generated.

### Manual Verification
1.  **AI Performance**: Verify that the Settings screen now displays the actual values (e.g., "8.50 tokens/sec") instead of the literal placeholder string.
2.  **Download Progress**: Verify that the download percentage is correctly displayed during model download.
