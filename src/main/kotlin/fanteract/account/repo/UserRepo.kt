package fanteract.account.repo

import fanteract.account.entity.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
@Repository
interface UserRepo : JpaRepository<User, Long> {

    @Query("""
        SELECT u
        FROM User u
        WHERE u.email = :email
          AND u.status = 'ACTIVATED'
    """)
    fun findByEmail(
        @Param("email") email: String
    ): User?

    @Query("""
        SELECT u
        FROM User u
        WHERE u.userId IN :idList
          AND u.status = 'ACTIVATED'
    """)
    fun findByUserIdIn(
        @Param("idList") idList: List<Long>
    ): List<User>

    fun existsByEmail(email: String): Boolean
}