package fanteract.account.dto.client

import fanteract.account.enumerate.ActivePoint
import fanteract.account.enumerate.RiskLevel
import java.time.LocalDateTime

data class ReadBoardInnerResponse(
    val boardId: Long = 0L,
    var title: String,
    var content: String,
    val userId: Long,
    val riskLevel: RiskLevel,
)

data class ReadBoardCountInnerResponse(
    val count: Long,
)

data class ReadBoardPageInnerResponse(
    val contents: List<ReadBoardInnerResponse>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int,
    val hasNext: Boolean,
)

data class ReadChatCountInnerResponse(
    val count: Long,
)

data class ReadChatroomCountInnerResponse(
    val count: Long,
)


data class ReadChatInnerResponse(
    val chatId: Long = 0L,
    val content: String,
    val chatroomId: Long,
    val userId: Long,
    val riskLevel: RiskLevel,
)

data class ReadChatPageInnerResponse(
    val contents: List<ReadChatInnerResponse>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int,
    val hasNext: Boolean,
)

data class ReadCommentCountInnerResponse(
    val count: Long,
)

data class ReadCommentInnerResponse(
    val commentId: Long,
    var content: String,
    val boardId: Long,
    val userId: Long,
    val riskLevel: RiskLevel,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
)

data class ReadCommentPageInnerResponse(
    val contents: List<ReadCommentInnerResponse>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int,
    val hasNext: Boolean,
)