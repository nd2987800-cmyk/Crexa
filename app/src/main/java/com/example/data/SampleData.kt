package com.example.data

import com.example.data.local.entities.*

object SampleData {
    val sampleUsers = listOf(
        UserEntity(
            id = "user_crexa_admin",
            username = "crexa",
            fullName = "CREXA Official 👑",
            bio = "Official Founder & Admin Account of CREXA ⚡ | Redefining Short Video & Creator Economy 🚀",
            avatarUrl = "android.resource://com.aistudio.lumina.social/drawable/img_crexa_brand_logo_1786179516858",
            email = "nd2987800@gmail.com",
            followersCount = 125000,
            followingCount = 12,
            postsCount = 45,
            isVerified = true,
            isFollowing = false,
            isCurrentUser = true,
            website = "https://crexa.app"
        ),
        UserEntity(
            id = "user_maya",
            username = "maya_lens",
            fullName = "Maya Lin",
            bio = "Architectural Photography & Urban Explorations 🌆 | Tokyo & NYC",
            avatarUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=500&auto=format&fit=crop&q=80",
            followersCount = 8920,
            followingCount = 310,
            postsCount = 142,
            isVerified = true,
            isFollowing = true
        ),
        UserEntity(
            id = "user_kai",
            username = "kai_cyber",
            fullName = "Kai Tanaka",
            bio = "Cyberpunk 3D visual artist ⚡ Synthwave & Neon Dreams 🎆",
            avatarUrl = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=500&auto=format&fit=crop&q=80",
            followersCount = 15400,
            followingCount = 180,
            postsCount = 95,
            isVerified = true,
            isFollowing = true
        ),
        UserEntity(
            id = "user_sophia",
            username = "sophia_travels",
            fullName = "Sophia Rossi",
            bio = "Wanderlust & Coastal Horizons 🌊 | Italian Riviera | Natural light addict ☀️",
            avatarUrl = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=500&auto=format&fit=crop&q=80",
            followersCount = 24500,
            followingCount = 520,
            postsCount = 310,
            isVerified = true,
            isFollowing = false
        ),
        UserEntity(
            id = "user_liam",
            username = "liam_sound",
            fullName = "Liam Cooper",
            bio = "Electronic Beats & Music Producer 🎧 | Soundscapes & Ambient Reels 🎶",
            avatarUrl = "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=500&auto=format&fit=crop&q=80",
            followersCount = 5100,
            followingCount = 290,
            postsCount = 64,
            isVerified = false,
            isFollowing = true
        )
    )

    val samplePosts = listOf(
        PostEntity(
            id = "post_1",
            userId = "user_kai",
            mediaUrl = "android.resource://com.aistudio.lumina.social/drawable/img_sample_post_1_1786177193097",
            mediaType = "IMAGE",
            caption = "Neon reflections after midnight rain in Shinjuku. The city never truly sleeps ⚡ What's your favorite night spot?",
            hashtags = "#cyberpunk,#tokyo,#neonvibes,#streetphotography,#lumina",
            location = "Shinjuku, Tokyo",
            likesCount = 1420,
            commentsCount = 84,
            sharesCount = 31,
            isLiked = true,
            isSaved = true,
            timestamp = System.currentTimeMillis() - 1000 * 60 * 45
        ),
        PostEntity(
            id = "post_2",
            userId = "user_sophia",
            mediaUrl = "android.resource://com.aistudio.lumina.social/drawable/img_sample_post_2_1786177203745",
            mediaType = "IMAGE",
            caption = "Golden hour ocean breeze 🌊 Warm coastal rays and endless horizons. Paradise found ✨",
            hashtags = "#sunset,#coastal,#beachvibes,#goldenhour,#travel",
            location = "Amalfi Coast, Italy",
            likesCount = 2890,
            commentsCount = 120,
            sharesCount = 65,
            isLiked = false,
            isSaved = false,
            timestamp = System.currentTimeMillis() - 1000 * 60 * 180
        ),
        PostEntity(
            id = "post_3",
            userId = "user_maya",
            mediaUrl = "android.resource://com.aistudio.lumina.social/drawable/img_sample_post_3_1786177217178",
            mediaType = "IMAGE",
            caption = "Morning espresso surrounded by minimalist timber and clean shadows. The quiet power of thoughtful interior design ☕",
            hashtags = "#coffee,#architecture,#minimalism,#design,#interior",
            location = "Kyoto, Japan",
            likesCount = 940,
            commentsCount = 42,
            sharesCount = 18,
            isLiked = true,
            isSaved = false,
            timestamp = System.currentTimeMillis() - 1000 * 60 * 360
        )
    )

