package com.example.data.repository

import com.example.data.local.SocialDao
import com.example.data.model.BlockEntity
import com.example.data.model.CommentEntity
import com.example.data.model.CommentItem
import com.example.data.model.CommentLikeEntity
import com.example.data.model.ConversationEntity
import com.example.data.model.ConversationItem
import com.example.data.model.FollowEntity
import com.example.data.model.MessageEntity
import com.example.data.model.MessageItem
import com.example.data.model.NotificationEntity
import com.example.data.model.NotificationItem
import com.example.data.model.PlatformStats
import com.example.data.model.PostEntity
import com.example.data.model.PostItem
import com.example.data.model.PostLikeEntity
import com.example.data.model.ReportEntity
import com.example.data.model.ReportItem
import com.example.data.model.SavedPostEntity
import com.example.data.model.SearchResult
import com.example.data.model.StoryEntity
import com.example.data.model.StoryGroup
import com.example.data.model.StoryViewEntity
import com.example.data.model.UserEntity
import com.example.data.model.UserSessionEntity
import com.example.util.SecurityUtils
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import java.util.UUID

@OptIn(ExperimentalCoroutinesApi::class)
class SocialRepository(val dao: SocialDao) {

    // --- ACTIVE USER SESSION ---
    val currentSession: Flow<UserSessionEntity?> = dao.observeActiveSession()

    val currentUser: Flow<UserEntity?> = currentSession.flatMapLatest { session ->
        if (session != null) {
            dao.observeUserById(session.userId)
        } else {
            flowOf(null)
        }
    }

    suspend fun login(usernameOrEmail: String, password: String): Result<UserEntity> {
        val trimmed = usernameOrEmail.trim()
        val user = dao.getUserByUsername(trimmed) ?: dao.getUserByEmail(trimmed)
        if (user == null) {
            return Result.failure(Exception("User not found with '$trimmed'"))
        }
        if (user.isSuspended) {
            return Result.failure(Exception("This account has been suspended by platform administrators."))
        }
        if (!SecurityUtils.verifyPassword(password, user.passwordHash)) {
            return Result.failure(Exception("Invalid password. Please try again."))
        }

        dao.setSession(UserSessionEntity(id = 1, userId = user.id))
        return Result.success(user)
    }

    suspend fun register(
        username: String,
        email: String,
        password: String,
        displayName: String
    ): Result<UserEntity> {
        val cleanUsername = username.trim().lowercase()
        val cleanEmail = email.trim().lowercase()

        if (cleanUsername.length < 3) {
            return Result.failure(Exception("Username must be at least 3 characters"))
        }
        if (!cleanEmail.contains("@") || !cleanEmail.contains(".")) {
            return Result.failure(Exception("Please enter a valid email address"))
        }
        if (password.length < 6) {
            return Result.failure(Exception("Password must be at least 6 characters"))
        }

        if (dao.getUserByUsername(cleanUsername) != null) {
            return Result.failure(Exception("Username '@$cleanUsername' is already taken"))
        }
        if (dao.getUserByEmail(cleanEmail) != null) {
            return Result.failure(Exception("An account with email '$cleanEmail' already exists"))
        }

        val newUser = UserEntity(
            id = "user_${UUID.randomUUID().toString().take(8)}",
            username = cleanUsername,
            email = cleanEmail,
            passwordHash = SecurityUtils.hashPassword(password),
            displayName = displayName.ifBlank { cleanUsername },
            avatarUrl = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=400&q=80",
            bio = "Hey there! I am using SUYAIB SOCIAL.",
            website = "",
            isVerified = false,
            isAdmin = false
        )

        dao.insertUser(newUser)
        dao.setSession(UserSessionEntity(id = 1, userId = newUser.id))
        return Result.success(newUser)
    }

    suspend fun logout() {
        dao.clearSession()
    }

    suspend fun switchUser(userId: String) {
        val user = dao.getUserById(userId) ?: return
        dao.setSession(UserSessionEntity(id = 1, userId = user.id))
    }

    suspend fun resetPassword(usernameOrEmail: String, newPassword: String): Result<Boolean> {
        val trimmed = usernameOrEmail.trim()
        val user = dao.getUserByUsername(trimmed) ?: dao.getUserByEmail(trimmed)
            ?: return Result.failure(Exception("Account not found"))
        if (newPassword.length < 6) {
            return Result.failure(Exception("Password must be at least 6 characters"))
        }
        val updated = user.copy(passwordHash = SecurityUtils.hashPassword(newPassword))
        dao.updateUser(updated)
        return Result.success(true)
    }

