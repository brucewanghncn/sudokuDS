package com.brucewang.sudokuds.sudoku

import kotlin.random.Random

/**
 * 数独游戏逻辑管理器
 */
object SudokuGameManager {

    /**
     * 生成一个新的数独游戏
     * @param difficulty 难度（移除的格子数：简单30，中等40，困难50）
     */
    fun generateNewGame(difficulty: Int = 40): List<List<SudokuCell>> {
        // 生成一个完整的已解数独
        val solution = generateCompleteSudoku()

        // 根据难度移除一些数字
        val board = solution.map { it.toMutableList() }
        val cellsToRemove = difficulty
        var removed = 0

        while (removed < cellsToRemove) {
            val row = Random.nextInt(9)
            val col = Random.nextInt(9)

            if (board[row][col] != 0) {
                board[row][col] = 0
                removed++
            }
        }

        // 转换为 SudokuCell 对象
        return board.mapIndexed { row, rowData ->
            rowData.mapIndexed { col, value ->
                SudokuCell(
                    row = row,
                    col = col,
                    value = value,
                    isInitial = value != 0
                )
            }
        }
    }

    /**
     * 生成一个完整的已解数独
     */
    private fun generateCompleteSudoku(): List<List<Int>> {
        val board = List(9) { MutableList(9) { 0 } }
        fillBoard(board)
        return board
    }

    /**
     * 递归填充数独板
     */
    private fun fillBoard(board: List<MutableList<Int>>): Boolean {
        for (row in 0..8) {
            for (col in 0..8) {
                if (board[row][col] == 0) {
                    val numbers = (1..9).shuffled()
                    for (num in numbers) {
                        if (isValidPlacement(board, row, col, num)) {
                            board[row][col] = num
                            if (fillBoard(board)) {
                                return true
                            }
                            board[row][col] = 0
                        }
                    }
                    return false
                }
            }
        }
        return true
    }

    /**
     * 检查在指定位置放置数字是否有效
     */
    fun isValidPlacement(board: List<List<Int>>, row: Int, col: Int, num: Int): Boolean {
        // 检查行
        for (c in 0..8) {
            if (board[row][c] == num) return false
        }

        // 检查列
        for (r in 0..8) {
            if (board[r][col] == num) return false
        }

        // 检查3x3宫格
        val boxRow = (row / 3) * 3
        val boxCol = (col / 3) * 3
        for (r in boxRow until boxRow + 3) {
            for (c in boxCol until boxCol + 3) {
                if (board[r][c] == num) return false
            }
        }

        return true
    }

    /**
     * 检查数独是否完成且正确
     */
    fun isComplete(board: List<List<SudokuCell>>): Boolean {
        // 检查是否所有格子都填满
        if (board.any { row -> row.any { it.value == 0 } }) {
            return false
        }

        // 检查所有行、列、宫格是否有效
        val intBoard = board.map { row -> row.map { it.value } }

        for (i in 0..8) {
            // 检查行
            if (intBoard[i].toSet().size != 9) return false

            // 检查列
            if (intBoard.map { it[i] }.toSet().size != 9) return false

            // 检查宫格
            val boxRow = (i / 3) * 3
            val boxCol = (i % 3) * 3
            val box = mutableListOf<Int>()
            for (r in boxRow until boxRow + 3) {
                for (c in boxCol until boxCol + 3) {
                    box.add(intBoard[r][c])
                }
            }
            if (box.toSet().size != 9) return false
        }

        return true
    }

    /**
     * 检查特定格子是否有冲突
     */
    fun hasConflict(board: List<List<SudokuCell>>, row: Int, col: Int): Boolean {
        val value = board[row][col].value
        if (value == 0) return false

        val intBoard = board.map { r -> r.map { it.value } }

        // 检查行
        var count = 0
        for (c in 0..8) {
            if (intBoard[row][c] == value) count++
        }
        if (count > 1) return true

        // 检查列
        count = 0
        for (r in 0..8) {
            if (intBoard[r][col] == value) count++
        }
        if (count > 1) return true

        // 检查宫格
        count = 0
        val boxRow = (row / 3) * 3
        val boxCol = (col / 3) * 3
        for (r in boxRow until boxRow + 3) {
            for (c in boxCol until boxCol + 3) {
                if (intBoard[r][c] == value) count++
            }
        }
        if (count > 1) return true

        return false
    }
}

