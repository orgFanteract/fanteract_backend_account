package fanteract.account.repo

import fanteract.account.entity.Product
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ProductRepo: JpaRepository<Product, Long>