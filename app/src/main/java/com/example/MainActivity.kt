package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.DynamicFeed
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.CommentSheet
import com.example.ui.components.CreateStoryDialog
import com.example.ui.components.ReportContentDialog
import com.example.ui.components.StoryViewerDialog
import com.example.ui.components.ToastNotificationBanner
import com.example.ui.components.UserAvatar
import com.example.ui.screens.admin.AdminDashboardScreen
import com.example.ui.screens.auth.AuthScreen
import com.example.ui.screens.create.CreatePostScreen
import com.example.ui.screens.explore.ExploreScreen
import com.example.ui.screens.feed.FeedScreen
import com.example.ui.screens.messages.ChatDetailScreen
import com.example.ui.screens.messages.ConversationsListScreen
import com.example.ui.screens.notifications.NotificationsScreen
import com.example.ui.screens.profile.ProfileScreen
import com.example.ui.screens.settings.SettingsScreen
import com.example.ui.theme.CrimsonError
import com.example.ui.theme.IndigoPrimary
import com.example.ui.theme.SuyaibGradient
import com.example.ui.theme.SuyaibSocialTheme
import com.example.ui.viewmodel.ScreenNav
import com.example.ui.viewmodel.SocialViewModel
import com.example.ui.viewmodel.SocialViewModelFactory

class MainActivity : ComponentActivity() {

