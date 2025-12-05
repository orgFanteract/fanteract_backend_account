package fanteract.account.api.outer

import io.swagger.v3.oas.annotations.Operation
import jakarta.servlet.http.HttpServletRequest
import fanteract.account.config.JwtParser
import fanteract.account.annotation.LoginRequired
import fanteract.account.dto.outer.*
import fanteract.account.service.UserService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/users")
class UserAPI(
    private val userService: UserService,
) {
    @Operation(summary = "로그인")
    @PostMapping("/sign-in")
    fun signIn(
        @RequestBody readUserSignInOuterRequest: ReadUserSignInOuterRequest
    ): ResponseEntity<ReadUserSignInOuterResponse>{
        val response = userService.signIn(readUserSignInOuterRequest)

        return ResponseEntity.ok(response)
    }

    @Operation(summary = "회원 가입")
    @PostMapping("/sign-up")
    fun signUpWithCredential(
        @RequestBody createUserSignUpOuterRequest: CreateUserSignUpOuterRequest,
    ): ResponseEntity<Void> {
        userService.signUp(createUserSignUpOuterRequest)

        return ResponseEntity.ok().build()
    }

    //@LoginRequired
    @Operation(summary = "마이페이지 조회")
    @GetMapping("/my-page")
    fun readMyPage(
        @RequestHeader("X-User-Id") userId: Long,
    ): ResponseEntity<ReadUserMyPageOuterResponse>{
        //val userId = JwtParser.extractKey(request, "userId")
        val response = userService.readMyPage(userId)

        return ResponseEntity.ok(response)
    }

    //@LoginRequired
    @Operation(summary = "마이페이지 - 제한된 게시글 조회")
    @GetMapping("/my-page/restricted-board")
    fun readRestrictedBoard(
        @RequestHeader("X-User-Id") userId: Long,
        @RequestParam("page", defaultValue = "0") page: Int,
        @RequestParam("size", defaultValue = "10") size: Int,
    ): ResponseEntity<ReadRestrictedBoardPageOuterResponse> {
        //val userId = JwtParser.extractKey(request, "userId")
        val response = userService.readRestrictedBoard(userId, page, size)

        return ResponseEntity.ok(response)
    }

    //@LoginRequired
    @Operation(summary = "마이페이지 - 제한된 코멘트 조회")
    @GetMapping("/my-page/restricted-comment")
    fun readRestrictedComment(
        @RequestHeader("X-User-Id") userId: Long,
        @RequestParam("page", defaultValue = "0") page: Int,
        @RequestParam("size", defaultValue = "10") size: Int,
    ): ResponseEntity<ReadRestrictedCommentPageOuterResponse> {
        //val userId = JwtParser.extractKey(request, "userId")
        val response = userService.readRestrictedComment(userId, page, size)

        return ResponseEntity.ok(response)
    }

    //@LoginRequired
    @Operation(summary = "마이페이지 - 제한된 채팅 조회")
    @GetMapping("/my-page/restricted-chat")
    fun readRestrictedChat(
        @RequestHeader("X-User-Id") userId: Long,
        @RequestParam("page", defaultValue = "0") page: Int,
        @RequestParam("size", defaultValue = "10") size: Int,
    ): ResponseEntity<ReadRestrictedChatPageOuterResponse> {
        //val userId = JwtParser.extractKey(request, "userId")
        val response = userService.readRestrictedChat(userId, page, size)

        return ResponseEntity.ok(response)
    }
}