    suspend fun changePassword(userId: String, currentPass: String, newPass: String): Result<Boolean> {
        val user = dao.getUserById(userId) ?: return Result.failure(Exception("User not found"))
        if (!SecurityUtils.verifyPassword(currentPass, user.passwordHash)) {
            return Result.failure(Exception("Current password is incorrect"))
        }
        if (newPass.length < 6) {
            return Result.failure(Exception("New password must be at least 6 characters"))
        }
        val updated = user.copy(passwordHash = SecurityUtils.hashPassword(newPass))
        dao.updateUser(updated)
        return Result.success(true)
    }

    suspend fun updateProfile(
        userId: String,
        displayName: String,
        username: String,
        bio: String,
        website: String,
        avatarUrl: String
    ): Result<UserEntity> {
        val user = dao.getUserById(userId) ?: return Result.failure(Exception("User not found"))
        val cleanUsername = username.trim().lowercase()

        // If username changed, check uniqueness
        if (cleanUsername != user.username) {
            val existing = dao.getUserByUsername(cleanUsername)
            if (existing != null && existing.id != userId) {
                return Result.failure(Exception("Username '@$cleanUsername' is already taken"))
            }
        }

        val updated = user.copy(
            displayName = displayName.trim(),
            username = cleanUsername,
            bio = bio.trim(),
            website = website.trim(),
            avatarUrl = avatarUrl.ifBlank { user.avatarUrl }
        )
        dao.updateUser(updated)
        return Result.success(updated)
    }

    suspend fun deleteAccount(userId: String) {
        dao.deleteUser(userId)
        dao.clearSession()
    }

    // --- FEED & POSTS ---
    fun observeHomeFeed(currentUserId: String): Flow<List<PostItem>> {
        return combine(
            dao.observeAllActivePosts(),
            dao.getAllUsers(),
            dao.observeUserLikes(currentUserId),
            dao.observeSavedPosts(currentUserId),
            dao.observeBlockedUserIds(currentUserId)
        ) { posts, users, likes, saves, blockedIds ->
            val userMap = users.associateBy { it.id }
            val likedSet = likes.map { it.postId }.toSet()
            val savedSet = saves.map { it.postId }.toSet()
            val blockedSet = blockedIds.toSet()

            posts.filter { post ->
                !blockedSet.contains(post.authorId) && userMap[post.authorId] != null
            }.map { post ->
                val author = userMap[post.authorId] ?: UserEntity(
                    id = post.authorId,
                    username = "unknown",
                    email = "",
                    passwordHash = "",
                    displayName = "User",
                    avatarUrl = ""
                )
                PostItem(
                    post = post,
                    author = author,
                    isLikedByMe = likedSet.contains(post.id),
                    isSavedByMe = savedSet.contains(post.id),
                    isMyPost = post.authorId == currentUserId
                )
            }
        }
    }

    fun observeExplorePosts(currentUserId: String): Flow<List<PostItem>> {
        return observeHomeFeed(currentUserId).map { posts ->
            // In explore, we sort by engagement (likeCount + commentCount) then date
            posts.sortedByDescending { it.post.likeCount * 2 + it.post.commentCount * 3 }
        }
    }

    fun observeUserPosts(authorId: String, currentUserId: String): Flow<List<PostItem>> {
        return combine(
            dao.observePostsByAuthor(authorId),
            dao.observeUserById(authorId),
            dao.observeUserLikes(currentUserId),
            dao.observeSavedPosts(currentUserId)
        ) { posts, author, likes, saves ->
            if (author == null) return@combine emptyList()
            val likedSet = likes.map { it.postId }.toSet()
            val savedSet = saves.map { it.postId }.toSet()

            posts.map { post ->
                PostItem(
                    post = post,
                    author = author,
                    isLikedByMe = likedSet.contains(post.id),
                    isSavedByMe = savedSet.contains(post.id),
                    isMyPost = post.authorId == currentUserId
                )
            }
        }
    }

