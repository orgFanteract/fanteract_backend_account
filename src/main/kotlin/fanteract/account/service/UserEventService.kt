package fanteract.account.service

import fanteract.account.adapter.MessageAdapter
import fanteract.account.adapter.UserReader
import fanteract.account.adapter.UserWriter
import fanteract.account.dto.client.UpdateActivePointRequest
import fanteract.account.enumerate.ActivePoint
import fanteract.account.enumerate.Balance
import fanteract.account.enumerate.TopicService
import fanteract.account.exception.ExceptionType
import fanteract.account.exception.MessageType
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component
import java.util.UUID
import fanteract.account.enumerate.EventStatus
import fanteract.account.util.messageResolver
import org.springframework.transaction.annotation.Transactional
import fanteract.account.dto.client.*
import mu.KotlinLogging

@Transactional
@Component
class UserEventService(
    private val userReader: UserReader,
    private val userWriter: UserWriter,
    private val messageAdapter: MessageAdapter,
) {
    private val log = KotlinLogging.logger {}
    /** 2번 **/
    @KafkaListener(
        topics = ["ACCOUNT_SERVICE.debitIfEnoughEvent.PROCESS"],
        groupId = "account-service"
    )
    fun debitIfEnoughEvent(message: String){
        log.info{"event! = debitIfEnoughEvent"}
        // receive message
        val response = messageResolver<DebitIfEnoughEventDto>(message)

        try {
            // exec
            val user = userReader.findById(response.payload.userId)

            if (user.balance < Balance.COMMENT.cost){
                throw ExceptionType.withType(MessageType.NOT_ENOUGH_BALANCE)
            }

            userWriter.updateBalance(response.payload.userId, -Balance.COMMENT.cost)

            // send message
            messageAdapter.sendEventUsingBroker(
                sagaId = response.sagaId,
                eventId = "EVENT-${UUID.randomUUID()}",
                eventName = "filterCommentContentEvent",
                causationId = response.eventId,
                topicService = TopicService.SOCIAL_SERVICE,
                eventStatus = EventStatus.PROCESS,
                payload = FilterCommentContentEventDto(
                    userId = response.payload.userId,
                    boardId = response.payload.boardId,
                    content = response.payload.content,
                    cost = Balance.COMMENT.cost,
                ),
            )
            // send success message
            messageAdapter.sendEventUsingBroker(
                sagaId = response.sagaId,
                eventId = "EVENT-${UUID.randomUUID()}",
                eventName = "debitIfEnoughEvent",
                causationId = response.eventId,
                topicService = TopicService.ACCOUNT_SERVICE,
                eventStatus = EventStatus.SUCCESS,
                payload = response.payload
            )
        } catch (e: Exception){
            // send fail message
            messageAdapter.sendEventUsingBroker(
                sagaId = response.sagaId,
                eventId = "EVENT-${UUID.randomUUID()}",
                eventName = "debitIfEnoughEvent",
                causationId = response.eventId,
                topicService = TopicService.ACCOUNT_SERVICE,
                eventStatus = EventStatus.FAIL,
                payload = CreateCommentEventCompensateDto(
                    userId = null,
                    refundCost = null,
                    refundActivePoint = null,
                    commentId = null,
                )
            )
        }
    }

    @KafkaListener(
        topics = ["ACCOUNT_SERVICE.updateActivePointEvent.PROCESS"],
        groupId = "account-service"
    )
    /** 5번 **/
    fun updateActivePointEvent(message: String){
        log.info{"event! = updateActivePointEvent"}
        // receive message
        val response = messageResolver<UpdateActivePointEventDto>(message)

        try {
            // exec
            messageAdapter.sendMessageUsingBroker(
                message =
                    UpdateActivePointRequest(
                        userId = response.payload.userId,
                        activePoint = ActivePoint.COMMENT.point
                    ),
                topicService = TopicService.ACCOUNT_SERVICE,
                methodName = "updateActivePoint"
            )

            // send message
            messageAdapter.sendEventUsingBroker(
                sagaId = response.sagaId,
                eventId = "EVENT-${UUID.randomUUID()}",
                eventName = "createAlarmToBoardUserEvent",
                causationId = response.eventId,
                topicService = TopicService.SOCIAL_SERVICE,
                eventStatus = EventStatus.PROCESS,
                payload = CreateAlarmToBoardUserEventDto(
                    userId = response.payload.userId,
                    boardId = response.payload.boardId,
                    commentId = response.payload.commentId,
                    cost = response.payload.cost,
                    activePoint = ActivePoint.COMMENT.point
                ),
            )
            // send success message
            messageAdapter.sendEventUsingBroker(
                sagaId = response.sagaId,
                eventId = "EVENT-${UUID.randomUUID()}",
                eventName = "updateActivePointEvent",
                causationId = response.eventId,
                topicService = TopicService.ACCOUNT_SERVICE,
                eventStatus = EventStatus.SUCCESS,
                payload = response.payload
            )
        } catch (e: Exception){
            // send fail message
            messageAdapter.sendEventUsingBroker(
                sagaId = response.sagaId,
                eventId = "EVENT-${UUID.randomUUID()}",
                eventName = "updateActivePointEvent",
                causationId = response.eventId,
                topicService = TopicService.ACCOUNT_SERVICE,
                eventStatus = EventStatus.FAIL,
                payload = CreateCommentEventCompensateDto(
                    userId = response.payload.userId,
                    refundCost = response.payload.cost,
                    refundActivePoint = null,
                    commentId = null,
                )
            )
        }
    }
}