package com.brucewang.sudokuds

import android.view.InputDevice
import android.view.KeyEvent
import android.view.MotionEvent
import com.brucewang.sudokuds.sudoku.SudokuGame

/**
 * 手柄输入处理器 - 统一处理手柄输入事件
 */
object GamepadInputHandler {
    // 记录上次的扳机值，避免重复输出
    private var lastL2 = 0f
    private var lastR2 = 0f

    // 记录上次输出时间，用于限制输出频率
    private var lastMotionEventTime = 0L

    // 记录当前摇杆和HAT轴的状态（用于连续移动）
    private var currentHatX = 0f
    private var currentHatY = 0f
    private var currentJoystickX = 0f
    private var currentJoystickY = 0f

    // 记录上次移动时间
    private var lastMoveTime = 0L

    // 移动时间间隔（毫秒）
    private const val MOVE_INTERVAL = 150L

    // 调试模式：显示所有轴的值
    private const val DEBUG_MODE = false

    // 游戏模式：true=数独游戏，false=测试模式
    var gameMode = true

    /**
     * 处理按键事件
     * @return 是否处理了该事件
     */
    fun handleKeyEvent(event: KeyEvent): Boolean {
        if (event.source and InputDevice.SOURCE_GAMEPAD == InputDevice.SOURCE_GAMEPAD) {
            when (event.action) {
                KeyEvent.ACTION_DOWN -> {
                    if (gameMode) {
                        handleSudokuKey(event.keyCode)
                    } else {
                        handleGamepadKey(event.keyCode, true)
                    }
                }
                KeyEvent.ACTION_UP -> {
                    if (!gameMode) {
                        handleGamepadKey(event.keyCode, false)
                    }
                }
            }
            return true
        }
        return false
    }

    /**
     * 检查并执行连续移动（由定时器调用）
     * 检查摇杆和HAT轴的状态，只要保持推动就持续移动
     */
    fun checkContinuousMove() {
        if (!gameMode) return

        val currentTime = System.currentTimeMillis()
        if (currentTime - lastMoveTime < MOVE_INTERVAL) return

        val threshold = 0.7f
        val hatThreshold = 0.5f

        var moved = false

        // 检查HAT轴状态（优先级高，因为是方向键）
        if (currentHatX < -hatThreshold) {
            SudokuGame.moveSelection(0, -1)
            moved = true
        } else if (currentHatX > hatThreshold) {
            SudokuGame.moveSelection(0, 1)
            moved = true
        }

        if (currentHatY < -hatThreshold) {
            SudokuGame.moveSelection(-1, 0)
            moved = true
        } else if (currentHatY > hatThreshold) {
            SudokuGame.moveSelection(1, 0)
            moved = true
        }

        // 如果HAT轴没有移动，则检查左摇杆
        if (!moved) {
            if (currentJoystickX < -threshold) {
                SudokuGame.moveSelection(0, -1)
                moved = true
            } else if (currentJoystickX > threshold) {
                SudokuGame.moveSelection(0, 1)
                moved = true
            }

            if (currentJoystickY < -threshold) {
                SudokuGame.moveSelection(-1, 0)
                moved = true
            } else if (currentJoystickY > threshold) {
                SudokuGame.moveSelection(1, 0)
                moved = true
            }
        }

        if (moved) {
            lastMoveTime = currentTime
        }
    }

    /**
     * 处理数独游戏的按键输入
     */
    private fun handleSudokuKey(keyCode: Int) {
        when (keyCode) {
            // 方向键控制光标移动 - 首次按下立即移动，后续由checkContinuousMove处理
            KeyEvent.KEYCODE_DPAD_UP -> SudokuGame.moveSelection(-1, 0)
            KeyEvent.KEYCODE_DPAD_DOWN -> SudokuGame.moveSelection(1, 0)
            KeyEvent.KEYCODE_DPAD_LEFT -> SudokuGame.moveSelection(0, -1)
            KeyEvent.KEYCODE_DPAD_RIGHT -> SudokuGame.moveSelection(0, 1)

            // A键 - 确认/选择
            KeyEvent.KEYCODE_BUTTON_A -> {
                if (SudokuGame.gameState == null) {
                    SudokuGame.startNewGame()
                }
            }

            // B键 - 擦除
            KeyEvent.KEYCODE_BUTTON_B -> SudokuGame.clearValue()

            // X键 - 数字1
            // KeyEvent.KEYCODE_BUTTON_X -> SudokuGame.setValue(1)

            // Y键 - 数字2
            // KeyEvent.KEYCODE_BUTTON_Y -> SudokuGame.setValue(2)

            // L1键 - 数字3
            // KeyEvent.KEYCODE_BUTTON_L1 -> SudokuGame.setValue(3)

            // R1键 - 数字4
            // KeyEvent.KEYCODE_BUTTON_R1 -> SudokuGame.setValue(4)

            // START键 - 重新开始
            KeyEvent.KEYCODE_BUTTON_START -> SudokuGame.startNewGame()
        }

        lastMoveTime = System.currentTimeMillis()
    }

