package com.brucewang.sudokuds.sudoku

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * 游戏状态存储管理器 - 使用 SharedPreferences 保存和恢复游戏状态
 */
object SudokuStateStorage {
    private const val PREFS_NAME = "sudoku_game_state"

    // 自动存档的键
    private const val KEY_HAS_SAVED_GAME = "has_saved_game"
    private const val KEY_BOARD = "board"
    private const val KEY_SELECTED_ROW = "selected_row"
    private const val KEY_SELECTED_COL = "selected_col"
    private const val KEY_ELAPSED_TIME = "elapsed_time"
    private const val KEY_START_TIME = "start_time"
    private const val KEY_MISTAKES = "mistakes"
    private const val KEY_HIGHLIGHTED_NUMBER = "highlighted_number"
    private const val KEY_EMPTY_CELLS_COUNT = "empty_cells_count"

    // 手动存档的键
    private const val KEY_MANUAL_HAS_SAVED = "manual_has_saved"
    private const val KEY_MANUAL_BOARD = "manual_board"
    private const val KEY_MANUAL_SELECTED_ROW = "manual_selected_row"
    private const val KEY_MANUAL_SELECTED_COL = "manual_selected_col"
    private const val KEY_MANUAL_MISTAKES = "manual_mistakes"
    private const val KEY_MANUAL_HIGHLIGHTED_NUMBER = "manual_highlighted_number"
    private const val KEY_MANUAL_EMPTY_CELLS_COUNT = "manual_empty_cells_count"

    private val gson = Gson()

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    /**
     * 保存游戏状态
     */
    fun saveGameState(context: Context, gameState: SudokuGameState, startTime: Long) {
        val prefs = getPrefs(context)
        val editor = prefs.edit()

        // 序列化棋盘数据
        val boardJson = gson.toJson(gameState.board)

        editor.putBoolean(KEY_HAS_SAVED_GAME, true)
        editor.putString(KEY_BOARD, boardJson)
        editor.putInt(KEY_SELECTED_ROW, gameState.selectedRow)
        editor.putInt(KEY_SELECTED_COL, gameState.selectedCol)
        editor.putLong(KEY_ELAPSED_TIME, gameState.elapsedTime)
        editor.putLong(KEY_START_TIME, startTime)
        editor.putInt(KEY_MISTAKES, gameState.mistakes)
        editor.putInt(KEY_HIGHLIGHTED_NUMBER, gameState.highlightedNumber)
        editor.putInt(KEY_EMPTY_CELLS_COUNT, gameState.emptyCellsCount)

        editor.apply()
    }

