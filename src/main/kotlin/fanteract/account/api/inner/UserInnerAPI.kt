package fanteract.account.api.inner

import fanteract.account.dto.inner.*
import fanteract.account.entity.User
import fanteract.account.service.UserService
import io.swagger.v3.oas.annotations.Hidden
import mu.KotlinLogging
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

//@Hidden
@RestController
@RequestMapping("/internal/users")
class UserInnerAPI(
    private val userService: UserService,
) {
    private val log = KotlinLogging.logger {}
    @GetMapping("/{userId}/exists")
    fun readUserExistsById(
        @PathVariable userId: Long,
    ): ResponseEntity<ReadUserExistsInnerResponse> {
        val payload = userService.existsById(userId)
        val response = ReadUserExistsInnerResponse(exists = payload)

        return ResponseEntity.ok().body(response)
    }

    // 단건 유저 조회
    @GetMapping("/{userId}")
    fun readUserById(
        @PathVariable userId: Long,
    ): ResponseEntity<ReadUserInnerResponse> {
        val response = userService.findById(userId)

        return ResponseEntity.ok().body(response)
    }

    // 유저 잔액 수정
    @PutMapping("/{userId}/balance")
    fun updateUserBalance(
        @PathVariable userId: Long,
        @RequestBody request: UpdateBalanceInnerRequest,
    ): ResponseEntity<Void> {
        userService.updateBalance(userId, request.balance)

        return ResponseEntity.ok().build()
    }

    // 유저 사용 가능 포인트 수정
    @PutMapping("/{userId}/active-point")
    fun updateActivePoint(
        @PathVariable userId: Long,
        @RequestBody request: UpdateActivePointInnerRequest,
    ): ResponseEntity<Void> {
        userService.updateActivePoint(userId, request.activePoint)

        return ResponseEntity.ok().build()
    }

    // 유저 부정 포인트 수정
    @PutMapping("/{userId}/abuse-point")
    fun updateAbusePoint(
        @PathVariable userId: Long,
        @RequestBody request: UpdateAbusePointInnerRequest,
    ): ResponseEntity<Void> {
        userService.updateAbusePoint(userId, request.abusePoint)

        return ResponseEntity.ok().build()
    }

    // 여러 유저 ID로 조회
    @GetMapping("/batch")
    fun findByIdIn(
        @RequestParam("userIds") userIds: List<Long>,
    ): ResponseEntity<ReadUserListInnerResponse> {
        val response = userService.findByIdIn(userIds)

        return ResponseEntity.ok().body(response)
    }

    @PutMapping("/{userId}/debit")
    fun debitIfEnough(
        @PathVariable userId: Long,
        @RequestBody request: UpdateUserDebitIfEnoughInnerRequest,
    ): ResponseEntity<UpdateUserDebitIfEnoughInnerResponse>{
        val response = userService.debitIfEnough(userId, request.amount)

        return ResponseEntity.ok().body(response)
    }

    private fun simulateDelay() {
        val randomValue = Math.random()
        log.info{"simulateDelay randomValue=$randomValue"}

        if (randomValue > 0.7) {
            Thread.sleep(5000) // 5초 지연
        }
    }
}