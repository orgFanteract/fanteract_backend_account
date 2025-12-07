package fanteract.account.client

import fanteract.account.dto.client.ReadBoardCountInnerResponse
import fanteract.account.dto.client.ReadBoardPageInnerResponse
import fanteract.account.dto.client.ReadCommentCountInnerResponse
import fanteract.account.dto.client.ReadCommentPageInnerResponse
import fanteract.account.enumerate.RiskLevel
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
) {

    fun countBoardByUserId(userId: Long): Long {
        val response = restClient.get()
            .uri("/internal/boards/{userId}/user/count", userId)
            .retrieve()
            .body(ReadBoardCountInnerResponse::class.java)

        return response?.count ?: 0L
    }

    fun countBoardByUserIdAndRiskLevel(userId: Long, riskLevel: RiskLevel): Long {
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

    ///

    fun countCommentByUserId(userId: Long): Long {
        val response = restClient.get()
            .uri("/internal/comments/{userId}/user/count", userId)
            .retrieve()
            .body(ReadCommentCountInnerResponse::class.java)

        return response?.count ?: 0L
    }

    fun countCommentByUserIdAndRiskLevel(userId: Long, riskLevel: RiskLevel): Long {
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
}