package fanteract.account.client

import fanteract.account.dto.client.*
import fanteract.account.enumerate.RiskLevel
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
) {
    fun countChatroomByUserId(userId: Long): Long {
        val response = restClient
            .get()
            .uri("/internal/chats/{userId}/chatroom/count", userId)
            .retrieve()
            .body(ReadChatroomCountInnerResponse::class.java)

        return response?.count ?: 0L
    }

    fun countChatByUserId(userId: Long): Long {
        val response = restClient
            .get()
            .uri("/internal/chats/{userId}/chat/count", userId)
            .retrieve()
            .body(ReadChatCountInnerResponse::class.java)

        return response?.count ?: 0L
    }

    fun countChatByUserIdAndRiskLevel(userId: Long, riskLevel: RiskLevel): Long {
        val response = restClient
            .get()
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
}