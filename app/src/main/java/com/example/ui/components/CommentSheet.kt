package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CommentItem
import com.example.data.model.PostItem
import com.example.data.model.UserEntity
import com.example.ui.theme.CrimsonError
import com.example.ui.theme.IndigoPrimary
import com.example.ui.theme.PinkAccent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommentSheet(
    postItem: PostItem,
    comments: List<CommentItem>,
    currentUser: UserEntity?,
    onDismiss: () -> Unit,
    onAddComment: (postId: String, content: String, parentId: String?) -> Unit,
    onDeleteComment: (commentId: String) -> Unit,
    onLikeComment: (commentId: String) -> Unit,
    onUserClick: (userId: String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var commentInput by remember { mutableStateOf("") }
    var replyingToComment by remember { mutableStateOf<CommentItem?>(null) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.testTag("comments_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.75f)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Comments (${comments.sumOf { 1 + it.replies.size }})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

            // Comments List
            if (comments.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No comments yet. Be the first to start the conversation!",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    item { Spacer(modifier = Modifier.height(4.dp)) }

                    items(comments) { commentItem ->
                        CommentRow(
                            commentItem = commentItem,
                            onUserClick = onUserClick,
                            onReplyClick = {
                                replyingToComment = commentItem
                                commentInput = "@${commentItem.author.username} "
                            },
                            onDeleteClick = { onDeleteComment(commentItem.comment.id) },
                            onLikeClick = { onLikeComment(commentItem.comment.id) }
                        )

                        // Nested Replies
                        if (commentItem.replies.isNotEmpty()) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 42.dp, top = 8.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                commentItem.replies.forEach { replyItem ->
                                    CommentRow(
                                        commentItem = replyItem,
                                        isReply = true,
                                        onUserClick = onUserClick,
                                        onReplyClick = {
                                            replyingToComment = commentItem
                                            commentInput = "@${replyItem.author.username} "
                                        },
                                        onDeleteClick = { onDeleteComment(replyItem.comment.id) },
                                        onLikeClick = { onLikeComment(replyItem.comment.id) }
                                    )
                                }
                            }
                        }
                    }

                    item { Spacer(modifier = Modifier.height(8.dp)) }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

            // Reply pill if active
            if (replyingToComment != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Replying to @${replyingToComment?.author?.username}",
                        fontSize = 12.sp,
                        color = IndigoPrimary,
                        fontWeight = FontWeight.Medium
                    )
                    TextButton(onClick = {
                        replyingToComment = null
                        commentInput = ""
                    }) {
                        Text("Cancel", fontSize = 12.sp)
                    }
                }
            }

            // Input Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                UserAvatar(
                    avatarUrl = currentUser?.avatarUrl ?: "",
                    displayName = currentUser?.displayName ?: "User",
                    size = 36.dp
                )

                Spacer(modifier = Modifier.width(8.dp))

                OutlinedTextField(
                    value = commentInput,
                    onValueChange = { commentInput = it },
                    placeholder = {
                        Text("Add a comment...", fontSize = 13.sp)
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("comment_input_field"),
                    shape = RoundedCornerShape(20.dp),
                    singleLine = false,
                    maxLines = 3
                )

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = {
                        if (commentInput.isNotBlank()) {
                            onAddComment(
                                postItem.post.id,
                                commentInput,
                                replyingToComment?.comment?.id
                            )
                            commentInput = ""
                            replyingToComment = null
                        }
                    },
                    enabled = commentInput.isNotBlank(),
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(if (commentInput.isNotBlank()) IndigoPrimary else MaterialTheme.colorScheme.surfaceVariant)
                        .testTag("comment_send_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send Comment",
                        tint = if (commentInput.isNotBlank()) Color.White else Color.Gray,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun CommentRow(
    commentItem: CommentItem,
    isReply: Boolean = false,
    onUserClick: (userId: String) -> Unit,
    onReplyClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onLikeClick: () -> Unit
) {
    val comment = commentItem.comment
    val author = commentItem.author

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        UserAvatar(
            avatarUrl = author.avatarUrl,
            displayName = author.displayName,
            size = if (isReply) 28.dp else 36.dp,
            onClick = { onUserClick(author.id) }
        )

        Spacer(modifier = Modifier.width(10.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = author.displayName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    modifier = Modifier.clickable { onUserClick(author.id) }
                )
                if (author.isVerified) {
                    Spacer(modifier = Modifier.width(3.dp))
                    VerifiedBadge(size = 12.dp)
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = formatTimestamp(comment.createdAt),
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Comment text with @mention highlighting
            val annotatedText = buildAnnotatedString {
                val words = comment.content.split(" ")
                for (word in words) {
                    if (word.startsWith("@")) {
                        withStyle(SpanStyle(color = PinkAccent, fontWeight = FontWeight.SemiBold)) {
                            append("$word ")
                        }
                    } else if (word.startsWith("#")) {
                        withStyle(SpanStyle(color = IndigoPrimary, fontWeight = FontWeight.SemiBold)) {
                            append("$word ")
                        }
                    } else {
                        append("$word ")
                    }
                }
            }

            Text(
                text = annotatedText,
                fontSize = 13.sp,
                lineHeight = 17.sp,
                modifier = Modifier.padding(vertical = 2.dp)
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Reply",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .clickable { onReplyClick() }
                        .padding(vertical = 2.dp)
                )

                if (commentItem.isMyComment) {
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "Delete",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = CrimsonError,
                        modifier = Modifier
                            .clickable { onDeleteClick() }
                            .padding(vertical = 2.dp)
                    )
                }
            }
        }

        // Like comment action
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(start = 6.dp)
        ) {
            IconButton(
                onClick = onLikeClick,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = if (commentItem.isLikedByMe) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Like comment",
                    tint = if (commentItem.isLikedByMe) PinkAccent else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
            }
            if (comment.likeCount > 0) {
                Text(
                    text = "${comment.likeCount}",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
