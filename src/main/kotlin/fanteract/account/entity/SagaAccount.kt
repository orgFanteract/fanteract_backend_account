package fanteract.account.entity

import fanteract.account.entity.constant.BaseEntity
import fanteract.account.enumerate.EventStatus
import jakarta.persistence.*

@Entity
@Table(name = "saga_account")
class SagaAccount (
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,
    val sagaId: String,
    val eventId: String,
    val eventName: String,
    val payload: String?,
    @Enumerated(EnumType.STRING)
    val eventStatus: EventStatus,
    val isExec: Boolean = false,

): BaseEntity()