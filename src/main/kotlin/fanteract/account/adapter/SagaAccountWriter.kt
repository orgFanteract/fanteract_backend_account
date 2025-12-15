package fanteract.account.adapter

import fanteract.account.entity.SagaAccount
import fanteract.account.enumerate.EventStatus
import fanteract.account.repo.SagaAccountRepo
import org.springframework.stereotype.Component

@Component
class SagaAccountWriter(
    private val sagaAccountRepo: SagaAccountRepo,
) {
    fun create(
        sagaId: String,
        eventId: String,
        eventName: String,
        payload: String?,
        eventStatus: EventStatus,
        isExec: Boolean = true
    ){
        val sagaAccount =
            sagaAccountRepo.save(
                SagaAccount(
                    sagaId = sagaId,
                    eventId = eventId,
                    eventName = eventName,
                    payload = payload,
                    eventStatus = eventStatus,
                    isExec = isExec
                )
            )
    }
}