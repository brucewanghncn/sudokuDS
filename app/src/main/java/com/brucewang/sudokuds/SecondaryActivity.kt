package com.brucewang.sudokuds

import android.annotation.SuppressLint
import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.hardware.display.DisplayManager
import android.os.Bundle
import android.view.KeyEvent
import android.view.MotionEvent
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.brucewang.sudokuds.ui.theme.DualScreenTheme
import com.brucewang.sudokuds.sudoku.SudokuGameBoard
import com.brucewang.sudokuds.sudoku.SudokuGame
import java.lang.ref.WeakReference

/**
 * 副屏的 Activity
 */
class SecondaryActivity : ComponentActivity() {
    private lateinit var displayManager: DisplayManager

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LanguageManager.wrap(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // 注册当前Activity
        ActivityLifecycleSync.secondaryActivity = WeakReference(this)

        displayManager = getSystemService(DISPLAY_SERVICE) as DisplayManager

        // 副屏显示内容
        setContent {
            var showMenu by remember { mutableStateOf(false) }

            DualScreenTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    // 显示数独游戏棋盘
                    SudokuGameBoard(
                        modifier = Modifier.padding(innerPadding)
                    )

                    if (showMenu) {
                        HamburgerMenu(
                            onDismiss = { showMenu = false },
                            onSettings = {
                                showMenu = false
                                ActivityLifecycleSync.isSettingsOpen = true
                                val intent = Intent(this@SecondaryActivity, SettingsActivity::class.java)
                                startActivity(intent)
                            },
                            onSwapScreens = {
                                showMenu = false
                                ActivityLifecycleSync.swapScreens()
                            },
                            onLanguageSwitch = {
                                showMenu = false
                                switchLanguage()
                            },
                            onInstructions = {
                                showMenu = false
                                val intent = Intent(this@SecondaryActivity, InstructionsActivity::class.java)
                                startActivity(intent)
                            },
                            onExit = {
                                showMenu = false
                                ActivityLifecycleSync.finishAll()
                            }
                        )
                    }
                }
            }

            // 定时检查连续移动（用于DPad按住不放的情况）
            LaunchedEffect(Unit) {
                while (true) {
                    kotlinx.coroutines.delay(50) // 每50ms检查一次
                    GamepadInputHandler.checkContinuousMove()
                }
            }

            // 处理返回按钮
            onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    showMenu = true
                }
            })
        }
    }

    @SuppressLint("RestrictedApi")
    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        return GamepadInputHandler.handleKeyEvent(event)
    }

    override fun dispatchGenericMotionEvent(event: MotionEvent?): Boolean {
        return if (event != null && GamepadInputHandler.handleMotionEvent(event)) {
            true
        } else {
            super.dispatchGenericMotionEvent(event)
        }
    }

    private fun showOnMainDisplay() {
        val displays = displayManager.displays

        // 如果有第二个显示器，在主屏显示内容
        if (displays.size > 1) {
            // 按屏幕大小排序，从大到小
            val sortedDisplays = displays.sortedByDescending {
                val mode = it.mode
                mode.physicalWidth * mode.physicalHeight
            }

            // 找到更大的屏幕（第一个）作为主屏
            val currentDisplayId = display?.displayId ?: 3
            val mainDisplay = sortedDisplays.firstOrNull { it.displayId != currentDisplayId }
            if (mainDisplay != null) {
                val intent = Intent(this, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                }

                val options = ActivityOptions.makeBasic()
                options.launchDisplayId = mainDisplay.displayId

                startActivity(intent, options.toBundle())
            }
        }
    }

    override fun onResume() {
        super.onResume()

        // 恢复计时器
        SudokuGame.resumeTimer()

        if (ActivityLifecycleSync.isMovedToBackground) {
            showOnMainDisplay()
            ActivityLifecycleSync.isMovedToBackground = false
        }
    }

    override fun onPause() {
        super.onPause()
        // 暂停计时器
        SudokuGame.pauseTimer()

        // 当SecondaryActivity进入后台时，检查是否是用户退出到桌面
        if (isFinishing) {
            // 如果当前Activity正在结束，同时关闭另一个Activity
            ActivityLifecycleSync.mainActivity?.get()?.finish()
        }
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        // 用户按下Home键或切换到其他应用时调用
        // 同步让另一个Activity也移动到后台
        if (!ActivityLifecycleSync.isSettingsOpen) {
            ActivityLifecycleSync.moveToBackground(0)
        }
    }

    override fun onStop() {
        super.onStop()
        // 移除自动关闭逻辑，让应用可以在后台运行
        // 只有用户主动选择"退出程序"时才会关闭
    }

    private fun switchLanguage() {
        val newLanguage = LanguageManager.toggleLanguage(this)

        // 重启SecondaryActivity
        finish()
        val intent = Intent(this, SecondaryActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

        val displays = displayManager.displays
        if (displays.size > 1) {
            val sortedDisplays = displays.sortedByDescending {
                val mode = it.mode
                mode.physicalWidth * mode.physicalHeight
            }
            val currentDisplayId = display?.displayId ?: 0
            val secondaryDisplay = sortedDisplays.firstOrNull { it.displayId != currentDisplayId }

            if (secondaryDisplay != null) {
                val options = ActivityOptions.makeBasic()
                options.launchDisplayId = secondaryDisplay.displayId
                startActivity(intent, options.toBundle())
            }
        }

        // 同时重启主Activity
        ActivityLifecycleSync.mainActivity?.get()?.let { main ->
            val mainIntent = Intent(this, MainActivity::class.java)
            mainIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(mainIntent)
        }
    }
}
