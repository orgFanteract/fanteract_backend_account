package fanteract.account

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaAuditing

@EnableJpaAuditing
@SpringBootApplication
class AccountApplication

fun main(args: Array<String>) {
	runApplication<AccountApplication>(*args)
}
