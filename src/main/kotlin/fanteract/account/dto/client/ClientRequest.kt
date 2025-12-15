package fanteract.account.dto.client

data class UpdateActivePointRequest(
    val userId: Long,
    val activePoint: Int,
)
