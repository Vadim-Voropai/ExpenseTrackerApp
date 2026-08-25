package com.vvv.openexpensetracker.core

object Constants {
    // Storage Names
    const val PREFS_NAME = "expense_tracker_prefs"
    const val SECURE_PREFS_NAME = "secure_expense_tracker_prefs"
    const val EXPENSES_FILE_FOLDER_NAME = "OpenExpensesTracker"
    const val EXPENSES_FILE_NAME = "expenses.json"

    // Storage Keys
    const val KEY_AUTH_TOKEN = "auth_token"
    const val KEY_USER_EMAIL = "user_email"
    const val KEY_USER_NAME = "user_name"
    const val KEY_LAST_SYNC_TIME = "last_sync_time"
    const val KEY_FOLDER_ID = "google_drive_folder_id"
    const val KEY_FILE_ID = "google_drive_file_id"
    const val KEY_LLM_DOWNLOADED = "llm_model_downloaded"
    const val KEY_LLM_TPS = "llm_tps"
    const val KEY_LLM_BENCHMARK_DURATION = "llm_benchmark_duration"

    // Google API URLs
    const val GOOGLE_USERINFO_URL = "https://www.googleapis.com/oauth2/v3/userinfo"
    const val GOOGLE_DRIVE_FILES_URL = "https://www.googleapis.com/drive/v3/files"
    const val GOOGLE_DRIVE_UPLOAD_URL = "https://www.googleapis.com/upload/drive/v3/files"

    // Google OAuth Scopes
    const val SCOPE_DRIVE_FILE = "https://www.googleapis.com/auth/drive"
    const val SCOPE_EMAIL = "email"
    const val SCOPE_PROFILE = "profile"

    // Auth Fallbacks
    const val AUTH_FALLBACK_EMAIL = "Authenticated"
    const val AUTH_FALLBACK_NAME = "Google User"
}
