package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "users",
    indices = [Index(value = ["username"], unique = true), Index(value = ["email"], unique = true)]
)
data class UserEntity(
    @PrimaryKey val id: String,
    val username: String,
    val email: String,
    val passwordHash: String,
    val displayName: String,
    val avatarUrl: String,
    val bio: String = "",
    val website: String = "",
    val isVerified: Boolean = false,
    val isAdmin: Boolean = false,
    val isSuspended: Boolean = false,
    val isPrivate: Boolean = false,
    val followersCount: Int = 0,
    val followingCount: Int = 0,
    val postsCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val lastActive: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "posts",
    indices = [Index(value = ["authorId"]), Index(value = ["createdAt"])]
)
data class PostEntity(
    @PrimaryKey val id: String,
    val authorId: String,
    val caption: String,
    val mediaUrls: String, // Comma-delimited list of image/video URLs
    val isVideo: Boolean = false,
    val hashtags: String = "",
    val location: String = "",
    val likeCount: Int = 0,
    val commentCount: Int = 0,
    val shareCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val isReported: Boolean = false,
    val isRemoved: Boolean = false
)

@Entity(
    tableName = "post_likes",
    indices = [Index(value = ["postId"]), Index(value = ["userId"])]
)
data class PostLikeEntity(
    @PrimaryKey val id: String, // "$postId-$userId"
    val postId: String,
    val userId: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "comments",
    indices = [Index(value = ["postId"]), Index(value = ["authorId"])]
)
data class CommentEntity(
    @PrimaryKey val id: String,
    val postId: String,
    val authorId: String,
    val parentCommentId: String? = null,
    val content: String,
    val likeCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val isReported: Boolean = false,
    val isRemoved: Boolean = false
)

@Entity(
    tableName = "comment_likes",
    indices = [Index(value = ["commentId"]), Index(value = ["userId"])]
)
data class CommentLikeEntity(
    @PrimaryKey val id: String, // "$commentId-$userId"
    val commentId: String,
    val userId: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "follows",
    indices = [Index(value = ["followerId"]), Index(value = ["followingId"])]
)
data class FollowEntity(
    @PrimaryKey val id: String, // "$followerId-$followingId"
    val followerId: String,
    val followingId: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "stories",
    indices = [Index(value = ["authorId"]), Index(value = ["expiresAt"])]
)
data class StoryEntity(
    @PrimaryKey val id: String,
    val authorId: String,
    val mediaUrl: String,
    val caption: String = "",
    val isVideo: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val expiresAt: Long = System.currentTimeMillis() + (24 * 60 * 60 * 1000)
)

@Entity(
    tableName = "story_views",
    indices = [Index(value = ["storyId"]), Index(value = ["viewerId"])]
)
data class StoryViewEntity(
    @PrimaryKey val id: String, // "$storyId-$viewerId"
    val storyId: String,
    val viewerId: String,
    val viewedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "saved_posts",
    indices = [Index(value = ["postId"]), Index(value = ["userId"])]
)
data class SavedPostEntity(
    @PrimaryKey val id: String, // "$postId-$userId"
    val postId: String,
    val userId: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "conversations",
    indices = [Index(value = ["user1Id"]), Index(value = ["user2Id"])]
)
data class ConversationEntity(
    @PrimaryKey val id: String,
    val user1Id: String,
    val user2Id: String,
    val lastMessage: String = "",
    val lastMessageTimestamp: Long = System.currentTimeMillis(),
    val user1UnreadCount: Int = 0,
    val user2UnreadCount: Int = 0
)

@Entity(
    tableName = "messages",
    indices = [Index(value = ["conversationId"]), Index(value = ["timestamp"])]
)
data class MessageEntity(
    @PrimaryKey val id: String,
    val conversationId: String,
    val senderId: String,
    val recipientId: String,
    val content: String,
    val isRead: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "notifications",
    indices = [Index(value = ["recipientId"]), Index(value = ["timestamp"])]
)
data class NotificationEntity(
    @PrimaryKey val id: String,
    val recipientId: String,
    val actorId: String,
    val type: String, // "LIKE", "COMMENT", "REPLY", "FOLLOW", "STORY_VIEW", "MENTION"
    val targetId: String? = null,
    val summary: String,
    val isRead: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "reports",
    indices = [Index(value = ["targetId"]), Index(value = ["targetType"])]
)
data class ReportEntity(
    @PrimaryKey val id: String,
    val reporterId: String,
    val targetType: String, // "POST", "USER", "COMMENT"
    val targetId: String,
    val reason: String,
    val status: String = "PENDING", // "PENDING", "RESOLVED", "DISMISSED"
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "blocks",
    indices = [Index(value = ["blockerId"]), Index(value = ["blockedUserId"])]
)
data class BlockEntity(
    @PrimaryKey val id: String, // "$blockerId-$blockedUserId"
    val blockerId: String,
    val blockedUserId: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "user_session")
data class UserSessionEntity(
    @PrimaryKey val id: Int = 1,
    val userId: String,
    val loggedInAt: Long = System.currentTimeMillis()
)
