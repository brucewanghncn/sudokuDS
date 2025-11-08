package com.brucewang.sudokuds

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.brucewang.sudokuds.ui.theme.DualScreenTheme

class StatisticsActivity : ComponentActivity() {
    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LanguageManager.wrap(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            DualScreenTheme {
                StatisticsScreen(
                    onBackClick = {
                        ActivityLifecycleSync.isSettingsOpen = false
                        finish()
                    }
                )
            }

            // 处理系统返回按钮
            onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    ActivityLifecycleSync.isSettingsOpen = false
                    finish()
                }
            })
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // 确保在Activity销毁时重置标志
        ActivityLifecycleSync.isSettingsOpen = false
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(onBackClick: () -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var statistics by remember { mutableStateOf(StatisticsManager.getStatistics(context)) }
    var showResetDialog by remember { mutableStateOf(false) }
    var showResetSuccess by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.statistics_title)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.settings_back)
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
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.statistics_completed_games),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            if (statistics.totalCompleted == 0) {
                // 无数据提示
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.statistics_no_data),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                // 统计卡片
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    StatisticsCard(
                        title = stringResource(R.string.statistics_easy_completed),
                        count = statistics.easyCompleted,
                        color = MaterialTheme.colorScheme.primaryContainer
                    )

                    StatisticsCard(
                        title = stringResource(R.string.statistics_medium_completed),
                        count = statistics.mediumCompleted,
                        color = MaterialTheme.colorScheme.secondaryContainer
                    )

                    StatisticsCard(
                        title = stringResource(R.string.statistics_hard_completed),
                        count = statistics.hardCompleted,
                        color = MaterialTheme.colorScheme.tertiaryContainer
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        thickness = 2.dp
                    )

                    StatisticsCard(
                        title = stringResource(R.string.statistics_total_completed),
                        count = statistics.totalCompleted,
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        isTotal = true
                    )
                }
            }

            // 重置按钮
            if (statistics.totalCompleted > 0) {
                Button(
                    onClick = { showResetDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    )
                ) {
                    Text(stringResource(R.string.statistics_reset))
                }
            }

            // 提示消息
            if (showResetSuccess) {
                LaunchedEffect(Unit) {
                    kotlinx.coroutines.delay(2000)
                    showResetSuccess = false
                }

                Text(
                    text = stringResource(R.string.statistics_reset_success),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 8.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        // 重置确认对话框
        if (showResetDialog) {
            AlertDialog(
                onDismissRequest = { showResetDialog = false },
                title = { Text(stringResource(R.string.statistics_reset)) },
                text = { Text(stringResource(R.string.statistics_reset_confirm)) },
                confirmButton = {
                    TextButton(
                        onClick = {
                            StatisticsManager.resetStatistics(context)
                            statistics = StatisticsManager.getStatistics(context)
                            showResetDialog = false
                            showResetSuccess = true
                        }
                    ) {
                        Text(stringResource(R.string.statistics_confirm))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showResetDialog = false }) {
                        Text(stringResource(R.string.statistics_cancel))
                    }
                }
            )
        }
    }
}

@Composable
fun StatisticsCard(
    title: String,
    count: Int,
    color: androidx.compose.ui.graphics.Color,
    isTotal: Boolean = false
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = color)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = if (isTotal) FontWeight.Bold else FontWeight.Normal
            )

            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = count.toString(),
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    fontSize = if (isTotal) 40.sp else 36.sp
                )
                Text(
                    text = stringResource(R.string.statistics_games),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
        }
    }
}

