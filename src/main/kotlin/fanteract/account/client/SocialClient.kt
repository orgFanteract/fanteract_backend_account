package fanteract.account.client

import fanteract.account.dto.client.ReadBoardCountInnerResponse
import fanteract.account.dto.client.ReadBoardPageInnerResponse
import fanteract.account.dto.client.ReadCommentCountInnerResponse
import fanteract.account.dto.client.ReadCommentPageInnerResponse
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
class SocialClient(
    @Value("\${client.social-service.url}") boardServiceUrl: String,
    private val restClient: RestClient = RestClient.builder()
        .baseUrl(boardServiceUrl)
        .build(),
    private val circuitBreakerRegistry: CircuitBreakerRegistry,
) {

    @CircuitBreaker(name = "socialClient", fallbackMethod = "countBoardFallback")
    fun countBoardByUserId(userId: Long): Long? {
        val response = restClient.get()
            .uri("/internal/boards/{userId}/user/count", userId)
            .retrieve()
            .body(ReadBoardCountInnerResponse::class.java)

        return response?.count ?: 0L
    }

    @CircuitBreaker(name = "socialClient", fallbackMethod = "countBoardByRiskFallback")
    fun countBoardByUserIdAndRiskLevel(userId: Long, riskLevel: RiskLevel): Long? {
        val response = restClient.get()
            .uri { builder ->
                builder
                    .path("/internal/boards/{userId}/user-risk/count")
                    .queryParam("riskLevel", riskLevel)
                    .build(userId)
            }
            .retrieve()
            .body(ReadBoardCountInnerResponse::class.java)

        return response?.count ?: 0L
    }

    @CircuitBreaker(name = "socialClient", fallbackMethod = "findBoardByRiskFallback")
    fun findBoardByUserIdAndRiskLevel(
        userId: Long,
        riskLevel: RiskLevel,
        pageable: Pageable
    ): ReadBoardPageInnerResponse {
        val response = restClient.get()
            .uri { builder ->
                builder
                    .path("/internal/boards/{userId}/user")
                    .queryParam("page", pageable.pageNumber)
                    .queryParam("size", pageable.pageSize)
                    .queryParam("riskLevel", riskLevel)
                    .build(userId)
            }
            .retrieve()
            .body(object : ParameterizedTypeReference<ReadBoardPageInnerResponse>() {})

        return requireNotNull(response) {
            "Failed to load boards for userId=$userId, riskLevel=$riskLevel"
        }
    }

    @CircuitBreaker(name = "socialClient", fallbackMethod = "countCommentFallback")
    fun countCommentByUserId(userId: Long): Long? {
        val response = restClient.get()
            .uri("/internal/comments/{userId}/user/count", userId)
            .retrieve()
            .body(ReadCommentCountInnerResponse::class.java)

        return response?.count ?: 0L
    }

    @CircuitBreaker(name = "socialClient", fallbackMethod = "countCommentByRiskFallback")
    fun countCommentByUserIdAndRiskLevel(userId: Long, riskLevel: RiskLevel): Long? {
        val response = restClient.get()
            .uri { builder ->
                builder
                    .path("/internal/comments/{userId}/user-risk/count")
                    .queryParam("riskLevel", riskLevel)
                    .build(userId)
            }
            .retrieve()
            .body(ReadCommentCountInnerResponse::class.java)

        return response?.count ?: 0L
    }

    @CircuitBreaker(name = "socialClient", fallbackMethod = "findCommentByRiskFallback")
    fun findCommentByUserIdAndRiskLevel(
        userId: Long,
        riskLevel: RiskLevel,
        pageable: Pageable
    ): ReadCommentPageInnerResponse {
        val response = restClient.get()
            .uri { builder ->
                builder
                    .path("/internal/comments/{userId}/user")
                    .queryParam("page", pageable.pageNumber)
                    .queryParam("size", pageable.pageSize)
                    .queryParam("riskLevel", riskLevel)
                    .build(userId)
            }
            .retrieve()
            .body(object : ParameterizedTypeReference<ReadCommentPageInnerResponse>() {})

        return requireNotNull(response) {
            "Failed to load comments for userId=$userId, riskLevel=$riskLevel"
        }
    }

    // ===== fallback methods =====

    @Suppress("unused")
    private fun countBoardFallback(userId: Long, ex: Throwable): Long? {
        returnStatus("socialClient", ex)
        return null
    }

    @Suppress("unused")
    private fun countBoardByRiskFallback(userId: Long, riskLevel: RiskLevel, ex: Throwable): Long? {
        returnStatus("socialClient", ex)
        return null
    }

    @Suppress("unused")
    private fun findBoardByRiskFallback(
        userId: Long,
        riskLevel: RiskLevel,
        pageable: Pageable,
        ex: Throwable
    ): ReadBoardPageInnerResponse {
        returnStatus("socialClient", ex)
        throw ExceptionType.withType(MessageType.INVALID_CONNECTED_SERVICE)
    }

    @Suppress("unused")
    private fun countCommentFallback(userId: Long, ex: Throwable): Long? {
        returnStatus("socialClient", ex)
        return null
    }

    @Suppress("unused")
    private fun countCommentByRiskFallback(userId: Long, riskLevel: RiskLevel, ex: Throwable): Long? {
        returnStatus("socialClient", ex)
        return null
    }

    @Suppress("unused")
    private fun findCommentByRiskFallback(
        userId: Long,
        riskLevel: RiskLevel,
        pageable: Pageable,
        ex: Throwable
    ): ReadCommentPageInnerResponse {
        returnStatus("socialClient", ex)
        throw ExceptionType.withType(MessageType.INVALID_CONNECTED_SERVICE)
    }

    fun returnStatus(client: String, ex: Throwable) {
        val cb = circuitBreakerRegistry.circuitBreaker(client)
        val state = cb.state
        println("fallback client=$client, ex=${ex::class.qualifiedName}:${ex.message}, state=$state")
    }
}