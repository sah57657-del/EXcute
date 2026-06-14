package com.example.data

import kotlinx.coroutines.flow.Flow
import java.util.UUID

sealed class ReferralResult {
    data class Success(val referrerName: String) : ReferralResult()
    data class Error(val message: String) : ReferralResult()
}

sealed class WithdrawResult {
    data object Success : WithdrawResult()
    data class Error(val message: String) : WithdrawResult()
}

class RewardRepository(
    private val userDao: UserDao,
    private val taskDao: TaskDao,
    private val taskCompletionDao: TaskCompletionDao,
    private val withdrawalDao: WithdrawalDao,
    private val earnLogDao: EarnLogDao
) {
    // Flows
    fun getUser(userId: String): Flow<UserEntity?> = userDao.getUser(userId)
    fun getActiveTasks(): Flow<List<TaskEntity>> = taskDao.getAllActiveTasks()
    fun getCompletedTaskIds(userId: String): Flow<List<String>> = taskCompletionDao.getCompletedTaskIdsForUser(userId)
    fun getWithdrawals(userId: String): Flow<List<WithdrawalEntity>> = withdrawalDao.getWithdrawalsForUser(userId)
    fun getLogs(userId: String): Flow<List<EarnLogEntity>> = earnLogDao.getLogsForUser(userId)
    fun getAllWithdrawalsAdmin(): Flow<List<WithdrawalEntity>> = withdrawalDao.getAllWithdrawals()

    // Sync helpers
    suspend fun getUserSync(userId: String): UserEntity? = userDao.getUserSync(userId)
    suspend fun getAllUsersSync(): List<UserEntity> = userDao.getAllUsersSync()

    // Add and Register new user profile
    suspend fun registerUser(name: String, email: String, phoneNumber: String = "", paypalEmail: String = ""): UserEntity {
        val existingUsersCount = userDao.getAllUsersSync().size
        val userId = "user_${UUID.randomUUID().toString().take(6)}"
        val referralCode = name.filter { it.isLetterOrDigit() }.uppercase().take(4) + (100..999).random().toString()
        val user = UserEntity(
            id = userId,
            name = name,
            email = email,
            phoneNumber = phoneNumber,
            paypalEmail = paypalEmail,
            coins = if (existingUsersCount == 0) 100 else 0, // Starter coins for the very first user!
            referralCode = referralCode
        )
        userDao.insertUser(user)
        earnLogDao.insertLog(
            EarnLogEntity(
                userId = userId,
                message = "Welcome to CoinReward! Account created.",
                amount = user.coins
            )
        )
        return user
    }

    // Update PayPal linked account details
    suspend fun updatePaypalEmail(userId: String, paypalEmail: String) {
        val user = userDao.getUserSync(userId) ?: return
        val updatedUser = user.copy(paypalEmail = paypalEmail)
        userDao.insertUser(updatedUser)
        earnLogDao.insertLog(
            EarnLogEntity(
                userId = userId,
                message = "PayPal account linked: $paypalEmail",
                amount = 0
            )
        )
    }

    // Direct coin addition (Ad rewards or other sources)
    suspend fun addCoins(userId: String, amount: Int, reason: String) {
        val user = userDao.getUserSync(userId) ?: return
        val currentCoins = user.coins
        val newCoins = currentCoins + amount
        userDao.updateCoins(userId, newCoins)
        earnLogDao.insertLog(
            EarnLogEntity(
                userId = userId,
                message = "Earned $amount coins for: $reason",
                amount = amount
            )
        )
    }

    // Task completion logic
    suspend fun completeTask(userId: String, taskId: String): Boolean {
        val alreadyCompleted = taskCompletionDao.isTaskCompletedSync(userId, taskId)
        if (alreadyCompleted) return false

        val task = taskDao.getTaskSync(taskId) ?: return false
        val user = userDao.getUserSync(userId) ?: return false

        // Check VIP security lock for annual extra tasks
        if (taskId.startsWith("task_vip_") && !user.isVip) {
            return false
        }

        val boostMultiplier = when (user.vipLevel.lowercase()) {
            "level 1" -> 1.2
            "level 2" -> 1.5
            "level 3" -> 2.0
            "level 4" -> 2.5
            "level 5" -> 3.0
            "gold vip" -> 2.0
            "silver vip" -> 1.5
            "bronze vip" -> 1.2
            else -> 1.0
        }
        val baseReward = task.reward
        val totalReward = (baseReward * boostMultiplier).toInt()
        val bonus = totalReward - baseReward

        val newCoins = user.coins + totalReward
        userDao.updateCoins(userId, newCoins)
        taskCompletionDao.insertCompletion(TaskCompletionEntity(userId = userId, taskId = taskId))
        
        val logMessage = if (bonus > 0) {
            "Earned +$baseReward coins + $bonus VIP bonus coins (${user.vipLevel}) for: '${task.title}'"
        } else {
            "Earned +$baseReward coins for completing task: '${task.title}'"
        }

        earnLogDao.insertLog(
            EarnLogEntity(
                userId = userId,
                message = logMessage,
                amount = totalReward
            )
        )
        return true
    }

    // Referral application logic
    suspend fun applyReferral(userId: String, referralCode: String): ReferralResult {
        val bob = userDao.getUserSync(userId) ?: return ReferralResult.Error("Active user profile not found.")
        
        if (bob.referredBy != null) {
            return ReferralResult.Error("You have already applied a referral code!")
        }
        if (bob.referralCode.equals(referralCode, ignoreCase = true)) {
            return ReferralResult.Error("You cannot enter your own referral code!")
        }

        // Find referer (Alice)
        val alice = userDao.getUserByReferralCodeSync(referralCode.uppercase()) 
            ?: return ReferralResult.Error("Referral code '$referralCode' is invalid.")

        // Grant 100 coins to referrer (Alice)
        val aliceNewCoins = alice.coins + 100
        userDao.updateCoins(alice.id, aliceNewCoins)
        earnLogDao.insertLog(
            EarnLogEntity(
                userId = alice.id,
                message = "Referral Bonus: invited ${bob.name} using your code!",
                amount = 100
            )
        )

        // Grant 50 free starter coins to referee (Bob) too, as an incentive!
        val bobNewCoins = bob.coins + 50
        val updatedBob = bob.copy(referredBy = referralCode.uppercase(), coins = bobNewCoins)
        userDao.insertUser(updatedBob)
        
        earnLogDao.insertLog(
            EarnLogEntity(
                userId = bob.id,
                message = "Entered ${alice.name}'s referral code. Earned +50 startup coins!",
                amount = 50
            )
        )

        return ReferralResult.Success(alice.name)
    }

    // Request Withdrawal
    suspend fun requestWithdraw(userId: String, amount: Int, method: String, details: String): WithdrawResult {
        val user = userDao.getUserSync(userId) ?: return WithdrawResult.Error("User profile not found.")
        if (user.coins < amount) {
            return WithdrawResult.Error("Insufficient balance. You have ${user.coins} coins but requested $amount.")
        }

        // Subtract coins (debit) from account securely
        val remainingCoins = user.coins - amount
        userDao.updateCoins(userId, remainingCoins)

        val requestId = "WITHDRAW_" + System.currentTimeMillis().toString().takeLast(6)
        val withdrawal = WithdrawalEntity(
            id = requestId,
            userId = userId,
            amount = amount,
            method = method,
            details = details,
            status = "Pending"
        )
        withdrawalDao.insertWithdrawal(withdrawal)

        earnLogDao.insertLog(
            EarnLogEntity(
                userId = userId,
                message = "Requested payout of $amount coins via $method ($details)",
                amount = -amount
            )
        )
        return WithdrawResult.Success
    }

    // Preload basic earning tasks
    suspend fun preseedTasks() {
        val tasks = listOf(
            TaskEntity("task_daily", "Claim Daily Check-In", "Log in today to claim your daily loyalty bonus.", 10, true, "Bonus"),
            TaskEntity("task_social_telegram", "Join Telegram Channel", "Subscribe to the news channel for community contests and secret codes.", 25, true, "Social"),
            TaskEntity("task_social_twitter", "Follow Twitter Account", "Follow official handle to receive direct code updates.", 20, true, "Social"),
            TaskEntity("task_survey_easy", "Quick Personality Survey", "Answer 5 multiple choice questions to tell us about your rewards interests.", 50, true, "Survey"),
            TaskEntity("task_watch_ad_1", "📺 Watch Video Presentation", "Learn more about modern fintech in Malaysia by watching this local sponsor reel.", 35, true, "Watch"),
            TaskEntity("task_watch_ad_2", "🎬 Watch Crypto Market Overview", "Analyze decentralized currency flows in under 2 minutes.", 45, true, "Watch"),
            TaskEntity("task_install_shopee", "📥 Install Shopee Malaysia App", "Install Shopee application and perform a fast initial account registration.", 150, true, "Install"),
            TaskEntity("task_install_tng", "📥 Download Touch 'n Go E-Wallet", "Get our partner's popular wallet utility for fast payments and toll rebates.", 120, true, "Install"),
            TaskEntity("task_ai_trivia", "Gemini Deep AI Challenge", "Complete a brain teaser generated dynamically by Gemini AI.", 100, true, "Special"),
            // 3 Extra High-Paying VIP Tasks for 1 Year
            TaskEntity("task_vip_1", "🏦 [VIP Level 1+] Premium Malaysia FPX Survey", "Analyze FPX instant transfer speeds across Maybank, CIMB, and RHB.", 250, true, "VIP Exclusive"),
            TaskEntity("task_vip_2", "🪙 [VIP Level 1+] E-Wallet Growth Survey", "Review daily usage caps for Touch 'n Go eWallet, GrabPay, and Boost.", 300, true, "VIP Exclusive"),
            TaskEntity("task_vip_3", "📦 [VIP Level 1+] ShopeePay Integration Poll", "Share feedback about ShopeePay cashback offerings in Malaysia.", 200, true, "VIP Exclusive")
        )
        for (task in tasks) {
            taskDao.insertTask(task)
        }
    }

    // Deposit money / balance
    suspend fun depositMoney(userId: String, amount: Double, method: String): Boolean {
        val user = userDao.getUserSync(userId) ?: return false
        val updatedUser = user.copy(
            usdBalance = user.usdBalance + amount
        )
        userDao.insertUser(updatedUser)
        earnLogDao.insertLog(
            EarnLogEntity(
                userId = userId,
                message = "Deposited RM ${String.format("%.2f", amount)} via $method into virtual wallet balance",
                amount = 0
            )
        )
        return true
    }

    // Buy membership
    suspend fun purchaseMembership(userId: String, level: String, priceCoins: Int, priceUsd: Double): Boolean {
        val user = userDao.getUserSync(userId) ?: return false
        
        val updatedUser = if (priceCoins > 0) {
            if (user.coins < priceCoins) return false
            user.copy(
                coins = user.coins - priceCoins,
                isVip = true,
                vipLevel = level
            )
        } else {
            if (user.usdBalance < priceUsd) return false
            user.copy(
                usdBalance = user.usdBalance - priceUsd,
                isVip = true,
                vipLevel = level
            )
        }
        
        userDao.insertUser(updatedUser)
        
        val spentMessage = if (priceCoins > 0) {
            "Deducted $priceCoins Coins"
        } else {
            "Deducted RM ${String.format("%.2f", priceUsd)}"
        }
        
        earnLogDao.insertLog(
            EarnLogEntity(
                userId = userId,
                message = "Purchased $level! ($spentMessage)",
                amount = if (priceCoins > 0) -priceCoins else 0
            )
        )
        return true
    }

    // Admin action simulation for demonstration/manual testing
    suspend fun simulateAdminActionApproval(requestId: String, approve: Boolean) {
        val targetStatus = if (approve) "Approved" else "Rejected"
        withdrawalDao.updateWithdrawalStatusSync(requestId, targetStatus)
    }

    suspend fun updateUserProfile(userId: String, name: String, email: String, phoneNumber: String, paypalEmail: String) {
        val user = userDao.getUserSync(userId) ?: return
        val updatedUser = user.copy(
            name = name,
            email = email,
            phoneNumber = phoneNumber,
            paypalEmail = paypalEmail
        )
        userDao.insertUser(updatedUser)
        earnLogDao.insertLog(
            EarnLogEntity(
                userId = userId,
                message = "Profile details updated: $name",
                amount = 0
            )
        )
    }

    suspend fun insertEarnLog(userId: String, message: String, amount: Int) {
        earnLogDao.insertLog(
            EarnLogEntity(
                userId = userId,
                message = message,
                amount = amount
            )
        )
    }
}
