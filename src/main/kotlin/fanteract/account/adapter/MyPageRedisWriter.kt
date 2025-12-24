package fanteract.account.adapter

import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Component

@Component
class MyPageRedisWriter(
    private val redis: StringRedisTemplate
) {
    private fun key(userId: Long) = "mypage:$userId"

    fun rebuildSnapshot(
        userId: Long,
        chatroomCount: Long,
        chatCount: Long,
        boardCount: Long,
        commentCount: Long,
        restrictedChatCount: Long,
        restrictedBoardCount: Long,
        restrictedCommentCount: Long,
    ) {
        val map = mapOf(
            "chatroomCount" to chatroomCount.toString(),
            "chatCount" to chatCount.toString(),
            "boardCount" to boardCount.toString(),
            "commentCount" to commentCount.toString(),
            "restrictedChatCount" to restrictedChatCount.toString(),
            "restrictedBoardCount" to restrictedBoardCount.toString(),
            "restrictedCommentCount" to restrictedCommentCount.toString(),
            "rebuiltAt" to System.currentTimeMillis().toString()
        )

        redis.delete(key(userId)) // ⚠️ 기존 delta 완전 제거
        redis.opsForHash<String, String>().putAll(key(userId), map)
    }

    fun increaseChatroom(userId: Long, delta: Long) =
        redis.opsForHash<String, String>().increment(key(userId), "chatroomCount", delta)

    fun increaseChat(userId: Long, delta: Long) =
        redis.opsForHash<String, String>().increment(key(userId), "chatCount", delta)

    fun increaseBoard(userId: Long, delta: Long) =
        redis.opsForHash<String, String>().increment(key(userId), "boardCount", delta)

    fun increaseComment(userId: Long, delta: Long) =
        redis.opsForHash<String, String>().increment(key(userId), "commentCount", delta)

    fun increaseRestrictedChat(userId: Long, delta: Long) =
        redis.opsForHash<String, String>().increment(key(userId), "restrictedChatCount", delta)

    fun increaseRestrictedBoard(userId: Long, delta: Long) =
        redis.opsForHash<String, String>().increment(key(userId), "restrictedBoardCount", delta)

    fun increaseRestrictedComment(userId: Long, delta: Long) =
        redis.opsForHash<String, String>().increment(key(userId), "restrictedCommentCount", delta)
}