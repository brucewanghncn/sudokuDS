package com.brucewang.sudokuds

import android.content.Context
import android.content.res.Configuration
import androidx.core.content.edit
import java.util.Locale

object LanguageManager {
    private const val PREFS_NAME = "language_prefs"
    private const val KEY_LANGUAGE = "selected_language"

    const val LANGUAGE_ENGLISH = "en"
    const val LANGUAGE_CHINESE = "zh"

    /**
     * 获取保存的语言设置
     */
    fun getSavedLanguage(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_LANGUAGE, getSystemLanguage()) ?: LANGUAGE_CHINESE
    }

    /**
     * 保存语言设置
     */
    fun saveLanguage(context: Context, language: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit {
            putString(KEY_LANGUAGE, language)
        }
    }

    /**
     * 获取系统语言
     */
    private fun getSystemLanguage(): String {
        val locale = Locale.getDefault()
        return if (locale.language == "zh") LANGUAGE_CHINESE else LANGUAGE_ENGLISH
    }

    /**
     * 切换语言
     */
    fun toggleLanguage(context: Context): String {
        val currentLanguage = getSavedLanguage(context)
        val newLanguage = if (currentLanguage == LANGUAGE_CHINESE) {
            LANGUAGE_ENGLISH
        } else {
            LANGUAGE_CHINESE
        }
        saveLanguage(context, newLanguage)
        return newLanguage
    }

    /**
     * 应用语言设置到Context
     */
    fun applyLanguage(context: Context, language: String): Context {
        val locale = when (language) {
            LANGUAGE_ENGLISH -> Locale.ENGLISH
            LANGUAGE_CHINESE -> Locale.CHINESE
            else -> Locale.CHINESE
        }

        Locale.setDefault(locale)

        val configuration = Configuration(context.resources.configuration)
        configuration.setLocale(locale)

        return context.createConfigurationContext(configuration)
    }

    /**
     * 包装Context以应用语言设置
     */
    fun wrap(context: Context): Context {
        val savedLanguage = getSavedLanguage(context)
        return applyLanguage(context, savedLanguage)
    }
}