    /**
     * 处理模拟输入事件（摇杆、扳机等）
     * @return 是否处理了该事件
     */
    fun handleMotionEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_MOVE &&
            event.source and InputDevice.SOURCE_JOYSTICK == InputDevice.SOURCE_JOYSTICK) {

            val currentTime = System.currentTimeMillis()

            // 处理 DPad HAT 轴（某些手柄的方向键通过模拟输入）
            val hatX = event.getAxisValue(MotionEvent.AXIS_HAT_X)
            val hatY = event.getAxisValue(MotionEvent.AXIS_HAT_Y)

            // 更新当前HAT轴状态（供checkContinuousMove使用）
            currentHatX = hatX
            currentHatY = hatY

            if (gameMode) {
                // 数独游戏模式：HAT轴控制光标 - 首次推动立即移动
                if (currentTime - lastMoveTime >= MOVE_INTERVAL) {
                    var moved = false

                    if (hatX < -0.5f) {
                        SudokuGame.moveSelection(0, -1)
                        moved = true
                    } else if (hatX > 0.5f) {
                        SudokuGame.moveSelection(0, 1)
                        moved = true
                    }

                    if (hatY < -0.5f) {
                        SudokuGame.moveSelection(-1, 0)
                        moved = true
                    } else if (hatY > 0.5f) {
                        SudokuGame.moveSelection(1, 0)
                        moved = true
                    }

                    if (moved) {
                        lastMoveTime = currentTime
                    }
                }
            } else {
                // 测试模式：显示输入
                if (hatX < 0) {
                    ActivityLifecycleSync.addGamepadKey("方向键左 按下")
                } else if (hatX > 0) {
                    ActivityLifecycleSync.addGamepadKey("方向键右 按下")
                }

                if (hatY < 0) {
                    ActivityLifecycleSync.addGamepadKey("方向键上 按下")
                } else if (hatY > 0) {
                    ActivityLifecycleSync.addGamepadKey("方向键下 按下")
                }
            }

            // 处理摇杆输入
            val lx = event.getAxisValue(MotionEvent.AXIS_X)
            val ly = event.getAxisValue(MotionEvent.AXIS_Y)

            // 更新当前摇杆状态（供checkContinuousMove使用）
            currentJoystickX = lx
            currentJoystickY = ly

            // 在游戏模式下，左摇杆也可以控制光标 - 首次推动立即移动
            if (gameMode) {
                val threshold = 0.7f
                if (currentTime - lastMoveTime >= MOVE_INTERVAL) {
                    var moved = false

                    if (lx < -threshold) {
                        SudokuGame.moveSelection(0, -1)
                        moved = true
                    } else if (lx > threshold) {
                        SudokuGame.moveSelection(0, 1)
                        moved = true
                    }

                    if (ly < -threshold) {
                        SudokuGame.moveSelection(-1, 0)
                        moved = true
                    } else if (ly > threshold) {
                        SudokuGame.moveSelection(1, 0)
                        moved = true
                    }

                    if (moved) {
                        lastMoveTime = currentTime
                    }
                }
            }

            val rx = event.getAxisValue(MotionEvent.AXIS_Z)
            val ry = event.getAxisValue(MotionEvent.AXIS_RZ)

            // 调试模式：输出所有可能的扳机轴值（仅在非游戏模式下）
            if (DEBUG_MODE && !gameMode) {
                val allAxes = mapOf(
                    "LTRIGGER" to event.getAxisValue(MotionEvent.AXIS_LTRIGGER),
                    "RTRIGGER" to event.getAxisValue(MotionEvent.AXIS_RTRIGGER),
                    "BRAKE" to event.getAxisValue(MotionEvent.AXIS_BRAKE),
                    "GAS" to event.getAxisValue(MotionEvent.AXIS_GAS),
                    "THROTTLE" to event.getAxisValue(MotionEvent.AXIS_THROTTLE),
                    "RUDDER" to event.getAxisValue(MotionEvent.AXIS_RUDDER),
                    "WHEEL" to event.getAxisValue(MotionEvent.AXIS_WHEEL),
                    "RX" to event.getAxisValue(MotionEvent.AXIS_RX),
                    "RY" to event.getAxisValue(MotionEvent.AXIS_RY),
                    "GENERIC_1" to event.getAxisValue(MotionEvent.AXIS_GENERIC_1),
                    "GENERIC_2" to event.getAxisValue(MotionEvent.AXIS_GENERIC_2),
                    "GENERIC_3" to event.getAxisValue(MotionEvent.AXIS_GENERIC_3),
                    "GENERIC_4" to event.getAxisValue(MotionEvent.AXIS_GENERIC_4)
                )

                // 只输出非零的轴值
                allAxes.forEach { (name, value) ->
                    if (kotlin.math.abs(value) > 0.01f) {
                        ActivityLifecycleSync.addGamepadKey("轴调试 - $name: ${"%.2f".format(value)}")
                    }
                }
            }

            // 处理扳机输入 - 使用手柄实际的轴映射
            // L2 使用 AXIS_BRAKE，R2 使用 AXIS_GAS
            val l2 = event.getAxisValue(MotionEvent.AXIS_BRAKE)
            val r2 = event.getAxisValue(MotionEvent.AXIS_GAS)

            // 摇杆死区：0.005
            val stickDeadZone = 0.005f
            // 扳机死区：0.05（扳机需要更大的死区）
            val triggerDeadZone = 0.05f

            val lxFiltered = if (kotlin.math.abs(lx) <= stickDeadZone) 0f else lx
            val lyFiltered = if (kotlin.math.abs(ly) <= stickDeadZone) 0f else ly
            val rxFiltered = if (kotlin.math.abs(rx) <= stickDeadZone) 0f else rx
            val ryFiltered = if (kotlin.math.abs(ry) <= stickDeadZone) 0f else ry
            val l2Filtered = if (kotlin.math.abs(l2) <= triggerDeadZone) 0f else l2
            val r2Filtered = if (kotlin.math.abs(r2) <= triggerDeadZone) 0f else r2

            // 仅在非游戏模式下显示调试信息
            if (!gameMode) {
                // 扳机值变化时才输出（避免重复输出相同值）
                if (kotlin.math.abs(l2Filtered - lastL2) > 0.01f) {
                    if (l2Filtered > 0f) {
                        ActivityLifecycleSync.addGamepadKey("左扳机 L2: ${"%.2f".format(l2Filtered)}")
                    } else {
                        ActivityLifecycleSync.addGamepadKey("左扳机 L2: 释放")
                    }
                    lastL2 = l2Filtered
                }

                if (kotlin.math.abs(r2Filtered - lastR2) > 0.01f) {
                    if (r2Filtered > 0f) {
                        ActivityLifecycleSync.addGamepadKey("右扳机 R2: ${"%.2f".format(r2Filtered)}")
                    } else {
                        ActivityLifecycleSync.addGamepadKey("右扳机 R2: 释放")
                    }
                    lastR2 = r2Filtered
                }

                // 限制摇杆输出频率（每100ms输出一次）
                if (currentTime - lastMotionEventTime >= 100) {
                    if (lxFiltered != 0f || lyFiltered != 0f) {
                        ActivityLifecycleSync.addGamepadKey("左摇杆: X=${"%.2f".format(lxFiltered)}, Y=${"%.2f".format(lyFiltered)}")
                    }
                    if (rxFiltered != 0f || ryFiltered != 0f) {
                        ActivityLifecycleSync.addGamepadKey("右摇杆: X=${"%.2f".format(rxFiltered)}, Y=${"%.2f".format(ryFiltered)}")
                    }
                    lastMotionEventTime = currentTime
                }
            }

            return true
        }
        return false
    }

    /**
     * 将按键码转换为友好的中文显示名称
     */
    private fun handleGamepadKey(keyCode: Int, isDown: Boolean) {
        val action = if (isDown) "按下" else "释放"
        val keyName = when (keyCode) {
            KeyEvent.KEYCODE_BUTTON_A -> "A键"
            KeyEvent.KEYCODE_BUTTON_B -> "B键"
            KeyEvent.KEYCODE_BUTTON_C -> "C键"
            KeyEvent.KEYCODE_BUTTON_X -> "X键"
            KeyEvent.KEYCODE_BUTTON_Y -> "Y键"
            KeyEvent.KEYCODE_BUTTON_Z -> "Z键"
            KeyEvent.KEYCODE_BUTTON_L1 -> "L1键"
            KeyEvent.KEYCODE_BUTTON_R1 -> "R1键"
            // L2/R2 作为按键时忽略，因为我们在 MotionEvent 中处理线性值
            KeyEvent.KEYCODE_BUTTON_L2 -> return
            KeyEvent.KEYCODE_BUTTON_R2 -> return
            KeyEvent.KEYCODE_BUTTON_THUMBL -> "左摇杆按键"
            KeyEvent.KEYCODE_BUTTON_THUMBR -> "右摇杆按键"
            KeyEvent.KEYCODE_BUTTON_START -> "START键"
            KeyEvent.KEYCODE_BUTTON_SELECT -> "SELECT键"
            KeyEvent.KEYCODE_BUTTON_MODE -> "MODE键"
            KeyEvent.KEYCODE_DPAD_UP -> "方向键上"
            KeyEvent.KEYCODE_DPAD_DOWN -> "方向键下"
            KeyEvent.KEYCODE_DPAD_LEFT -> "方向键左"
            KeyEvent.KEYCODE_DPAD_RIGHT -> "方向键右"
            KeyEvent.KEYCODE_DPAD_CENTER -> "方向键中心"
            else -> "按键码: $keyCode"
        }

        if (isDown) {
            ActivityLifecycleSync.addGamepadKey("$keyName $action")
        }
    }
}

