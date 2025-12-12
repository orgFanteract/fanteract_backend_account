package fanteract.account.client

import fanteract.account.dto.client.*
import fanteract.account.enumerate.RiskLevel
import fanteract.account.exception.ExceptionType
import fanteract.account.exception.MessageType
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
    private val circuitBreakerRegistry: CircuitBreakerRegistry,
) {

    @CircuitBreaker(name = "connectClient", fallbackMethod = "countChatroomFallback")
    fun countChatroomByUserId(userId: Long): Long? {
        val response = restClient.get()
            .uri("/internal/chats/{userId}/chatroom/count", userId)
            .retrieve()
            .body(ReadChatroomCountInnerResponse::class.java)

        return response?.count ?: 0L
    }

    @CircuitBreaker(name = "connectClient", fallbackMethod = "countChatFallback")
    fun countChatByUserId(userId: Long): Long? {
        val response = restClient.get()
            .uri("/internal/chats/{userId}/chat/count", userId)
            .retrieve()
            .body(ReadChatCountInnerResponse::class.java)

        return response?.count ?: 0L
    }

    @CircuitBreaker(name = "connectClient", fallbackMethod = "countChatByRiskFallback")
    fun countChatByUserIdAndRiskLevel(userId: Long, riskLevel: RiskLevel): Long? {
        val response = restClient.get()
            .uri { builder ->
                builder
                    .path("/internal/chats/{userId}/user/count")
                    .queryParam("riskLevel", riskLevel)
                    .build(userId)
            }
            .retrieve()
            .body(ReadChatCountInnerResponse::class.java)

        return response?.count ?: 0L
    }

    @CircuitBreaker(name = "connectClient", fallbackMethod = "findChatByRiskFallback")
    fun findChatByUserIdAndRiskLevel(
        userId: Long,
        riskLevel: RiskLevel,
        pageable: Pageable
    ): ReadChatPageInnerResponse {
        val response = restClient.get()
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

        return requireNotNull(response) {
            "Failed to load chats for userId=$userId, riskLevel=$riskLevel"
        }
    }

    // ===== fallback methods =====

    @Suppress("unused")
    private fun countChatroomFallback(userId: Long, ex: Throwable): Long? {
        returnStatus("connectClient",  ex)
        return null
    }

    @Suppress("unused")
    private fun countChatFallback(userId: Long, ex: Throwable): Long? {
        returnStatus("connectClient",  ex)
        return null
    }

    @Suppress("unused")
    private fun countChatByRiskFallback(userId: Long, riskLevel: RiskLevel, ex: Throwable): Long? {
        returnStatus("connectClient",  ex)
        return null
    }

    @Suppress("unused")
    private fun findChatByRiskFallback(
        userId: Long,
        riskLevel: RiskLevel,
        pageable: Pageable,
        ex: Throwable
    ): ReadChatPageInnerResponse {
        returnStatus("connectClient",ex)
        throw ExceptionType.withType(MessageType.INVALID_CONNECTED_SERVICE)
    }

    private fun returnStatus(client: String, ex: Throwable) {
        val cb = circuitBreakerRegistry.circuitBreaker(client)
        val state = cb.state
        println("fallback client=$client, ex=${ex::class.qualifiedName}:${ex.message}, state=$state")
    }
}