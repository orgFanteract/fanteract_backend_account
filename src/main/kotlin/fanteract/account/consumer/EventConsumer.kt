package fanteract.account.consumer

import fanteract.account.domain.UserReader
import fanteract.account.domain.UserWriter
import fanteract.account.repo.UserRepo
import fanteract.account.util.BaseUtil
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component
import java.util.Base64

@Component
class EventConsumer(
    private val userWriter: UserWriter,
) {
    @KafkaListener(
        topics = ["ACCOUNT_SERVICE.updateActivePoint"],
        groupId = "account-service"
    )
    fun consumeUpdateActivePoint(message: String){
        println("consumed")
        val decodedJson = String(Base64.getDecoder().decode(message))
        println(decodedJson)
        val response = BaseUtil.fromJson<OutboxMessage<UpdateActivePointSendRequest>>(decodedJson)

        userWriter.updateActivePoint(
            userId = response.content.userId,
            activePoint = response.content.activePoint
        )

        println("${response.content.userId} - ${response.content.activePoint}")
    }

    data class OutboxMessage<T>(
        val methodName: String,
        val content: T
    )

    data class UpdateActivePointSendRequest(
        val userId: Long,
        val activePoint: Int
    )
}