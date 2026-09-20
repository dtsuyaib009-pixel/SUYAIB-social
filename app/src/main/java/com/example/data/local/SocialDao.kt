package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.BlockEntity
import com.example.data.model.CommentEntity
import com.example.data.model.CommentLikeEntity
import com.example.data.model.ConversationEntity
import com.example.data.model.FollowEntity
import com.example.data.model.MessageEntity
import com.example.data.model.NotificationEntity
import com.example.data.model.PostEntity
import com.example.data.model.PostLikeEntity
import com.example.data.model.ReportEntity
import com.example.data.model.SavedPostEntity
import com.example.data.model.StoryEntity
import com.example.data.model.StoryViewEntity
import com.example.data.model.UserEntity
import com.example.data.model.UserSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SocialDao {

    // --- USERS & SESSION ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsers(users: List<UserEntity>)

    @Update
    suspend fun updateUser(user: UserEntity)

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun getUserById(id: String): UserEntity?

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    fun observeUserById(id: String): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE LOWER(username) = LOWER(:username) LIMIT 1")
    suspend fun getUserByUsername(username: String): UserEntity?

    @Query("SELECT * FROM users WHERE LOWER(email) = LOWER(:email) LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Query("SELECT * FROM users ORDER BY createdAt DESC")
    fun getAllUsers(): Flow<List<UserEntity>>

    @Query("SELECT * FROM users WHERE isSuspended = 0 AND (LOWER(username) LIKE '%' || LOWER(:query) || '%' OR LOWER(displayName) LIKE '%' || LOWER(:query) || '%')")
    suspend fun searchUsers(query: String): List<UserEntity>

    @Query("SELECT COUNT(*) FROM users")
    suspend fun getTotalUsersCount(): Int

    @Query("DELETE FROM users WHERE id = :id")
    suspend fun deleteUser(id: String)

    @Query("SELECT * FROM user_session WHERE id = 1 LIMIT 1")
    fun observeActiveSession(): Flow<UserSessionEntity?>

    @Query("SELECT * FROM user_session WHERE id = 1 LIMIT 1")
    suspend fun getActiveSession(): UserSessionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setSession(session: UserSessionEntity)

    @Query("DELETE FROM user_session WHERE id = 1")
    suspend fun clearSession()

    // --- POSTS ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPost(post: PostEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPosts(posts: List<PostEntity>)

    @Update
    suspend fun updatePost(post: PostEntity)

    @Query("UPDATE posts SET caption = :caption, hashtags = :hashtags WHERE id = :id")
    suspend fun updatePostContent(id: String, caption: String, hashtags: String)

    @Query("UPDATE posts SET isRemoved = 1 WHERE id = :id")
    suspend fun markPostRemoved(id: String)

    @Query("DELETE FROM posts WHERE id = :id")
    suspend fun deletePost(id: String)

    @Query("SELECT * FROM posts WHERE id = :id LIMIT 1")
    suspend fun getPostById(id: String): PostEntity?

    @Query("SELECT * FROM posts WHERE isRemoved = 0 ORDER BY createdAt DESC")
    fun observeAllActivePosts(): Flow<List<PostEntity>>

    @Query("SELECT * FROM posts WHERE authorId = :authorId AND isRemoved = 0 ORDER BY createdAt DESC")
    fun observePostsByAuthor(authorId: String): Flow<List<PostEntity>>

    @Query("SELECT * FROM posts WHERE isRemoved = 0 AND (LOWER(caption) LIKE '%' || LOWER(:query) || '%' OR LOWER(hashtags) LIKE '%' || LOWER(:query) || '%' OR LOWER(location) LIKE '%' || LOWER(:query) || '%') ORDER BY createdAt DESC")
    suspend fun searchPosts(query: String): List<PostEntity>

    @Query("SELECT * FROM posts ORDER BY createdAt DESC")
    fun observeAdminPosts(): Flow<List<PostEntity>>

    @Query("SELECT COUNT(*) FROM posts WHERE isRemoved = 0")
    suspend fun getTotalPostsCount(): Int

    @Query("UPDATE posts SET likeCount = :count WHERE id = :postId")
    suspend fun updatePostLikeCount(postId: String, count: Int)

    @Query("UPDATE posts SET commentCount = :count WHERE id = :postId")
    suspend fun updatePostCommentCount(postId: String, count: Int)

    @Query("UPDATE posts SET shareCount = shareCount + 1 WHERE id = :postId")
    suspend fun incrementShareCount(postId: String)

    // --- LIKES ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPostLike(like: PostLikeEntity)

    @Query("DELETE FROM post_likes WHERE postId = :postId AND userId = :userId")
    suspend fun deletePostLike(postId: String, userId: String)

    @Query("SELECT COUNT(*) FROM post_likes WHERE postId = :postId AND userId = :userId")
    suspend fun isPostLikedBy(postId: String, userId: String): Int

    @Query("SELECT COUNT(*) FROM post_likes WHERE postId = :postId")
    suspend fun getPostLikeCount(postId: String): Int

    @Query("SELECT * FROM post_likes WHERE userId = :userId")
    fun observeUserLikes(userId: String): Flow<List<PostLikeEntity>>

    // --- COMMENTS ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComment(comment: CommentEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComments(comments: List<CommentEntity>)

    @Query("DELETE FROM comments WHERE id = :id")
    suspend fun deleteComment(id: String)

    @Query("UPDATE comments SET isRemoved = 1 WHERE id = :id")
    suspend fun markCommentRemoved(id: String)

    @Query("SELECT * FROM comments WHERE postId = :postId AND isRemoved = 0 ORDER BY createdAt ASC")
    fun observeCommentsForPost(postId: String): Flow<List<CommentEntity>>

    @Query("SELECT * FROM comments WHERE id = :id LIMIT 1")
    suspend fun getCommentById(id: String): CommentEntity?

    @Query("SELECT COUNT(*) FROM comments WHERE postId = :postId AND isRemoved = 0")
    suspend fun getCommentCountForPost(postId: String): Int

    @Query("SELECT COUNT(*) FROM comments WHERE isRemoved = 0")
    suspend fun getTotalCommentsCount(): Int

    @Query("SELECT * FROM comments ORDER BY createdAt DESC")
    fun observeAdminComments(): Flow<List<CommentEntity>>

    // --- COMMENT LIKES ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCommentLike(like: CommentLikeEntity)

    @Query("DELETE FROM comment_likes WHERE commentId = :commentId AND userId = :userId")
    suspend fun deleteCommentLike(commentId: String, userId: String)

    @Query("SELECT COUNT(*) FROM comment_likes WHERE commentId = :commentId AND userId = :userId")
    suspend fun isCommentLikedBy(commentId: String, userId: String): Int

    @Query("SELECT COUNT(*) FROM comment_likes WHERE commentId = :commentId")
    suspend fun getCommentLikeCount(commentId: String): Int

    @Query("UPDATE comments SET likeCount = :count WHERE id = :commentId")
    suspend fun updateCommentLikeCount(commentId: String, count: Int)

    // --- FOLLOWS ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFollow(follow: FollowEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFollows(follows: List<FollowEntity>)

    @Query("DELETE FROM follows WHERE followerId = :followerId AND followingId = :followingId")
    suspend fun deleteFollow(followerId: String, followingId: String)

    @Query("SELECT COUNT(*) FROM follows WHERE followerId = :followerId AND followingId = :followingId")
    suspend fun isFollowing(followerId: String, followingId: String): Int

    @Query("SELECT COUNT(*) FROM follows WHERE followerId = :followerId AND followingId = :followingId")
    fun observeIsFollowing(followerId: String, followingId: String): Flow<Int>

    @Query("SELECT followingId FROM follows WHERE followerId = :userId")
    fun observeFollowingIds(userId: String): Flow<List<String>>

    @Query("SELECT followerId FROM follows WHERE followingId = :userId")
    fun observeFollowerIds(userId: String): Flow<List<String>>

    @Query("SELECT COUNT(*) FROM follows WHERE followingId = :userId")
    suspend fun getFollowersCount(userId: String): Int

    @Query("SELECT COUNT(*) FROM follows WHERE followerId = :userId")
    suspend fun getFollowingCount(userId: String): Int

    // --- STORIES ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStory(story: StoryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStories(stories: List<StoryEntity>)

    @Query("DELETE FROM stories WHERE id = :id")
    suspend fun deleteStory(id: String)

    @Query("SELECT * FROM stories WHERE expiresAt > :now ORDER BY createdAt DESC")
    fun observeActiveStories(now: Long = System.currentTimeMillis()): Flow<List<StoryEntity>>

    @Query("SELECT * FROM stories WHERE authorId = :authorId AND expiresAt > :now ORDER BY createdAt ASC")
    fun observeUserActiveStories(authorId: String, now: Long = System.currentTimeMillis()): Flow<List<StoryEntity>>

    @Query("SELECT COUNT(*) FROM stories WHERE expiresAt > :now")
    suspend fun getTotalActiveStoriesCount(now: Long = System.currentTimeMillis()): Int

    // --- STORY VIEWS ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStoryView(view: StoryViewEntity)

    @Query("SELECT COUNT(*) FROM story_views WHERE storyId = :storyId AND viewerId = :viewerId")
    suspend fun hasViewedStory(storyId: String, viewerId: String): Int

    @Query("SELECT viewerId FROM story_views WHERE storyId = :storyId ORDER BY viewedAt DESC")
    suspend fun getStoryViewerIds(storyId: String): List<String>

    @Query("SELECT * FROM story_views WHERE viewerId = :userId")
    fun observeUserStoryViews(userId: String): Flow<List<StoryViewEntity>>

    // --- SAVED POSTS ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSavedPost(savedPost: SavedPostEntity)

    @Query("DELETE FROM saved_posts WHERE postId = :postId AND userId = :userId")
    suspend fun deleteSavedPost(postId: String, userId: String)

    @Query("SELECT COUNT(*) FROM saved_posts WHERE postId = :postId AND userId = :userId")
    suspend fun isPostSaved(postId: String, userId: String): Int

    @Query("SELECT * FROM saved_posts WHERE userId = :userId ORDER BY createdAt DESC")
    fun observeSavedPosts(userId: String): Flow<List<SavedPostEntity>>

    // --- CONVERSATIONS & MESSAGES ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConversation(conv: ConversationEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConversations(convs: List<ConversationEntity>)

    @Query("SELECT * FROM conversations WHERE user1Id = :userId OR user2Id = :userId ORDER BY lastMessageTimestamp DESC")
    fun observeConversationsForUser(userId: String): Flow<List<ConversationEntity>>

    @Query("SELECT * FROM conversations WHERE (user1Id = :u1 AND user2Id = :u2) OR (user1Id = :u2 AND user2Id = :u1) LIMIT 1")
    suspend fun getConversationBetween(u1: String, u2: String): ConversationEntity?

    @Query("UPDATE conversations SET lastMessage = :lastMessage, lastMessageTimestamp = :timestamp WHERE id = :convId")
    suspend fun updateConversationLastMessage(convId: String, lastMessage: String, timestamp: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(msg: MessageEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessages(msgs: List<MessageEntity>)

    @Query("DELETE FROM messages WHERE id = :id")
    suspend fun deleteMessage(id: String)

    @Query("SELECT * FROM messages WHERE conversationId = :convId ORDER BY timestamp ASC")
    fun observeMessagesForConversation(convId: String): Flow<List<MessageEntity>>

    @Query("UPDATE messages SET isRead = 1 WHERE conversationId = :convId AND recipientId = :userId")
    suspend fun markMessagesAsRead(convId: String, userId: String)

    // --- NOTIFICATIONS ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notif: NotificationEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotifications(notifs: List<NotificationEntity>)

    @Query("SELECT * FROM notifications WHERE recipientId = :userId ORDER BY timestamp DESC")
    fun observeNotificationsForUser(userId: String): Flow<List<NotificationEntity>>

    @Query("SELECT COUNT(*) FROM notifications WHERE recipientId = :userId AND isRead = 0")
    fun observeUnreadNotificationsCount(userId: String): Flow<Int>

    @Query("UPDATE notifications SET isRead = 1 WHERE id = :id")
    suspend fun markNotificationRead(id: String)

    @Query("UPDATE notifications SET isRead = 1 WHERE recipientId = :userId")
    suspend fun markAllNotificationsRead(userId: String)

    @Query("DELETE FROM notifications WHERE recipientId = :userId")
    suspend fun clearNotifications(userId: String)

    // --- REPORTS ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReport(report: ReportEntity)

    @Query("SELECT * FROM reports ORDER BY timestamp DESC")
    fun observeAllReports(): Flow<List<ReportEntity>>

    @Query("SELECT COUNT(*) FROM reports WHERE status = 'PENDING'")
    suspend fun getPendingReportsCount(): Int

    @Query("UPDATE reports SET status = :status WHERE id = :id")
    suspend fun updateReportStatus(id: String, status: String)

    // --- BLOCKS ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBlock(block: BlockEntity)

    @Query("DELETE FROM blocks WHERE blockerId = :blockerId AND blockedUserId = :blockedUserId")
    suspend fun deleteBlock(blockerId: String, blockedUserId: String)

    @Query("SELECT blockedUserId FROM blocks WHERE blockerId = :userId")
    fun observeBlockedUserIds(userId: String): Flow<List<String>>

    @Query("SELECT blockedUserId FROM blocks WHERE blockerId = :userId")
    suspend fun getBlockedUserIds(userId: String): List<String>

    @Query("SELECT COUNT(*) FROM blocks WHERE blockerId = :blockerId AND blockedUserId = :blockedUserId")
    suspend fun isUserBlocked(blockerId: String, blockedUserId: String): Int
}
