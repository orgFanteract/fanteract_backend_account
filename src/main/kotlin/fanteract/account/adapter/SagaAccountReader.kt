package fanteract.account.adapter

import fanteract.account.entity.SagaAccount
import fanteract.account.enumerate.EventStatus
import fanteract.account.exception.ExceptionType
import fanteract.account.exception.MessageType
import fanteract.account.repo.SagaAccountRepo
import org.springframework.stereotype.Component

@Component
class SagaAccountReader(
    private val sagaAccountRepo: SagaAccountRepo,
) {
fun findBySagaIdAndEventNameAndEventStatus(sagaId: String, eventName: String, eventStatus: EventStatus): SagaAccount {
        return sagaAccountRepo.findBySagaIdAndEventNameAndEventStatus(sagaId, eventName, eventStatus).firstOrNull()
            ?: throw ExceptionType.withType(MessageType.NOT_EXIST)
    }

    fun existsBySagaIdAndEventNameAndEventStatus(
        sagaId: String,
        eventName: String,
        eventStatus: EventStatus
    ): Boolean {
        return sagaAccountRepo.existsBySagaIdAndEventNameAndEventStatus(sagaId, eventName, eventStatus)
    }
}