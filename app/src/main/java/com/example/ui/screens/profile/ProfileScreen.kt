package com.example.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PersonRemove
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.data.model.PostItem
import com.example.data.model.UserEntity
import com.example.ui.components.EmptyStateView
import com.example.ui.components.GradientButton
import com.example.ui.components.UserAvatar
import com.example.ui.components.VerifiedBadge
import com.example.ui.theme.CrimsonError
import com.example.ui.theme.IndigoPrimary
import com.example.ui.theme.SuyaibGradient
import com.example.ui.viewmodel.ScreenNav
import com.example.ui.viewmodel.SocialViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: SocialViewModel,
    targetUserId: String? = null,
    onBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val isMyProfile = targetUserId == null || targetUserId == currentUser?.id

    var displayedUser by remember { mutableStateOf<UserEntity?>(null) }
    var userPosts by remember { mutableStateOf<List<PostItem>>(emptyList()) }
    var isFollowing by remember { mutableStateOf(false) }

    val savedPosts by viewModel.savedPosts.collectAsStateWithLifecycle()

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Posts, 1: Saved (if mine)
    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showOptionsMenu by remember { mutableStateOf(false) }

    var showFollowersSheet by remember { mutableStateOf(false) }
    var showFollowingSheet by remember { mutableStateOf(false) }
    var modalUserList by remember { mutableStateOf<List<UserEntity>>(emptyList()) }
    var modalTitle by remember { mutableStateOf("") }

    val scope = rememberCoroutineScope()

    LaunchedEffect(targetUserId, currentUser) {
        val effectiveId = targetUserId ?: currentUser?.id ?: return@LaunchedEffect
        if (effectiveId == currentUser?.id) {
            displayedUser = currentUser
        } else {
            displayedUser = viewModel.repository.dao.getUserById(effectiveId)
        }

        // Collect posts
        viewModel.repository.observeUserPosts(effectiveId, currentUser?.id ?: "").collect { posts ->
            userPosts = posts
        }
    }

    LaunchedEffect(targetUserId, currentUser) {
        val effectiveId = targetUserId ?: return@LaunchedEffect
        val me = currentUser?.id ?: return@LaunchedEffect
        if (effectiveId != me) {
            viewModel.repository.observeIsFollowing(me, effectiveId).collect { following ->
                isFollowing = following
            }
        }
    }

    val user = displayedUser ?: currentUser

    if (user == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("User not found")
        }
        return
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("profile_screen_${user.id}")
    ) {
        // Top app bar
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (onBack != null) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                    Text(
                        text = "@${user.username}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    if (user.isVerified) {
                        Spacer(modifier = Modifier.width(4.dp))
                        VerifiedBadge(size = 16.dp)
                    }
                    if (user.isAdmin) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = "Admin",
                            tint = IndigoPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Row {
                    if (isMyProfile) {
                        IconButton(onClick = { viewModel.navigateTo(ScreenNav.Settings) }) {
                            Icon(Icons.Default.Settings, contentDescription = "Settings")
                        }
                    } else {
                        Box {
                            IconButton(onClick = { showOptionsMenu = true }) {
                                Icon(Icons.Default.MoreVert, contentDescription = "Options")
                            }
                            DropdownMenu(
                                expanded = showOptionsMenu,
                                onDismissRequest = { showOptionsMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Report User") },
                                    leadingIcon = { Icon(Icons.Default.Warning, contentDescription = null) },
                                    onClick = {
                                        showOptionsMenu = false
                                        viewModel.promptReport("USER", user.id, "@${user.username}")
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Block User", color = CrimsonError) },
                                    leadingIcon = { Icon(Icons.Default.Close, contentDescription = null, tint = CrimsonError) },
                                    onClick = {
                                        showOptionsMenu = false
                                        viewModel.blockUser(user.id)
                                        onBack?.invoke()
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Profile Details Card
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Avatar
                    UserAvatar(
                        avatarUrl = user.avatarUrl,
                        displayName = user.displayName,
                        size = 80.dp,
                        onClick = {
                            if (isMyProfile) showEditProfileDialog = true
                        }
                    )

                    // Counts (Posts, Followers, Following)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CountStat(count = userPosts.size, label = "Posts")
                        CountStat(
                            count = user.followersCount,
                            label = "Followers",
                            onClick = {
                                scope.launch {
                                    modalUserList = viewModel.repository.getFollowers(user.id)
                                    modalTitle = "Followers"
                                    showFollowersSheet = true
                                }
                            }
                        )
                        CountStat(
                            count = user.followingCount,
                            label = "Following",
                            onClick = {
                                scope.launch {
                                    modalUserList = viewModel.repository.getFollowing(user.id)
                                    modalTitle = "Following"
                                    showFollowingSheet = true
                                }
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Name & Bio
                Text(
                    text = user.displayName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                if (user.bio.isNotBlank()) {
                    Text(
                        text = user.bio,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }

                if (user.website.isNotBlank()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Link,
                            contentDescription = null,
                            tint = IndigoPrimary,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = user.website,
                            fontSize = 12.sp,
                            color = IndigoPrimary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Action Buttons Row
                if (isMyProfile) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showEditProfileDialog = true },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("edit_profile_button"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Edit Profile", fontSize = 13.sp)
                        }
                        OutlinedButton(
                            onClick = { viewModel.showToast("Profile link copied!") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Share Profile", fontSize = 13.sp)
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { viewModel.toggleFollow(user.id) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("follow_user_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isFollowing) MaterialTheme.colorScheme.surfaceVariant else IndigoPrimary
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = if (isFollowing) Icons.Default.Check else Icons.Default.PersonAdd,
                                contentDescription = null,
                                tint = if (isFollowing) MaterialTheme.colorScheme.onSurfaceVariant else Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isFollowing) "Following" else "Follow",
                                color = if (isFollowing) MaterialTheme.colorScheme.onSurfaceVariant else Color.White,
                                fontSize = 13.sp
                            )
                        }

                        OutlinedButton(
                            onClick = { viewModel.openChat(user) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("message_user_button"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Message", fontSize = 13.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        // Tabs: Posts vs Saved (if my profile)
        item {
            PrimaryTabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.GridOn, contentDescription = "Posts") },
                    text = { Text("Posts (${userPosts.size})") }
                )
                if (isMyProfile) {
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        icon = { Icon(Icons.Default.Bookmark, contentDescription = "Saved") },
                        text = { Text("Saved (${savedPosts.size})") }
                    )
                }
            }
        }

        // Tab Content
        val postsToDisplay = if (selectedTab == 0) userPosts else savedPosts
        if (postsToDisplay.isEmpty()) {
            item {
                EmptyStateView(
                    title = if (selectedTab == 0) "No Posts Yet" else "No Saved Posts",
                    subtitle = if (selectedTab == 0) "When posts are published, they will show up here." else "Save photos and videos you want to revisit later.",
                    icon = {
                        Icon(
                            imageVector = if (selectedTab == 0) Icons.Default.GridOn else Icons.Default.Bookmark,
                            contentDescription = null,
                            modifier = Modifier.padding(16.dp)
                        )
                    },
                    modifier = Modifier.padding(top = 30.dp)
                )
            }
        } else {
            item {
                // Render 3-column grid for user photos
                val rows = postsToDisplay.chunked(3)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    rows.forEach { rowPosts ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            for (i in 0 until 3) {
                                if (i < rowPosts.size) {
                                    val item = rowPosts[i]
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .aspectRatio(1f)
                                            .clip(RoundedCornerShape(6.dp))
                                            .clickable { viewModel.openComments(item) }
                                    ) {
                                        AsyncImage(
                                            model = item.mediaList.firstOrNull() ?: item.author.avatarUrl,
                                            contentDescription = item.post.caption,
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                    }
                                } else {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(70.dp))
        }
    }

    // Edit Profile Dialog
    if (showEditProfileDialog) {
        var editName by remember { mutableStateOf(user.displayName) }
        var editUsername by remember { mutableStateOf(user.username) }
        var editBio by remember { mutableStateOf(user.bio) }
        var editWebsite by remember { mutableStateOf(user.website) }
        var editAvatarUrl by remember { mutableStateOf(user.avatarUrl) }
        var editError by remember { mutableStateOf<String?>(null) }

        val presetAvatars = listOf(
            "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=400&q=80",
            "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=400&q=80",
            "https://images.unsplash.com/photo-1570295999919-56ceb5ecca61?w=400&q=80",
            "https://images.unsplash.com/photo-1580489944761-15a19d654956?w=400&q=80",
            "https://images.unsplash.com/photo-1522075469751-3a6694fb2f61?w=400&q=80"
        )

        AlertDialog(
            onDismissRequest = { showEditProfileDialog = false },
            title = { Text("Edit Profile", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    if (editError != null) {
                        Text(editError!!, color = CrimsonError, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                    }

                    Text("Choose Avatar Preset:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        presetAvatars.forEach { url ->
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .border(
                                        if (editAvatarUrl == url) 2.dp else 1.dp,
                                        if (editAvatarUrl == url) IndigoPrimary else Color.Transparent,
                                        CircleShape
                                    )
                                    .clickable { editAvatarUrl = url }
                            ) {
                                AsyncImage(
                                    model = url,
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("Display Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = editUsername,
                        onValueChange = { editUsername = it },
                        label = { Text("Username") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = editBio,
                        onValueChange = { editBio = it },
                        label = { Text("Bio") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = editWebsite,
                        onValueChange = { editWebsite = it },
                        label = { Text("Website / Link") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    scope.launch {
                        val res = viewModel.repository.updateProfile(
                            userId = user.id,
                            displayName = editName,
                            username = editUsername,
                            bio = editBio,
                            website = editWebsite,
                            avatarUrl = editAvatarUrl
                        )
                        res.onSuccess {
                            showEditProfileDialog = false
                            viewModel.showToast("Profile successfully updated!")
                        }.onFailure {
                            editError = it.message
                        }
                    }
                }) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditProfileDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Modal Sheet for Followers or Following
    if (showFollowersSheet || showFollowingSheet) {
        val title = if (showFollowersSheet) "Followers" else "Following"
        ModalBottomSheet(
            onDismissRequest = {
                showFollowersSheet = false
                showFollowingSheet = false
            }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "$title (${modalUserList.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider()

                if (modalUserList.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No users found.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(modalUserList) { u ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        showFollowersSheet = false
                                        showFollowingSheet = false
                                        viewModel.navigateTo(ScreenNav.UserProfile(u.id))
                                    }
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                UserAvatar(
                                    avatarUrl = u.avatarUrl,
                                    displayName = u.displayName,
                                    size = 40.dp
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(u.displayName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text("@${u.username}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CountStat(
    count: Int,
    label: String,
    onClick: (() -> Unit)? = null
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
    ) {
        Text(
            text = "$count",
            fontWeight = FontWeight.Bold,
            fontSize = 17.sp
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