    val sampleStories = listOf(
        StoryEntity(
            id = "story_1",
            userId = "user_me",
            mediaUrl = "android.resource://com.aistudio.lumina.social/drawable/img_lumina_logo_1786177176474",
            caption = "Testing the new Lumina Studio lens 🚀",
            timestamp = System.currentTimeMillis() - 1000 * 60 * 30,
            isSeen = true
        ),
        StoryEntity(
            id = "story_2",
            userId = "user_maya",
            mediaUrl = "android.resource://com.aistudio.lumina.social/drawable/img_sample_post_3_1786177217178",
            caption = "Brewing fresh roast in Kyoto ☕",
            timestamp = System.currentTimeMillis() - 1000 * 60 * 90,
            isSeen = false
        ),
        StoryEntity(
            id = "story_3",
            userId = "user_kai",
            mediaUrl = "android.resource://com.aistudio.lumina.social/drawable/img_sample_post_1_1786177193097",
            caption = "Late night render finished ⚡",
            timestamp = System.currentTimeMillis() - 1000 * 60 * 240,
            isSeen = false
        ),
        StoryEntity(
            id = "story_4",
            userId = "user_sophia",
            mediaUrl = "android.resource://com.aistudio.lumina.social/drawable/img_sample_post_2_1786177203745",
            caption = "Sunset swim! 🌊",
            timestamp = System.currentTimeMillis() - 1000 * 60 * 400,
            isSeen = false
        )
    )

    val sampleReels = listOf(
        ReelEntity(
            id = "reel_1",
            userId = "user_kai",
            videoUrl = "sample_reel_cyber",
            thumbnailUrl = "android.resource://com.aistudio.lumina.social/drawable/img_sample_post_1_1786177193097",
            caption = "Creating a 3D hologram in Unreal Engine 5 🚀 Swipe up for part 2! #3d #cyberpunk #cgi",
            audioTitle = "Neon Pulse (Original Mix)",
            audioArtist = "Kai Tanaka",
            likesCount = 18400,
            commentsCount = 620,
            sharesCount = 1200,
            isLiked = true,
            isSaved = true,
            timestamp = System.currentTimeMillis() - 1000 * 60 * 120
        ),
        ReelEntity(
            id = "reel_2",
            userId = "user_sophia",
            videoUrl = "sample_reel_beach",
            thumbnailUrl = "android.resource://com.aistudio.lumina.social/drawable/img_sample_post_2_1786177203745",
            caption = "5 hidden beaches in Positano you CANNOT miss this summer 🏖️ Save this for your next trip!",
            audioTitle = "Italian Waves Vol. 4",
            audioArtist = "Sophia Travels",
            likesCount = 34200,
            commentsCount = 1150,
            sharesCount = 4800,
            isLiked = false,
            isSaved = true,
            timestamp = System.currentTimeMillis() - 1000 * 60 * 300
        ),
        ReelEntity(
            id = "reel_3",
            userId = "user_maya",
            videoUrl = "sample_reel_coffee",
            thumbnailUrl = "android.resource://com.aistudio.lumina.social/drawable/img_sample_post_3_1786177217178",
            caption = "Perfecting latte art in 15 seconds ☕ Rosetta pattern breakdown!",
            audioTitle = "Chill Lofi Morning",
            audioArtist = "Liam Cooper",
            likesCount = 9800,
            commentsCount = 210,
            sharesCount = 540,
            isLiked = true,
            isSaved = false,
            timestamp = System.currentTimeMillis() - 1000 * 60 * 600
        )
    )