    fun observeSavedPosts(currentUserId: String): Flow<List<PostItem>> {
        return combine(
            dao.observeSavedPosts(currentUserId),
            dao.observeAllActivePosts(),
            dao.getAllUsers(),
            dao.observeUserLikes(currentUserId)
        ) { saves, posts, users, likes ->
            val postMap = posts.associateBy { it.id }
            val userMap = users.associateBy { it.id }
            val likedSet = likes.map { it.postId }.toSet()

            saves.mapNotNull { save ->
                val post = postMap[save.postId] ?: return@mapNotNull null
                val author = userMap[post.authorId] ?: return@mapNotNull null
                PostItem(
                    post = post,
                    author = author,
                    isLikedByMe = likedSet.contains(post.id),
                    isSavedByMe = true,
                    isMyPost = post.authorId == currentUserId
                )
            }
        }
    }

    suspend fun createPost(
        authorId: String,
        caption: String,
        mediaUrls: String,
        isVideo: Boolean,
        hashtags: String,
        location: String
    ): Result<PostEntity> {
        val author = dao.getUserById(authorId) ?: return Result.failure(Exception("Author not found"))
        val postId = "post_${UUID.randomUUID().toString().take(8)}"
        val cleanHashtags = hashtags.trim().split(" ")
            .filter { it.isNotBlank() }
            .joinToString(" ") { if (it.startsWith("#")) it else "#$it" }

        val post = PostEntity(
            id = postId,
            authorId = authorId,
            caption = caption.trim(),
            mediaUrls = mediaUrls.trim(),
            isVideo = isVideo,
            hashtags = cleanHashtags,
            location = location.trim(),
            likeCount = 0,
            commentCount = 0,
            shareCount = 0,
            createdAt = System.currentTimeMillis()
        )
        dao.insertPost(post)

        // Increment user post count
        dao.updateUser(author.copy(postsCount = author.postsCount + 1))
        return Result.success(post)
    }

    suspend fun updatePostCaption(postId: String, authorId: String, caption: String, hashtags: String): Result<Unit> {
        val post = dao.getPostById(postId) ?: return Result.failure(Exception("Post not found"))
        if (post.authorId != authorId) {
            return Result.failure(Exception("Only post owner can edit the caption"))
        }
        val cleanHashtags = hashtags.trim().split(" ")
            .filter { it.isNotBlank() }
            .joinToString(" ") { if (it.startsWith("#")) it else "#$it" }

        dao.updatePostContent(postId, caption.trim(), cleanHashtags)
        return Result.success(Unit)
    }

    suspend fun deletePost(postId: String, currentUserId: String, isAdmin: Boolean): Result<Unit> {
        val post = dao.getPostById(postId) ?: return Result.failure(Exception("Post not found"))
        if (post.authorId != currentUserId && !isAdmin) {
            return Result.failure(Exception("You do not have permission to delete this post"))
        }
        dao.deletePost(postId)
        val author = dao.getUserById(post.authorId)
        if (author != null && author.postsCount > 0) {
            dao.updateUser(author.copy(postsCount = author.postsCount - 1))
        }
        return Result.success(Unit)
    }

    suspend fun toggleLikePost(postId: String, currentUserId: String) {
        val post = dao.getPostById(postId) ?: return
        val isLiked = dao.isPostLikedBy(postId, currentUserId) > 0

        if (isLiked) {
            dao.deletePostLike(postId, currentUserId)
            val newCount = maxOf(0, post.likeCount - 1)
            dao.updatePostLikeCount(postId, newCount)
        } else {
            dao.insertPostLike(PostLikeEntity(id = "$postId-$currentUserId", postId = postId, userId = currentUserId))
            val newCount = post.likeCount + 1
            dao.updatePostLikeCount(postId, newCount)

            // Notify post author if not self
            if (post.authorId != currentUserId) {
                val actor = dao.getUserById(currentUserId)
                dao.insertNotification(
                    NotificationEntity(
                        id = "notif_${UUID.randomUUID().toString().take(8)}",
                        recipientId = post.authorId,
                        actorId = currentUserId,
                        type = "LIKE",
                        targetId = postId,
                        summary = "liked your post: '${post.caption.take(30)}...'"
                    )
                )
            }
        }
    }

