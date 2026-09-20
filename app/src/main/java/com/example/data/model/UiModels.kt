package com.example.data.model

data class PostItem(
    val post: PostEntity,
    val author: UserEntity,
    val isLikedByMe: Boolean = false,
    val isSavedByMe: Boolean = false,
    val isMyPost: Boolean = false
) {
    val mediaList: List<String>
        get() = if (post.mediaUrls.isBlank()) emptyList() else post.mediaUrls.split(",").map { it.trim() }
}

data class CommentItem(
    val comment: CommentEntity,
    val author: UserEntity,
    val isLikedByMe: Boolean = false,
    val isMyComment: Boolean = false,
    val replies: List<CommentItem> = emptyList()
)

data class StoryGroup(
    val author: UserEntity,
    val stories: List<StoryEntity>,
    val hasUnseen: Boolean = true,
    val isMyStory: Boolean = false
)

data class ConversationItem(
    val conversation: ConversationEntity,
    val otherUser: UserEntity,
    val unreadCount: Int = 0
)

data class MessageItem(
    val message: MessageEntity,
    val sender: UserEntity,
    val isFromMe: Boolean
)

data class NotificationItem(
    val notification: NotificationEntity,
    val actor: UserEntity
)

data class ReportItem(
    val report: ReportEntity,
    val reporter: UserEntity?,
    val targetSummary: String
)

data class PlatformStats(
    val totalUsers: Int,
    val activeUsers: Int,
    val totalPosts: Int,
    val totalComments: Int,
    val totalStories: Int,
    val pendingReports: Int
)

data class SearchResult(
    val users: List<UserEntity> = emptyList(),
    val posts: List<PostItem> = emptyList(),
    val hashtags: List<String> = emptyList()
)
