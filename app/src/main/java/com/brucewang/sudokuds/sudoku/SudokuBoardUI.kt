package com.brucewang.sudokuds.sudoku

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.brucewang.sudokuds.R
import kotlinx.coroutines.delay

/**
 * 数独游戏界面（副屏显示）
 */
@Composable
fun SudokuGameBoard(modifier: Modifier = Modifier) {
    val gameState = SudokuGame.gameState

    // 定时更新时间 - 游戏完成时停止计时
    LaunchedEffect(gameState?.isComplete) {
        while (gameState?.isComplete != true) {
            delay(1000)
            SudokuGame.updateElapsedTime()
        }
    }

    if (gameState == null) {
        // 显示欢迎界面
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = stringResource(R.string.sudoku_title),
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = stringResource(R.string.sudoku_press_to_start),
                    fontSize = 24.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }
    } else {
        Row(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 左侧：计时器、状态和数字完成指示器
            Column(
                modifier = Modifier
                    .width(180.dp)
                    .fillMaxHeight(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // 上部分：计时器和状态
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(48.dp))
                    Text(
                        text = stringResource(R.string.sudoku_time),
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = SudokuGame.formatTime(gameState.elapsedTime),
                        fontSize = 42.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    if (gameState.isComplete) {
                        Spacer(modifier = Modifier.height(48.dp))
                        Text(
                            text = stringResource(R.string.sudoku_complete),
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF4CAF50)  // 绿色
                        )
                    }
                }

                // 下部分：数字完成指示器
                NumberCompletionIndicator(gameState)
            }

            Spacer(modifier = Modifier.width(16.dp))

            // 中间：数独棋盘 - 使用可用空间绘制正方形棋盘
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize(0.7f)
                        .aspectRatio(1f, matchHeightConstraintsFirst = false)
                ) {
                    SudokuBoard(gameState)
                }
            }
        }
    }
}

/**
 * 绘制数独棋盘
 */