    suspend fun toggleSavePost(postId: String, currentUserId: String) {
        val isSaved = dao.isPostSaved(postId, currentUserId) > 0
        if (isSaved) {
            dao.deleteSavedPost(postId, currentUserId)
        } else {
            dao.insertSavedPost(SavedPostEntity(id = "$postId-$currentUserId", postId = postId, userId = currentUserId))
        }
    }

    suspend fun sharePost(postId: String) {
        dao.incrementShareCount(postId)
    }

    // --- COMMENTS & REPLIES ---
    fun observeCommentsForPost(postId: String, currentUserId: String): Flow<List<CommentItem>> {
        return combine(
            dao.observeCommentsForPost(postId),
            dao.getAllUsers()
        ) { comments, users ->
            val userMap = users.associateBy { it.id }

            // Group replies by parentCommentId
            val rootComments = comments.filter { it.parentCommentId == null }
            val repliesMap = comments.filter { it.parentCommentId != null }.groupBy { it.parentCommentId!! }

            rootComments.map { root ->
                val author = userMap[root.authorId] ?: UserEntity(
                    id = root.authorId,
                    username = "unknown",
                    email = "",
                    passwordHash = "",
                    displayName = "User",
                    avatarUrl = ""
                )
                val replyItems = (repliesMap[root.id] ?: emptyList()).map { reply ->
                    val replyAuthor = userMap[reply.authorId] ?: author
                    CommentItem(
                        comment = reply,
                        author = replyAuthor,
                        isLikedByMe = false,
                        isMyComment = reply.authorId == currentUserId
                    )
                }

                CommentItem(
                    comment = root,
                    author = author,
                    isLikedByMe = false,
                    isMyComment = root.authorId == currentUserId,
                    replies = replyItems
                )
            }
        }
    }

    suspend fun addComment(
        postId: String,
        authorId: String,
        content: String,
        parentCommentId: String? = null
    ): Result<CommentEntity> {
        val cleanContent = content.trim()
        if (cleanContent.isBlank()) {
            return Result.failure(Exception("Comment cannot be empty"))
        }

        val commentId = "comm_${UUID.randomUUID().toString().take(8)}"
        val comment = CommentEntity(
            id = commentId,
            postId = postId,
            authorId = authorId,
            parentCommentId = parentCommentId,
            content = cleanContent,
            createdAt = System.currentTimeMillis()
        )
        dao.insertComment(comment)

        // Update post comment count
        val totalComments = dao.getCommentCountForPost(postId)
        dao.updatePostCommentCount(postId, totalComments)

        // Notification logic
        val post = dao.getPostById(postId)
        if (post != null && post.authorId != authorId) {
            dao.insertNotification(
                NotificationEntity(
                    id = "notif_${UUID.randomUUID().toString().take(8)}",
                    recipientId = post.authorId,
                    actorId = authorId,
                    type = if (parentCommentId != null) "REPLY" else "COMMENT",
                    targetId = postId,
                    summary = "commented: '${cleanContent.take(35)}...'"
                )
            )
        }

        // Check for mentions like @username
        val mentionRegex = Regex("""@([a-zA-Z0-9_]+)""")
        mentionRegex.findAll(cleanContent).forEach { match ->
            val mentionedUsername = match.groupValues[1]
            val mentionedUser = dao.getUserByUsername(mentionedUsername)
            if (mentionedUser != null && mentionedUser.id != authorId) {
                dao.insertNotification(
                    NotificationEntity(
                        id = "notif_${UUID.randomUUID().toString().take(8)}",
                        recipientId = mentionedUser.id,
                        actorId = authorId,
                        type = "MENTION",
                        targetId = postId,
                        summary = "mentioned you in a comment: '${cleanContent.take(30)}...'"
                    )
                )
            }
        }

        return Result.success(comment)
    }

    suspend fun deleteComment(commentId: String, currentUserId: String, isAdmin: Boolean): Result<Unit> {
        val comment = dao.getCommentById(commentId) ?: return Result.failure(Exception("Comment not found"))
        if (comment.authorId != currentUserId && !isAdmin) {
            return Result.failure(Exception("You can only delete your own comments"))
        }
        dao.deleteComment(commentId)
        val count = dao.getCommentCountForPost(comment.postId)
        dao.updatePostCommentCount(comment.postId, count)
        return Result.success(Unit)
    }

