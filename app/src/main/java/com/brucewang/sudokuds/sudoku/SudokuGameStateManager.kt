package com.brucewang.sudokuds.sudoku

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

/**
 * 全局数独游戏状态管理器
 */
object SudokuGame {
    var gameState by mutableStateOf<SudokuGameState?>(null)
        private set

    private var sessionStartTime = 0L  // 本次会话开始时间
    private var totalElapsedSeconds = 0L  // 总累计时间（秒）
    private var isTimerPaused = false  // 计时器是否暂停

    // Context用于保存和加载游戏状态
    private var appContext: Context? = null

    /**
     * 初始化 Context
     */
    fun init(context: Context) {
        appContext = context.applicationContext
    }

    /**
     * 检查是否有保存的游戏
     */
    fun hasSavedGame(): Boolean {
        return appContext?.let { SudokuStateStorage.hasSavedGame(it) } ?: false
    }

    /**
     * 开始新游戏
     */
    fun startNewGame(difficulty: Int = 40) {
        val board = SudokuGameManager.generateNewGame(difficulty)
        gameState = SudokuGameState(
            board = board,
            selectedRow = 0,
            selectedCol = 0,
            elapsedTime = 0L,
            isComplete = false,
            mistakes = 0,
            highlightedNumber = 0,  // 初始没有高亮数字
            emptyCellsCount = difficulty  // 保存难度
        )
        sessionStartTime = System.currentTimeMillis()
        totalElapsedSeconds = 0L
        isTimerPaused = false  // 确保计时器开始

        // 清除旧存档，保存新游戏状态
        autoSave()
    }

    /**
     * 继续游戏（从存档加载）
     */
    fun continueGame() {
        appContext?.let { context ->
            val savedData = SudokuStateStorage.loadGameState(context)
            if (savedData != null) {
                val (state, _) = savedData
                gameState = state
                // 恢复累计时间，重新开始计时
                totalElapsedSeconds = state.elapsedTime
                sessionStartTime = System.currentTimeMillis()
                isTimerPaused = false  // 确保计时器开始
            }
        }
    }

    /**
     * 自动保存游戏状态
     */
    private fun autoSave() {
        appContext?.let { context ->
            gameState?.let { state ->
                // 保存前先更新时间
                val currentSessionTime = (System.currentTimeMillis() - sessionStartTime) / 1000
                val totalTime = totalElapsedSeconds + currentSessionTime
                val updatedState = state.copy(elapsedTime = totalTime)
                SudokuStateStorage.saveGameState(context, updatedState, 0L)
            }
        }
    }

    /**
     * 移动选中的格子
     */
    fun moveSelection(deltaRow: Int, deltaCol: Int) {
        gameState?.let { state ->
            val newRow = (state.selectedRow + deltaRow).coerceIn(0, 8)
            val newCol = (state.selectedCol + deltaCol).coerceIn(0, 8)
            val selectedCell = state.board[newRow][newCol]

            // 只有移动到的格子有数字时，才更新高亮数字
            val newHighlightedNumber = if (selectedCell.value != 0) {
                selectedCell.value
            } else {
                // 移动到空格子时，保持之前的高亮数字
                state.highlightedNumber
            }

            gameState = state.copy(
                selectedRow = newRow,
                selectedCol = newCol,
                highlightedNumber = newHighlightedNumber
            )
            autoSave()
        }
    }

    /**
     * 直接设置选中的格子（用于点击）
     */
    fun moveSelectionTo(row: Int, col: Int) {
        gameState?.let { state ->
            val newRow = row.coerceIn(0, 8)
            val newCol = col.coerceIn(0, 8)
            val selectedCell = state.board[newRow][newCol]

            // 只有选中的格子有数字时，才更新高亮数字
            val newHighlightedNumber = if (selectedCell.value != 0) {
                selectedCell.value
            } else {
                // 选中空格子时，保持之前的高亮数字
                state.highlightedNumber
            }

            gameState = state.copy(
                selectedRow = newRow,
                selectedCol = newCol,
                highlightedNumber = newHighlightedNumber
            )
            autoSave()
        }
    }

    /**
     * 设置当前选中格子的值
     */
    fun setValue(value: Int) {
        gameState?.let { state ->
            val row = state.selectedRow
            val col = state.selectedCol
            val cell = state.board[row][col]

            // 只能修改非初始格子
            if (cell.isInitial) return

            // 更新格子值，同时清空当前格子的备注
            var newBoard = state.board.mapIndexed { r, rowData ->
                rowData.mapIndexed { c, cellData ->
                    if (r == row && c == col) {
                        cellData.copy(value = value, notes = emptySet())
                    } else {
                        cellData
                    }
                }
            }

            // 如果填入了有效数字（非0），清除相关格子中的备注
            if (value != 0) {
                newBoard = clearInvalidNotes(newBoard, row, col, value)
            }

            // 检查是否有冲突
            val updatedBoard = newBoard.mapIndexed { r, rowData ->
                rowData.mapIndexed { c, cellData ->
                    cellData.copy(isError = SudokuGameManager.hasConflict(newBoard, r, c))
                }
            }

            // 检查是否完成
            val isComplete = SudokuGameManager.isComplete(updatedBoard)

            gameState = state.copy(
                board = updatedBoard,
                isComplete = isComplete,
                highlightedNumber = value  // 填写数字后，高亮新数字
            )

            // 如果游戏完成，暂停计时器、删除自动存档并记录统计
            if (isComplete) {
                pauseTimer()
                appContext?.let { context ->
                    SudokuStateStorage.clearSavedGame(context)
                    // 记录完成统计
                    gameState?.let { gs ->
                        com.brucewang.sudokuds.StatisticsManager.recordCompletion(context, gs.emptyCellsCount)
                    }
                }
            } else {
                autoSave()
            }
        }
    }

