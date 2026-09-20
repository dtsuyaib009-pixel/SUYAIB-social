package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.data.model.StoryGroup
import com.example.data.model.UserEntity
import com.example.ui.theme.CrimsonError
import com.example.ui.theme.IndigoPrimary
import com.example.ui.theme.StoryRingGradient
import kotlinx.coroutines.delay

@Composable
fun StoryTray(
    currentUser: UserEntity?,
    storyGroups: List<StoryGroup>,
    onAddStoryClick: () -> Unit,
    onStoryGroupClick: (StoryGroup) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        contentPadding = PaddingValues(horizontal = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // "Add Story" or "Your Story" item
        item {
            val myStoryGroup = storyGroups.firstOrNull { it.isMyStory }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .width(68.dp)
                    .clickable {
                        if (myStoryGroup != null && myStoryGroup.stories.isNotEmpty()) {
                            onStoryGroupClick(myStoryGroup)
                        } else {
                            onAddStoryClick()
                        }
                    }
                    .testTag("story_tray_my_story")
            ) {
                Box(contentAlignment = Alignment.BottomEnd) {
                    val borderModifier = if (myStoryGroup != null && myStoryGroup.stories.isNotEmpty()) {
                        Modifier
                            .border(2.5.dp, StoryRingGradient, CircleShape)
                            .padding(2.5.dp)
                    } else Modifier

                    Box(
                        modifier = Modifier
                            .size(62.dp)
                            .then(borderModifier)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        if (currentUser?.avatarUrl?.isNotBlank() == true) {
                            AsyncImage(
                                model = currentUser.avatarUrl,
                                contentDescription = "Your Story",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Text(
                                text = "You",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Plus badge
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(IndigoPrimary)
                            .border(1.5.dp, MaterialTheme.colorScheme.background, CircleShape)
                            .clickable { onAddStoryClick() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Story",
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Your Story",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }

        // Other users' active stories
        items(storyGroups.filter { !it.isMyStory }) { group ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .width(68.dp)
                    .clickable { onStoryGroupClick(group) }
                    .testTag("story_tray_item_${group.author.id}")
            ) {
                val borderModifier = if (group.hasUnseen) {
                    Modifier
                        .border(2.5.dp, StoryRingGradient, CircleShape)
                        .padding(2.5.dp)
                } else {
                    Modifier
                        .border(1.5.dp, Color.Gray.copy(alpha = 0.5f), CircleShape)
                        .padding(2.dp)
                }

                Box(
                    modifier = Modifier
                        .size(62.dp)
                        .then(borderModifier)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = group.author.avatarUrl,
                        contentDescription = group.author.displayName,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = group.author.displayName.split(" ").firstOrNull() ?: group.author.username,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Normal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }
    }
}

@Composable
fun StoryViewerDialog(
    storyGroup: StoryGroup,
    onDismiss: () -> Unit,
    onDeleteStory: (storyId: String) -> Unit
) {
    var currentIndex by remember { mutableIntStateOf(0) }
    val stories = storyGroup.stories
    if (stories.isEmpty()) {
        onDismiss()
        return
    }

    val currentStory = stories[currentIndex.coerceIn(0, stories.lastIndex)]
    var progress by remember(currentIndex) { mutableFloatStateOf(0f) }

    LaunchedEffect(currentIndex) {
        val durationMs = 5000L
        val intervalMs = 50L
        val steps = durationMs / intervalMs
        for (i in 0..steps) {
            progress = i.toFloat() / steps
            delay(intervalMs)
        }
        if (currentIndex < stories.size - 1) {
            currentIndex++
        } else {
            onDismiss()
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            // Media
            AsyncImage(
                model = currentStory.mediaUrl,
                contentDescription = "Story Media",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // Touch navigators (left/right tap)
            Row(modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize()
                        .clickable {
                            if (currentIndex > 0) currentIndex-- else onDismiss()
                        }
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize()
                        .clickable {
                            if (currentIndex < stories.size - 1) currentIndex++ else onDismiss()
                        }
                )
            }

            // Top Header: Segmented progress bars + Author Info + Close
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .background(Color.Black.copy(alpha = 0.45f))
                    .padding(horizontal = 14.dp, vertical = 12.dp)
            ) {
                // Segmented Progress Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    stories.forEachIndexed { index, _ ->
                        val segmentProgress = when {
                            index < currentIndex -> 1f
                            index == currentIndex -> progress
                            else -> 0f
                        }
                        LinearProgressIndicator(
                            progress = { segmentProgress },
                            modifier = Modifier
                                .weight(1f)
                                .height(3.dp)
                                .clip(RoundedCornerShape(2.dp)),
                            color = Color.White,
                            trackColor = Color.White.copy(alpha = 0.35f)
                        )
                    }
                }

                // Author row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    UserAvatar(
                        avatarUrl = storyGroup.author.avatarUrl,
                        displayName = storyGroup.author.displayName,
                        size = 36.dp
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = storyGroup.author.displayName,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            if (storyGroup.author.isVerified) {
                                Spacer(modifier = Modifier.width(4.dp))
                                VerifiedBadge(size = 13.dp)
                            }
                        }
                        Text(
                            text = formatTimestamp(currentStory.createdAt),
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 11.sp
                        )
                    }

                    if (storyGroup.isMyStory) {
                        IconButton(onClick = { onDeleteStory(currentStory.id) }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete Story",
                                tint = CrimsonError
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White
                        )
                    }
                }
            }

            // Bottom Caption & Info
            if (currentStory.caption.isNotBlank()) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = Color.Black.copy(alpha = 0.6f)
                ) {
                    Text(
                        text = currentStory.caption,
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(14.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun CreateStoryDialog(
    onDismiss: () -> Unit,
    onSubmit: (mediaUrl: String, caption: String, isVideo: Boolean) -> Unit
) {
    var mediaUrl by remember { mutableStateOf("https://images.unsplash.com/photo-1517841905240-472988babdf9?w=1080&q=80") }
    var caption by remember { mutableStateOf("") }

    val presetImages = listOf(
        "https://images.unsplash.com/photo-1517841905240-472988babdf9?w=1080&q=80",
        "https://images.unsplash.com/photo-1542744094-3a31f272c490?w=1080&q=80",
        "https://images.unsplash.com/photo-1476514525535-07fb3b4ae5f1?w=1080&q=80",
        "https://images.unsplash.com/photo-1506744038136-46273834b3fb?w=1080&q=80"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Add to Your Story", fontWeight = FontWeight.Bold)
        },
        text = {
            Column {
                Text(
                    "Stories disappear automatically after 24 hours.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))

                Text("Choose image or enter custom URL:", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    presetImages.forEach { url ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .border(
                                    if (mediaUrl == url) 2.dp else 1.dp,
                                    if (mediaUrl == url) IndigoPrimary else Color.Transparent,
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable { mediaUrl = url }
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
                    value = mediaUrl,
                    onValueChange = { mediaUrl = it },
                    label = { Text("Image URL") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = caption,
                    onValueChange = { caption = it },
                    label = { Text("Add caption / sticker text (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (mediaUrl.isNotBlank()) {
                        onSubmit(mediaUrl, caption, false)
                    }
                }
            ) {
                Text("Share Story")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
