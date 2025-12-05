package fanteract.account.dto.outer

data class ReadUserSignInOuterRequest(
    val email: String,
    val password: String,
)

data class CreateUserSignUpOuterRequest(
    val email: String,
    val password: String,
    val name: String,
)