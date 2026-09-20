package com.example.ui.screens.create

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.ui.components.GradientButton
import com.example.ui.components.UserAvatar
import com.example.ui.theme.CrimsonError
import com.example.ui.theme.IndigoPrimary
import com.example.ui.viewmodel.SocialViewModel

@Composable
fun CreatePostScreen(
    viewModel: SocialViewModel,
    modifier: Modifier = Modifier
) {
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()

    var caption by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var hashtags by remember { mutableStateOf("#suyaibsocial ") }
    var isVideo by remember { mutableStateOf(false) }

    // Media list
    var mediaUrls by remember {
        mutableStateOf(listOf("https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=1080&q=80"))
    }
    var customUrlInput by remember { mutableStateOf("") }

    val presetImages = listOf(
        "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=1080&q=80",
        "https://images.unsplash.com/photo-1550745165-9bc0b252726f?w=1080&q=80",
        "https://images.unsplash.com/photo-1518770660439-4636190af475?w=1080&q=80",
        "https://images.unsplash.com/photo-1579783902614-a3fb3927b675?w=1080&q=80",
        "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=1080&q=80"
    )

    val quickHashtags = listOf("#suyaibsocial", "#creative", "#android", "#design", "#tech", "#lifestyle", "#art")

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("create_post_screen")
    ) {
        Text(
            text = "Create New Post",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Share photos, thoughts, and connect with your audience.",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Author Preview row
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 12.dp)
        ) {
            UserAvatar(
                avatarUrl = currentUser?.avatarUrl ?: "",
                displayName = currentUser?.displayName ?: "User",
                size = 40.dp
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = currentUser?.displayName ?: "User",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Text(
                    text = "@${currentUser?.username ?: "user"}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Caption TextField
        OutlinedTextField(
            value = caption,
            onValueChange = { caption = it },
            placeholder = { Text("What's happening? Write your story, share insights...") },
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp)
                .testTag("create_post_caption_input"),
            shape = RoundedCornerShape(16.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Selected Media Gallery Preview
        Text("Attached Media (${mediaUrls.size}):", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
        Spacer(modifier = Modifier.height(6.dp))
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(mediaUrls) { url ->
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                ) {
                    AsyncImage(
                        model = url,
                        contentDescription = "Selected media",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    IconButton(
                        onClick = { mediaUrls = mediaUrls.filter { it != url } },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .size(24.dp)
                            .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(bottomStart = 8.dp))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Remove media",
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Preset media picker
        Text("Choose from sample library:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(6.dp))
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(presetImages) { url ->
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .clickable {
                            if (!mediaUrls.contains(url)) {
                                mediaUrls = mediaUrls + url
                            }
                        }
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

        // Custom URL input
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = customUrlInput,
                onValueChange = { customUrlInput = it },
                label = { Text("Or paste image URL") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = {
                    if (customUrlInput.isNotBlank()) {
                        mediaUrls = mediaUrls + customUrlInput.trim()
                        customUrlInput = ""
                    }
                },
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add custom URL", tint = IndigoPrimary)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Video toggle
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Videocam, contentDescription = null, tint = IndigoPrimary)
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text("Video Format", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    Text("Tags this media as a video stream", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Switch(checked = isVideo, onCheckedChange = { isVideo = it })
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Hashtags chips
        Text("Quick Hashtags:", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.padding(vertical = 6.dp)
        ) {
            items(quickHashtags) { tag ->
                FilterChip(
                    selected = hashtags.contains(tag),
                    onClick = {
                        hashtags = if (hashtags.contains(tag)) {
                            hashtags.replace(tag, "").trim()
                        } else {
                            "$hashtags $tag".trim()
                        }
                    },
                    label = { Text(tag, fontSize = 12.sp) }
                )
            }
        }

        OutlinedTextField(
            value = hashtags,
            onValueChange = { hashtags = it },
            label = { Text("Hashtags") },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Location field
        OutlinedTextField(
            value = location,
            onValueChange = { location = it },
            label = { Text("Add Location (optional)") },
            leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = IndigoPrimary) },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Submit Button
        GradientButton(
            text = "Publish Post",
            onClick = {
                if (caption.isBlank() && mediaUrls.isEmpty()) {
                    viewModel.showToast("Please write a caption or attach media")
                    return@GradientButton
                }
                viewModel.createPost(
                    caption = caption,
                    mediaUrls = mediaUrls.joinToString(","),
                    isVideo = isVideo,
                    hashtags = hashtags,
                    location = location,
                    onSuccess = {
                        caption = ""
                        location = ""
                    }
                )
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(70.dp))
    }
}
