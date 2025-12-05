package fanteract.account.service

import fanteract.account.domain.PaymentHistoryWriter
import fanteract.account.domain.ProductReader
import fanteract.account.domain.UserReader
import fanteract.account.domain.UserWriter
import fanteract.account.dto.outer.*
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Transactional
@Service
class PaymentService(
    private val userReader: UserReader,
    private val userWriter: UserWriter,
    private val paymentHistoryWriter: PaymentHistoryWriter,
    private val productReader: ProductReader,
) {
    fun purchaseProduct(productId: Long, userId: Long): PurchaseProductOuterResponse {
        val product = productReader.findById(productId)

        // user balance 갱신
        val user = userReader.findById(userId)

        userWriter.updateBalance(
            userId = user.userId,
            balance = product.cost
        )

        // payment 기록
        val response =
            paymentHistoryWriter.create(
                userId = userId,
                productId = productId,
            )

        return PurchaseProductOuterResponse(paymentHistoryId = response.paymentHistoryId)
    }
}