package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
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
import com.example.util.SecurityUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        UserEntity::class,
        PostEntity::class,
        PostLikeEntity::class,
        CommentEntity::class,
        CommentLikeEntity::class,
        FollowEntity::class,
        StoryEntity::class,
        StoryViewEntity::class,
        SavedPostEntity::class,
        ConversationEntity::class,
        MessageEntity::class,
        NotificationEntity::class,
        ReportEntity::class,
        BlockEntity::class,
        UserSessionEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun socialDao(): SocialDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "suyaib_social.db"
                ).addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        // Seed database
                        CoroutineScope(Dispatchers.IO).launch {
                            getInstance(context).seedInitialData()
                        }
                    }
                }).build()
                INSTANCE = instance
                instance
            }
        }
    }

    suspend fun seedInitialData() {
        val dao = socialDao()
        val defaultPasswordHash = SecurityUtils.hashPassword("Password123!")

        // Seed Users
        val users = listOf(
            UserEntity(
                id = "user_admin",
                username = "admin",
                email = "admin@suyaib.social",
                passwordHash = SecurityUtils.hashPassword("Admin@2026"),
                displayName = "SUYAIB Security & Admin",
                avatarUrl = "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=400&q=80",
                bio = "Official Administrator & Platform Integrity Officer for SUYAIB SOCIAL.",
                website = "https://suyaib.social/admin",
                isVerified = true,
                isAdmin = true,
                followersCount = 1420,
                followingCount = 42,
                postsCount = 3
            ),
            UserEntity(
                id = "user_suyaib",
                username = "suyaib",
                email = "dtsuyaib009@gmail.com",
                passwordHash = defaultPasswordHash,
                displayName = "Suyaib Al-Sayed",
                avatarUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=400&q=80",
                bio = "Founder & Lead Architect at SUYAIB SOCIAL 🌐 Building next-gen digital experiences & community tools.",
                website = "https://suyaib.social",
                isVerified = true,
                isAdmin = false,
                followersCount = 3280,
                followingCount = 180,
                postsCount = 4
            ),
            UserEntity(
                id = "user_elena",
                username = "elena_designs",
                email = "elena@designcraft.io",
                passwordHash = defaultPasswordHash,
                displayName = "Elena Vance",
                avatarUrl = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=400&q=80",
                bio = "Senior Product & Motion Designer 🎨 Minimalist interfaces & generative typography.",
                website = "https://dribbble.com/elena_designs",
                isVerified = true,
                isAdmin = false,
                followersCount = 890,
                followingCount = 210,
                postsCount = 3
            ),
            UserEntity(
                id = "user_alex",
                username = "alex_nomad",
                email = "alex@worldcaptures.org",
                passwordHash = defaultPasswordHash,
                displayName = "Alex Rivera",
                avatarUrl = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=400&q=80",
                bio = "Visual storyteller 🌍 Chasing golden hour in Kyoto, Iceland & Patagonia.",
                website = "https://nomadlens.photos",
                isVerified = false,
                isAdmin = false,
                followersCount = 650,
                followingCount = 340,
                postsCount = 3
            ),
            UserEntity(
                id = "user_maya",
                username = "maya_codes",
                email = "maya@techdev.net",
                passwordHash = defaultPasswordHash,
                displayName = "Maya Lin",
                avatarUrl = "https://images.unsplash.com/photo-1517841905240-472988babdf9?w=400&q=80",
                bio = "Android Engineer & Jetpack Compose devotee 🚀 Building open-source mobile tools.",
                website = "https://github.com/maya-lin",
                isVerified = true,
                isAdmin = false,
                followersCount = 1200,
                followingCount = 150,
                postsCount = 2
            )
        )
        dao.insertUsers(users)

        // Seed default active session as "suyaib"
        dao.setSession(UserSessionEntity(id = 1, userId = "user_suyaib"))

        // Seed Posts
        val now = System.currentTimeMillis()
        val posts = listOf(
            PostEntity(
                id = "post_1",
                authorId = "user_suyaib",
                caption = "Welcome to SUYAIB SOCIAL! We designed this platform from the ground up for authentic connections, fluid micro-interactions, and real-time community engagement. Tap follow and share your thoughts! 🚀✨",
                mediaUrls = "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=1080&q=80,https://images.unsplash.com/photo-1550745165-9bc0b252726f?w=1080&q=80",
                isVideo = false,
                hashtags = "#suyaibsocial #launch #community #design #modern",
                location = "Silicon Valley, CA",
                likeCount = 142,
                commentCount = 18,
                shareCount = 34,
                createdAt = now - (2 * 60 * 60 * 1000)
            ),
            PostEntity(
                id = "post_2",
                authorId = "user_elena",
                caption = "Explored a dark-mode obsidian aesthetic for our new design system tokens today. What do you think of these high-contrast neon violet accents? 💜🎨 Feedback welcome @suyaib",
                mediaUrls = "https://images.unsplash.com/photo-1600132806370-bf17e65e942f?w=1080&q=80",
                isVideo = false,
                hashtags = "#design #uidesign #typography #darkmode",
                location = "Design Studio, NYC",
                likeCount = 89,
                commentCount = 12,
                shareCount = 15,
                createdAt = now - (5 * 60 * 60 * 1000)
            ),
            PostEntity(
                id = "post_3",
                authorId = "user_alex",
                caption = "Golden morning mist settling over Mount Fuji. Took 3 hours of hiking before sunrise, but this stillness was worth every single step. 🏔️🌅",
                mediaUrls = "https://images.unsplash.com/photo-1493976040374-85c8e12f0c0e?w=1080&q=80,https://images.unsplash.com/photo-1503899036084-c55cdd92da26?w=1080&q=80",
                isVideo = false,
                hashtags = "#travel #japan #photography #adventure #nature",
                location = "Fujinomiya, Shizuoka, Japan",
                likeCount = 215,
                commentCount = 24,
                shareCount = 42,
                createdAt = now - (10 * 60 * 60 * 1000)
            ),
            PostEntity(
                id = "post_4",
                authorId = "user_maya",
                caption = "Just open-sourced a custom Jetpack Compose canvas animation library! Sub-millisecond recomposition with Spring physics. Try it out! 📱⚡",
                mediaUrls = "https://images.unsplash.com/photo-1555066931-4365d14bab8c?w=1080&q=80",
                isVideo = true,
                hashtags = "#android #kotlin #jetpackcompose #developer #tech",
                location = "Austin, TX",
                likeCount = 98,
                commentCount = 14,
                shareCount = 27,
                createdAt = now - (18 * 60 * 60 * 1000)
            )
        )
        dao.insertPosts(posts)

        // Seed Comments
        val comments = listOf(
            CommentEntity(
                id = "comm_1",
                postId = "post_1",
                authorId = "user_elena",
                parentCommentId = null,
                content = "Congratulations @suyaib! The interface feels incredibly snappy and clean. Love the responsive animations!",
                likeCount = 8,
                createdAt = now - (100 * 60 * 1000)
            ),
            CommentEntity(
                id = "comm_2",
                postId = "post_1",
                authorId = "user_suyaib",
                parentCommentId = "comm_1",
                content = "@elena_designs Thank you Elena! Your design token contributions made this possible.",
                likeCount = 4,
                createdAt = now - (80 * 60 * 1000)
            ),
            CommentEntity(
                id = "comm_3",
                postId = "post_2",
                authorId = "user_maya",
                parentCommentId = null,
                content = "The purple violet contrast is gorgeous! Are these tokens exported to Compose Color tokens yet?",
                likeCount = 5,
                createdAt = now - (200 * 60 * 1000)
            ),
            CommentEntity(
                id = "comm_4",
                postId = "post_3",
                authorId = "user_suyaib",
                parentCommentId = null,
                content = "Breathtaking shot Alex! The lighting on the second slide is unreal.",
                likeCount = 6,
                createdAt = now - (350 * 60 * 1000)
            )
        )
        dao.insertComments(comments)

        // Seed Follows
        val follows = listOf(
            FollowEntity(id = "user_suyaib-user_elena", followerId = "user_suyaib", followingId = "user_elena"),
            FollowEntity(id = "user_suyaib-user_alex", followerId = "user_suyaib", followingId = "user_alex"),
            FollowEntity(id = "user_elena-user_suyaib", followerId = "user_elena", followingId = "user_suyaib"),
            FollowEntity(id = "user_maya-user_suyaib", followerId = "user_maya", followingId = "user_suyaib"),
            FollowEntity(id = "user_alex-user_suyaib", followerId = "user_alex", followingId = "user_suyaib")
        )
        dao.insertFollows(follows)

        // Seed Likes
        dao.insertPostLike(PostLikeEntity(id = "post_1-user_elena", postId = "post_1", userId = "user_elena"))
        dao.insertPostLike(PostLikeEntity(id = "post_1-user_alex", postId = "post_1", userId = "user_alex"))
        dao.insertPostLike(PostLikeEntity(id = "post_2-user_suyaib", postId = "post_2", userId = "user_suyaib"))

        // Seed Active Stories (expires in 24 hours)
        val stories = listOf(
            StoryEntity(
                id = "story_1",
                authorId = "user_suyaib",
                mediaUrl = "https://images.unsplash.com/photo-1522071820081-009f0129c71c?w=1080&q=80",
                caption = "Late night team sync 💻 Finalizing the release build!",
                createdAt = now - (3 * 60 * 60 * 1000),
                expiresAt = now + (21 * 60 * 60 * 1000)
            ),
            StoryEntity(
                id = "story_2",
                authorId = "user_elena",
                mediaUrl = "https://images.unsplash.com/photo-1542744094-3a31f272c490?w=1080&q=80",
                caption = "Color palettes for autumn collection 🍁",
                createdAt = now - (5 * 60 * 60 * 1000),
                expiresAt = now + (19 * 60 * 60 * 1000)
            ),
            StoryEntity(
                id = "story_3",
                authorId = "user_alex",
                mediaUrl = "https://images.unsplash.com/photo-1476514525535-07fb3b4ae5f1?w=1080&q=80",
                caption = "Boat ride through Lake Brienz, Switzerland 🛥️",
                createdAt = now - (8 * 60 * 60 * 1000),
                expiresAt = now + (16 * 60 * 60 * 1000)
            )
        )
        dao.insertStories(stories)

        // Seed Notifications for user_suyaib
        val notifications = listOf(
            NotificationEntity(
                id = "notif_1",
                recipientId = "user_suyaib",
                actorId = "user_elena",
                type = "LIKE",
                targetId = "post_1",
                summary = "liked your post: 'Welcome to SUYAIB SOCIAL!'",
                isRead = false,
                timestamp = now - (45 * 60 * 1000)
            ),
            NotificationEntity(
                id = "notif_2",
                recipientId = "user_suyaib",
                actorId = "user_elena",
                type = "COMMENT",
                targetId = "post_1",
                summary = "commented: 'Congratulations @suyaib! The interface feels...'",
                isRead = false,
                timestamp = now - (90 * 60 * 1000)
            ),
            NotificationEntity(
                id = "notif_3",
                recipientId = "user_suyaib",
                actorId = "user_maya",
                type = "FOLLOW",
                targetId = null,
                summary = "started following you.",
                isRead = true,
                timestamp = now - (3 * 60 * 60 * 1000)
            ),
            NotificationEntity(
                id = "notif_4",
                recipientId = "user_suyaib",
                actorId = "user_alex",
                type = "MENTION",
                targetId = "post_3",
                summary = "mentioned you in a photo caption.",
                isRead = true,
                timestamp = now - (8 * 60 * 60 * 1000)
            )
        )
        dao.insertNotifications(notifications)

        // Seed Conversation and Messages
        val convId = "conv_suyaib_elena"
        dao.insertConversation(
            ConversationEntity(
                id = convId,
                user1Id = "user_suyaib",
                user2Id = "user_elena",
                lastMessage = "Thanks for the feedback! Just deployed the update.",
                lastMessageTimestamp = now - (30 * 60 * 1000),
                user1UnreadCount = 0,
                user2UnreadCount = 1
            )
        )

        val messages = listOf(
            MessageEntity(
                id = "msg_1",
                conversationId = convId,
                senderId = "user_elena",
                recipientId = "user_suyaib",
                content = "Hey Suyaib! How's the new story viewer feature shaping up?",
                isRead = true,
                timestamp = now - (120 * 60 * 1000)
            ),
            MessageEntity(
                id = "msg_2",
                conversationId = convId,
                senderId = "user_suyaib",
                recipientId = "user_elena",
                content = "It's working beautifully with 24-hour expiration and viewer tracking!",
                isRead = true,
                timestamp = now - (90 * 60 * 1000)
            ),
            MessageEntity(
                id = "msg_3",
                conversationId = convId,
                senderId = "user_suyaib",
                recipientId = "user_elena",
                content = "Thanks for the feedback! Just deployed the update.",
                isRead = true,
                timestamp = now - (30 * 60 * 1000)
            )
        )
        dao.insertMessages(messages)
    }
}
