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
import fanteract.account.util.BaseUtil
import mu.KotlinLogging
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component
import java.util.Base64
import java.util.UUID

@Component
class CreateCommentOrchestratorListener(
    private val userReader: UserReader,
    private val userWriter: UserWriter,
    private val messageAdapter: MessageAdapter,
) {
    private val log = KotlinLogging.logger {}
    // 2번
    @KafkaListener(
        topics = ["ACCOUNT_SERVICE.UpdateDebitCommand.PROCESS"],
        groupId = "account-service"
    )
    fun onUpdateDebitCommand(message: String) {
        log.info{"onUpdateDebitCommand"}
        val decodedJson = String(Base64.getDecoder().decode(message))
        val command = BaseUtil.fromJson<EventWrapper<UpdateDebitCommand>>(decodedJson)

        val (sagaId, causationId, payload) = Triple(command.sagaId, command.eventId, command.payload)

        try {
            val user = userReader.findById(payload.userId)

            if (user.balance < payload.cost) {
                throw IllegalStateException("NOT_ENOUGH_BALANCE")
            }

            userWriter.updateBalance(
                userId = payload.userId,
                balance = -payload.cost
            )

            // 성공
            messageAdapter.sendEventUsingBroker(
                sagaId = sagaId,
                eventId = "EVENT-${UUID.randomUUID()}",
                eventName = "UpdateDebitReply",
                causationId = causationId,
                topicService = TopicService.ACCOUNT_SERVICE,
                eventStatus = EventStatus.SUCCESS,
                payload = UpdateDebitReply(
                    sagaId = sagaId,
                    eventId = causationId,
                    success = true,
                    cost = payload.cost
                )
            )

        } catch (e: Exception) {
            messageAdapter.sendEventUsingBroker(
                sagaId = sagaId,
                eventId = "EVENT-${UUID.randomUUID()}",
                eventName = "UpdateDebitReply",
                causationId = causationId,
                topicService = TopicService.ACCOUNT_SERVICE,
                eventStatus = EventStatus.FAIL,
                payload = UpdateDebitReply(
                    sagaId = sagaId,
                    eventId = causationId,
                    success = false,
                    cost = null,
                )
            )
        }
    }

    // 5번
    @KafkaListener(
        topics = ["ACCOUNT_SERVICE.UpdateActivePointCommand.PROCESS"],
        groupId = "account-service"
    )
    fun onUpdateActivePointCommand(message: String){
        val decodedJson = String(Base64.getDecoder().decode(message))
        val command = BaseUtil.fromJson<EventWrapper<UpdateActivePointCommend>>(decodedJson)

        val (sagaId, causationId, payload) = Triple(command.sagaId, command.eventId, command.payload)

        try {
            messageAdapter.sendMessageUsingBroker(
                message =
                    UpdateActivePointRequest(
                        userId = payload.userId,
                        activePoint = ActivePoint.COMMENT.point
                    ),
                topicService = TopicService.ACCOUNT_SERVICE,
                methodName = "updateActivePoint"
            )

            // 성공
            messageAdapter.sendEventUsingBroker(
                sagaId = sagaId,
                eventId = "EVENT-${UUID.randomUUID()}",
                eventName = "UpdateActivePointReply",
                causationId = causationId,
                topicService = TopicService.ACCOUNT_SERVICE,
                eventStatus = EventStatus.SUCCESS,
                payload = UpdateActivePointReply(
                    sagaId = sagaId,
                    eventId = causationId,
                    success = true,
                    point = ActivePoint.COMMENT.point
                )
            )

        } catch (e: Exception) {
            messageAdapter.sendEventUsingBroker(
                sagaId = sagaId,
                eventId = "EVENT-${UUID.randomUUID()}",
                eventName = "UpdateActivePointReply",
                causationId = causationId,
                topicService = TopicService.ACCOUNT_SERVICE,
                eventStatus = EventStatus.FAIL,
                payload = UpdateActivePointReply(
                    sagaId = sagaId,
                    eventId = causationId,
                    success = false,
                    point = null,
                )
            )
        }
    }

    // 보상 커멘드
    @KafkaListener(
        topics = ["ACCOUNT_SERVICE.RefundBalanceCommand.PROCESS"],
        groupId = "account-service"
    )
    fun onRefundBalanceCommand(message: String) {
        log.info{"onRefundBalanceCommand"}
        val decodedJson = String(Base64.getDecoder().decode(message))
        val command = BaseUtil.fromJson<EventWrapper<RefundBalanceCommand>>(decodedJson)

        val (sagaId, causationId, payload) = Triple(command.sagaId, command.eventId, command.payload)

        try {
            // 보상 로직 적용
            userWriter.updateBalance(
                userId = payload.userId,
                balance = Balance.COMMENT.cost
            )

            // success
            messageAdapter.sendEventUsingBroker(
                sagaId = sagaId,
                eventId = "EVENT-${UUID.randomUUID()}",
                eventName = "RefundBalanceReply",
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
                eventName = "RefundBalanceReply",
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
        topics = ["ACCOUNT_SERVICE.RollbackActivePointCommand.PROCESS"],
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
                eventName = "RollbackActivePointReply",
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
                eventName = "RollbackActivePointReply",
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


    data class UpdateDebitCommand(val boardId: Long, val userId: Long, val content: String, val cost: Int)
    data class UpdateDebitReply(val sagaId: String, val eventId: String, val success: Boolean, val cost: Int?)

    data class UpdateActivePointCommend(val userId: Long , val point: Int)
    data class UpdateActivePointReply(val sagaId: String, val eventId: String, val success: Boolean, val point: Int?)

    data class RefundBalanceCommand(val userId: Long, val cost: Int)
    data class RefundBalanceReply(val sagaId: String, val eventId: String, val success: Boolean)

    data class RollbackActivePointCommand(val userId: Long, val point: Int)
    data class RollbackActivePointReply(val sagaId: String, val eventId: String, val success: Boolean)

    data class DeleteCommentCommand(val commentId: Long)
    data class DeleteCommentReply(val sagaId: String, val eventId: String, val success: Boolean)
}