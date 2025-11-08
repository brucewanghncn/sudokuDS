package com.brucewang.sudokuds.sudoku

/**
 * 数独游戏数据模型
 */
data class SudokuCell(
    val row: Int,
    val col: Int,
    val value: Int,          // 当前值（0表示空）
    val isInitial: Boolean,  // 是否是初始题目的一部分
    val isError: Boolean = false,  // 是否有错误
    val notes: Set<Int> = emptySet()  // 备注的可能数字（1-9）
)

/**
 * 数独游戏状态
 */
data class SudokuGameState(
    val board: List<List<SudokuCell>>,  // 9x9的棋盘
    val selectedRow: Int = 0,             // 当前选中的行
    val selectedCol: Int = 0,             // 当前选中的列
    val elapsedTime: Long = 0L,           // 已用时间（秒）
    val isComplete: Boolean = false,      // 是否完成
    val mistakes: Int = 0,                // 错误次数
    val highlightedNumber: Int = 0,       // 当前高亮的数字（0表示无高亮）
    val emptyCellsCount: Int = 40         // 初始空格数量（难度：30简单/40中等/50困难）
)

