package com.example.ui.screens.feed

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DynamicFeed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.PostItem
import com.example.ui.components.EmptyStateView
import com.example.ui.components.GradientButton
import com.example.ui.components.PostCard
import com.example.ui.components.StoryTray
import com.example.ui.viewmodel.ScreenNav
import com.example.ui.viewmodel.SocialViewModel

@Composable
fun FeedScreen(
    viewModel: SocialViewModel,
    modifier: Modifier = Modifier
) {
    val posts by viewModel.homeFeed.collectAsStateWithLifecycle()
    val stories by viewModel.storyGroups.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()

    var postToEdit by remember { mutableStateOf<PostItem?>(null) }
    var editCaptionText by remember { mutableStateOf("") }
    var editHashtagsText by remember { mutableStateOf("") }

    var postToDeleteId by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("feed_screen_list")
    ) {
        // Story Tray at the top
        item {
            StoryTray(
                currentUser = currentUser,
                storyGroups = stories,
                onAddStoryClick = { viewModel.showCreateStory.value = true },
                onStoryGroupClick = { group -> viewModel.openStoryViewer(group) }
            )
        }

        if (posts.isEmpty()) {
            item {
                EmptyStateView(
                    title = "Your Feed is Quiet",
                    subtitle = "Follow creators or explore trending posts to see updates here.",
                    icon = {
                        Icon(
                            imageVector = Icons.Default.DynamicFeed,
                            contentDescription = null,
                            modifier = Modifier.padding(16.dp)
                        )
                    },
                    actionButton = {
                        GradientButton(
                            text = "Discover on Explore",
                            onClick = { viewModel.navigateTo(ScreenNav.Explore) }
                        )
                    },
                    modifier = Modifier.padding(top = 40.dp)
                )
            }
        } else {
            items(posts, key = { it.post.id }) { postItem ->
                PostCard(
                    postItem = postItem,
                    onLikeClick = { viewModel.toggleLikePost(postItem.post.id) },
                    onCommentClick = { viewModel.openComments(postItem) },
                    onShareClick = { viewModel.sharePost(postItem.post.id) },
                    onSaveClick = { viewModel.toggleSavePost(postItem.post.id) },
                    onAuthorClick = { userId ->
                        viewModel.navigateTo(ScreenNav.UserProfile(userId))
                    },
                    onDeleteClick = { postToDeleteId = postItem.post.id },
                    onEditClick = {
                        postToEdit = postItem
                        editCaptionText = postItem.post.caption
                        editHashtagsText = postItem.post.hashtags
                    },
                    onReportClick = {
                        viewModel.promptReport(
                            type = "POST",
                            targetId = postItem.post.id,
                            title = "Post by @${postItem.author.username}"
                        )
                    },
                    onBlockUserClick = {
                        viewModel.blockUser(postItem.author.id)
                    }
                )
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }

    // Edit Caption Dialog
    if (postToEdit != null) {
        AlertDialog(
            onDismissRequest = { postToEdit = null },
            title = { Text("Edit Caption") },
            text = {
                Column {
                    OutlinedTextField(
                        value = editCaptionText,
                        onValueChange = { editCaptionText = it },
                        label = { Text("Caption") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = editHashtagsText,
                        onValueChange = { editHashtagsText = it },
                        label = { Text("Hashtags") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.editPostCaption(postToEdit!!.post.id, editCaptionText, editHashtagsText)
                    postToEdit = null
                }) {
                    Text("Save Changes")
                }
            },
            dismissButton = {
                TextButton(onClick = { postToEdit = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Confirm Delete Dialog
    if (postToDeleteId != null) {
        AlertDialog(
            onDismissRequest = { postToDeleteId = null },
            title = { Text("Delete Post") },
            text = { Text("Are you sure you want to permanently delete this post?") },
            confirmButton = {
                Button(onClick = {
                    viewModel.deletePost(postToDeleteId!!)
                    postToDeleteId = null
                }) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { postToDeleteId = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}
