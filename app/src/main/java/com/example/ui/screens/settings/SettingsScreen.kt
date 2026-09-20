package com.example.ui.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SwitchAccount
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.UserAvatar
import com.example.ui.theme.CrimsonError
import com.example.ui.theme.IndigoPrimary
import com.example.ui.viewmodel.ScreenNav
import com.example.ui.viewmodel.SocialViewModel
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    viewModel: SocialViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val isDark by viewModel.isDarkTheme.collectAsStateWithLifecycle()
    val blockedUsers by viewModel.blockedUsers.collectAsStateWithLifecycle()
    val allUsers by viewModel.allUsers.collectAsStateWithLifecycle()

    var showChangePasswordDialog by remember { mutableStateOf(false) }
    var showBlockedUsersDialog by remember { mutableStateOf(false) }
    var showSwitchAccountDialog by remember { mutableStateOf(false) }
    var showDeleteAccountConfirm by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("settings_screen")
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Settings & Privacy",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Preferences section
            Text("Preferences", style = MaterialTheme.typography.labelLarge, color = IndigoPrimary, fontWeight = FontWeight.Bold)

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Brightness4, contentDescription = null, tint = IndigoPrimary)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Dark Theme", fontWeight = FontWeight.Medium)
                        }
                        Switch(
                            checked = isDark,
                            onCheckedChange = { viewModel.toggleTheme() },
                            modifier = Modifier.testTag("theme_switch")
                        )
                    }
                }
            }

            // Security & Privacy section
            Text("Account & Security", style = MaterialTheme.typography.labelLarge, color = IndigoPrimary, fontWeight = FontWeight.Bold)

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column {
                    SettingsActionRow(
                        icon = Icons.Default.Lock,
                        title = "Change Password",
                        subtitle = "Update your account password securely",
                        onClick = { showChangePasswordDialog = true }
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 14.dp))
                    SettingsActionRow(
                        icon = Icons.Default.Block,
                        title = "Blocked Users (${blockedUsers.size})",
                        subtitle = "Manage restricted and blocked accounts",
                        onClick = { showBlockedUsersDialog = true }
                    )
                    if (currentUser?.isAdmin == true) {
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 14.dp))
                        SettingsActionRow(
                            icon = Icons.Default.AdminPanelSettings,
                            title = "Admin Dashboard",
                            subtitle = "Access moderation controls and platform telemetry",
                            onClick = { viewModel.navigateTo(ScreenNav.AdminDashboard) }
                        )
                    }
                }
            }

            // Demo / Account Management
            Text("Demo & Session", style = MaterialTheme.typography.labelLarge, color = IndigoPrimary, fontWeight = FontWeight.Bold)

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column {
                    SettingsActionRow(
                        icon = Icons.Default.SwitchAccount,
                        title = "Switch Demo Account",
                        subtitle = "Test interactions as Suyaib, Admin, or Elena",
                        onClick = { showSwitchAccountDialog = true }
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 14.dp))
                    SettingsActionRow(
                        icon = Icons.Default.Logout,
                        title = "Log Out",
                        subtitle = "Sign out from this device",
                        onClick = { viewModel.logout() }
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 14.dp))
                    SettingsActionRow(
                        icon = Icons.Default.DeleteForever,
                        title = "Delete Account",
                        subtitle = "Permanently remove your profile and all data",
                        onClick = { showDeleteAccountConfirm = true },
                        titleColor = CrimsonError
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }

    // Change Password Dialog
    if (showChangePasswordDialog) {
        var currentPass by remember { mutableStateOf("") }
        var newPass by remember { mutableStateOf("") }
        var confirmNewPass by remember { mutableStateOf("") }
        var passError by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = { showChangePasswordDialog = false },
            title = { Text("Change Password", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    if (passError != null) {
                        Text(passError!!, color = CrimsonError, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                    }
                    OutlinedTextField(
                        value = currentPass,
                        onValueChange = { currentPass = it },
                        label = { Text("Current Password") },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newPass,
                        onValueChange = { newPass = it },
                        label = { Text("New Password (min 6 chars)") },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = confirmNewPass,
                        onValueChange = { confirmNewPass = it },
                        label = { Text("Confirm New Password") },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (newPass != confirmNewPass) {
                        passError = "New passwords do not match"
                        return@Button
                    }
                    val user = currentUser ?: return@Button
                    scope.launch {
                        val res = viewModel.repository.changePassword(user.id, currentPass, newPass)
                        res.onSuccess {
                            showChangePasswordDialog = false
                            viewModel.showToast("Password updated successfully")
                        }.onFailure {
                            passError = it.message
                        }
                    }
                }) {
                    Text("Change")
                }
            },
            dismissButton = {
                TextButton(onClick = { showChangePasswordDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Blocked Users Dialog
    if (showBlockedUsersDialog) {
        AlertDialog(
            onDismissRequest = { showBlockedUsersDialog = false },
            title = { Text("Blocked Users", fontWeight = FontWeight.Bold) },
            text = {
                if (blockedUsers.isEmpty()) {
                    Text("You have not blocked any users.")
                } else {
                    LazyColumn(modifier = Modifier.height(200.dp)) {
                        items(blockedUsers) { user ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    UserAvatar(avatarUrl = user.avatarUrl, displayName = user.displayName, size = 32.dp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("@${user.username}", fontSize = 13.sp)
                                }
                                OutlinedButton(onClick = { viewModel.unblockUser(user.id) }) {
                                    Text("Unblock", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showBlockedUsersDialog = false }) {
                    Text("Done")
                }
            }
        )
    }

    // Switch Demo Account Dialog
    if (showSwitchAccountDialog) {
        AlertDialog(
            onDismissRequest = { showSwitchAccountDialog = false },
            title = { Text("Switch Account (Demo)", fontWeight = FontWeight.Bold) },
            text = {
                LazyColumn(modifier = Modifier.height(240.dp)) {
                    items(allUsers) { user ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    showSwitchAccountDialog = false
                                    viewModel.switchUser(user.id)
                                }
                                .padding(vertical = 8.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            UserAvatar(avatarUrl = user.avatarUrl, displayName = user.displayName, size = 36.dp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(user.displayName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("@${user.username} ${if (user.isAdmin) "• Admin" else ""}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showSwitchAccountDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Delete Account Confirmation
    if (showDeleteAccountConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteAccountConfirm = false },
            title = { Text("Delete Account") },
            text = { Text("This will permanently delete your account, posts, and comments. This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        val uid = currentUser?.id ?: return@Button
                        scope.launch {
                            viewModel.repository.deleteAccount(uid)
                            showDeleteAccountConfirm = false
                            viewModel.showToast("Account deleted.")
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonError)
                ) {
                    Text("Permanently Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAccountConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun SettingsActionRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    titleColor: Color = Color.Unspecified
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = if (titleColor != Color.Unspecified) titleColor else IndigoPrimary)
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = titleColor)
            Text(text = subtitle, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        )
    }
}
