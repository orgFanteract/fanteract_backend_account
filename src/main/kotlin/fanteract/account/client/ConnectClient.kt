package fanteract.account.client

import fanteract.account.dto.client.*
import fanteract.account.enumerate.RiskLevel
import fanteract.account.exception.ExceptionType
import fanteract.account.exception.MessageType
import fanteract.account.util.CircuitBreakerManager
import fanteract.account.util.CircuitBreakerUtil
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.ParameterizedTypeReference
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient

@Component
class ConnectClient(
    @Value("\${client.connect-service.url}") chatServiceUrl: String,
    private val restClient: RestClient = RestClient.builder()
        .baseUrl(chatServiceUrl)
        .build(),
    private val circuitBreakerUtil: CircuitBreakerUtil,
    private val circuitBreakerManager: CircuitBreakerManager,
) {

    fun countChatroomByUserId(userId: Long): Long? {
        val response =
            circuitBreakerUtil.circuitBreaker(
                profile = circuitBreakerManager.connectConfig
            ){
                restClient.get()
                    .uri("/internal/chats/{userId}/chatroom/count", userId)
                    .retrieve()
                    .body(ReadChatroomCountInnerResponse::class.java)
            }.fallbackIfOpen {
                throw ExceptionType.withType(MessageType.CALL_NOT_PERMITTED)
            }.fallback{
                throw ExceptionType.withType(MessageType.INVALID_ACCESS_RESOURCE)
            }.get()

        return response?.count ?: 0L
    }

    fun countChatByUserId(userId: Long): Long? {
        val response =
            circuitBreakerUtil.circuitBreaker(
                profile = circuitBreakerManager.connectConfig
            ){
                restClient.get()
                    .uri("/internal/chats/{userId}/chat/count", userId)
                    .retrieve()
                    .body(ReadChatCountInnerResponse::class.java)
            }.fallbackIfOpen {
                throw ExceptionType.withType(MessageType.CALL_NOT_PERMITTED)
            }.fallback{
                throw ExceptionType.withType(MessageType.INVALID_ACCESS_RESOURCE)
            }.get()

        return response?.count ?: 0L
    }

    fun countChatByUserIdAndRiskLevel(userId: Long, riskLevel: RiskLevel): Long? {
        val response =
            circuitBreakerUtil.circuitBreaker(
                profile = circuitBreakerManager.connectConfig
            ){
                restClient.get()
                    .uri { builder ->
                        builder
                            .path("/internal/chats/{userId}/user/count")
                            .queryParam("riskLevel", riskLevel)
                            .build(userId)
                    }
                    .retrieve()
                    .body(ReadChatCountInnerResponse::class.java)
            }.fallbackIfOpen {
                throw ExceptionType.withType(MessageType.CALL_NOT_PERMITTED)
            }.fallback{
                throw ExceptionType.withType(MessageType.INVALID_ACCESS_RESOURCE)
            }.get()

        return response?.count ?: 0L
    }

    fun findChatByUserIdAndRiskLevel(
        userId: Long,
        riskLevel: RiskLevel,
        pageable: Pageable
    ): ReadChatPageInnerResponse {
        val response =
            circuitBreakerUtil.circuitBreaker(
                profile = circuitBreakerManager.connectConfig
            ){
                restClient.get()
                    .uri { builder ->
                        builder
                            .path("/internal/chats/{userId}/user")
                            .queryParam("page", pageable.pageNumber)
                            .queryParam("size", pageable.pageSize)
                            .queryParam("riskLevel", riskLevel)
                            .build(userId)
                    }
                    .retrieve()
                    .body(object : ParameterizedTypeReference<ReadChatPageInnerResponse>() {})
            }.fallbackIfOpen {
                throw ExceptionType.withType(MessageType.CALL_NOT_PERMITTED)
            }.fallback{
                throw ExceptionType.withType(MessageType.INVALID_ACCESS_RESOURCE)
            }.get()

        return requireNotNull(response) {
            "Failed to load chats for userId=$userId, riskLevel=$riskLevel"
        }
    }
}