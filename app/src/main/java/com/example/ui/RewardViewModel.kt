package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class RewardViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val repository = RewardRepository(
        userDao = database.userDao(),
        taskDao = database.taskDao(),
        taskCompletionDao = database.taskCompletionDao(),
        withdrawalDao = database.withdrawalDao(),
        earnLogDao = database.earnLogDao()
    )

    // User management state
    private val _registeredAccounts = MutableStateFlow<List<UserEntity>>(emptyList())
    val registeredAccounts: StateFlow<List<UserEntity>> = _registeredAccounts.asStateFlow()

    private val _activeUserId = MutableStateFlow<String>("")
    val activeUserId: StateFlow<String> = _activeUserId.asStateFlow()

    val currentUser: StateFlow<UserEntity?> = _activeUserId.flatMapLatest { uid ->
        if (uid.isEmpty()) flowOf(null)
        else repository.getUser(uid)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Dynamic database streams bound to the current selected user
    val activeTasks: StateFlow<List<TaskEntity>> = repository.getActiveTasks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val completedTaskIds: StateFlow<List<String>> = _activeUserId.flatMapLatest { uid ->
        if (uid.isEmpty()) flowOf(emptyList())
        else repository.getCompletedTaskIds(uid)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val withdrawals: StateFlow<List<WithdrawalEntity>> = _activeUserId.flatMapLatest { uid ->
        if (uid.isEmpty()) flowOf(emptyList())
        else repository.getWithdrawals(uid)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val logs: StateFlow<List<EarnLogEntity>> = _activeUserId.flatMapLatest { uid ->
        if (uid.isEmpty()) flowOf(emptyList())
        else repository.getLogs(uid)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Admin view of ALL withdrawals (to simulate approval/rejections)
    val adminWithdrawals: StateFlow<List<WithdrawalEntity>> = repository.getAllWithdrawalsAdmin()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Watch Ad Simulation State
    private val _adTimerSec = MutableStateFlow<Int>(0)
    val adTimerSec: StateFlow<Int> = _adTimerSec.asStateFlow()

    private val _isAdPlaying = MutableStateFlow<Boolean>(false)
    val isAdPlaying: StateFlow<Boolean> = _isAdPlaying.asStateFlow()

    private val _lastAdRewardGranted = MutableStateFlow<Int?>(null)
    val lastAdRewardGranted: StateFlow<Int?> = _lastAdRewardGranted.asStateFlow()

    // Gemini AI Challenge State
    private val _aiChallengeQuiz = MutableStateFlow<GeminiQuiz?>(null)
    val aiChallengeQuiz: StateFlow<GeminiQuiz?> = _aiChallengeQuiz.asStateFlow()

    private val _isGeneratingQuiz = MutableStateFlow<Boolean>(false)
    val isGeneratingQuiz: StateFlow<Boolean> = _isGeneratingQuiz.asStateFlow()

    private val _quizSelectedOption = MutableStateFlow<Int?>(null)
    val quizSelectedOption: StateFlow<Int?> = _quizSelectedOption.asStateFlow()

    private val _quizChecked = MutableStateFlow<Boolean>(false)
    val quizChecked: StateFlow<Boolean> = _quizChecked.asStateFlow()

    private val _quizMessage = MutableStateFlow<String>("")
    val quizMessage: StateFlow<String> = _quizMessage.asStateFlow()

    // Feedback notifications
    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    init {
        // Pre-seed default database tasks and load initial profiles
        viewModelScope.launch {
            repository.preseedTasks()
            refreshProfilesList()
            // Set first profile active if any exists, otherwise create a starter profile
            val profiles = repository.getAllUsersSync()
            if (profiles.isNotEmpty()) {
                _activeUserId.value = profiles.first().id
            } else {
                val demoAdmin = repository.registerUser("Alice Gold", "alice@coinreward.io", "123456", "alice.paypal@coinreward.io")
                // Create a secondary user Bob to facilitate referral testing right out of the box!
                repository.registerUser("Bob Builder", "bob@coinreward.io", "654321", "bob.paypal@coinreward.io")
                _activeUserId.value = demoAdmin.id
                refreshProfilesList()
            }
        }
    }

    private suspend fun refreshProfilesList() {
        _registeredAccounts.value = repository.getAllUsersSync()
    }

    fun clearToast() {
        _toastMessage.value = null
    }

    // Swapping Account Mode (Sandbox Simulation)
    fun switchProfile(userId: String) {
        viewModelScope.launch {
            _activeUserId.value = userId
            _toastMessage.value = "Switched to profile: ${repository.getUserSync(userId)?.name}"
        }
    }

    // Link a PayPal account email
    fun linkPaypalAccount(paypalEmail: String) {
        val uid = _activeUserId.value
        if (uid.isEmpty()) return
        if (paypalEmail.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(paypalEmail.trim()).matches()) {
            _toastMessage.value = "Please enter a valid PayPal email address!"
            return
        }
        viewModelScope.launch {
            repository.updatePaypalEmail(uid, paypalEmail.trim())
            _toastMessage.value = "PayPal account linked successfully!"
            refreshProfilesList()
        }
    }

    // Update entire user profile fields with local validations
    fun updateUserProfile(name: String, email: String, phoneNumber: String, paypalEmail: String) {
        val uid = _activeUserId.value
        if (uid.isEmpty()) return
        if (name.isBlank() || email.isBlank() || phoneNumber.isBlank()) {
            _toastMessage.value = "Name, Email, and Phone Number cannot be empty!"
            return
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()) {
            _toastMessage.value = "Please enter a valid format for Email!"
            return
        }
        if (paypalEmail.isNotBlank() && !android.util.Patterns.EMAIL_ADDRESS.matcher(paypalEmail.trim()).matches()) {
            _toastMessage.value = "Please enter a valid format for PayPal Email!"
            return
        }
        viewModelScope.launch {
            repository.updateUserProfile(uid, name.trim(), email.trim(), phoneNumber.trim(), paypalEmail.trim())
            _toastMessage.value = "User Profile successfully updated!"
            refreshProfilesList()
        }
    }

    // Submit a persistent Support Ticket to the Helpdesk
    fun submitSupportTicket(category: String, description: String) {
        val uid = _activeUserId.value
        if (uid.isEmpty()) return
        if (description.isBlank()) {
            _toastMessage.value = "Please input feedback or inquiry details!"
            return
        }
        viewModelScope.launch {
            repository.insertEarnLog(uid, "[SUPPORT_TICKET] [$category] [Pending] $description", 0)
            _toastMessage.value = "Priority support ticket successfully submitted!"
            refreshProfilesList()
        }
    }

    // Login using registered phone number/register number & email
    fun loginWithDetails(email: String, registerNumber: String) {
        if (email.isBlank() || registerNumber.isBlank()) {
            _toastMessage.value = "Please fill in both Email and Register Number!"
            return
        }
        viewModelScope.launch {
            val users = repository.getAllUsersSync()
            val matchedUser = users.find {
                it.email.equals(email.trim(), ignoreCase = true) &&
                it.phoneNumber.trim() == registerNumber.trim()
            }
            if (matchedUser != null) {
                _activeUserId.value = matchedUser.id
                _toastMessage.value = "Welcome back, ${matchedUser.name}!"
            } else {
                _toastMessage.value = "Account not found! Ensure both Email and Register Number are correct."
            }
        }
    }

    // Logout
    fun logout() {
        _activeUserId.value = ""
        _toastMessage.value = "Signed out successfully!"
    }

    // Create a new customized user profile
    fun createNewProfile(name: String, email: String, phoneNumber: String) {
        if (name.isBlank() || email.isBlank() || phoneNumber.isBlank()) {
            _toastMessage.value = "Name, email, and register number cannot be blank!"
            return
        }
        viewModelScope.launch {
            // Check for duplicate register fields
            val existing = repository.getAllUsersSync()
            val duplicate = existing.any {
                it.email.equals(email.trim(), ignoreCase = true) ||
                it.phoneNumber.trim() == phoneNumber.trim()
            }
            if (duplicate) {
                _toastMessage.value = "Account with this Email or Register Number already exists!"
                return@launch
            }

            val newUser = repository.registerUser(name.trim(), email.trim(), phoneNumber.trim())
            refreshProfilesList()
            _activeUserId.value = newUser.id
            _toastMessage.value = "Profile '${newUser.name}' successfully registered and logged in!"
        }
    }

    // 1. WATCH AD MOB VIDEO SIMULATOR
    fun watchRewardedAd() {
        val uid = _activeUserId.value
        if (uid.isEmpty()) return

        viewModelScope.launch {
            _lastAdRewardGranted.value = null
            _isAdPlaying.value = true
            _adTimerSec.value = 10 // 10 seconds premium watch countdown

            while (_adTimerSec.value > 0) {
                delay(1000)
                _adTimerSec.value = _adTimerSec.value - 1
            }

            // Reward calculation matching ad reward (usually between 15 and 45 coins)
            val earnedCoins = (15..45).random()
            repository.addCoins(uid, earnedCoins, "Watched Video Spot AD")
            _lastAdRewardGranted.value = earnedCoins
            _toastMessage.value = "Congratulations! Earned +$earnedCoins coins."
        }
    }

    fun dismissAdOverlay() {
        _isAdPlaying.value = false
        _lastAdRewardGranted.value = null
    }

    // 2. COMPLETE SOCIAL AND NORMAL TASKS
    fun triggerTaskCompletion(taskId: String) {
        val uid = _activeUserId.value
        if (uid.isEmpty()) return

        val user = currentUser.value
        if (taskId.startsWith("task_vip_") && (user == null || !user.isVip)) {
            _toastMessage.value = "🔒 Level 1-5 VIP Exclusive! Purchase a Membership Level 1 to 5 to access these extra 1-year tasks."
            return
        }

        viewModelScope.launch {
            val success = repository.completeTask(uid, taskId)
            if (success) {
                _toastMessage.value = "Task verified successfully! Points added."
            } else {
                _toastMessage.value = "Task is already completed or invalid."
            }
        }
    }

    // 3. ENTER REFERRAL CODE
    fun submitReferralCode(code: String) {
        val uid = _activeUserId.value
        if (uid.isEmpty() || code.isBlank()) return

        viewModelScope.launch {
            val result = repository.applyReferral(uid, code.trim())
            when (result) {
                is ReferralResult.Success -> {
                    _toastMessage.value = "Success! Applied referral of ${result.referrerName}. You got +50 coins, they got +100 coins!"
                    refreshProfilesList() // Refresh balances across profiles
                }
                is ReferralResult.Error -> {
                    _toastMessage.value = result.message
                }
            }
        }
    }

    // 4. GENERATE GEMINI AI TRIVIA
    fun startGeminiTriviaChallenge() {
        viewModelScope.launch {
            _isGeneratingQuiz.value = true
            _aiChallengeQuiz.value = null
            _quizSelectedOption.value = null
            _quizChecked.value = false
            _quizMessage.value = ""

            val generatedQuiz = GeminiService.generateTriviaChallenge()
            _aiChallengeQuiz.value = generatedQuiz
            _isGeneratingQuiz.value = false
        }
    }

    fun selectQuizOption(index: Int) {
        if (_quizChecked.value) return
        _quizSelectedOption.value = index
    }

    fun verifyQuizAnswer() {
        val quiz = _aiChallengeQuiz.value ?: return
        val selected = _quizSelectedOption.value ?: return
        val uid = _activeUserId.value ?: return

        _quizChecked.value = true
        if (selected == quiz.correctIndex) {
            _quizMessage.value = "Correct! ${quiz.explanation}"
            viewModelScope.launch {
                val success = repository.completeTask(uid, "task_ai_trivia")
                if (success) {
                    _toastMessage.value = "Superb! Brain challenge completed: +100 Coins added!"
                } else {
                    _toastMessage.value = "Correct, but you've already claimed this AI task bounty before today!"
                }
            }
        } else {
            _quizMessage.value = "Oops, that is incorrect. ${quiz.explanation}"
        }
    }

    // 5. SUBMIT PAYOUT WITHDRAWAL
    fun createWithdrawalRequest(amount: Int, method: String, details: String) {
        val uid = _activeUserId.value
        if (uid.isEmpty() || details.isBlank()) {
            _toastMessage.value = "Payment details can't be empty."
            return
        }

        viewModelScope.launch {
            val result = repository.requestWithdraw(uid, amount, method, details.trim())
            when (result) {
                is WithdrawResult.Success -> {
                    _toastMessage.value = "Payout requested successfully! Deducted $amount coins."
                }
                is WithdrawResult.Error -> {
                    _toastMessage.value = result.message
                }
            }
        }
    }

    // 6. SIMULATE ADMIN PENDING TRANSACTIONS (Approval/Rejection Sandbox)
    fun simulateAdminPayoutAction(requestId: String, approve: Boolean) {
        viewModelScope.launch {
            repository.simulateAdminActionApproval(requestId, approve)
            
            // Re-deposit coins if rejected so user retains their funds
            if (!approve) {
                database.withdrawalDao().getWithdrawalsForUser(_activeUserId.value).firstOrNull()?.find { it.id == requestId }?.let {
                    repository.addCoins(it.userId, it.amount, "Refunded withdrawal request rejection (${it.id})")
                }
            }
            
            _toastMessage.value = "Withdrawal request $requestId simulated: ${if (approve) "Approved ✅" else "Rejected ❌"}"
            refreshProfilesList()
        }
    }

    // 7. DEPOSIT BALANCE IN RM
    fun depositVirtualMoney(amount: Double, method: String) {
        val uid = _activeUserId.value
        if (uid.isEmpty()) return
        viewModelScope.launch {
            val success = repository.depositMoney(uid, amount, method)
            if (success) {
                _toastMessage.value = "Deposited RM ${String.format("%.2f", amount)} successfully via $method!"
                refreshProfilesList()
            } else {
                _toastMessage.value = "Deposit failed."
            }
        }
    }

    // 8. UPGRADE OR BUY MEMBERSHIP
    fun purchaseVipMembership(level: String, priceCoins: Int, priceUsd: Double) {
        val uid = _activeUserId.value
        if (uid.isEmpty()) return
        viewModelScope.launch {
            val success = repository.purchaseMembership(uid, level, priceCoins, priceUsd)
            if (success) {
                _toastMessage.value = "Congratulations! You are now a $level! Enjoy your earning boost!"
                refreshProfilesList()
            } else {
                _toastMessage.value = "Purchase failed! Check your balance."
            }
        }
    }
}