@Composable
private fun SudokuBoard(gameState: SudokuGameState) {
    // 获取主题颜色
    val primaryColor = MaterialTheme.colorScheme.primary
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val errorColor = MaterialTheme.colorScheme.error

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .padding(4.dp)
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    // 计算点击的格子位置
                    val cellSize = size.width / 9f
                    val col = (offset.x / cellSize).toInt().coerceIn(0, 8)
                    val row = (offset.y / cellSize).toInt().coerceIn(0, 8)

                    // 更新选中位置
                    SudokuGame.moveSelectionTo(row, col)
                }
            }
    ) {
        val cellSize = size.width / 9f

        // 获取当前高亮的数字
        val selectedRow = gameState.selectedRow
        val selectedCol = gameState.selectedCol
        val highlightedNumber = gameState.highlightedNumber

        // 绘制相同数字的高亮背景（优先级最低）
        if (highlightedNumber != 0) {
            gameState.board.forEachIndexed { row, rowData ->
                rowData.forEachIndexed { col, cell ->
                    if (cell.value == highlightedNumber) {
                        drawRect(
                            color = primaryColor.copy(alpha = 0.15f),
                            topLeft = Offset(col * cellSize, row * cellSize),
                            size = androidx.compose.ui.geometry.Size(cellSize, cellSize)
                        )
                    }
                }
            }
        }

        // 绘制选中格子高亮 - 使用主题颜色（覆盖在相同数字高亮之上）
        drawRect(
            color = primaryColor.copy(alpha = 0.3f),
            topLeft = Offset(selectedCol * cellSize, selectedRow * cellSize),
            size = androidx.compose.ui.geometry.Size(cellSize, cellSize)
        )

        // 绘制格子背景（错误高亮，优先级最高）
        gameState.board.forEachIndexed { row, rowData ->
            rowData.forEachIndexed { col, cell ->
                if (cell.isError) {
                    drawRect(
                        color = errorColor.copy(alpha = 0.3f),
                        topLeft = Offset(col * cellSize, row * cellSize),
                        size = androidx.compose.ui.geometry.Size(cellSize, cellSize)
                    )
                }
            }
        }

        // 绘制网格线
        for (i in 0..9) {
            val isThickLine = i % 3 == 0
            val strokeWidth = if (isThickLine) 5f else 2f
            val lineColor = if (isThickLine) onSurfaceColor else onSurfaceColor.copy(alpha = 0.5f)

            // 垂直线
            drawLine(
                color = lineColor,
                start = Offset(i * cellSize, 0f),
                end = Offset(i * cellSize, size.height),
                strokeWidth = strokeWidth
            )

            // 水平线
            drawLine(
                color = lineColor,
                start = Offset(0f, i * cellSize),
                end = Offset(size.width, i * cellSize),
                strokeWidth = strokeWidth
            )
        }

        // 绘制数字
        val textPaint = android.graphics.Paint().apply {
            textAlign = android.graphics.Paint.Align.CENTER
            textSize = cellSize * 0.6f
            isAntiAlias = true
        }

        // 绘制备注的文字画笔
        val notePaint = android.graphics.Paint().apply {
            textAlign = android.graphics.Paint.Align.CENTER
            textSize = cellSize * 0.3f  // 备注字体很小
            isAntiAlias = true
            color = onSurfaceColor.copy(alpha = 0.6f).toArgb()
        }

        gameState.board.forEachIndexed { row, rowData ->
            rowData.forEachIndexed { col, cell ->
                if (cell.value != 0) {
                    // 绘制填写的数字
                    textPaint.color = when {
                        cell.isError -> errorColor.toArgb()
                        cell.isInitial -> onSurfaceColor.toArgb()
                        else -> primaryColor.toArgb()
                    }
                    textPaint.isFakeBoldText = cell.isInitial

                    val x = col * cellSize + cellSize / 2
                    val y = row * cellSize + cellSize / 2 + cellSize * 0.2f

                    drawContext.canvas.nativeCanvas.drawText(
                        cell.value.toString(),
                        x,
                        y,
                        textPaint
                    )
                } else if (cell.notes.isNotEmpty()) {
                    // 绘制备注（3x3网格显示1-9）
                    cell.notes.forEach { note ->
                        val noteRow = (note - 1) / 3
                        val noteCol = (note - 1) % 3

                        val noteX = col * cellSize + cellSize / 6 + noteCol * cellSize / 3
                        val noteY = row * cellSize + cellSize / 6 + noteRow * cellSize / 3 + cellSize * 0.08f

                        drawContext.canvas.nativeCanvas.drawText(
                            note.toString(),
                            noteX,
                            noteY,
                            notePaint
                        )
                    }
                }
            }
        }
    }
}

/**
 * 数字完成指示器 - 显示1-9每个数字的完成状态
 */
@Composable
private fun NumberCompletionIndicator(gameState: SudokuGameState) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface

    // 计算每个数字（1-9）在棋盘上出现的次数
    val numberCounts = IntArray(10) { 0 }
    gameState.board.forEach { row ->
        row.forEach { cell ->
            if (cell.value in 1..9) {
                numberCounts[cell.value]++
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 显示3x3的数字网格（1-9）
        for (row in 0..2) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                for (col in 0..2) {
                    val number = row * 3 + col + 1
                    val isComplete = numberCounts[number] >= 9

                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                color = if (isComplete) primaryColor else Color.Transparent,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .then(
                                if (!isComplete) {
                                    Modifier.border(
                                        width = 2.dp,
                                        color = onSurfaceColor.copy(alpha = 0.3f),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                } else Modifier
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = number.toString(),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isComplete) {
                                MaterialTheme.colorScheme.onPrimary
                            } else {
                                onSurfaceColor.copy(alpha = 0.4f)
                            }
                        )
                    }
                }
            }
        }
    }
}
