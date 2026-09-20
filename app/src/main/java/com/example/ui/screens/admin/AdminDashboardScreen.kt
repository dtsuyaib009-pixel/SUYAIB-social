package com.example.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DynamicFeed
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.HistoryToggleOff
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.ChatBubbleOutline
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
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.ReportItem
import com.example.data.model.UserEntity
import com.example.ui.components.EmptyStateView
import com.example.ui.components.UserAvatar
import com.example.ui.components.VerifiedBadge
import com.example.ui.components.formatTimestamp
import com.example.ui.theme.CrimsonError
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.IndigoPrimary
import com.example.ui.viewmodel.SocialViewModel

@androidx.compose.material3.ExperimentalMaterial3Api
@Composable
fun AdminDashboardScreen(
    viewModel: SocialViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val stats by viewModel.platformStats.collectAsStateWithLifecycle()
    val reports by viewModel.adminReports.collectAsStateWithLifecycle()
    val allUsers by viewModel.allUsers.collectAsStateWithLifecycle()

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Reports, 1: Users, 2: Metrics

    LaunchedEffect(Unit) {
        viewModel.refreshPlatformStats()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("admin_dashboard_screen")
    ) {
        // Top App Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                Spacer(modifier = Modifier.width(4.dp))
                Icon(Icons.Default.Security, contentDescription = null, tint = IndigoPrimary)
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Admin Center",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Moderation & System Governance",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            IconButton(onClick = { viewModel.refreshPlatformStats() }) {
                Icon(Icons.Default.Refresh, contentDescription = "Refresh Stats")
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

        // Key Metrics Summary Cards
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatMetricBox(
                title = "Users",
                count = "${stats?.totalUsers ?: 0}",
                icon = Icons.Default.Group,
                color = IndigoPrimary,
                modifier = Modifier.weight(1f)
            )
            StatMetricBox(
                title = "Posts",
                count = "${stats?.totalPosts ?: 0}",
                icon = Icons.Default.DynamicFeed,
                color = EmeraldSuccess,
                modifier = Modifier.weight(1f)
            )
            StatMetricBox(
                title = "Reports",
                count = "${stats?.pendingReports ?: 0}",
                icon = Icons.Default.Warning,
                color = CrimsonError,
                modifier = Modifier.weight(1f)
            )
        }

        // Tabs
        PrimaryTabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Reports (${reports.size})") }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Users (${allUsers.size})") }
            )
        }

        when (selectedTab) {
            0 -> {
                // Reports Tab
                if (reports.isEmpty()) {
                    EmptyStateView(
                        title = "No Pending Reports",
                        subtitle = "All community reports have been reviewed and resolved.",
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = EmeraldSuccess,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(reports, key = { it.report.id }) { reportItem ->
                            AdminReportCard(
                                reportItem = reportItem,
                                onDismiss = { viewModel.adminResolveReport(reportItem.report.id, "DISMISSED") },
                                onRemoveContent = {
                                    if (reportItem.report.targetType == "POST") {
                                        viewModel.adminRemovePost(reportItem.report.targetId)
                                    } else if (reportItem.report.targetType == "COMMENT") {
                                        viewModel.adminRemoveComment(reportItem.report.targetId)
                                    }
                                    viewModel.adminResolveReport(reportItem.report.id, "REMOVED")
                                }
                            )
                        }
                    }
                }
            }
            1 -> {
                // Users Tab
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(allUsers, key = { it.id }) { user ->
                        AdminUserRow(
                            user = user,
                            onToggleSuspend = { viewModel.adminToggleSuspendUser(user.id) },
                            onDeleteUser = { viewModel.adminDeleteUser(user.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatMetricBox(
    title: String,
    count: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = count, fontWeight = FontWeight.Bold, fontSize = 20.sp)
            Text(text = title, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun AdminReportCard(
    reportItem: ReportItem,
    onDismiss: () -> Unit,
    onRemoveContent: () -> Unit
) {
    val report = reportItem.report
    val reporter = reportItem.reporter

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = CrimsonError.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "FLAGGED ${report.targetType}",
                        color = CrimsonError,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
                Text(
                    text = formatTimestamp(report.timestamp),
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Reason: ${report.reason}", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Text(
                text = "Target ID: ${report.targetId}",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (reporter != null) {
                Text(
                    text = "Reported by: @${reporter.username}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text("Dismiss", fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = onRemoveContent,
                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonError),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text("Take Action", color = Color.White, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun AdminUserRow(
    user: UserEntity,
    onToggleSuspend: () -> Unit,
    onDeleteUser: () -> Unit
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            UserAvatar(
                avatarUrl = user.avatarUrl,
                displayName = user.displayName,
                size = 40.dp
            )

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(user.displayName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    if (user.isVerified) {
                        Spacer(modifier = Modifier.width(4.dp))
                        VerifiedBadge(size = 12.dp)
                    }
                    if (user.isAdmin) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(Icons.Default.Shield, contentDescription = "Admin", tint = IndigoPrimary, modifier = Modifier.size(13.dp))
                    }
                }
                Text("@${user.username} • ${user.postsCount} posts", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (user.isSuspended) {
                    Text("ACCOUNT SUSPENDED", fontSize = 10.sp, color = CrimsonError, fontWeight = FontWeight.Bold)
                }
            }

            OutlinedButton(
                onClick = onToggleSuspend,
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = if (user.isSuspended) "Unsuspend" else "Suspend",
                    color = if (user.isSuspended) EmeraldSuccess else CrimsonError,
                    fontSize = 11.sp
                )
            }

            IconButton(onClick = { showDeleteConfirm = true }, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = CrimsonError, modifier = Modifier.size(18.dp))
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete User") },
            text = { Text("Permanently remove @${user.username} and all their posts and activity?") },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteUser()
                        showDeleteConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonError)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
