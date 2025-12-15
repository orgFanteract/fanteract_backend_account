package fanteract.account.adapter

import fanteract.account.entity.User
import fanteract.account.enumerate.Status
import fanteract.account.exception.ExceptionType
import fanteract.account.exception.MessageType
import fanteract.account.repo.UserRepo
import org.springframework.stereotype.Component

@Component
class UserReader(
    private val userRepo: UserRepo,
) {
    fun findByEmail(email: String): User {
        return userRepo.findByEmail(email) ?: throw ExceptionType.withType(MessageType.NOT_EXIST)
    }

    fun findByIdIn(idList: List<Long>): List<User> {
        return userRepo.findByUserIdIn(idList)
    }

    fun findById(userId: Long): User {
        return userRepo.findById(userId).orElseThrow{ExceptionType.withType(MessageType.NOT_EXIST)}
    }

    fun existsById(userId: Long) {
        val user = userRepo.findById(userId).orElseThrow{ExceptionType.withType(MessageType.NOT_EXIST)}

        if (user.status == Status.DELETED){
            throw ExceptionType.withType(MessageType.NOT_EXIST)
        }
    }
}