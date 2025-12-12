package fanteract.account.api.inner

import fanteract.account.dto.inner.*
import fanteract.account.entity.User
import fanteract.account.service.UserService
import io.swagger.v3.oas.annotations.Hidden
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@Hidden
@RestController
@RequestMapping("/internal/users")
class UserInnerAPI(
    private val userService: UserService,
) {
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
        //simulateDelay() // 서비스 실행 전 랜덤 지연
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

    private fun simulateDelay() {
        val randomValue = Math.random()
        println("simulateDelay randomValue=$randomValue")

        if (randomValue > 0.7) {
            println("simulateDelay → randomValue > 0.7, sleeping 5 seconds...")
            Thread.sleep(5000) // 5초 지연
        }
    }
}