    /**
     * 切换当前选中格子的备注
     */
    fun toggleNote(number: Int) {
        gameState?.let { state ->
            val row = state.selectedRow
            val col = state.selectedCol
            val cell = state.board[row][col]

            // 只能修改非初始格子且未填写的格子
            if (cell.isInitial || cell.value != 0) return

            // 检查这个数字在当前位置是否合法
            if (!SudokuGameManager.isValidPlacement(
                    state.board.map { r -> r.map { it.value } },
                    row, col, number
                )) {
                // 如果不合法，不允许添加备注
                return
            }

            // 切换备注
            val newNotes = if (cell.notes.contains(number)) {
                cell.notes - number
            } else {
                cell.notes + number
            }

            val newBoard = state.board.mapIndexed { r, rowData ->
                rowData.mapIndexed { c, cellData ->
                    if (r == row && c == col) {
                        cellData.copy(notes = newNotes)
                    } else {
                        cellData
                    }
                }
            }

            gameState = state.copy(board = newBoard)
            autoSave()
        }
    }

    /**
     * 清除相关格子中会导致冲突的备注
     */
    private fun clearInvalidNotes(
        board: List<List<SudokuCell>>,
        row: Int,
        col: Int,
        value: Int
    ): List<List<SudokuCell>> {
        return board.mapIndexed { r, rowData ->
            rowData.mapIndexed { c, cellData ->
                // 如果是同一行、同一列或同一宫格，清除相应的备注
                val isSameRow = r == row
                val isSameCol = c == col
                val isSameBox = (r / 3 == row / 3) && (c / 3 == col / 3)

                if ((isSameRow || isSameCol || isSameBox) && cellData.notes.contains(value)) {
                    cellData.copy(notes = cellData.notes - value)
                } else {
                    cellData
                }
            }
        }
    }

    /**
     * 清除当前选中格子的值
     */
    fun clearValue() {
        setValue(0)
    }

    /**
     * 更新游戏时间
     */
    fun updateElapsedTime() {
        // 如果计时器暂停，不更新时间
        if (isTimerPaused) return

        gameState?.let { state ->
            // 当前会话时间 + 之前累计时间 = 总时间
            val currentSessionTime = (System.currentTimeMillis() - sessionStartTime) / 1000
            val totalTime = totalElapsedSeconds + currentSessionTime
            gameState = state.copy(elapsedTime = totalTime)
            autoSave()
        }
    }

    /**
     * 暂停计时器（当app失去焦点时调用）
     */
    fun pauseTimer() {
        if (isTimerPaused) return

        // 保存当前累计时间
        gameState?.let { state ->
            val currentSessionTime = (System.currentTimeMillis() - sessionStartTime) / 1000
            totalElapsedSeconds += currentSessionTime
            gameState = state.copy(elapsedTime = totalElapsedSeconds)
            autoSave()
        }

        isTimerPaused = true
    }

    /**
     * 恢复计时器（当app重新获得焦点时调用）
     */
    fun resumeTimer() {
        if (!isTimerPaused) return

        // 重新开始计时
        sessionStartTime = System.currentTimeMillis()
        isTimerPaused = false
    }

    /**
     * 格式化时间显示
     */
    fun formatTime(seconds: Long): String {
        val minutes = seconds / 60
        val secs = seconds % 60
        return String.format("%02d:%02d", minutes, secs)
    }

    /**
     * 手动存档（不保存时间）
     */
    fun manualSave(): Boolean {
        return appContext?.let { context ->
            gameState?.let { state ->
                SudokuStateStorage.saveManualGameState(context, state)
            } ?: false
        } ?: false
    }

    /**
     * 读取手动存档（时间继续走）
     */
    fun loadManualSave(): Boolean {
        return appContext?.let { context ->
            val loadedState = SudokuStateStorage.loadManualGameState(context)
            if (loadedState != null) {
                // 保持当前的时间，只恢复棋盘状态
                val currentTime = gameState?.elapsedTime ?: 0L
                gameState = loadedState.copy(elapsedTime = currentTime)
                autoSave()
                true
            } else {
                false
            }
        } ?: false
    }

    /**
     * 检查是否有手动存档
     */
    fun hasManualSave(): Boolean {
        return appContext?.let { SudokuStateStorage.hasManualSave(it) } ?: false
    }
}

