package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.CommentItem
import com.example.data.model.ConversationItem
import com.example.data.model.MessageItem
import com.example.data.model.NotificationItem
import com.example.data.model.PlatformStats
import com.example.data.model.PostItem
import com.example.data.model.ReportItem
import com.example.data.model.SearchResult
import com.example.data.model.StoryGroup
import com.example.data.model.UserEntity
import com.example.data.repository.SocialRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class ScreenNav {
    object Home : ScreenNav()
    object Explore : ScreenNav()
    object CreatePost : ScreenNav()
    object Notifications : ScreenNav()
    object Profile : ScreenNav()
    object AdminDashboard : ScreenNav()
    object DirectMessages : ScreenNav()
    data class ChatDetail(val conversationId: String, val otherUser: UserEntity) : ScreenNav()
    data class UserProfile(val userId: String) : ScreenNav()
    object Settings : ScreenNav()
    object SavedPosts : ScreenNav()
}

@OptIn(ExperimentalCoroutinesApi::class)
class SocialViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    val repository = SocialRepository(db.socialDao())

    // Theme state
    val isDarkTheme = MutableStateFlow(true)

    // Current navigation destination
    private val _currentScreen = MutableStateFlow<ScreenNav>(ScreenNav.Home)
    val currentScreen: StateFlow<ScreenNav> = _currentScreen.asStateFlow()

    // Navigation back stack for inner detail pages
    private val screenStack = mutableListOf<ScreenNav>()

    // Current active user
    val currentUser: StateFlow<UserEntity?> = repository.currentUser.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    // Home feed
    val homeFeed: StateFlow<List<PostItem>> = currentUser.flatMapLatest { user ->
        if (user != null) repository.observeHomeFeed(user.id) else flowOf(emptyList())
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Story groups
    val storyGroups: StateFlow<List<StoryGroup>> = currentUser.flatMapLatest { user ->
        if (user != null) repository.observeActiveStoryGroups(user.id) else flowOf(emptyList())
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Explore posts
    val explorePosts: StateFlow<List<PostItem>> = currentUser.flatMapLatest { user ->
        if (user != null) repository.observeExplorePosts(user.id) else flowOf(emptyList())
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Saved posts
    val savedPosts: StateFlow<List<PostItem>> = currentUser.flatMapLatest { user ->
        if (user != null) repository.observeSavedPosts(user.id) else flowOf(emptyList())
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Notifications
    val notifications: StateFlow<List<NotificationItem>> = currentUser.flatMapLatest { user ->
        if (user != null) repository.observeNotifications(user.id) else flowOf(emptyList())
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val unreadNotificationsCount: StateFlow<Int> = currentUser.flatMapLatest { user ->
        if (user != null) repository.observeUnreadNotificationsCount(user.id) else flowOf(0)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0
    )

    // Conversations
    val conversations: StateFlow<List<ConversationItem>> = currentUser.flatMapLatest { user ->
        if (user != null) repository.observeConversations(user.id) else flowOf(emptyList())
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Active Chat detail
    private val _activeChatConversationId = MutableStateFlow<String?>(null)
    val activeChatMessages: StateFlow<List<MessageItem>> = _activeChatConversationId.flatMapLatest { convId ->
        val user = currentUser.value
        if (convId != null && user != null) {
            repository.observeMessages(convId, user.id)
        } else {
            flowOf(emptyList())
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Admin state
    val adminReports: StateFlow<List<ReportItem>> = repository.observeAdminReports().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _platformStats = MutableStateFlow<PlatformStats?>(null)
    val platformStats: StateFlow<PlatformStats?> = _platformStats.asStateFlow()

    val allUsers: StateFlow<List<UserEntity>> = repository.getAllUsers().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val blockedUsers: StateFlow<List<UserEntity>> = currentUser.flatMapLatest { user ->
        if (user != null) repository.observeBlockedUsers(user.id) else flowOf(emptyList())
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Search state
    val searchQuery = MutableStateFlow("")
    private val _searchResult = MutableStateFlow(SearchResult())
    val searchResult: StateFlow<SearchResult> = _searchResult.asStateFlow()

    // Overlay dialogs / sheets state
    val activeCommentPost = MutableStateFlow<PostItem?>(null)
    val activeStoryGroup = MutableStateFlow<StoryGroup?>(null)
    val showCreateStory = MutableStateFlow(false)
    val showReportDialog = MutableStateFlow<Triple<String, String, String>?>(null) // (type, targetId, title)
    val toastMessage = MutableStateFlow<String?>(null)

    // Comments for active post
    val activePostComments: StateFlow<List<CommentItem>> = activeCommentPost.flatMapLatest { postItem ->
        val user = currentUser.value
        if (postItem != null && user != null) {
            repository.observeCommentsForPost(postItem.post.id, user.id)
        } else {
            flowOf(emptyList())
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    init {
        // Refresh admin metrics periodically or on init
        refreshPlatformStats()
    }

    fun navigateTo(screen: ScreenNav) {
        if (_currentScreen.value != screen) {
            screenStack.add(_currentScreen.value)
            _currentScreen.value = screen
        }
    }

    fun navigateBack(): Boolean {
        if (screenStack.isNotEmpty()) {
            _currentScreen.value = screenStack.removeAt(screenStack.size - 1)
            return true
        }
        if (_currentScreen.value != ScreenNav.Home) {
            _currentScreen.value = ScreenNav.Home
            return true
        }
        return false
    }

    fun showToast(msg: String) {
        toastMessage.value = msg
    }

    fun dismissToast() {
        toastMessage.value = null
    }

    fun toggleTheme() {
        isDarkTheme.value = !isDarkTheme.value
    }

    // --- SEARCH ---
    fun onSearchQueryChanged(query: String) {
        searchQuery.value = query
        viewModelScope.launch {
            val user = currentUser.value
            _searchResult.value = repository.search(query, user?.id ?: "")
        }
    }

    // --- POST ACTIONS ---
    fun toggleLikePost(postId: String) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            repository.toggleLikePost(postId, user.id)
        }
    }

    fun toggleSavePost(postId: String) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            repository.toggleSavePost(postId, user.id)
            showToast("Saved posts updated")
        }
    }

    fun sharePost(postId: String) {
        viewModelScope.launch {
            repository.sharePost(postId)
            showToast("Post shared to network!")
        }
    }

    fun deletePost(postId: String) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            val result = repository.deletePost(postId, user.id, user.isAdmin)
            result.onSuccess {
                showToast("Post deleted")
                if (activeCommentPost.value?.post?.id == postId) {
                    activeCommentPost.value = null
                }
            }.onFailure {
                showToast(it.message ?: "Failed to delete post")
            }
        }
    }

    fun editPostCaption(postId: String, newCaption: String, hashtags: String) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            val res = repository.updatePostCaption(postId, user.id, newCaption, hashtags)
            res.onSuccess { showToast("Post updated") }
                .onFailure { showToast(it.message ?: "Failed to update") }
        }
    }

    fun createPost(
        caption: String,
        mediaUrls: String,
        isVideo: Boolean,
        hashtags: String,
        location: String,
        onSuccess: () -> Unit
    ) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            val res = repository.createPost(user.id, caption, mediaUrls, isVideo, hashtags, location)
            res.onSuccess {
                showToast("Post published successfully!")
                onSuccess()
                _currentScreen.value = ScreenNav.Home
            }.onFailure {
                showToast(it.message ?: "Failed to publish post")
            }
        }
    }

    // --- COMMENTS ---
    fun openComments(post: PostItem) {
        activeCommentPost.value = post
    }

    fun closeComments() {
        activeCommentPost.value = null
    }

    fun addComment(postId: String, content: String, parentCommentId: String? = null) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            val res = repository.addComment(postId, user.id, content, parentCommentId)
            res.onFailure { showToast(it.message ?: "Could not post comment") }
        }
    }

    fun deleteComment(commentId: String) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            repository.deleteComment(commentId, user.id, user.isAdmin)
            showToast("Comment removed")
        }
    }

    fun toggleLikeComment(commentId: String) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            repository.toggleLikeComment(commentId, user.id)
        }
    }

    // --- FOLLOW ---
    fun toggleFollow(targetUserId: String) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            repository.toggleFollow(user.id, targetUserId)
        }
    }

    // --- STORIES ---
    fun openStoryViewer(group: StoryGroup) {
        activeStoryGroup.value = group
        val user = currentUser.value ?: return
        // Mark stories viewed
        viewModelScope.launch {
            group.stories.forEach { story ->
                repository.viewStory(story.id, user.id)
            }
        }
    }

    fun closeStoryViewer() {
        activeStoryGroup.value = null
    }

    fun createStory(mediaUrl: String, caption: String, isVideo: Boolean) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            val res = repository.createStory(user.id, mediaUrl, caption, isVideo)
            res.onSuccess {
                showToast("Story shared! Active for 24 hours.")
                showCreateStory.value = false
            }.onFailure {
                showToast(it.message ?: "Failed to share story")
            }
        }
    }

    fun deleteStory(storyId: String) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            repository.deleteStory(storyId, user.id)
            showToast("Story removed")
            closeStoryViewer()
        }
    }

    // --- CHAT & MESSAGES ---
    fun openChat(otherUser: UserEntity) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            val conv = repository.dao.getConversationBetween(user.id, otherUser.id)
            val convId = conv?.id ?: "conv_${user.id}_${otherUser.id}"
            _activeChatConversationId.value = convId
            repository.markConversationAsRead(convId, user.id)
            navigateTo(ScreenNav.ChatDetail(convId, otherUser))
        }
    }

    fun sendMessage(recipientId: String, text: String) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            val res = repository.sendMessage(user.id, recipientId, text)
            res.onFailure { showToast(it.message ?: "Failed to send message") }
        }
    }

    fun deleteMessage(messageId: String) {
        viewModelScope.launch {
            repository.deleteMessage(messageId)
        }
    }

    // --- NOTIFICATIONS ---
    fun markNotificationRead(notifId: String) {
        viewModelScope.launch { repository.markNotificationRead(notifId) }
    }

    fun markAllNotificationsRead() {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            repository.markAllNotificationsRead(user.id)
            showToast("All marked as read")
        }
    }

    fun clearNotifications() {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            repository.clearAllNotifications(user.id)
            showToast("Notifications cleared")
        }
    }

    // --- REPORT & BLOCK ---
    fun promptReport(type: String, targetId: String, title: String) {
        showReportDialog.value = Triple(type, targetId, title)
    }

    fun submitReport(reason: String) {
        val user = currentUser.value ?: return
        val target = showReportDialog.value ?: return
        viewModelScope.launch {
            repository.report(user.id, target.first, target.second, reason)
            showReportDialog.value = null
            showToast("Report submitted to moderation team.")
            refreshPlatformStats()
        }
    }

    fun blockUser(targetUserId: String) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            repository.blockUser(user.id, targetUserId)
            showToast("User blocked")
        }
    }

    fun unblockUser(targetUserId: String) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            repository.unblockUser(user.id, targetUserId)
            showToast("User unblocked")
        }
    }

    // --- ADMIN DASHBOARD ---
    fun refreshPlatformStats() {
        viewModelScope.launch {
            _platformStats.value = repository.getPlatformStats()
        }
    }

    fun adminResolveReport(reportId: String, action: String) {
        viewModelScope.launch {
            repository.resolveReport(reportId, action)
            refreshPlatformStats()
            showToast("Report marked as $action")
        }
    }

    fun adminRemovePost(postId: String) {
        viewModelScope.launch {
            repository.adminRemovePost(postId)
            refreshPlatformStats()
            showToast("Post removed by Admin")
        }
    }

    fun adminRemoveComment(commentId: String) {
        viewModelScope.launch {
            repository.adminRemoveComment(commentId)
            refreshPlatformStats()
            showToast("Comment removed by Admin")
        }
    }

    fun adminToggleSuspendUser(userId: String) {
        viewModelScope.launch {
            repository.adminToggleSuspendUser(userId)
            showToast("User suspension status updated")
        }
    }

    fun adminDeleteUser(userId: String) {
        viewModelScope.launch {
            repository.adminDeleteUser(userId)
            refreshPlatformStats()
            showToast("User deleted from platform")
        }
    }

    // --- SWITCH ACCOUNT (Dev/Demo convenience) ---
    fun switchUser(userId: String) {
        viewModelScope.launch {
            repository.switchUser(userId)
            _currentScreen.value = ScreenNav.Home
            showToast("Switched account successfully")
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
            _currentScreen.value = ScreenNav.Home
            showToast("Logged out")
        }
    }
}

class SocialViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SocialViewModel::class.java)) {
            return SocialViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
