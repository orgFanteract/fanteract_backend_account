package fanteract.account.dto.inner

data class UpdateBalanceInnerRequest(
    val balance: Int,
)

data class UpdateActivePointInnerRequest(
    val activePoint: Int,
)

data class UpdateAbusePointInnerRequest(
    val abusePoint: Int,
)