    private val viewModel: SocialViewModel by viewModels {
        SocialViewModelFactory(application)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val isDark by viewModel.isDarkTheme.collectAsStateWithLifecycle()
            val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
            val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
            val toastMessage by viewModel.toastMessage.collectAsStateWithLifecycle()

            // Overlays state
            val activeCommentPost by viewModel.activeCommentPost.collectAsStateWithLifecycle()
            val activePostComments by viewModel.activePostComments.collectAsStateWithLifecycle()
            val activeStoryGroup by viewModel.activeStoryGroup.collectAsStateWithLifecycle()
            val showCreateStory by viewModel.showCreateStory.collectAsStateWithLifecycle()
            val showReportDialog by viewModel.showReportDialog.collectAsStateWithLifecycle()

            SuyaibSocialTheme(darkTheme = isDark) {
                // Intercept back button for nested screens
                BackHandler(enabled = currentScreen !is ScreenNav.Home) {
                    viewModel.navigateBack()
                }

                Box(modifier = Modifier.fillMaxSize()) {
                    if (currentUser == null) {
                        AuthScreen(viewModel = viewModel)
                    } else {
                        MainAppContent(
                            viewModel = viewModel,
                            currentScreen = currentScreen
                        )
                    }

                    // Global Toast Notification
                    ToastNotificationBanner(
                        message = toastMessage,
                        onDismiss = { viewModel.dismissToast() },
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 40.dp)
                    )

                    // Comments Modal Bottom Sheet
                    if (activeCommentPost != null) {
                        CommentSheet(
                            postItem = activeCommentPost!!,
                            comments = activePostComments,
                            currentUser = currentUser,
                            onDismiss = { viewModel.closeComments() },
                            onAddComment = { postId, content, parentId ->
                                viewModel.addComment(postId, content, parentId)
                            },
                            onDeleteComment = { commentId ->
                                viewModel.deleteComment(commentId)
                            },
                            onLikeComment = { commentId ->
                                viewModel.toggleLikeComment(commentId)
                            },
                            onUserClick = { userId ->
                                viewModel.closeComments()
                                viewModel.navigateTo(ScreenNav.UserProfile(userId))
                            }
                        )
                    }

                    // Story Viewer Fullscreen
                    if (activeStoryGroup != null) {
                        StoryViewerDialog(
                            storyGroup = activeStoryGroup!!,
                            onDismiss = { viewModel.closeStoryViewer() },
                            onDeleteStory = { storyId ->
                                viewModel.deleteStory(storyId)
                            }
                        )
                    }

                    // Create Story Dialog
                    if (showCreateStory) {
                        CreateStoryDialog(
                            onDismiss = { viewModel.showCreateStory.value = false },
                            onSubmit = { mediaUrl, caption, isVideo ->
                                viewModel.createStory(mediaUrl, caption, isVideo)
                            }
                        )
                    }

                    // Report Dialog
                    if (showReportDialog != null) {
                        val reportTarget = showReportDialog!!
                        ReportContentDialog(
                            targetType = reportTarget.first,
                            targetTitle = reportTarget.third,
                            onDismiss = { viewModel.showReportDialog.value = null },
                            onSubmit = { reason ->
                                viewModel.submitReport(reason)
                            }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppContent(
    viewModel: SocialViewModel,
    currentScreen: ScreenNav,
    modifier: Modifier = Modifier
) {
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val unreadNotifsCount by viewModel.unreadNotificationsCount.collectAsStateWithLifecycle()
    val conversations by viewModel.conversations.collectAsStateWithLifecycle()
    val totalUnreadMessages = conversations.sumOf { it.unreadCount }

    val showTopBar = currentScreen is ScreenNav.Home || currentScreen is ScreenNav.Explore
    val showBottomBar = currentScreen is ScreenNav.Home ||
            currentScreen is ScreenNav.Explore ||
            currentScreen is ScreenNav.CreatePost ||
            currentScreen is ScreenNav.Notifications ||
            currentScreen is ScreenNav.Profile

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            if (showTopBar) {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(30.dp)
                                    .clip(CircleShape)
                                    .background(SuyaibGradient),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "S",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "SUYAIB SOCIAL",
                                fontWeight = FontWeight.Black,
                                fontSize = 19.sp,
                                letterSpacing = 1.2.sp,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    },
                    actions = {
                        // Admin Shield Button (if admin)
                        if (currentUser?.isAdmin == true) {
                            IconButton(
                                onClick = { viewModel.navigateTo(ScreenNav.AdminDashboard) },
                                modifier = Modifier.testTag("topbar_admin_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Shield,
                                    contentDescription = "Admin Panel",
                                    tint = IndigoPrimary
                                )
                            }
                        }

                        // Direct Messages Button
                        IconButton(
                            onClick = { viewModel.navigateTo(ScreenNav.DirectMessages) },
                            modifier = Modifier.testTag("topbar_dm_button")
                        ) {
                            BadgedBox(
                                badge = {
                                    if (totalUnreadMessages > 0) {
                                        Badge(containerColor = CrimsonError) {
                                            Text("$totalUnreadMessages")
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ChatBubbleOutline,
                                    contentDescription = "Direct Messages",
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )
            }
        },
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 6.dp,
                    modifier = Modifier.testTag("bottom_nav_bar")
                ) {
                    // Home
                    NavigationBarItem(
                        selected = currentScreen is ScreenNav.Home,
                        onClick = { viewModel.navigateTo(ScreenNav.Home) },
                        icon = {
                            Icon(
                                imageVector = if (currentScreen is ScreenNav.Home) Icons.Default.Home else Icons.Outlined.Home,
                                contentDescription = "Home"
                            )
                        },
                        label = { Text("Feed", fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = IndigoPrimary,
                            indicatorColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        modifier = Modifier.testTag("nav_item_home")
                    )

                    // Explore
                    NavigationBarItem(
                        selected = currentScreen is ScreenNav.Explore,
                        onClick = { viewModel.navigateTo(ScreenNav.Explore) },
                        icon = {
                            Icon(
                                imageVector = if (currentScreen is ScreenNav.Explore) Icons.Default.Explore else Icons.Outlined.Explore,
                                contentDescription = "Explore"
                            )
                        },
                        label = { Text("Explore", fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = IndigoPrimary,
                            indicatorColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        modifier = Modifier.testTag("nav_item_explore")
                    )

                    // Create Post (+)
                    NavigationBarItem(
                        selected = currentScreen is ScreenNav.CreatePost,
                        onClick = { viewModel.navigateTo(ScreenNav.CreatePost) },
                        icon = {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(SuyaibGradient),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Create Post",
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        },
                        label = { Text("Post", fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = Color.Transparent
                        ),
                        modifier = Modifier.testTag("nav_item_create")
                    )

                    // Notifications
                    NavigationBarItem(
                        selected = currentScreen is ScreenNav.Notifications,
                        onClick = { viewModel.navigateTo(ScreenNav.Notifications) },
                        icon = {
                            BadgedBox(
                                badge = {
                                    if (unreadNotifsCount > 0) {
                                        Badge(containerColor = CrimsonError) {
                                            Text("$unreadNotifsCount")
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = if (currentScreen is ScreenNav.Notifications) Icons.Default.Notifications else Icons.Outlined.Notifications,
                                    contentDescription = "Notifications"
                                )
                            }
                        },
                        label = { Text("Activity", fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = IndigoPrimary,
                            indicatorColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        modifier = Modifier.testTag("nav_item_notifications")
                    )

                    // Profile
                    NavigationBarItem(
                        selected = currentScreen is ScreenNav.Profile,
                        onClick = { viewModel.navigateTo(ScreenNav.Profile) },
                        icon = {
                            if (currentUser != null && currentUser!!.avatarUrl.isNotBlank()) {
                                UserAvatar(
                                    avatarUrl = currentUser!!.avatarUrl,
                                    displayName = currentUser!!.displayName,
                                    size = 24.dp
                                )
                            } else {
                                Icon(
                                    imageVector = if (currentScreen is ScreenNav.Profile) Icons.Default.Person else Icons.Outlined.Person,
                                    contentDescription = "Profile"
                                )
                            }
                        },
                        label = { Text("Profile", fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = IndigoPrimary,
                            indicatorColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        modifier = Modifier.testTag("nav_item_profile")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentScreen) {
                is ScreenNav.Home -> FeedScreen(viewModel = viewModel)
                is ScreenNav.Explore -> ExploreScreen(viewModel = viewModel)
                is ScreenNav.CreatePost -> CreatePostScreen(viewModel = viewModel)
                is ScreenNav.Notifications -> NotificationsScreen(viewModel = viewModel)
                is ScreenNav.Profile -> ProfileScreen(viewModel = viewModel)
                is ScreenNav.AdminDashboard -> AdminDashboardScreen(
                    viewModel = viewModel,
                    onBack = { viewModel.navigateBack() }
                )
                is ScreenNav.DirectMessages -> ConversationsListScreen(
                    viewModel = viewModel,
                    onBack = { viewModel.navigateBack() }
                )
                is ScreenNav.ChatDetail -> ChatDetailScreen(
                    viewModel = viewModel,
                    otherUser = currentScreen.otherUser,
                    onBack = { viewModel.navigateBack() }
                )
                is ScreenNav.UserProfile -> ProfileScreen(
                    viewModel = viewModel,
                    targetUserId = currentScreen.userId,
                    onBack = { viewModel.navigateBack() }
                )
                is ScreenNav.Settings -> SettingsScreen(
                    viewModel = viewModel,
                    onBack = { viewModel.navigateBack() }
                )
                is ScreenNav.SavedPosts -> ProfileScreen(
                    viewModel = viewModel,
                    targetUserId = currentUser?.id,
                    onBack = { viewModel.navigateBack() }
                )
            }
        }
    }
}
