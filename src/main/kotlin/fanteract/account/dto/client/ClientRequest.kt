package fanteract.account.dto.client

import fanteract.account.enumerate.RiskLevel
import fanteract.account.enumerate.WriteStatus

data class UpdateActivePointRequest(
    val userId: Long,
    val activePoint: Int,
)
data class WriteCommentForUserRequest(
    val userId: Long,
    val writeStatus: WriteStatus,
    val riskLevel: RiskLevel,
)