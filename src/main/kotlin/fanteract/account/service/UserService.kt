package fanteract.account.service

import fanteract.account.adapter.MessageAdapter
import fanteract.account.adapter.MyPageRedisReader
import fanteract.account.adapter.MyPageRedisWriter
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import fanteract.account.client.ConnectClient
import fanteract.account.client.SocialClient
import fanteract.account.adapter.UserReader
import fanteract.account.adapter.UserWriter
import fanteract.account.dto.client.UpdateActivePointRequest
import fanteract.account.dto.inner.*
import fanteract.account.dto.outer.*
import fanteract.account.entity.User
import fanteract.account.enumerate.ActivePoint
import fanteract.account.enumerate.Balance
import fanteract.account.enumerate.RiskLevel
import fanteract.account.enumerate.TopicService
import fanteract.account.exception.ExceptionType
import fanteract.account.exception.MessageType
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import kotlin.Long
import kotlin.collections.map
import kotlin.text.toByteArray

@Transactional
@Service
class UserService(
    private val userReader: UserReader,
    private val userWriter: UserWriter,
    private val socialClient: SocialClient,
    private val connectClient: ConnectClient,
    private val messageAdapter: MessageAdapter,
    private val myPageRedisReader: MyPageRedisReader,
    private val myPageRedisWriter: MyPageRedisWriter,
    @Value($$"${jwt.secret}") private val jwtSecret: String,
) {
    fun signIn(readUserSignInOuterRequest: ReadUserSignInOuterRequest): ReadUserSignInOuterResponse {
        val user = userReader.findByEmail(readUserSignInOuterRequest.email)

        if (user.password != readUserSignInOuterRequest.password){
            throw ExceptionType.withType(MessageType.NOT_EXIST)
        }

        val secretKey = Keys.hmacShaKeyFor(jwtSecret.toByteArray())
        val token = Jwts.builder().subject(user.userId.toString()).signWith(secretKey).compact()

        return ReadUserSignInOuterResponse(token)
    }

    fun signUp(createUserSignUpOuterRequest: CreateUserSignUpOuterRequest): User {
        val user =
            userWriter.create(
                email = createUserSignUpOuterRequest.email,
                password = createUserSignUpOuterRequest.password,
                name = createUserSignUpOuterRequest.name,
            )

        return user
    }

    fun readMyPage(userId: Long): ReadUserMyPageOuterResponse {
        val user = userReader.findById(userId)

        val chatroomCount = connectClient.countChatroomByUserId(user.userId)
        val chatCount = connectClient.countChatByUserId(user.userId)
        val boardCount = socialClient.countBoardByUserId(user.userId)
        val commentCount = socialClient.countCommentByUserId(user.userId)

        val restrictedChatCount = connectClient.countChatByUserIdAndRiskLevel(user.userId, RiskLevel.BLOCK)
        val restrictedBoardCount = socialClient.countBoardByUserIdAndRiskLevel(user.userId, RiskLevel.BLOCK)
        val restrictedCommentCount = socialClient.countCommentByUserIdAndRiskLevel(user.userId, RiskLevel.BLOCK)

        val activityStats =
            ActivityStats(
                totalChatRoomCount = chatroomCount,
                totalChatCount = chatCount,
                totalBoardCount = boardCount,
                totalCommentCount = commentCount,
            )
        val restrictionStats =
            RestrictionStats(
                totalRestrictedChatCount = restrictedChatCount,
                totalRestrictedBoardCount = restrictedBoardCount,
                totalRestrictedCommentCount = restrictedCommentCount,
            )
        val userScore = 
            UserScore(
                activePoint = user.activePoint,
                abusePoint = user.abusePoint,
                balance = user.balance,
        )

        println("activityStats = ${activityStats.totalChatRoomCount} / ${activityStats.totalChatCount} / ${activityStats.totalBoardCount} / ${activityStats.totalCommentCount}")
        println("restrictionStats = ${restrictionStats.totalRestrictedChatCount} / ${restrictionStats.totalRestrictedBoardCount} / ${restrictionStats.totalRestrictedCommentCount}")
        println("userScore = ${userScore.activePoint} / ${userScore.abusePoint} / ${userScore.balance}")

        return ReadUserMyPageOuterResponse(
            email = user.email,
            name = user.name,
            activityStats = activityStats,
            restrictionStats = restrictionStats,
            userScore = userScore
        )
    }

    fun readMyPageNew(userId: Long): ReadUserMyPageOuterResponse {
        val user = userReader.findById(userId)

        // 1) Redis snapshot 우선
        val snapshot = myPageRedisReader.readSnapshot(user.userId)
        if (snapshot != null) {
            return ReadUserMyPageOuterResponse(
                email = user.email,
                name = user.name,
                activityStats = ActivityStats(
                    totalChatRoomCount = snapshot.chatroomCount,
                    totalChatCount = snapshot.chatCount,
                    totalBoardCount = snapshot.boardCount,
                    totalCommentCount = snapshot.commentCount,
                ),
                restrictionStats = RestrictionStats(
                    totalRestrictedChatCount = snapshot.restrictedChatCount,
                    totalRestrictedBoardCount = snapshot.restrictedBoardCount,
                    totalRestrictedCommentCount = snapshot.restrictedCommentCount,
                ),
                userScore = UserScore(
                    activePoint = user.activePoint,
                    abusePoint = user.abusePoint,
                    balance = user.balance,
                )
            )
        }

        // 2) fallback: Redis가 없을 때만 외부 count 호출
        val chatroomCount = connectClient.countChatroomByUserId(user.userId) ?: 0L
        val chatCount = connectClient.countChatByUserId(user.userId) ?: 0L
        val boardCount = socialClient.countBoardByUserId(user.userId) ?: 0L
        val commentCount = socialClient.countCommentByUserId(user.userId) ?: 0L

        val restrictedChatCount = connectClient.countChatByUserIdAndRiskLevel(user.userId, RiskLevel.BLOCK) ?: 0L
        val restrictedBoardCount = socialClient.countBoardByUserIdAndRiskLevel(user.userId, RiskLevel.BLOCK) ?: 0L
        val restrictedCommentCount = socialClient.countCommentByUserIdAndRiskLevel(user.userId, RiskLevel.BLOCK) ?: 0L

        // 3) Redis rebuild (다음 요청부터는 Redis만 읽음)
        myPageRedisWriter.rebuildSnapshot(
            userId = user.userId,
            chatroomCount = chatroomCount,
            chatCount = chatCount,
            boardCount = boardCount,
            commentCount = commentCount,
            restrictedChatCount = restrictedChatCount,
            restrictedBoardCount = restrictedBoardCount,
            restrictedCommentCount = restrictedCommentCount,
        )

        return ReadUserMyPageOuterResponse(
            email = user.email,
            name = user.name,
            activityStats = ActivityStats(
                totalChatRoomCount = chatroomCount,
                totalChatCount = chatCount,
                totalBoardCount = boardCount,
                totalCommentCount = commentCount,
            ),
            restrictionStats = RestrictionStats(
                totalRestrictedChatCount = restrictedChatCount,
                totalRestrictedBoardCount = restrictedBoardCount,
                totalRestrictedCommentCount = restrictedCommentCount,
            ),
            userScore = UserScore(
                activePoint = user.activePoint,
                abusePoint = user.abusePoint,
                balance = user.balance,
            )
        )
    }

    fun readRestrictedBoard(userId: Long, page: Int, size: Int): ReadRestrictedBoardPageOuterResponse {
        val pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))

        val boardPage = socialClient.findBoardByUserIdAndRiskLevel(userId, RiskLevel.BLOCK, pageable)

        val contents = boardPage.contents.map { board ->
            ReadRestrictedBoardOuterResponse(
                boardId = board.boardId,
                title = board.title,
                content = board.content,
                riskLevel = board.riskLevel
            )
        }

        return ReadRestrictedBoardPageOuterResponse(
            contents = contents,
            page = page,
            size = size,
            totalElements = boardPage.totalElements,
            totalPages = boardPage.totalPages,
            hasNext = boardPage.hasNext
        )
    }
    fun readRestrictedComment(userId: Long, page: Int, size: Int): ReadRestrictedCommentPageOuterResponse {
        val pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))

        val commentPage = socialClient.findCommentByUserIdAndRiskLevel(userId, RiskLevel.BLOCK, pageable)

        val contents = commentPage.contents.map { comment ->
            ReadRestrictedCommentOuterResponse(
                commentId = comment.commentId,
                content = comment.content,
                riskLevel = comment.riskLevel
            )
        }

        return ReadRestrictedCommentPageOuterResponse(
            contents = contents,
            page = page,
            size = size,
            totalElements = commentPage.totalElements,
            totalPages = commentPage.totalPages,
            hasNext = commentPage.hasNext
        )
    }
    fun readRestrictedChat(userId: Long, page: Int, size: Int): ReadRestrictedChatPageOuterResponse {
        val pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))
        val chatPage = connectClient.findChatByUserIdAndRiskLevel(userId, RiskLevel.BLOCK, pageable)

        val contents = chatPage.contents.map { chat ->
            ReadRestrictedChatOuterResponse(
                chatId = chat.chatId,
                content = chat.content,
                riskLevel = chat.riskLevel
            )
        }

        return ReadRestrictedChatPageOuterResponse(
            contents = contents,
            page = page,
            size = size,
            totalElements = chatPage.totalElements,
            totalPages = chatPage.totalPages,
            hasNext = chatPage.hasNext
        )
    }

    fun existsById(userId: Long): Boolean {
        userReader.existsById(userId)

        return true
    }
    fun findById(userId: Long): ReadUserInnerResponse {
        val user = userReader.findById(userId)

        return ReadUserInnerResponse(
            userId = user.userId,
            email = user.email,
            password = user.password,
            name = user.name,
            balance = user.balance,
            activePoint = user.activePoint,
            abusePoint = user.abusePoint,
        )
    }
    fun updateBalance(userId: Long, balance: Int) {
        userWriter.updateBalance(userId, balance)
    }
    fun updateActivePoint(userId: Long, activePoint: Int) {
        userWriter.updateActivePoint(userId, activePoint)
    }

    fun updateAbusePoint(userId: Long, abusePoint: Int) {
        userWriter.updateAbusePoint(userId, abusePoint)
    }
    fun findByIdIn(userIds: List<Long>): ReadUserListInnerResponse {
        val userList = userReader.findByIdIn(userIds)

        val payload = userList.map{ user ->
            ReadUserInnerResponse(
                userId = user.userId,
                email = user.email,
                password = user.password,
                name = user.name,
                balance = 0,
                activePoint = 0,
                abusePoint = 0,
            )
        }

        return ReadUserListInnerResponse(payload)
    }

    fun debitIfEnough(
        userId: Long,
        amount: Int,
    ): UpdateUserDebitIfEnoughInnerResponse{
        val response = userWriter.debitIfEnough(userId, amount)

        return UpdateUserDebitIfEnoughInnerResponse(response)
    }


}