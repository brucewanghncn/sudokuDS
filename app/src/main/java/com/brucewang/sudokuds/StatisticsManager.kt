package com.brucewang.sudokuds

import android.content.Context
import androidx.core.content.edit

object StatisticsManager {
    private const val PREFS_NAME = "statistics_prefs"
    private const val KEY_EASY_COMPLETED = "easy_completed"
    private const val KEY_MEDIUM_COMPLETED = "medium_completed"
    private const val KEY_HARD_COMPLETED = "hard_completed"

    /**
     * 获取已完成的简单难度游戏数量
     */
    fun getEasyCompleted(context: Context): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getInt(KEY_EASY_COMPLETED, 0)
    }

    /**
     * 获取已完成的中等难度游戏数量
     */
    fun getMediumCompleted(context: Context): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getInt(KEY_MEDIUM_COMPLETED, 0)
    }

    /**
     * 获取已完成的困难难度游戏数量
     */
    fun getHardCompleted(context: Context): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getInt(KEY_HARD_COMPLETED, 0)
    }

    /**
     * 获取总完成游戏数量
     */
    fun getTotalCompleted(context: Context): Int {
        return getEasyCompleted(context) + getMediumCompleted(context) + getHardCompleted(context)
    }

    /**
     * 记录完成一局游戏
     */
    fun recordCompletion(context: Context, difficulty: Int) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val key = when (difficulty) {
            30 -> KEY_EASY_COMPLETED
            40 -> KEY_MEDIUM_COMPLETED
            50 -> KEY_HARD_COMPLETED
            else -> return
        }
        prefs.edit {
            val current = prefs.getInt(key, 0)
            putInt(key, current + 1)
        }
    }

    /**
     * 重置所有统计数据
     */
    fun resetStatistics(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit {
            clear()
        }
    }

    /**
     * 获取统计数据类
     */
    data class Statistics(
        val easyCompleted: Int,
        val mediumCompleted: Int,
        val hardCompleted: Int,
        val totalCompleted: Int
    )

    /**
     * 获取所有统计数据
     */
    fun getStatistics(context: Context): Statistics {
        return Statistics(
            easyCompleted = getEasyCompleted(context),
            mediumCompleted = getMediumCompleted(context),
            hardCompleted = getHardCompleted(context),
            totalCompleted = getTotalCompleted(context)
        )
    }
}