    suspend fun toggleLikeComment(commentId: String, currentUserId: String) {
        val isLiked = dao.isCommentLikedBy(commentId, currentUserId) > 0
        val comment = dao.getCommentById(commentId) ?: return

        if (isLiked) {
            dao.deleteCommentLike(commentId, currentUserId)
            val newCount = maxOf(0, comment.likeCount - 1)
            dao.updateCommentLikeCount(commentId, newCount)
        } else {
            dao.insertCommentLike(CommentLikeEntity(id = "$commentId-$currentUserId", commentId = commentId, userId = currentUserId))
            val newCount = comment.likeCount + 1
            dao.updateCommentLikeCount(commentId, newCount)
        }
    }

    // --- FOLLOW SYSTEM ---
    fun observeIsFollowing(followerId: String, followingId: String): Flow<Boolean> {
        return dao.observeIsFollowing(followerId, followingId).map { it > 0 }
    }

    suspend fun toggleFollow(followerId: String, targetUserId: String) {
        if (followerId == targetUserId) return // Cannot follow self
        val isFollowing = dao.isFollowing(followerId, targetUserId) > 0

        val follower = dao.getUserById(followerId) ?: return
        val target = dao.getUserById(targetUserId) ?: return

        if (isFollowing) {
            dao.deleteFollow(followerId, targetUserId)
            dao.updateUser(follower.copy(followingCount = maxOf(0, follower.followingCount - 1)))
            dao.updateUser(target.copy(followersCount = maxOf(0, target.followersCount - 1)))
        } else {
            dao.insertFollow(FollowEntity(id = "$followerId-$targetUserId", followerId = followerId, followingId = targetUserId))
            dao.updateUser(follower.copy(followingCount = follower.followingCount + 1))
            dao.updateUser(target.copy(followersCount = target.followersCount + 1))

            // Create notification
            dao.insertNotification(
                NotificationEntity(
                    id = "notif_${UUID.randomUUID().toString().take(8)}",
                    recipientId = targetUserId,
                    actorId = followerId,
                    type = "FOLLOW",
                    targetId = null,
                    summary = "started following you."
                )
            )
        }
    }

    suspend fun getFollowers(userId: String): List<UserEntity> {
        val ids = dao.observeFollowerIds(userId).firstOrNull() ?: emptyList()
        return ids.mapNotNull { dao.getUserById(it) }
    }

    suspend fun getFollowing(userId: String): List<UserEntity> {
        val ids = dao.observeFollowingIds(userId).firstOrNull() ?: emptyList()
        return ids.mapNotNull { dao.getUserById(it) }
    }

    // --- STORIES ---
    fun observeActiveStoryGroups(currentUserId: String): Flow<List<StoryGroup>> {
        return combine(
            dao.observeActiveStories(),
            dao.getAllUsers(),
            dao.observeUserStoryViews(currentUserId)
        ) { stories, users, views ->
            val userMap = users.associateBy { it.id }
            val viewedStoryIds = views.map { it.storyId }.toSet()

            // Group stories by author
            val grouped = stories.groupBy { it.authorId }

            grouped.mapNotNull { (authorId, authorStories) ->
                val author = userMap[authorId] ?: return@mapNotNull null
                val hasUnseen = authorStories.any { !viewedStoryIds.contains(it.id) }
                StoryGroup(
                    author = author,
                    stories = authorStories.sortedBy { it.createdAt },
                    hasUnseen = hasUnseen,
                    isMyStory = authorId == currentUserId
                )
            }.sortedWith(compareByDescending<StoryGroup> { it.isMyStory }.thenByDescending { it.hasUnseen })
        }
    }

    suspend fun createStory(
        authorId: String,
        mediaUrl: String,
        caption: String,
        isVideo: Boolean
    ): Result<StoryEntity> {
        val storyId = "story_${UUID.randomUUID().toString().take(8)}"
        val now = System.currentTimeMillis()
        val story = StoryEntity(
            id = storyId,
            authorId = authorId,
            mediaUrl = mediaUrl.trim(),
            caption = caption.trim(),
            isVideo = isVideo,
            createdAt = now,
            expiresAt = now + (24 * 60 * 60 * 1000)
        )
        dao.insertStory(story)
        return Result.success(story)
    }

    suspend fun viewStory(storyId: String, viewerId: String) {
        if (dao.hasViewedStory(storyId, viewerId) == 0) {
            dao.insertStoryView(StoryViewEntity(id = "$storyId-$viewerId", storyId = storyId, viewerId = viewerId))
        }
    }

