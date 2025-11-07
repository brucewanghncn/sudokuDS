package com.brucewang.sudokuds.sudoku

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.brucewang.sudokuds.R

/**
 * 数字选择界面（主屏显示）
 */
@Composable
fun SudokuControlPanel(modifier: Modifier = Modifier) {
    val gameState = SudokuGame.gameState
    val hasSavedGame = SudokuGame.hasSavedGame()
    val hasManualSave = SudokuGame.hasManualSave()


    // 用于显示提示消息
    var showSaveMessage by remember { mutableStateOf(false) }
    var showLoadMessage by remember { mutableStateOf(false) }
    var saveSuccessful by remember { mutableStateOf(false) }

    // 消息自动消失
    LaunchedEffect(showSaveMessage) {
        if (showSaveMessage) {
            kotlinx.coroutines.delay(2000)
            showSaveMessage = false
        }
    }

    LaunchedEffect(showLoadMessage) {
        if (showLoadMessage) {
            kotlinx.coroutines.delay(2000)
            showLoadMessage = false
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        if (gameState == null) {
            // 显示难度选择和开始按钮
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(16.dp)
            ) {
                // 标题
                Text(
                    text = stringResource(R.string.sudoku_title),
                    fontSize = 42.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(32.dp))

                // 难度选择标题
                Text(
                    text = stringResource(R.string.sudoku_difficulty),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(24.dp))

                // 难度按钮
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 简单
                    Button(
                        onClick = { SudokuGame.startNewGame(difficulty = 30) },
                        modifier = Modifier
                            .width(160.dp)
                            .height(100.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "😊",
                                fontSize = 36.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = stringResource(R.string.sudoku_easy),
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = stringResource(R.string.sudoku_empty_cells_30),
                                fontSize = 14.sp
                            )
                        }
                    }

                    // 中等
                    Button(
                        onClick = { SudokuGame.startNewGame(difficulty = 40) },
                        modifier = Modifier
                            .width(160.dp)
                            .height(100.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary
                        )
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "🤔",
                                fontSize = 36.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = stringResource(R.string.sudoku_medium),
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = stringResource(R.string.sudoku_empty_cells_40),
                                fontSize = 14.sp
                            )
                        }
                    }

                    // 困难
                    Button(
                        onClick = { SudokuGame.startNewGame(difficulty = 50) },
                        modifier = Modifier
                            .width(160.dp)
                            .height(100.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.tertiary
                        )
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "😰",
                                fontSize = 36.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = stringResource(R.string.sudoku_hard),
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = stringResource(R.string.sudoku_empty_cells_50),
                                fontSize = 14.sp
                            )
                        }
                    }
                }

                // 如果有存档，显示继续游戏按钮
                if (hasSavedGame) {
                    Spacer(modifier = Modifier.height(32.dp))

                    Button(
                        onClick = { SudokuGame.continueGame() },
                        modifier = Modifier
                            .width(280.dp)
                            .height(80.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Text(
                            text = stringResource(R.string.sudoku_continue_game),
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // 顶部：功能按钮行
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    // 重新开始按钮
                    Button(
                        onClick = { SudokuGame.startNewGame() },
                        modifier = Modifier
                            .weight(1f)
                            .height(80.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary
                        )
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                Icons.Default.Refresh,
                                contentDescription = stringResource(R.string.sudoku_new_game),
                                modifier = Modifier.size(32.dp)
                            )
                            Text(stringResource(R.string.sudoku_new_game), fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // 手动存档按钮
                    Button(
                        onClick = {
                            saveSuccessful = SudokuGame.manualSave()
                            showSaveMessage = true
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(80.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.tertiary
                        )
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "💾",
                                fontSize = 32.sp
                            )
                            Text(stringResource(R.string.sudoku_save), fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // 读取存档按钮
                    Button(
                        onClick = {
                            saveSuccessful = SudokuGame.loadManualSave()
                            showLoadMessage = true
                        },
                        enabled = hasManualSave,
                        modifier = Modifier
                            .weight(1f)
                            .height(80.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.tertiary
                        )
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "📂",
                                fontSize = 32.sp
                            )
                            Text(stringResource(R.string.sudoku_load), fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // 擦除按钮
                    Button(
                        onClick = { SudokuGame.clearValue() },
                        modifier = Modifier
                            .weight(1f)
                            .height(80.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                Icons.Default.Clear,
                                contentDescription = stringResource(R.string.sudoku_erase),
                                modifier = Modifier.size(32.dp)
                            )
                            Text(stringResource(R.string.sudoku_erase), fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // 底部：两个键盘并排
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(32.dp, Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 左侧：填写键盘
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = stringResource(R.string.sudoku_note_mode),
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.tertiary
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // 数字按钮 1-9（3x3网格）
                        Column(
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            for (row in 0..2) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    for (col in 0..2) {
                                        val number = row * 3 + col + 1
                                        NumberButton(
                                            number = number,
                                            onClick = { SudokuGame.toggleNote(number) },
                                            buttonColor = MaterialTheme.colorScheme.tertiaryContainer,
                                            borderColor = MaterialTheme.colorScheme.tertiary,
                                            textColor = MaterialTheme.colorScheme.onTertiaryContainer
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // 右侧：备注键盘
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = stringResource(R.string.sudoku_fill_mode),
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // 数字按钮 1-9（3x3网格）
                        Column(
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            for (row in 0..2) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    for (col in 0..2) {
                                        val number = row * 3 + col + 1
                                        NumberButton(
                                            number = number,
                                            onClick = { SudokuGame.setValue(number) },
                                            buttonColor = MaterialTheme.colorScheme.primaryContainer,
                                            borderColor = MaterialTheme.colorScheme.primary,
                                            textColor = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // 提示消息
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    if (showSaveMessage) {
                        Text(
                            text = if (saveSuccessful)
                                stringResource(R.string.sudoku_save_success)
                            else
                                stringResource(R.string.sudoku_save_failed),
                            fontSize = 20.sp,
                            color = if (saveSuccessful)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.error
                        )
                    }

                    if (showLoadMessage) {
                        Text(
                            text = if (saveSuccessful)
                                stringResource(R.string.sudoku_load_success)
                            else
                                stringResource(R.string.sudoku_load_failed),
                            fontSize = 20.sp,
                            color = if (saveSuccessful)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

/**
 * 数字按钮
 */
@Composable
private fun NumberButton(
    number: Int,
    onClick: () -> Unit,
    buttonColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.primaryContainer,
    borderColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.primary,
    textColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onPrimaryContainer
) {
    Box(
        modifier = Modifier
            .size(100.dp)
            .background(
                color = buttonColor,
                shape = RoundedCornerShape(8.dp)
            )
            .border(
                width = 2.dp,
                color = borderColor,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = number.toString(),
            fontSize = 52.sp,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
    }
}

