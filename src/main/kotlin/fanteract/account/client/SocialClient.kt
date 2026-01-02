package fanteract.account.client

import fanteract.account.dto.client.ReadBoardCountInnerResponse
import fanteract.account.dto.client.ReadBoardPageInnerResponse
import fanteract.account.dto.client.ReadChatroomCountInnerResponse
import fanteract.account.dto.client.ReadCommentCountInnerResponse
import fanteract.account.dto.client.ReadCommentPageInnerResponse
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
class SocialClient(
    @Value("\${client.social-service.url}") boardServiceUrl: String,
    private val restClient: RestClient = RestClient.builder()
        .baseUrl(boardServiceUrl)
        .build(),
    private val circuitBreakerUtil: CircuitBreakerUtil,
    private val circuitBreakerManager: CircuitBreakerManager,
) {
    fun countBoardByUserId(userId: Long): Long? {
        val response =
            circuitBreakerUtil.circuitBreaker(
                profile = circuitBreakerManager.baseConfig
            ){
                restClient.get()
                    .uri("/internal/boards/{userId}/user/count", userId)
                    .retrieve()
                    .body(ReadBoardCountInnerResponse::class.java)
            }.fallbackIfOpen {
                throw ExceptionType.withType(MessageType.CALL_NOT_PERMITTED)
            }.fallback{
                throw ExceptionType.withType(MessageType.INVALID_ACCESS_RESOURCE)
            }.get()

        return response?.count ?: 0L
    }

    fun countBoardByUserIdAndRiskLevel(userId: Long, riskLevel: RiskLevel): Long? {
        val response =
            circuitBreakerUtil.circuitBreaker(
                profile = circuitBreakerManager.socialConfig
            ){
                restClient.get()
                    .uri { builder ->
                        builder
                            .path("/internal/boards/{userId}/user-risk/count")
                            .queryParam("riskLevel", riskLevel)
                            .build(userId)
                    }
                    .retrieve()
                    .body(ReadBoardCountInnerResponse::class.java)
            }.fallbackIfOpen {
                throw ExceptionType.withType(MessageType.CALL_NOT_PERMITTED)
            }.fallback{
                throw ExceptionType.withType(MessageType.INVALID_ACCESS_RESOURCE)
            }.get()

        return response?.count ?: 0L
    }

    fun findBoardByUserIdAndRiskLevel(
        userId: Long,
        riskLevel: RiskLevel,
        pageable: Pageable
    ): ReadBoardPageInnerResponse {
        val response =
            circuitBreakerUtil.circuitBreaker(
                profile = circuitBreakerManager.socialConfig
            ){
                restClient.get()
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
            }.fallbackIfOpen {
                throw ExceptionType.withType(MessageType.CALL_NOT_PERMITTED)
            }.fallback{
                throw ExceptionType.withType(MessageType.INVALID_ACCESS_RESOURCE)
            }.get()

        return requireNotNull(response) {
            "Failed to load boards for userId=$userId, riskLevel=$riskLevel"
        }
    }

    fun countCommentByUserId(userId: Long): Long? {
        val response =
            circuitBreakerUtil.circuitBreaker(
                profile = circuitBreakerManager.socialConfig
            ){
                restClient.get()
                    .uri("/internal/comments/{userId}/user/count", userId)
                    .retrieve()
                    .body(ReadCommentCountInnerResponse::class.java)
            }.fallbackIfOpen {
                throw ExceptionType.withType(MessageType.CALL_NOT_PERMITTED)
            }.fallback{
                throw ExceptionType.withType(MessageType.INVALID_ACCESS_RESOURCE)
            }.get()

        return response?.count ?: 0L
    }

    fun countCommentByUserIdAndRiskLevel(userId: Long, riskLevel: RiskLevel): Long? {
        val response =
            circuitBreakerUtil.circuitBreaker(
                profile = circuitBreakerManager.socialConfig
            ){
                restClient.get()
                    .uri { builder ->
                        builder
                            .path("/internal/comments/{userId}/user-risk/count")
                            .queryParam("riskLevel", riskLevel)
                            .build(userId)
                    }
                    .retrieve()
                    .body(ReadCommentCountInnerResponse::class.java)
            }.fallbackIfOpen {
                throw ExceptionType.withType(MessageType.CALL_NOT_PERMITTED)
            }.fallback{
                throw ExceptionType.withType(MessageType.INVALID_ACCESS_RESOURCE)
            }.get()

        return response?.count ?: 0L
    }

    @CircuitBreaker(name = "socialClient", fallbackMethod = "findCommentByRiskFallback")
    fun findCommentByUserIdAndRiskLevel(
        userId: Long,
        riskLevel: RiskLevel,
        pageable: Pageable
    ): ReadCommentPageInnerResponse {
        val response =
            circuitBreakerUtil.circuitBreaker(
                profile = circuitBreakerManager.socialConfig
            ){
                restClient.get()
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
            }.fallbackIfOpen {
                throw ExceptionType.withType(MessageType.CALL_NOT_PERMITTED)
            }.fallback{
                throw ExceptionType.withType(MessageType.INVALID_ACCESS_RESOURCE)
            }.get()

        return requireNotNull(response) {
            "Failed to load comments for userId=$userId, riskLevel=$riskLevel"
        }
    }
}