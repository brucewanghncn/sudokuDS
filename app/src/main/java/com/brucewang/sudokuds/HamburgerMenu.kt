package com.brucewang.sudokuds

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * 汉堡菜单组件
 */
@Composable
fun HamburgerMenu(
    onDismiss: () -> Unit,
    onSettings: () -> Unit,
    onSwapScreens: () -> Unit,
    onLanguageSwitch: () -> Unit,
    onInstructions: () -> Unit,
    onExit: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = stringResource(R.string.menu_title), fontWeight = FontWeight.Bold)
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Settings, contentDescription = stringResource(R.string.menu_settings)) },
                    label = { Text(stringResource(R.string.menu_settings)) },
                    selected = false,
                    onClick = onSettings
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Refresh, contentDescription = stringResource(R.string.menu_swap_screens)) },
                    label = { Text(stringResource(R.string.menu_swap_screens)) },
                    selected = false,
                    onClick = onSwapScreens
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Info, contentDescription = stringResource(R.string.menu_switch_language)) },
                    label = { Text(stringResource(R.string.menu_switch_language)) },
                    selected = false,
                    onClick = onLanguageSwitch
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Notifications, contentDescription = stringResource(R.string.menu_instructions)) },
                    label = { Text(stringResource(R.string.menu_instructions)) },
                    selected = false,
                    onClick = onInstructions
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                NavigationDrawerItem(
                    icon = { Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = stringResource(R.string.menu_exit)) },
                    label = { Text(stringResource(R.string.menu_exit)) },
                    selected = false,
                    onClick = onExit
                )
            }
        },
        confirmButton = {}
    )
}

