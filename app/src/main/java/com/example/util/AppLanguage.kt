package com.example.util

import androidx.compose.ui.unit.LayoutDirection

enum class AppLanguage(val code: String, val title: String, val layoutDirection: LayoutDirection) {
    AR("ar", "العربية", LayoutDirection.Rtl),
    EN("en", "English", LayoutDirection.Ltr),
    FR("fr", "Français", LayoutDirection.Ltr);

    companion object {
        fun fromCode(code: String): AppLanguage {
            return entries.find { it.code.equals(code, ignoreCase = true) } ?: AR
        }
    }
}
