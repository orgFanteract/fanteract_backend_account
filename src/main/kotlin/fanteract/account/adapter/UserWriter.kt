package fanteract.account.adapter

import fanteract.account.entity.User
import fanteract.account.exception.ExceptionType
import fanteract.account.exception.MessageType
import fanteract.account.repo.UserRepo
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
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

    @Transactional
    fun updateActivePoint(
        userId: Long,
        activePoint: Int
    ) {
        val updated = userRepo.increaseActivePoint(userId, activePoint)
        if (updated == 0) {
            throw ExceptionType.withType(MessageType.NOT_EXIST)
        }
    }

    @Transactional
    fun updateAbusePoint(userId: Long, abusePoint: Int) {
        val updated = userRepo.increaseAbusePoint(userId, abusePoint)
        if (updated == 0) {
            throw ExceptionType.withType(MessageType.NOT_EXIST)
        }
    }

    @Transactional
    fun updateBalance(userId: Long, balance: Int) {
        val updated = userRepo.increaseBalance(userId, balance)
        if (updated == 0) {
            throw ExceptionType.withType(MessageType.NOT_ENOUGH_BALANCE)
        }
    }

    fun debitIfEnough(userId: Long, amount: Int): Int {
        return userRepo.debitIfEnough(userId, amount)
    }
}