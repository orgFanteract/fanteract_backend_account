package fanteract.account.repo

import fanteract.account.entity.SagaAccount
import fanteract.account.enumerate.EventStatus
import org.springframework.data.jpa.repository.JpaRepository

interface SagaAccountRepo: JpaRepository<SagaAccount, Long> {
    fun findBySagaIdAndEventNameAndEventStatus(sagaId: String, eventName: String, eventStatus: EventStatus): List<SagaAccount>
    fun existsBySagaIdAndEventNameAndEventStatus(sagaId: String, eventName: String, eventStatus: fanteract.account.enumerate.EventStatus): Boolean
}