    suspend fun deleteStory(storyId: String, currentUserId: String): Result<Unit> {
        dao.deleteStory(storyId)
        return Result.success(Unit)
    }

    suspend fun getStoryViewers(storyId: String): List<UserEntity> {
        val viewerIds = dao.getStoryViewerIds(storyId)
        return viewerIds.mapNotNull { dao.getUserById(it) }
    }

    // --- DIRECT MESSAGES ---
    fun observeConversations(currentUserId: String): Flow<List<ConversationItem>> {
        return combine(
            dao.observeConversationsForUser(currentUserId),
            dao.getAllUsers()
        ) { convs, users ->
            val userMap = users.associateBy { it.id }

            convs.mapNotNull { conv ->
                val otherUserId = if (conv.user1Id == currentUserId) conv.user2Id else conv.user1Id
                val otherUser = userMap[otherUserId] ?: return@mapNotNull null
                val unread = if (conv.user1Id == currentUserId) conv.user1UnreadCount else conv.user2UnreadCount

                ConversationItem(
                    conversation = conv,
                    otherUser = otherUser,
                    unreadCount = unread
                )
            }
        }
    }

    fun observeMessages(conversationId: String, currentUserId: String): Flow<List<MessageItem>> {
        return combine(
            dao.observeMessagesForConversation(conversationId),
            dao.getAllUsers()
        ) { messages, users ->
            val userMap = users.associateBy { it.id }
            messages.mapNotNull { msg ->
                val sender = userMap[msg.senderId] ?: return@mapNotNull null
                MessageItem(
                    message = msg,
                    sender = sender,
                    isFromMe = msg.senderId == currentUserId
                )
            }
        }
    }

    suspend fun sendMessage(senderId: String, recipientId: String, content: String): Result<MessageEntity> {
        val cleanContent = content.trim()
        if (cleanContent.isBlank()) return Result.failure(Exception("Message cannot be empty"))

        var conv = dao.getConversationBetween(senderId, recipientId)
        val now = System.currentTimeMillis()

        if (conv == null) {
            val convId = "conv_${UUID.randomUUID().toString().take(8)}"
            conv = ConversationEntity(
                id = convId,
                user1Id = senderId,
                user2Id = recipientId,
                lastMessage = cleanContent,
                lastMessageTimestamp = now,
                user1UnreadCount = 0,
                user2UnreadCount = 1
            )
            dao.insertConversation(conv)
        } else {
            val isSenderUser1 = conv.user1Id == senderId
            val updated = conv.copy(
                lastMessage = cleanContent,
                lastMessageTimestamp = now,
                user1UnreadCount = if (isSenderUser1) conv.user1UnreadCount else conv.user1UnreadCount + 1,
                user2UnreadCount = if (isSenderUser1) conv.user2UnreadCount + 1 else conv.user2UnreadCount
            )
            dao.insertConversation(updated)
        }

        val msgId = "msg_${UUID.randomUUID().toString().take(8)}"
        val message = MessageEntity(
            id = msgId,
            conversationId = conv.id,
            senderId = senderId,
            recipientId = recipientId,
            content = cleanContent,
            isRead = false,
            timestamp = now
        )
        dao.insertMessage(message)
        return Result.success(message)
    }

    suspend fun deleteMessage(messageId: String) {
        dao.deleteMessage(messageId)
    }

    suspend fun markConversationAsRead(convId: String, currentUserId: String) {
        dao.markMessagesAsRead(convId, currentUserId)
    }

    // --- SEARCH ---
    suspend fun search(query: String, currentUserId: String): SearchResult {
        val clean = query.trim()
        if (clean.isBlank()) return SearchResult()

        val matchingUsers = dao.searchUsers(clean)
        val matchingPosts = dao.searchPosts(clean)

        val userMap = dao.getAllUsers().firstOrNull()?.associateBy { it.id } ?: emptyMap()
        val postItems = matchingPosts.mapNotNull { post ->
            val author = userMap[post.authorId] ?: return@mapNotNull null
            PostItem(
                post = post,
                author = author,
                isLikedByMe = false,
                isSavedByMe = false,
                isMyPost = post.authorId == currentUserId
            )
        }

        // Hashtag extraction
        val allHashtags = matchingPosts.flatMap {
            it.hashtags.split(" ").filter { tag -> tag.startsWith("#") }
        }.distinct().filter { it.contains(clean, ignoreCase = true) }

        return SearchResult(
            users = matchingUsers,
            posts = postItems,
            hashtags = allHashtags
        )
    }

