package com.brucewang.sudokuds

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.brucewang.sudokuds.ui.theme.DualScreenTheme

class InstructionsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 应用语言设置
        val language = LanguageManager.getSavedLanguage(this)
        LanguageManager.applyLanguage(this, language)

        enableEdgeToEdge()

        setContent {
            DualScreenTheme {
                InstructionsScreen(
                    onBackClick = {
                        finish()
                    }
                )
            }

            // 处理系统返回按钮
            onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    finish()
                }
            })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InstructionsScreen(onBackClick: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.instructions_title)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // 手柄操作
            InstructionSection(
                title = stringResource(R.string.instructions_gamepad),
                items = listOf(
                    stringResource(R.string.instructions_gamepad_dpad),
                    stringResource(R.string.instructions_gamepad_buttons),
                    stringResource(R.string.instructions_gamepad_back)
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 游戏说明
            InstructionSection(
                title = stringResource(R.string.instructions_game),
                items = listOf(
                    stringResource(R.string.instructions_game_rule),
                    stringResource(R.string.instructions_game_difficulty),
                    stringResource(R.string.instructions_game_note),
                    stringResource(R.string.instructions_game_save)
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 双屏说明
            InstructionSection(
                title = stringResource(R.string.instructions_screens),
                items = listOf(
                    stringResource(R.string.instructions_screens_secondary),
                    stringResource(R.string.instructions_screens_main)
                )
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun InstructionSection(
    title: String,
    items: List<String>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            items.forEachIndexed { index, item ->
                Row(
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Text(
                        text = "• ",
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = item,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 24.sp
                    )
                }
            }
        }
    }
}

