package fanteract.account.adapter

import fanteract.account.entity.PaymentHistory
import fanteract.account.repo.PaymentHistoryRepo
import org.springframework.stereotype.Component

@Component
class PaymentHistoryWriter(
    private val paymentHistoryRepo: PaymentHistoryRepo
) {
    fun create(userId: Long, productId: Long): PaymentHistory {
        return paymentHistoryRepo.save(
            PaymentHistory(
                userId = userId,
                productId = productId,
            )
        )
    }

}