    // --- NOTIFICATIONS ---
    fun observeNotifications(currentUserId: String): Flow<List<NotificationItem>> {
        return combine(
            dao.observeNotificationsForUser(currentUserId),
            dao.getAllUsers()
        ) { notifs, users ->
            val userMap = users.associateBy { it.id }
            notifs.mapNotNull { notif ->
                val actor = userMap[notif.actorId] ?: return@mapNotNull null
                NotificationItem(notification = notif, actor = actor)
            }
        }
    }

    fun observeUnreadNotificationsCount(currentUserId: String): Flow<Int> {
        return dao.observeUnreadNotificationsCount(currentUserId)
    }

    suspend fun markNotificationRead(notifId: String) {
        dao.markNotificationRead(notifId)
    }

    suspend fun markAllNotificationsRead(currentUserId: String) {
        dao.markAllNotificationsRead(currentUserId)
    }

    suspend fun clearAllNotifications(currentUserId: String) {
        dao.clearNotifications(currentUserId)
    }

    // --- MODERATION, REPORTS & BLOCKING ---
    suspend fun report(reporterId: String, targetType: String, targetId: String, reason: String): Result<Unit> {
        val report = ReportEntity(
            id = "rep_${UUID.randomUUID().toString().take(8)}",
            reporterId = reporterId,
            targetType = targetType,
            targetId = targetId,
            reason = reason.trim(),
            status = "PENDING"
        )
        dao.insertReport(report)
        return Result.success(Unit)
    }

    suspend fun blockUser(currentUserId: String, targetUserId: String) {
        if (currentUserId == targetUserId) return
        dao.insertBlock(BlockEntity(id = "$currentUserId-$targetUserId", blockerId = currentUserId, blockedUserId = targetUserId))
    }

    suspend fun unblockUser(currentUserId: String, targetUserId: String) {
        dao.deleteBlock(currentUserId, targetUserId)
    }

    fun observeBlockedUsers(currentUserId: String): Flow<List<UserEntity>> {
        return combine(
            dao.observeBlockedUserIds(currentUserId),
            dao.getAllUsers()
        ) { ids, users ->
            val idSet = ids.toSet()
            users.filter { idSet.contains(it.id) }
        }
    }

    // --- ADMIN PANEL ---
    suspend fun getPlatformStats(): PlatformStats {
        val totalUsers = dao.getTotalUsersCount()
        val totalPosts = dao.getTotalPostsCount()
        val totalComments = dao.getTotalCommentsCount()
        val totalStories = dao.getTotalActiveStoriesCount()
        val pendingReports = dao.getPendingReportsCount()
        return PlatformStats(
            totalUsers = totalUsers,
            activeUsers = maxOf(1, totalUsers),
            totalPosts = totalPosts,
            totalComments = totalComments,
            totalStories = totalStories,
            pendingReports = pendingReports
        )
    }

    fun observeAdminReports(): Flow<List<ReportItem>> {
        return combine(
            dao.observeAllReports(),
            dao.getAllUsers()
        ) { reports, users ->
            val userMap = users.associateBy { it.id }
            reports.map { rep ->
                val reporter = userMap[rep.reporterId]
                ReportItem(
                    report = rep,
                    reporter = reporter,
                    targetSummary = "Target ${rep.targetType}: ${rep.targetId}"
                )
            }
        }
    }

    suspend fun resolveReport(reportId: String, action: String) {
        dao.updateReportStatus(reportId, action)
    }

    suspend fun adminRemovePost(postId: String) {
        dao.markPostRemoved(postId)
    }

    suspend fun adminRemoveComment(commentId: String) {
        dao.markCommentRemoved(commentId)
    }

    suspend fun adminToggleSuspendUser(userId: String) {
        val user = dao.getUserById(userId) ?: return
        dao.updateUser(user.copy(isSuspended = !user.isSuspended))
    }

    suspend fun adminDeleteUser(userId: String) {
        dao.deleteUser(userId)
    }

    fun getAllUsers(): Flow<List<UserEntity>> = dao.getAllUsers()
}
