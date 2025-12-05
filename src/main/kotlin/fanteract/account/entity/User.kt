package fanteract.account.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Table
import jakarta.persistence.Id
import fanteract.account.entity.constant.BaseEntity
import java.time.LocalDateTime

@Entity
@Table(name = "users")
class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val userId: Long = 0L,
    @Column(unique = true)
    val email: String,
    val password: String,
    val name: String,
    var balance: Int = 0,
    var activePoint: Int = 0,
    var abusePoint: Int = 0,
    val passExpiredAt: LocalDateTime? = null,
): BaseEntity()