    /**
     * 加载游戏状态
     */
    fun loadGameState(context: Context): Pair<SudokuGameState, Long>? {
        val prefs = getPrefs(context)

        if (!prefs.getBoolean(KEY_HAS_SAVED_GAME, false)) {
            return null
        }

        val boardJson = prefs.getString(KEY_BOARD, null) ?: return null

        try {
            // 反序列化棋盘数据
            val type = object : TypeToken<List<List<SudokuCell>>>() {}.type
            val board: List<List<SudokuCell>> = gson.fromJson(boardJson, type)

            val selectedRow = prefs.getInt(KEY_SELECTED_ROW, 0)
            val selectedCol = prefs.getInt(KEY_SELECTED_COL, 0)
            val elapsedTime = prefs.getLong(KEY_ELAPSED_TIME, 0L)
            val startTime = prefs.getLong(KEY_START_TIME, System.currentTimeMillis())
            val mistakes = prefs.getInt(KEY_MISTAKES, 0)
            val highlightedNumber = prefs.getInt(KEY_HIGHLIGHTED_NUMBER, 0)
            val emptyCellsCount = prefs.getInt(KEY_EMPTY_CELLS_COUNT, 40)

            // 重新检查完成状态和错误状态
            val updatedBoard = board.mapIndexed { r, rowData ->
                rowData.mapIndexed { c, cellData ->
                    cellData.copy(isError = SudokuGameManager.hasConflict(board, r, c))
                }
            }

            val isComplete = SudokuGameManager.isComplete(updatedBoard)

            val gameState = SudokuGameState(
                board = updatedBoard,
                selectedRow = selectedRow,
                selectedCol = selectedCol,
                elapsedTime = elapsedTime,
                isComplete = isComplete,
                mistakes = mistakes,
                highlightedNumber = highlightedNumber,
                emptyCellsCount = emptyCellsCount
            )

            return Pair(gameState, startTime)
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    /**
     * 检查是否有保存的游戏
     */
    fun hasSavedGame(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_HAS_SAVED_GAME, false)
    }

    /**
     * 清除保存的游戏
     */
    fun clearSavedGame(context: Context) {
        val editor = getPrefs(context).edit()
        editor.clear()
        editor.apply()
    }

    /**
     * 手动存档游戏状态（不保存时间）
     */
    fun saveManualGameState(context: Context, gameState: SudokuGameState): Boolean {
        return try {
            val prefs = getPrefs(context)
            val editor = prefs.edit()

            // 序列化棋盘数据
            val boardJson = gson.toJson(gameState.board)

            editor.putBoolean(KEY_MANUAL_HAS_SAVED, true)
            editor.putString(KEY_MANUAL_BOARD, boardJson)
            editor.putInt(KEY_MANUAL_SELECTED_ROW, gameState.selectedRow)
            editor.putInt(KEY_MANUAL_SELECTED_COL, gameState.selectedCol)
            editor.putInt(KEY_MANUAL_MISTAKES, gameState.mistakes)
            editor.putInt(KEY_MANUAL_HIGHLIGHTED_NUMBER, gameState.highlightedNumber)
            editor.putInt(KEY_MANUAL_EMPTY_CELLS_COUNT, gameState.emptyCellsCount)

            editor.apply()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * 加载手动存档（不恢复时间）
     */
    fun loadManualGameState(context: Context): SudokuGameState? {
        val prefs = getPrefs(context)

        if (!prefs.getBoolean(KEY_MANUAL_HAS_SAVED, false)) {
            return null
        }

        val boardJson = prefs.getString(KEY_MANUAL_BOARD, null) ?: return null

        return try {
            // 反序列化棋盘数据
            val type = object : TypeToken<List<List<SudokuCell>>>() {}.type
            val board: List<List<SudokuCell>> = gson.fromJson(boardJson, type)

            val selectedRow = prefs.getInt(KEY_MANUAL_SELECTED_ROW, 0)
            val selectedCol = prefs.getInt(KEY_MANUAL_SELECTED_COL, 0)
            val mistakes = prefs.getInt(KEY_MANUAL_MISTAKES, 0)
            val highlightedNumber = prefs.getInt(KEY_MANUAL_HIGHLIGHTED_NUMBER, 0)
            val emptyCellsCount = prefs.getInt(KEY_MANUAL_EMPTY_CELLS_COUNT, 40)

            // 重新检查完成状态和错误状态
            val updatedBoard = board.mapIndexed { r, rowData ->
                rowData.mapIndexed { c, cellData ->
                    cellData.copy(isError = SudokuGameManager.hasConflict(board, r, c))
                }
            }

            val isComplete = SudokuGameManager.isComplete(updatedBoard)

            // 注意：不恢复时间，时间由调用者管理
            SudokuGameState(
                board = updatedBoard,
                selectedRow = selectedRow,
                selectedCol = selectedCol,
                elapsedTime = 0L,  // 时间由调用者设置
                isComplete = isComplete,
                mistakes = mistakes,
                highlightedNumber = highlightedNumber,
                emptyCellsCount = emptyCellsCount
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * 检查是否有手动存档
     */
    fun hasManualSave(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_MANUAL_HAS_SAVED, false)
    }

    /**
     * 清除手动存档
     */
    fun clearManualSave(context: Context) {
        val editor = getPrefs(context).edit()
        editor.remove(KEY_MANUAL_HAS_SAVED)
        editor.remove(KEY_MANUAL_BOARD)
        editor.remove(KEY_MANUAL_SELECTED_ROW)
        editor.remove(KEY_MANUAL_SELECTED_COL)
        editor.remove(KEY_MANUAL_MISTAKES)
        editor.apply()
    }
}

