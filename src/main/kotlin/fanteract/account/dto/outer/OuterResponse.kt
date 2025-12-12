package fanteract.account.dto.outer

import fanteract.account.enumerate.RiskLevel

data class ReadUserSignInOuterResponse(
    val token: String,
)

data class ReadUserMyPageOuterResponse(
    val email: String,
    val name: String,

    val activityStats: ActivityStats,
    val restrictionStats: RestrictionStats,
    val userScore: UserScore,
)

data class ReadRestrictedBoardPageOuterResponse(
    val contents: List<ReadRestrictedBoardOuterResponse>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int,
    val hasNext: Boolean,
)

data class ReadRestrictedBoardOuterResponse(
    val boardId: Long,
    val title: String,
    val content: String,
    val riskLevel: RiskLevel,
)

data class ReadRestrictedCommentPageOuterResponse(
    val contents: List<ReadRestrictedCommentOuterResponse>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int,
    val hasNext: Boolean,
)

data class ReadRestrictedCommentOuterResponse(
    val commentId: Long,
    val content: String,
    val riskLevel: RiskLevel,
)

data class ReadRestrictedChatPageOuterResponse(
    val contents: List<ReadRestrictedChatOuterResponse>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int,
    val hasNext: Boolean,
)

data class ActivityStats(
    val totalChatRoomCount: Long?,
    val totalChatCount: Long?,
    val totalBoardCount: Long?,
    val totalCommentCount: Long?,
)

data class RestrictionStats(
    val totalRestrictedChatCount: Long?,
    val totalRestrictedBoardCount: Long?,
    val totalRestrictedCommentCount: Long?,
)

data class UserScore(
    val activePoint: Int,
    val abusePoint: Int,
    val balance: Int,
)

data class ReadRestrictedChatOuterResponse(
    val chatId: Long,
    val content: String,
    val riskLevel: RiskLevel,
)

data class PurchaseProductOuterResponse(
    val paymentHistoryId: Long,
)