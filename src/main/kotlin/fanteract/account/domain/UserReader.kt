package fanteract.account.domain

import fanteract.account.entity.User
import fanteract.account.enumerate.Status
import fanteract.account.repo.UserRepo
import org.springframework.stereotype.Component

@Component
class UserReader(
    private val userRepo: UserRepo,
) {
    fun findByEmail(email: String): User {
        return userRepo.findByEmail(email) ?: throw NoSuchElementException("조건에 맞는 사용자가 존재하지 않습니다")
    }

    fun findByIdIn(idList: List<Long>): List<User> {
        return userRepo.findByUserIdIn(idList)
    }

    fun findById(userId: Long): User {
        return userRepo.findById(userId).orElseThrow{NoSuchElementException("조건에 맞는 사용자가 존재하지 않습니다")}
    }

    fun existsById(userId: Long) {
        val user = userRepo.findById(userId).orElseThrow{NoSuchElementException("조건에 맞는 사용자가 존재하지 않습니다")}

        if (user.status == Status.DELETED){
            throw NoSuchElementException("조건에 맞는 사용자가 존재하지 않습니다")
        }
    }
}