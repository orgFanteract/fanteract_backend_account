package fanteract.account.consumer

import com.fasterxml.jackson.databind.ObjectMapper
import fanteract.account.adapter.MyPageRedisWriter
import fanteract.account.dto.client.MyPageDeltaEvent
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

@Component
class MyPageDeltaConsumer(
    private val objectMapper: ObjectMapper,
    private val dedupStore: EventDedupRedisStore,
    private val myPageRedisWriter: MyPageRedisWriter,
) {

    @KafkaListener(
        topics = ["account.mypage.delta"],
        groupId = "account-mypage-delta-consumer"
    )
    fun consume(message: String) {
        val event = objectMapper.readValue(message, MyPageDeltaEvent::class.java)

        // 1) 멱등 체크
        val isExist = dedupStore.isDuplicate(event.eventId)
        println(isExist)
        println(event)
        if (isExist) {
            return
        }

        // 2) delta 반영
        event.deltas.forEach { (field, delta) ->
            when (field) {
                "chatroomCount" -> myPageRedisWriter.increaseChatroom(event.userId, delta)
                "chatCount" -> myPageRedisWriter.increaseChat(event.userId, delta)
                "boardCount" -> myPageRedisWriter.increaseBoard(event.userId, delta)
                "commentCount" -> myPageRedisWriter.increaseComment(event.userId, delta)

                "restrictedChatCount" -> myPageRedisWriter.increaseRestrictedChat(event.userId, delta)
                "restrictedBoardCount" -> myPageRedisWriter.increaseRestrictedBoard(event.userId, delta)
                "restrictedCommentCount" -> myPageRedisWriter.increaseRestrictedComment(event.userId, delta)

                else -> { /* ignore or log */ }
            }
        }
    }
}