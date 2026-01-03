package fanteract.account.consumer

import fanteract.account.adapter.MyPageRedisWriter
import fanteract.account.adapter.SagaAccountReader
import fanteract.account.adapter.SagaAccountWriter
import fanteract.account.adapter.UserWriter
import fanteract.account.dto.client.*
import fanteract.account.enumerate.Balance
import fanteract.account.enumerate.EventStatus
import fanteract.account.enumerate.RiskLevel
import fanteract.account.exception.ExceptionType
import fanteract.account.exception.MessageType
import fanteract.account.util.BaseUtil
import mu.KotlinLogging
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.util.Base64
import kotlin.toString

@Transactional
@Component
class EventConsumer(
    private val userWriter: UserWriter,
    private val sagaAccountWriter: SagaAccountWriter,
    private val sagaAccountReader: SagaAccountReader,
    private val kafkaTemplate: KafkaTemplate<String, String>,
    private val myPageRedisWriter: MyPageRedisWriter,
) {
    private val log = KotlinLogging.logger {}
    @KafkaListener(
        topics = ["ACCOUNT_SERVICE.updateActivePoint"],
        groupId = "account-service"
    )
    fun consumeUpdateActivePoint(message: String){
        val decodedJson = String(Base64.getDecoder().decode(message))
        val response = BaseUtil.fromJson<MessageWrapper<UpdateActivePointSendRequest>>(decodedJson)

        userWriter.updateActivePoint(
            userId = response.content.userId,
            activePoint = response.content.activePoint
        )
    }

    //
    @KafkaListener(
        topics = [
            "ACCOUNT_SERVICE.debitIfEnoughEvent.SUCCESS",
            "ACCOUNT_SERVICE.updateActivePointEvent.SUCCESS",
        ],
        groupId = "account-service"
    )
    fun consumeCreateCommentEventSuccess(message: String){
        val decodedJson = String(Base64.getDecoder().decode(message))
        val response = BaseUtil.fromJson<EventWrapperForLog>(decodedJson)

        log.info{"success event : ${response.eventName}"}

        // 사가 트랜잭션 기록
        sagaAccountWriter.create(
            sagaId = response.sagaId,
            eventId = response.eventId,
            eventName = response.eventName,
            payload = response.payload?.toString(),
            eventStatus = EventStatus.SUCCESS,
            isExec = true,
        )
    }

    @KafkaListener(
        topics = [
            "ACCOUNT_SERVICE.debitIfEnoughEvent.FAIL",
            "ACCOUNT_SERVICE.updateActivePointEvent.FAIL",
        ],
        groupId = "account-service"
    )
    fun consumeCreateCommentEventFail(message: String){
        val decodedJson = String(Base64.getDecoder().decode(message))
        val response = BaseUtil.fromJson<EventWrapperForLog>(decodedJson)

        log.info{"fail event : ${response.eventName}"}
        // 보상 트랜잭션 진행


        // 사가 트랜잭션 기록
        sagaAccountWriter.create(
            sagaId = response.sagaId,
            eventId = response.eventId,
            eventName = response.eventName,
            payload = response.payload?.toString(),
            eventStatus = EventStatus.FAIL,
            isExec = false,
        )

        kafkaTemplate.send(
            "ACCOUNT_SERVICE.${response.eventName}.${EventStatus.COMPENSATE}",
            message,
        )
    }

    /** 2번 보상 **/
    @KafkaListener(
        topics = [
            "ACCOUNT_SERVICE.createComment.debitIfEnoughEvent.COMPENSATE",
        ],
        groupId = "account-service"
    )
    fun debitIfEnoughEventCompensate(message: String){
        log.info{"compensate : debitIfEnoughEventCompensate"}
        val decodedJson = String(Base64.getDecoder().decode(message))
        val response = BaseUtil.fromJson<EventWrapper<CreateCommentEventCompensateDto>>(decodedJson)

        // 보상 여부 확인
        if (sagaAccountReader.existsBySagaIdAndEventNameAndEventStatus(
                sagaId = response.sagaId,
                eventName = "debitIfEnoughEvent",
                eventStatus = EventStatus.COMPENSATE,
            )
        ){
            return
        }

        // 보상 로직 적용
        if (response.payload.userId != null){
            userWriter.updateBalance(
                userId = response.payload.userId,
                balance = Balance.COMMENT.cost
            )
        } else {
            throw ExceptionType.withType(MessageType.NOT_EXIST)
        }

        // 보상 체이닝 메세지 전송 없음

        // 보상 로그 기록
        sagaAccountWriter.create(
            sagaId = response.sagaId,
            eventId = response.eventId,
            eventName = "debitIfEnoughEvent",
            payload = response.payload.toString(),
            eventStatus = EventStatus.COMPENSATE,
            isExec = true,
        )
    }

    /** 5번 보상 **/
    @KafkaListener(
        topics = [
            "ACCOUNT_SERVICE.createComment.updateActivePointEvent.COMPENSATE",
        ],
        groupId = "account-service"
    )
    fun updateActivePointEvent(message: String){
        log.info{"compensate : updateActivePointEvent"}
        val decodedJson = String(Base64.getDecoder().decode(message))
        val response = BaseUtil.fromJson<EventWrapper<CreateCommentEventCompensateDto>>(decodedJson)

        // 보상 여부 확인
        if (sagaAccountReader.existsBySagaIdAndEventNameAndEventStatus(
                sagaId = response.sagaId,
                eventName = "updateActivePointEvent",
                eventStatus = EventStatus.COMPENSATE,
            )
        ){
            return
        }

        // 보상 로직 적용
        if (response.payload.refundActivePoint != null && response.payload.userId != null){
            userWriter.updateActivePoint(
                userId = response.payload.userId,
                activePoint = -response.payload.refundActivePoint
            )
        } else {
            throw ExceptionType.withType(MessageType.NOT_EXIST)
        }

        // 보상 체이닝 메세지 전송
        kafkaTemplate.send(
            "SOCIAL_SERVICE.createComment.createCommentEvent.COMPENSATE",
            message,
        )

        // 보상 로그 기록
        sagaAccountWriter.create(
            sagaId = response.sagaId,
            eventId = response.eventId,
            eventName = "updateActivePointEvent",
            payload = response.payload.toString(),
            eventStatus = EventStatus.COMPENSATE,
            isExec = true,
        )
    }

    // CQRS
    @KafkaListener(
        topics = [
            "SOCIAL_SERVICE.createCommentForUser",
            "SOCIAL_SERVICE.deleteCommentForUser",
            "SOCIAL_SERVICE.createBoardForUser",
            "CONNECT_SERVICE.createChatroomForUser",
            "CONNECT_SERVICE.createChatForUser",
        ],
        groupId = "account-service"
    )
    fun readSocialAndConnectEvent(message: String){
        val decodedJson = String(Base64.getDecoder().decode(message))
        val response = BaseUtil.fromJson<MessageWrapper<WriteCommentForUserRequest>>(decodedJson)

        val payload = response.content
        val userId = payload.userId
        val riskLevel = payload.riskLevel
        val isRestricted = (riskLevel == RiskLevel.BLOCK)

        when (response.methodName) {
            "createChatroomForUser" -> {
                myPageRedisWriter.increaseChatroom(userId, +1)
            }
            "deleteChatroomForUser" -> {
                myPageRedisWriter.increaseChatroom(userId, -1)
            }
            "createChatForUser" -> {
                myPageRedisWriter.increaseChat(userId, +1)
                if (isRestricted) myPageRedisWriter.increaseRestrictedChat(userId, +1)
            }
            "deleteChatForUser" -> {
                myPageRedisWriter.increaseChat(userId, -1)
                if (isRestricted) myPageRedisWriter.increaseRestrictedChat(userId, -1)
            }
            "createBoardForUser" -> {
                myPageRedisWriter.increaseBoard(userId, +1)
                if (isRestricted) myPageRedisWriter.increaseRestrictedBoard(userId, +1)
            }
            "deleteBoardForUser" -> {
                myPageRedisWriter.increaseBoard(userId, -1)
                if (isRestricted) myPageRedisWriter.increaseRestrictedBoard(userId, -1)
            }
            "createCommentForUser" -> {
                myPageRedisWriter.increaseComment(userId, +1)
                if (isRestricted) myPageRedisWriter.increaseRestrictedComment(userId, +1)
            }
            "deleteCommentForUser" -> {
                myPageRedisWriter.increaseComment(userId, -1)
                if (isRestricted) myPageRedisWriter.increaseRestrictedComment(userId, -1)
            }
        }
    }
}