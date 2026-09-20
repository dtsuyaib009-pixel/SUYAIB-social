package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.PostItem
import com.example.ui.theme.CrimsonError
import com.example.ui.theme.IndigoPrimary
import com.example.ui.theme.PinkAccent
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun PostCard(
    postItem: PostItem,
    onLikeClick: () -> Unit,
    onCommentClick: () -> Unit,
    onShareClick: () -> Unit,
    onSaveClick: () -> Unit,
    onAuthorClick: (userId: String) -> Unit,
    onDeleteClick: (() -> Unit)? = null,
    onEditClick: (() -> Unit)? = null,
    onReportClick: (() -> Unit)? = null,
    onBlockUserClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val post = postItem.post
    val author = postItem.author
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .testTag("post_card_${post.id}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header: Author details & More Menu
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                UserAvatar(
                    avatarUrl = author.avatarUrl,
                    displayName = author.displayName,
                    size = 42.dp,
                    onClick = { onAuthorClick(author.id) }
                )

                Spacer(modifier = Modifier.width(10.dp))

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onAuthorClick(author.id) }
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = author.displayName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (author.isVerified) {
                            Spacer(modifier = Modifier.width(4.dp))
                            VerifiedBadge(size = 14.dp)
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "@${author.username}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = " • ${formatTimestamp(post.createdAt)}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        )
                    }
                    if (post.location.isNotBlank()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 1.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = IndigoPrimary,
                                modifier = Modifier.size(11.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = post.location,
                                fontSize = 11.sp,
                                color = IndigoPrimary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                Box {
                    IconButton(
                        onClick = { showMenu = true },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Post options",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        if (postItem.isMyPost) {
                            if (onEditClick != null) {
                                DropdownMenuItem(
                                    text = { Text("Edit Caption") },
                                    leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                                    onClick = {
                                        showMenu = false
                                        onEditClick()
                                    }
                                )
                            }
                            if (onDeleteClick != null) {
                                DropdownMenuItem(
                                    text = { Text("Delete Post", color = CrimsonError) },
                                    leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = CrimsonError) },
                                    onClick = {
                                        showMenu = false
                                        onDeleteClick()
                                    }
                                )
                            }
                        } else {
                            if (onReportClick != null) {
                                DropdownMenuItem(
                                    text = { Text("Report Post") },
                                    leadingIcon = { Icon(Icons.Default.Warning, contentDescription = null) },
                                    onClick = {
                                        showMenu = false
                                        onReportClick()
                                    }
                                )
                            }
                            if (onBlockUserClick != null) {
                                DropdownMenuItem(
                                    text = { Text("Block @${author.username}", color = CrimsonError) },
                                    leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = CrimsonError) },
                                    onClick = {
                                        showMenu = false
                                        onBlockUserClick()
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Media Section
            val mediaList = postItem.mediaList
            if (mediaList.isNotEmpty()) {
                val pagerState = rememberPagerState(pageCount = { mediaList.size })

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1.15f)
                        .background(Color.Black.copy(alpha = 0.05f))
                ) {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize()
                    ) { page ->
                        Box(modifier = Modifier.fillMaxSize()) {
                            AsyncImage(
                                model = mediaList[page],
                                contentDescription = "Post media ${page + 1}",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                            if (post.isVideo) {
                                Surface(
                                    modifier = Modifier
                                        .align(Alignment.Center)
                                        .size(48.dp),
                                    shape = CircleShape,
                                    color = Color.Black.copy(alpha = 0.65f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = "Video Play",
                                        tint = Color.White,
                                        modifier = Modifier
                                            .padding(8.dp)
                                            .fillMaxSize()
                                    )
                                }
                            }
                        }
                    }

                    // Multi-photo indicator pills
                    if (mediaList.size > 1) {
                        Surface(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(12.dp),
                            shape = RoundedCornerShape(12.dp),
                            color = Color.Black.copy(alpha = 0.6f)
                        ) {
                            Text(
                                text = "${pagerState.currentPage + 1}/${mediaList.size}",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        // Bottom dots indicator
                        Row(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 10.dp),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            repeat(mediaList.size) { iteration ->
                                val isSelected = pagerState.currentPage == iteration
                                Box(
                                    modifier = Modifier
                                        .padding(2.dp)
                                        .size(if (isSelected) 7.dp else 5.dp)
                                        .clip(CircleShape)
                                        .background(if (isSelected) IndigoPrimary else Color.White.copy(alpha = 0.6f))
                                )
                            }
                        }
                    }
                }
            }

            // Interactive Actions Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Like Button
                val heartScale by animateFloatAsState(
                    targetValue = if (postItem.isLikedByMe) 1.15f else 1f,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                    label = "like_scale"
                )
                val heartColor by animateColorAsState(
                    targetValue = if (postItem.isLikedByMe) PinkAccent else MaterialTheme.colorScheme.onSurface,
                    label = "like_color"
                )

                IconButton(
                    onClick = onLikeClick,
                    modifier = Modifier
                        .scale(heartScale)
                        .testTag("like_button_${post.id}")
                ) {
                    Icon(
                        imageVector = if (postItem.isLikedByMe) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Like",
                        tint = heartColor
                    )
                }
                Text(
                    text = "${post.likeCount}",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.width(12.dp))

                // Comment Button
                IconButton(
                    onClick = onCommentClick,
                    modifier = Modifier.testTag("comment_button_${post.id}")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ChatBubbleOutline,
                        contentDescription = "Comment",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
                Text(
                    text = "${post.commentCount}",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.width(12.dp))

                // Share Button
                IconButton(
                    onClick = onShareClick,
                    modifier = Modifier.testTag("share_button_${post.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
                Text(
                    text = "${post.shareCount}",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.weight(1f))

                // Bookmark / Save Button
                IconButton(
                    onClick = onSaveClick,
                    modifier = Modifier.testTag("save_button_${post.id}")
                ) {
                    Icon(
                        imageVector = if (postItem.isSavedByMe) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        contentDescription = "Save",
                        tint = if (postItem.isSavedByMe) IndigoPrimary else MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Caption and Hashtags
            if (post.caption.isNotBlank()) {
                val annotatedCaption = buildAnnotatedString {
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)) {
                        append("${author.username} ")
                    }
                    val words = post.caption.split(" ")
                    for (word in words) {
                        if (word.startsWith("#")) {
                            withStyle(SpanStyle(color = IndigoPrimary, fontWeight = FontWeight.SemiBold)) {
                                append("$word ")
                            }
                        } else if (word.startsWith("@")) {
                            withStyle(SpanStyle(color = PinkAccent, fontWeight = FontWeight.SemiBold)) {
                                append("$word ")
                            }
                        } else {
                            withStyle(SpanStyle(color = MaterialTheme.colorScheme.onSurface)) {
                                append("$word ")
                            }
                        }
                    }
                }

                Text(
                    text = annotatedCaption,
                    fontSize = 14.sp,
                    lineHeight = 19.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 2.dp)
                )
            }

            if (post.hashtags.isNotBlank()) {
                Text(
                    text = post.hashtags,
                    color = IndigoPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 2.dp)
                )
            }

            // "View all comments" button
            if (post.commentCount > 0) {
                TextButton(
                    onClick = onCommentClick,
                    modifier = Modifier.padding(horizontal = 6.dp)
                ) {
                    Text(
                        text = if (post.commentCount == 1) "View 1 comment" else "View all ${post.commentCount} comments",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

fun formatTimestamp(timestamp: Long): String {
    val diff = System.currentTimeMillis() - timestamp
    val seconds = diff / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    val days = hours / 24

    return when {
        minutes < 1 -> "just now"
        minutes < 60 -> "${minutes}m"
        hours < 24 -> "${hours}h"
        days < 7 -> "${days}d"
        else -> SimpleDateFormat("MMM d", Locale.getDefault()).format(Date(timestamp))
    }
}
