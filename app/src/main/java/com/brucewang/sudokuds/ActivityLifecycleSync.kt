package com.brucewang.sudokuds

import android.app.ActivityOptions
import android.content.Intent
import androidx.compose.runtime.mutableStateListOf
import java.lang.ref.WeakReference

/**
 * 用于同步两个Activity的生命周期
 */
object ActivityLifecycleSync {
    var mainActivity: WeakReference<MainActivity>? = null
    var secondaryActivity: WeakReference<SecondaryActivity>? = null
    private var isSwapping = false
    var isSettingsOpen = false  // 标记设置页面是否打开
    var hasRepositioned = false  // 标记是否已经重新定位过MainActivity
    private var isMovingToBackground = false  // 标记是否正在移动到后台
    var isMovedToBackground = false  // 标记是否已经移动到后台

    // 手柄按键信息
    val gamepadKeys = mutableStateListOf<String>()

    fun addGamepadKey(keyName: String) {
        gamepadKeys.add(0, keyName)
        if (gamepadKeys.size > 10) {
            gamepadKeys.removeAt(gamepadKeys.size - 1)
        }
    }

    fun finishAll() {
        mainActivity?.get()?.finish()
        secondaryActivity?.get()?.finish()
        mainActivity = null
        secondaryActivity = null
        hasRepositioned = false
        // 退出应用
        android.os.Process.killProcess(android.os.Process.myPid())
    }

    fun moveToBackground(displayNo: Int) {
        if (isMovingToBackground) return
        isMovingToBackground = true
        hasRepositioned = false

        // 让两个Activity都移动到后台
        if (displayNo == 1) {
            secondaryActivity?.get()?.moveTaskToBack(true)
        } else {
            mainActivity?.get()?.moveTaskToBack(true)
        }
        // 延迟重置标志
        mainActivity?.get()?.window?.decorView?.postDelayed({
            isMovingToBackground = false
            isMovedToBackground = true
        }, 500)
    }

    fun swapScreens() {
        if (isSwapping) return
        isSwapping = true

        val main = mainActivity?.get()
        val secondary = secondaryActivity?.get()

        if (main != null && secondary != null) {
            val mainDisplayId = main.display?.displayId ?: 0
            val secondaryDisplayId = secondary.display?.displayId ?: 0

            // 先关闭两个Activity
            main.finish()
            secondary.finish()

            // 使用延迟确保Activity完全关闭后再启动新的
            main.window.decorView.postDelayed({
                // 在原来副屏的位置启动MainActivity
                val mainIntent = Intent(main, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                val mainOptions = ActivityOptions.makeBasic()
                mainOptions.launchDisplayId = secondaryDisplayId
                main.startActivity(mainIntent, mainOptions.toBundle())

                // 在原来主屏的位置启动SecondaryActivity
                val secondaryIntent = Intent(secondary, SecondaryActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                val secondaryOptions = ActivityOptions.makeBasic()
                secondaryOptions.launchDisplayId = mainDisplayId
                secondary.startActivity(secondaryIntent, secondaryOptions.toBundle())

                hasRepositioned = true
                isSwapping = false
            }, 100)
        } else {
            isSwapping = false
        }
    }
}

