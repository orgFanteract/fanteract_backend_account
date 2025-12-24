package fanteract.account.repo

import fanteract.account.entity.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
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

    @Modifying
    @Query("""
        update User u
        set u.balance = u.balance - :amount
        where u.userId = :userId and u.balance >= :amount
    """)
    fun debitIfEnough(
        @Param("userId") userId: Long,
        @Param("amount") amount: Int
    ): Int

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        update User u
        set u.activePoint = u.activePoint + :delta
        where u.userId = :userId
    """)
    fun increaseActivePoint(
        @Param("userId") userId: Long,
        @Param("delta") delta: Int
    ): Int


    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        update User u
        set u.abusePoint = u.abusePoint + :delta
        where u.userId = :userId
    """)
    fun increaseAbusePoint(
        @Param("userId") userId: Long,
        @Param("delta") delta: Int
    ): Int

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        update User u
        set u.balance = u.balance + :delta
        where u.userId = :userId
    """)
    fun increaseBalance(
        @Param("userId") userId: Long,
        @Param("delta") delta: Int
    ): Int
}