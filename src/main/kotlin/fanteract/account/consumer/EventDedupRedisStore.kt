package fanteract.account.consumer

import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Component
import java.time.Duration

@Component
class EventDedupRedisStore(
    private val redis: StringRedisTemplate,
) {
    fun isDuplicate(eventId: String, ttlHours: Long = 24): Boolean {
        val key = "dedup:event:$eventId"
        val ok = redis.opsForValue().setIfAbsent(key, "1", Duration.ofHours(ttlHours))
        return ok != true // false면 이미 존재(중복)
    }
}