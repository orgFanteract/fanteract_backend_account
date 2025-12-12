package fanteract.account.domain

import fanteract.account.entity.User
import fanteract.account.exception.ExceptionType
import fanteract.account.exception.MessageType
import fanteract.account.repo.UserRepo
import org.springframework.stereotype.Component
import kotlin.String

@Component
class UserWriter(
    private val userRepo: UserRepo,
) {
    fun create(
        email: String,
        password: String,
        name: String,
    ): User {
        if (userRepo.existsByEmail(email)){
            throw ExceptionType.withType(MessageType.ALREADY_EXIST)
        }
        return userRepo.save(
            User(
                email = email,
                name = name,
                password = password,
            )
        )
    }

    fun updateActivePoint(
        userId: Long,
        activePoint: Int
    ) {
        val user = userRepo.findById(userId).orElseThrow{ ExceptionType.withType(MessageType.NOT_EXIST)}
        user.activePoint += activePoint

        userRepo.save(user)
    }

    fun updateAbusePoint(
        userId: Long,
        abusePoint: Int
    ) {
        val user = userRepo.findById(userId).orElseThrow{ExceptionType.withType(MessageType.NOT_EXIST)}
        user.abusePoint += abusePoint

        userRepo.save(user)
    }

    fun updateBalance(
        userId: Long,
        balance: Int
    ) {
        val user = userRepo.findById(userId).orElseThrow{ExceptionType.withType(MessageType.NOT_EXIST)}
        user.balance += balance

        userRepo.save(user)
    }

    fun debitIfEnough(userId: Long, amount: Int): Int {
        return userRepo.debitIfEnough(userId, amount)
    }
}