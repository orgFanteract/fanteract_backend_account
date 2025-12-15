package fanteract.account.adapter

import fanteract.account.entity.Product
import fanteract.account.exception.ExceptionType
import fanteract.account.exception.MessageType
import fanteract.account.repo.ProductRepo
import org.springframework.stereotype.Component

@Component
class ProductReader(
    private val productRepo: ProductRepo,
) {
    fun findById(productId: Long): Product {
        return productRepo.findById(productId).orElseThrow{ExceptionType.withType(MessageType.NOT_EXIST)}
    }

}