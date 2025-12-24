package fanteract.account.listener

import fanteract.account.adapter.MessageAdapter
import fanteract.account.adapter.UserReader
import fanteract.account.adapter.UserWriter
import fanteract.account.dto.client.EventWrapper
import fanteract.account.dto.client.UpdateActivePointRequest
import fanteract.account.enumerate.ActivePoint
import fanteract.account.enumerate.Balance
import fanteract.account.enumerate.EventStatus
import fanteract.account.enumerate.TopicService
import fanteract.account.exception.ExceptionType
import fanteract.account.exception.MessageType
import fanteract.account.listener.CreateCommentOrchestratorListener.RefundBalanceCommand
import fanteract.account.listener.CreateCommentOrchestratorListener.RefundBalanceReply
import fanteract.account.listener.CreateCommentOrchestratorListener.RollbackActivePointCommand
import fanteract.account.listener.CreateCommentOrchestratorListener.RollbackActivePointReply
import fanteract.account.util.BaseUtil
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component
import java.util.Base64
import java.util.UUID

@Component
class CreateCommentOrchestratorListenerV2(
    private val userReader: UserReader,
    private val userWriter: UserWriter,
    private val messageAdapter: MessageAdapter,
) {
    // 2번 - 사용자 잔액 차감
    @KafkaListener(topics = ["ACCOUNT_SERVICE.UpdateDebitCommandV2.PROCESS"], groupId = "account-service")
    fun onUpdateDebitCommandV2(message: String) {
        println("onUpdateDebitCommandV2")
        val command = BaseUtil.fromJson<EventWrapper<DebitBalanceCommand>>(String(Base64.getDecoder().decode(message)))
        val (sagaId, causationId, payload) = Triple(command.sagaId, command.eventId, command.payload)

        try {
            // 사용자 잔액 차감 로직
            userWriter.updateBalance(
                userId = payload.userId,
                balance = -payload.cost
            )

            messageAdapter.sendEventUsingBroker(
                sagaId = sagaId,
                eventId = "EVENT-${UUID.randomUUID()}",
                eventName = "UpdateDebitReplyV2",
                causationId = causationId,
                topicService = TopicService.ACCOUNT_SERVICE,
                eventStatus = EventStatus.SUCCESS,
                payload = DebitBalanceReply(sagaId, causationId, true, payload.cost),
            )
        } catch (e: Exception) {
            messageAdapter.sendEventUsingBroker(
                sagaId = sagaId,
                eventId = "EVENT-${UUID.randomUUID()}",
                eventName = "UpdateDebitReplyV2",
                causationId = causationId,
                topicService = TopicService.ACCOUNT_SERVICE,
                eventStatus = EventStatus.FAIL,
                payload = DebitBalanceReply(sagaId, causationId, false, payload.cost),
            )
        }
    }

    data class DebitBalanceCommand(val boardId: Long, val userId: Long, val content: String, val cost: Int)
    data class DebitBalanceReply(val sagaId: String, val eventId: String, val success: Boolean, val cost: Int?)

    // 보상 커멘드
    @KafkaListener(
        topics = ["ACCOUNT_SERVICE.RefundBalanceCommandV2.PROCESS"],
        groupId = "account-service"
    )
    fun onRefundBalanceCommand(message: String) {
        println("onRefundBalanceCommand")
        val decodedJson = String(Base64.getDecoder().decode(message))
        val command = BaseUtil.fromJson<EventWrapper<RefundBalanceCommand>>(decodedJson)

        val (sagaId, causationId, payload) = Triple(command.sagaId, command.eventId, command.payload)

        try {
            // 보상 로직 적용
            userWriter.updateBalance(
                userId = payload.userId,
                balance = payload.cost
            )

            // success
            messageAdapter.sendEventUsingBroker(
                sagaId = sagaId,
                eventId = "EVENT-${UUID.randomUUID()}",
                eventName = "RefundBalanceReplyV2",
                causationId = causationId,
                topicService = TopicService.ACCOUNT_SERVICE,
                eventStatus = EventStatus.SUCCESS,
                payload = RefundBalanceReply(
                    sagaId = sagaId,
                    eventId = causationId,
                    success = true,
                )
            )
        } catch (e: Exception) {
            // fail
            messageAdapter.sendEventUsingBroker(
                sagaId = sagaId,
                eventId = "EVENT-${UUID.randomUUID()}",
                eventName = "RefundBalanceReplyV2",
                causationId = causationId,
                topicService = TopicService.ACCOUNT_SERVICE,
                eventStatus = EventStatus.FAIL,
                payload = RefundBalanceReply(
                    sagaId = sagaId,
                    eventId = causationId,
                    success = false,
                )
            )
        }
    }

    @KafkaListener(
        topics = ["ACCOUNT_SERVICE.RollbackActivePointCommandV2.PROCESS"],
        groupId = "account-service"
    )
    fun onRollbackActivePointCommand(message: String) {
        val decodedJson = String(Base64.getDecoder().decode(message))
        val command = BaseUtil.fromJson<EventWrapper<RollbackActivePointCommand>>(decodedJson)

        val (sagaId, causationId, payload) = Triple(command.sagaId, command.eventId, command.payload)

        try {
            // 보상 로직 적용
            userWriter.updateActivePoint(
                userId = payload.userId,
                activePoint = -payload.point
            )

            // success
            messageAdapter.sendEventUsingBroker(
                sagaId = sagaId,
                eventId = "EVENT-${UUID.randomUUID()}",
                eventName = "RollbackActivePointReplyV2",
                causationId = causationId,
                topicService = TopicService.ACCOUNT_SERVICE,
                eventStatus = EventStatus.SUCCESS,
                payload = RollbackActivePointReply(
                    sagaId = sagaId,
                    eventId = causationId,
                    success = true,
                )
            )
        } catch (e: Exception) {
            // fail
            messageAdapter.sendEventUsingBroker(
                sagaId = sagaId,
                eventId = "EVENT-${UUID.randomUUID()}",
                eventName = "RollbackActivePointReplyV2",
                causationId = causationId,
                topicService = TopicService.ACCOUNT_SERVICE,
                eventStatus = EventStatus.FAIL,
                payload = RollbackActivePointReply(
                    sagaId = sagaId,
                    eventId = causationId,
                    success = false,
                )
            )
        }
    }
}