    val sampleComments = listOf(
        CommentEntity(
            id = "c_1",
            postId = "post_1",
            userId = "user_maya",
            text = "These colors are absolutely wild Kai! What camera setup did you use for this shot?",
            likesCount = 14,
            isLiked = true,
            timestamp = System.currentTimeMillis() - 1000 * 60 * 35
        ),
        CommentEntity(
            id = "c_2",
            postId = "post_1",
            userId = "user_sophia",
            text = "Love the wet reflection on the pavement! ✨ Tokyo night vibes are unmatched.",
            likesCount = 8,
            isLiked = false,
            timestamp = System.currentTimeMillis() - 1000 * 60 * 20
        ),
        CommentEntity(
            id = "c_3",
            postId = "post_2",
            userId = "user_kai",
            text = "Need a vacation ASAP looking at this photo! ☀️🌊",
            likesCount = 22,
            isLiked = true,
            timestamp = System.currentTimeMillis() - 1000 * 60 * 100
        )
    )

    val sampleNotifications = listOf(
        NotificationEntity(
            id = "n_1",
            recipientUserId = "user_me",
            actorUserId = "user_kai",
            type = "LIKE",
            postOrReelId = "post_1",
            message = "liked your photo in Tokyo.",
            timestamp = System.currentTimeMillis() - 1000 * 60 * 8,
            isRead = false
        ),
        NotificationEntity(
            id = "n_2",
            recipientUserId = "user_me",
            actorUserId = "user_maya",
            type = "COMMENT",
            postOrReelId = "post_1",
            message = "commented: 'Incredible mood and lighting! ✨'",
            timestamp = System.currentTimeMillis() - 1000 * 60 * 25,
            isRead = false
        ),
        NotificationEntity(
            id = "n_3",
            recipientUserId = "user_me",
            actorUserId = "user_sophia",
            type = "FOLLOW",
            message = "started following you.",
            timestamp = System.currentTimeMillis() - 1000 * 60 * 120,
            isRead = true
        ),
        NotificationEntity(
            id = "n_4",
            recipientUserId = "user_me",
            actorUserId = "user_liam",
            type = "LIKE",
            postOrReelId = "post_2",
            message = "liked your sunset reel.",
            timestamp = System.currentTimeMillis() - 1000 * 60 * 360,
            isRead = true
        ),
        NotificationEntity(
            id = "n_5",
            recipientUserId = "user_me",
            actorUserId = "user_kai",
            type = "COMMENT",
            postOrReelId = "post_3",
            message = "commented: 'Love the minimalist framing ☕'",
            timestamp = System.currentTimeMillis() - 1000 * 60 * 1440,
            isRead = true
        ),
        NotificationEntity(
            id = "n_6",
            recipientUserId = "user_me",
            actorUserId = "user_liam",
            type = "FOLLOW",
            message = "started following you.",
            timestamp = System.currentTimeMillis() - 1000 * 60 * 2880,
            isRead = true
        )
    )

    val sampleMessages = listOf(
        MessageEntity(
            id = "m_1",
            senderUserId = "user_kai",
            receiverUserId = "user_me",
            text = "Hey Alex! Loved your latest motion reel submission. Want to collaborate on a cyber art series?",
            timestamp = System.currentTimeMillis() - 1000 * 60 * 60,
            isRead = true
        ),
        MessageEntity(
            id = "m_2",
            senderUserId = "user_me",
            receiverUserId = "user_kai",
            text = "Thanks Kai! That sounds awesome. I'm finishing up a few keyframes right now.",
            timestamp = System.currentTimeMillis() - 1000 * 60 * 45,
            isRead = true
        ),
        MessageEntity(
            id = "m_3",
            senderUserId = "user_kai",
            receiverUserId = "user_me",
            text = "Sweet! Send over a preview whenever you're ready ⚡",
            timestamp = System.currentTimeMillis() - 1000 * 60 * 30,
            isRead = false
        )
    )
}
