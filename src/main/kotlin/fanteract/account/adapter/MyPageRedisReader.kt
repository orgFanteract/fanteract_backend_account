package fanteract.account.adapter

import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Component

data class MyPageSnapshot(
    val chatroomCount: Long,
    val chatCount: Long,
    val boardCount: Long,
    val commentCount: Long,
    val restrictedChatCount: Long,
    val restrictedBoardCount: Long,
    val restrictedCommentCount: Long,
    val rebuiltAt: Long,
)

@Component
class MyPageRedisReader(
    private val redis: StringRedisTemplate
) {
    private fun key(userId: Long) = "mypage:$userId"

    fun readSnapshot(userId: Long): MyPageSnapshot? {
        val map = redis.opsForHash<String, String>().entries(key(userId))
        if (map.isEmpty()) return null

        // 누락 필드가 있으면 snapshot 불완전으로 판단 (fallback 유도)
        fun getLong(field: String): Long? = map[field]?.toLongOrNull()

        val chatroomCount = getLong("chatroomCount") ?: return null
        val chatCount = getLong("chatCount") ?: return null
        val boardCount = getLong("boardCount") ?: return null
        val commentCount = getLong("commentCount") ?: return null
        val restrictedChatCount = getLong("restrictedChatCount") ?: return null
        val restrictedBoardCount = getLong("restrictedBoardCount") ?: return null
        val restrictedCommentCount = getLong("restrictedCommentCount") ?: return null
        val rebuiltAt = getLong("rebuiltAt") ?: 0L

        return MyPageSnapshot(
            chatroomCount = chatroomCount,
            chatCount = chatCount,
            boardCount = boardCount,
            commentCount = commentCount,
            restrictedChatCount = restrictedChatCount,
            restrictedBoardCount = restrictedBoardCount,
            restrictedCommentCount = restrictedCommentCount,
            rebuiltAt = rebuiltAt,
        )
    }
}