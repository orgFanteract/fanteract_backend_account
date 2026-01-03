package fanteract.account.interceptor

import io.jsonwebtoken.security.Keys
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.web.method.HandlerMethod
import org.springframework.web.servlet.HandlerInterceptor
import io.jsonwebtoken.Jwts
import fanteract.account.annotation.LoginRequired
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import kotlin.jvm.java
import kotlin.text.startsWith
import kotlin.text.substringAfter
import kotlin.text.toByteArray

@Component
class AuthInterceptor(
    @Value($$"${jwt.secret}") private val jwtSecret: String,
) : HandlerInterceptor {
    private val log = KotlinLogging.logger {}
    override fun preHandle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any
    ): Boolean {
        if (handler !is HandlerMethod) return true

        val hasAnnotation = handler.getMethodAnnotation(LoginRequired::class.java) != null
        if (!hasAnnotation) return true

        val authHeader = request.getHeader("Authorization") ?: return unauthorized(response)
        if (!authHeader.startsWith("Bearer ")) return unauthorized(response)

        val token = authHeader.substringAfter("Bearer ")
        val secretKey = Keys.hmacShaKeyFor(jwtSecret.toByteArray())

        return try {
            val subject = Jwts.parser().verifyWith(secretKey).build()
                .parseSignedClaims(token).payload.subject

            request.setAttribute("userId", subject)

            true

        } catch (e: Exception) {
            unauthorized(response)
        }
    }

    private fun unauthorized(response: HttpServletResponse): Boolean {
        response.status = 401
        return false
    }
}