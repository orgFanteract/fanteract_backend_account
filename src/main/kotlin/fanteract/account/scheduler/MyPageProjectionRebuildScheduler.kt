package fanteract.account.scheduler

import fanteract.account.adapter.MyPageRedisWriter
import fanteract.account.adapter.UserReader
import fanteract.account.adapter.UserWriter
import fanteract.account.client.ConnectClient
import fanteract.account.client.SocialClient
import fanteract.account.enumerate.RiskLevel
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class MyPageProjectionRebuildScheduler(
    private val redisWriter: MyPageRedisWriter,
    private val userReader: UserReader,
    private val socialClient: SocialClient,
    private val connectClient: ConnectClient,
) {
    //@Scheduled(fixedDelay = 10_000)
    fun rebuildMyPageProjection() {
        println("rebuildMyPageProjection exec")
        val userIdList = userReader.findAll().map{it.userId}

        for (userId in userIdList){
            val chatroomCount = connectClient.countChatroomByUserId(userId)
            val chatCount = connectClient.countChatByUserId(userId)
            val boardCount = socialClient.countBoardByUserId(userId)
            val commentCount = socialClient.countCommentByUserId(userId)

            val restrictedChatCount = connectClient.countChatByUserIdAndRiskLevel(userId, RiskLevel.BLOCK)
            val restrictedBoardCount = socialClient.countBoardByUserIdAndRiskLevel(userId, RiskLevel.BLOCK)
            val restrictedCommentCount = socialClient.countCommentByUserIdAndRiskLevel(userId, RiskLevel.BLOCK)

            redisWriter.rebuildSnapshot(
                userId = userId,
                chatroomCount = chatroomCount ?: 0L,
                chatCount = chatCount ?: 0L,
                boardCount = boardCount ?: 0L,
                commentCount = commentCount ?: 0L,
                restrictedChatCount = restrictedChatCount ?: 0L,
                restrictedBoardCount = restrictedBoardCount ?: 0L,
                restrictedCommentCount = restrictedCommentCount ?: 0L,
            )
        }
    }
}