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
import com.brucewang.sudokuds.sudoku.SudokuControlPanel
import com.brucewang.sudokuds.sudoku.SudokuGame
import java.lang.ref.WeakReference

class MainActivity : ComponentActivity() {
    private lateinit var displayManager: DisplayManager
    private var isSecondaryActivityLaunched = false

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LanguageManager.wrap(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // 注册当前Activity
        ActivityLifecycleSync.mainActivity = WeakReference(this)

        displayManager = getSystemService(DISPLAY_SERVICE) as DisplayManager

        // 初始化数独游戏状态管理器
        SudokuGame.init(this)

        // 检查是否需要移动到正确的屏幕
        if (!ActivityLifecycleSync.hasRepositioned && shouldRepositionToSmallerScreen()) {
            ActivityLifecycleSync.hasRepositioned = true
            repositionToSmallerScreen()
            return
        }

        // 主屏显示内容
        setContent {
            var showMenu by remember { mutableStateOf(false) }

            DualScreenTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    // 显示数独控制面板
                    SudokuControlPanel(
                        modifier = Modifier.padding(innerPadding)
                    )

                    if (showMenu) {
                        HamburgerMenu(
                            onDismiss = { showMenu = false },
                            onSettings = {
                                showMenu = false
                                ActivityLifecycleSync.isSettingsOpen = true
                                val intent = Intent(this@MainActivity, SettingsActivity::class.java)
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
                                val intent = Intent(this@MainActivity, InstructionsActivity::class.java)
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

        // 立即检查并显示副屏
        showOnSecondaryDisplay()
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

    override fun onResume() {
        super.onResume()

        // 恢复计时器
        SudokuGame.resumeTimer()

        if (ActivityLifecycleSync.isMovedToBackground) {
            // 如果之前是移动到后台的，重新显示副屏
            showOnSecondaryDisplay()
            ActivityLifecycleSync.isMovedToBackground = false
        } else {
            // 正常情况下也检查一次副屏显示
            showOnSecondaryDisplay()
        }
    }

    override fun onPause() {
        super.onPause()
        // 暂停计时器
        SudokuGame.pauseTimer()

        // 当MainActivity进入后台时，检查是否是用户退出到桌面
        if (isFinishing) {
            // 如果当前Activity正在结束，同时关闭另一个Activity
            ActivityLifecycleSync.secondaryActivity?.get()?.finish()
        }
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        // 用户按下Home键或切换到其他应用时调用
        // 同步让另一个Activity也移动到后台
        if (!ActivityLifecycleSync.isSettingsOpen) {
            ActivityLifecycleSync.moveToBackground(1)
        }
    }

    override fun onStop() {
        super.onStop()
        // 移除自动关闭逻辑，让应用可以在后台运行
        // 只有用户主动选择"退出程序"时才会关闭
    }

    /**
     * 在副屏显示内容
     */
    private fun showOnSecondaryDisplay() {
        val displays = displayManager.displays

        // 如果有第二个显示器，在副屏显示内容
        if (displays.size > 1) {
            // 按屏幕大小排序，从大到小
            val sortedDisplays = displays.sortedByDescending {
                val mode = it.mode
                mode.physicalWidth * mode.physicalHeight
            }

            // 找到更大的屏幕（第一个）作为副屏
            val currentDisplayId = display?.displayId ?: 0
            val secondaryDisplay = sortedDisplays.firstOrNull { it.displayId != currentDisplayId }

            if (secondaryDisplay != null) {
                val intent = Intent(this, SecondaryActivity::class.java).apply {
                    flags = if (isSecondaryActivityLaunched) {
                        Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                    } else {
                        Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                }

                val options = ActivityOptions.makeBasic()
                options.launchDisplayId = secondaryDisplay.displayId

                startActivity(intent, options.toBundle())
                isSecondaryActivityLaunched = true
            }
        }
    }

    private fun shouldRepositionToSmallerScreen(): Boolean {
        val displays = displayManager.displays
        if (displays.size <= 1) return false

        // 按屏幕大小排序，从大到小
        val sortedDisplays = displays.sortedByDescending {
            val mode = it.mode
            mode.physicalWidth * mode.physicalHeight
        }

        val currentDisplayId = display?.displayId ?: 0
        val smallerDisplay = sortedDisplays.lastOrNull()

        // 如果当前不在较小的屏幕上，需要重新定位
        return smallerDisplay != null && smallerDisplay.displayId != currentDisplayId
    }

    private fun repositionToSmallerScreen() {
        val displays = displayManager.displays
        if (displays.size <= 1) return

        // 按屏幕大小排序，从大到小
        val sortedDisplays = displays.sortedByDescending {
            val mode = it.mode
            mode.physicalWidth * mode.physicalHeight
        }

        val smallerDisplay = sortedDisplays.lastOrNull()

        if (smallerDisplay != null) {
            finish()
            val intent = Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            val options = ActivityOptions.makeBasic()
            options.launchDisplayId = smallerDisplay.displayId
            startActivity(intent, options.toBundle())
        }
    }

    private fun switchLanguage() {
        val newLanguage = LanguageManager.toggleLanguage(this)

        // 重启MainActivity
        finish()
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)

        // 如果有副屏Activity，也重启它
        ActivityLifecycleSync.secondaryActivity?.get()?.let { secondary ->
            val secondaryIntent = Intent(this, SecondaryActivity::class.java)
            secondaryIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

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
                    startActivity(secondaryIntent, options.toBundle())
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        isSecondaryActivityLaunched = false
    }
}