package fanteract.account.repo

import fanteract.account.entity.PaymentHistory
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface PaymentHistoryRepo: JpaRepository<PaymentHistory, Long>