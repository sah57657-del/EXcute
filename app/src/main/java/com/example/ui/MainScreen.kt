package com.example.ui

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class ActiveTab {
    EARN, TASKS, REFERRALS, PAYOUT, SETTINGS
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: RewardViewModel, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val registeredAccounts by viewModel.registeredAccounts.collectAsStateWithLifecycle()
    
    // Toast observer
    val toastMsg by viewModel.toastMessage.collectAsStateWithLifecycle()
    LaunchedEffect(toastMsg) {
        toastMsg?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
            viewModel.clearToast()
        }
    }

    var currentTab by remember { mutableStateOf(ActiveTab.EARN) }
    var showProfileSelector by remember { mutableStateOf(false) }
    var showCreateProfileDialog by remember { mutableStateOf(false) }
    var showDepositDialog by remember { mutableStateOf(false) }
    var showMembershipDialog by remember { mutableStateOf(false) }

    // Simulated Ad States
    val isAdPlaying by viewModel.isAdPlaying.collectAsStateWithLifecycle()
    val adTimerSec by viewModel.adTimerSec.collectAsStateWithLifecycle()
    val adRewardGranted by viewModel.lastAdRewardGranted.collectAsStateWithLifecycle()

    if (currentUser == null) {
        AuthScreen(
            onLogin = { email, regNum ->
                viewModel.loginWithDetails(email, regNum)
            },
            onRegister = { name, email, regNum ->
                viewModel.createNewProfile(name, email, regNum)
            }
        )
    } else {
        Scaffold(
            modifier = modifier.fillMaxSize(),
        topBar = {
            if (currentTab != ActiveTab.EARN) {
                TopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFFFD700))
                                    .wrapContentSize(Alignment.Center)
                            ) {
                                Text("$", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                            Text(
                                text = when (currentTab) {
                                    ActiveTab.TASKS -> "Tasks"
                                    ActiveTab.REFERRALS -> "Invite & Share"
                                    ActiveTab.PAYOUT -> "Payouts"
                                    ActiveTab.SETTINGS -> "Settings & Support"
                                    else -> "CoinReward"
                                },
                                fontWeight = FontWeight.Black,
                                fontSize = 20.sp,
                                color = Color(0xFF001B3D)
                            )
                        }
                    },
                    actions = {
                        currentUser?.let { user ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .testTag("active_profile_chip")
                                    .padding(end = 8.dp)
                                    .background(
                                        Color(0xFFD9E2FF),
                                        shape = RoundedCornerShape(16.dp)
                                    )
                                    .clickable { showProfileSelector = true }
                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AccountCircle,
                                    contentDescription = "Active Profile",
                                    tint = Color(0xFF001B3D),
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = user.name,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF001B3D),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Switch profile",
                                    modifier = Modifier.size(12.dp),
                                    tint = Color(0xFF001B3D)
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color(0xFFF7F9FF)
                    )
                )
            }
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars),
                containerColor = Color.White
            ) {
                NavigationBarItem(
                    selected = currentTab == ActiveTab.EARN,
                    onClick = { currentTab = ActiveTab.EARN },
                    label = { Text("Home", fontWeight = FontWeight.Bold, fontSize = 10.sp) },
                    icon = { Text("🏠", fontSize = 20.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF001B3D),
                        selectedTextColor = Color(0xFF001B3D),
                        unselectedTextColor = Color(0xFF44474E),
                        indicatorColor = Color(0xFFD9E2FF)
                    ),
                    modifier = Modifier.testTag("tab_earn")
                )
                NavigationBarItem(
                    selected = currentTab == ActiveTab.TASKS,
                    onClick = { currentTab = ActiveTab.TASKS },
                    label = { Text("Tasks", fontWeight = FontWeight.Bold, fontSize = 10.sp) },
                    icon = { Text("🎁", fontSize = 20.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF001B3D),
                        selectedTextColor = Color(0xFF001B3D),
                        unselectedTextColor = Color(0xFF44474E),
                        indicatorColor = Color(0xFFD9E2FF)
                    ),
                    modifier = Modifier.testTag("tab_tasks")
                )
                NavigationBarItem(
                    selected = currentTab == ActiveTab.REFERRALS,
                    onClick = { currentTab = ActiveTab.REFERRALS },
                    label = { Text("Invite", fontWeight = FontWeight.Bold, fontSize = 10.sp) },
                    icon = { Text("🤝", fontSize = 20.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF001B3D),
                        selectedTextColor = Color(0xFF001B3D),
                        unselectedTextColor = Color(0xFF44474E),
                        indicatorColor = Color(0xFFD9E2FF)
                    ),
                    modifier = Modifier.testTag("tab_referrals")
                )
                NavigationBarItem(
                    selected = currentTab == ActiveTab.PAYOUT,
                    onClick = { currentTab = ActiveTab.PAYOUT },
                    label = { Text("Payout", fontWeight = FontWeight.Bold, fontSize = 10.sp) },
                    icon = { Text("💸", fontSize = 20.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF001B3D),
                        selectedTextColor = Color(0xFF001B3D),
                        unselectedTextColor = Color(0xFF44474E),
                        indicatorColor = Color(0xFFD9E2FF)
                    ),
                    modifier = Modifier.testTag("tab_payout")
                )
                NavigationBarItem(
                    selected = currentTab == ActiveTab.SETTINGS,
                    onClick = { currentTab = ActiveTab.SETTINGS },
                    label = { Text("Settings", fontWeight = FontWeight.Bold, fontSize = 10.sp) },
                    icon = { Text("⚙️", fontSize = 20.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF001B3D),
                        selectedTextColor = Color(0xFF001B3D),
                        unselectedTextColor = Color(0xFF44474E),
                        indicatorColor = Color(0xFFD9E2FF)
                    ),
                    modifier = Modifier.testTag("tab_settings")
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF7F9FF))
        ) {
            Crossfade(targetState = currentTab, label = "TabTransition") { tab ->
                when (tab) {
                    ActiveTab.EARN -> EarnWorkspace(
                        viewModel = viewModel,
                        onTabSelected = { currentTab = it },
                        onAvatarClick = { showProfileSelector = true },
                        onDepositClick = { showDepositDialog = true },
                        onMembershipClick = { showMembershipDialog = true },
                        onLogoutClick = { viewModel.logout() }
                    )
                    ActiveTab.TASKS -> TasksWorkspace(viewModel)
                    ActiveTab.REFERRALS -> ReferralsWorkspace(viewModel)
                    ActiveTab.PAYOUT -> WithdrawWorkspace(viewModel)
                    ActiveTab.SETTINGS -> SettingsWorkspace(viewModel)
                }
            }

            if (showDepositDialog) {
                DepositDialog(
                    onDeposit = { amount, method ->
                        viewModel.depositVirtualMoney(amount, method)
                        showDepositDialog = false
                    },
                    onDismiss = { showDepositDialog = false }
                )
            }

            if (showMembershipDialog) {
                val coins = currentUser?.coins ?: 0
                val cash = currentUser?.usdBalance ?: 0.0
                MembershipDialog(
                    userCoins = coins,
                    userUsd = cash,
                    onPurchase = { level, priceCoins, priceUsd ->
                        viewModel.purchaseVipMembership(level, priceCoins, priceUsd)
                        showMembershipDialog = false
                    },
                    onDismiss = { showMembershipDialog = false }
                )
            }

            // Sandbox Profile Switcher Overlay bottom sheet / dialog
            if (showProfileSelector) {
                ProfileSelectorSheet(
                    users = registeredAccounts,
                    activeId = currentUser?.id ?: "",
                    onSwitch = {
                        viewModel.switchProfile(it)
                        showProfileSelector = false
                    },
                    onCreateNewClick = {
                        showProfileSelector = false
                        showCreateProfileDialog = true
                    },
                    onDismiss = { showProfileSelector = false }
                )
            }

            if (showCreateProfileDialog) {
                CreateProfileDialog(
                    onCreate = { name, email, phoneNumber ->
                        viewModel.createNewProfile(name, email, phoneNumber)
                        showCreateProfileDialog = false
                    },
                    onDismiss = { showCreateProfileDialog = false }
                )
            }

            // High Fidelity Simulated AD Fullscreen overlay dialog
            if (isAdPlaying) {
                FullscreenAdPlayer(
                    timerSeconds = adTimerSec,
                    rewardGranted = adRewardGranted,
                    onDismiss = { viewModel.dismissAdOverlay() }
                )
            }
        }
    }
}
}

// ==========================================
// WORKSPACE 1: EARN & REWARD OVERVIEW (BENTO GRID STYLE)
// ==========================================
@Composable
fun EarnWorkspace(
    viewModel: RewardViewModel,
    onTabSelected: (ActiveTab) -> Unit,
    onAvatarClick: () -> Unit,
    onDepositClick: () -> Unit,
    onMembershipClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val logs by viewModel.logs.collectAsStateWithLifecycle()
    val activeTasks by viewModel.activeTasks.collectAsStateWithLifecycle()
    val completedTaskIds by viewModel.completedTaskIds.collectAsStateWithLifecycle()
    val withdrawals by viewModel.withdrawals.collectAsStateWithLifecycle()

    val pendingTasksValue = remember(activeTasks, completedTaskIds) {
        val nonCompleted = activeTasks.count { !completedTaskIds.contains(it.id) }
        if (activeTasks.isEmpty()) {
            "No active tasks"
        } else {
            "$nonCompleted Pending"
        }
    }

    val lastWithdrawalTx = remember(withdrawals) {
        val last = withdrawals.firstOrNull()
        if (last != null) {
            "Last: $${String.format("%.2f", last.amount / 1000.0)} ${last.status.replaceFirstChar { it.uppercase() }}"
        } else {
            "Tap to withdraw funds"
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp)
    ) {
        // Bento Custom Header
        item {
            BentoHeader(user = currentUser, onAvatarClick = onAvatarClick, onLogoutClick = onLogoutClick)
        }

        // 1. Bento Grid Component: Balance & Deposit Card (Full Width)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("wallet_balance_card"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFD9E2FF)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .drawBehind {
                            drawCircle(
                                color = Color(0xFFADC6FF).copy(alpha = 0.35f),
                                radius = 260f,
                                center = androidx.compose.ui.geometry.Offset(size.width + 30f, -30f)
                            )
                            drawCircle(
                                color = Color(0xFFADC6FF).copy(alpha = 0.35f),
                                radius = 200f,
                                center = androidx.compose.ui.geometry.Offset(-30f, size.height + 30f)
                            )
                        }
                        .padding(20.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "WALLET COIN & CASH ACCOUNT",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF001B3D).copy(alpha = 0.6f),
                                letterSpacing = 1.sp
                            )
                            Box(
                                modifier = Modifier
                                    .background(Color(0xFF003087).copy(alpha = 0.1f), RoundedCornerShape(100.dp))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "Verified Profile",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF003087)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Left: Coins Balance
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Exchangeable Coins",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF001B3D).copy(alpha = 0.7f)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("🪙", fontSize = 18.sp)
                                    val balanceCoins = currentUser?.coins ?: 0
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = java.text.NumberFormat.getIntegerInstance().format(balanceCoins),
                                        fontSize = 22.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color(0xFF001B3D)
                                    )
                                }
                            }

                            // Vertical Divider
                            Box(
                                modifier = Modifier
                                    .width(1.dp)
                                    .height(36.dp)
                                    .background(Color(0xFF001B3D).copy(alpha = 0.15f))
                            )

                            // Right: USD Cash Balance
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(start = 16.dp)
                            ) {
                                Text(
                                    text = "Cash Wallet",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF001B3D).copy(alpha = 0.7f)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("💵", fontSize = 18.sp)
                                    val balanceUsd = currentUser?.usdBalance ?: 0.0
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "RM ${String.format("%.2f", balanceUsd)}",
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color(0xFF1B6A3E)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Deposit Funds button
                        Button(
                            onClick = onDepositClick,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("deposit_funds_button"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF001B3D),
                                contentColor = Color.White
                            )
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text("💳", fontSize = 14.sp)
                                Text("Deposit Virtual Money (RM)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // 2. Bento Grid Component: Membership Status Card (Full Width)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("membership_status_card"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF9C4)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                border = BorderStroke(1.dp, Color(0xFFFFF176))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("👑", fontSize = 18.sp)
                                Text(
                                    text = "MEMBERSHIP STATUS",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF5D4037),
                                    letterSpacing = 1.sp
                                )
                            }

                            // Render user VIP tier badge on the right
                            PremiumVIPBadge(
                                vipLevel = currentUser?.vipLevel ?: "Free",
                                isVip = currentUser?.isVip == true
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        val boostText = when (currentUser?.vipLevel?.lowercase()) {
                            "level 1" -> "1.2x Multiplier Boost active on all task payouts."
                            "level 2" -> "1.5x Multiplier Boost active on all task payouts."
                            "level 3" -> "2.0x Multiplier Boost active on all task payouts."
                            "level 4" -> "2.5x Multiplier Boost active on all task payouts."
                            "level 5" -> "3.0x Multiplier Boost active on all task payouts."
                            else -> "1.0x standard payout speed. Purchase Level 1 to 5 to boost."
                        }

                        Text(
                            text = "Benefits: $boostText Plus, you secure 3 extra high-paying tasks available for an entire 1-year duration.",
                            fontSize = 12.sp,
                            lineHeight = 16.sp,
                            color = Color(0xFF5D4037).copy(alpha = 0.85f)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = onMembershipClick,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("buy_vip_membership_button"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFFF9800),
                                contentColor = Color.White
                            )
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text("⚡", fontSize = 14.sp)
                                Text(
                                    text = if (currentUser?.isVip == true) "Upgrade Membership Level" else "Unlock Premium Level 1 to 5 Now",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // 3 & 4. Bento Grid Component: Watch Ads & Active Tasks/Refer Row (Side-By-Side Grid)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Max),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Left Bento Box: Watch Ads Card
                Card(
                    modifier = Modifier
                        .weight(1.1f)
                        .fillMaxHeight()
                        .clickable { viewModel.watchRewardedAd() }
                        .testTag("ads_earning_card"),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE2E2E6)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(Color.White, RoundedCornerShape(10.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("📺", fontSize = 18.sp)
                                }

                                Box(
                                    modifier = Modifier
                                        .background(Color(0xFFFFF3CD), RoundedCornerShape(6.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "+50 COINS",
                                        color = Color(0xFF856404),
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = "Watch Ads",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color(0xFF1A1C1E)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Play unlimited ad video reels",
                                fontSize = 10.sp,
                                color = Color(0xFF5D5E62),
                                lineHeight = 13.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Ad simulation logic
                        val isAdPlaying by viewModel.isAdPlaying.collectAsStateWithLifecycle()
                        val adTimerSec by viewModel.adTimerSec.collectAsStateWithLifecycle()

                        if (isAdPlaying) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFFADC6FF).copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(12.dp),
                                    strokeWidth = 2.dp,
                                    color = Color(0xFF003087)
                                )
                                Text(
                                    text = "Ad playing... ${adTimerSec}s",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF003087)
                                )
                            }
                        } else {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text("🟢", fontSize = 8.sp)
                                Text(
                                    text = "Ready to play",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF2E7D32)
                                )
                            }
                        }
                    }
                }

                // Right Bento Box: Secondary Column of Active Tasks and Referral Code
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Right mini-box: Active Tasks
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .clickable { onTabSelected(ActiveTab.TASKS) }
                            .testTag("tasks_earning_card"),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF1E0FF)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .background(Color.White, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("🎁", fontSize = 14.sp)
                                }
                                Text(
                                    text = "TASKS",
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF4A148C)
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "Active Tasks",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = Color(0xFF1A1C1E)
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = pendingTasksValue,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF4A148C)
                                )
                                Box(
                                    modifier = Modifier
                                        .background(Color.White, CircleShape)
                                        .size(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("➔", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4A148C))
                                }
                            }
                        }
                    }

                    // Right mini-box: Refer Code
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .clickable { onTabSelected(ActiveTab.REFERRALS) },
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFD3E4FF)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .background(Color.White, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("🤝", fontSize = 14.sp)
                                }
                                Text(
                                    text = "REFER",
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF003087)
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "Share Code",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = Color(0xFF1A1C1E)
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = currentUser?.referralCode ?: "ALX92",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    color = Color(0xFF003087)
                                )
                                Box(
                                    modifier = Modifier
                                        .background(Color.White, CircleShape)
                                        .size(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("➔", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color(0xFF003087))
                                }
                            }
                        }
                    }
                }
            }
        }

        // 5. Bento Grid Component: Withdrawal History Card (Full Width)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onTabSelected(ActiveTab.PAYOUT) }
                    .testTag("withdrawal_history_bento_card"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8DEF8)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(Color.White, RoundedCornerShape(10.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("💸", fontSize = 18.sp)
                            }
                            Column {
                                Text(
                                    text = "Withdrawal & History",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = Color(0xFF1D1B20)
                                )
                                Text(
                                    text = lastWithdrawalTx,
                                    fontSize = 10.sp,
                                    color = Color(0xFF49454F)
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .background(Color.White, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("➔", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1D1B20))
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    val pendingSum = withdrawals.filter { it.status.lowercase() == "pending" }.sumOf { it.amount }
                    val approvedSum = withdrawals.filter { it.status.lowercase() == "approved" }.sumOf { it.amount }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Pending Stats
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .background(Color.White.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                                .padding(10.dp)
                        ) {
                            Text("In Process ⏳", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF625B71))
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "RM ${String.format("%.2f", pendingSum / 1000.0)}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF311B92)
                            )
                        }

                        // Approved Stats
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .background(Color.White.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                                .padding(10.dp)
                        ) {
                            Text("Approved ✅", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1B5E20))
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "RM ${String.format("%.2f", approvedSum / 1000.0)}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF1B5E20)
                            )
                        }
                    }
                }
            }
        }

        // Sandbox Profile switcher reminder card - styled elegantly in Bento theme
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFE2E2E6).copy(alpha = 0.4f), shape = RoundedCornerShape(20.dp))
                    .padding(16.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text("💡", fontSize = 16.sp)
                    Text(
                        text = "SANDBOX SIMULATOR: Tap your avatar circular chip in the top right to swap profiles and test live network code accumulation!",
                        fontSize = 11.sp,
                        lineHeight = 15.sp,
                        color = Color(0xFF44474E)
                    )
                }
            }
        }

        // Recent Audit Trace header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp, bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Anti-Cheat Audit Ledger",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF001B3D)
                )
            }
        }

        // Logs trace list rendering
        if (logs.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFE2E2E6))
                ) {
                    Text(
                        text = "No logged transitions yet. Earn coins to populate audit traces.",
                        fontSize = 12.sp,
                        color = Color(0xFF44474E).copy(alpha = 0.7f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp)
                    )
                }
            }
        } else {
            items(logs) { log ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFE2E2E6))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.Absolute.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = log.message,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF1A1C1E)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = java.text.SimpleDateFormat("MMM dd, HH:mm:ss", java.util.Locale.getDefault()).format(log.timestamp),
                                fontSize = 10.sp,
                                color = Color(0xFF44474E)
                            )
                        }
                        Text(
                            text = if (log.amount >= 0) "+${log.amount}" else "${log.amount}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black,
                            color = if (log.amount >= 0) Color(0xFF2E7D32) else Color(0xFFC62828)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PremiumVIPBadge(vipLevel: String, isVip: Boolean) {
    val levelLower = vipLevel.lowercase()
    val badgeColors = remember(vipLevel, isVip) {
        when {
            !isVip || levelLower.contains("free") || levelLower.contains("standard") || levelLower.isBlank() -> {
                Triple(
                    listOf(Color(0xFFE2E8F0), Color(0xFFCBD5E1)),
                    Color(0xFF475569),
                    "Standard Tier"
                )
            }
            levelLower == "level 1" -> {
                Triple(
                    listOf(Color(0xFFFFE0B2), Color(0xFFFFB74D)),
                    Color(0xFF5D4037),
                    "Level 1 VIP"
                )
            }
            levelLower == "level 2" -> {
                Triple(
                    listOf(Color(0xFFECEFF1), Color(0xFFB0BEC5)),
                    Color(0xFF37474F),
                    "Level 2 VIP"
                )
            }
            levelLower == "level 3" -> {
                Triple(
                    listOf(Color(0xFFFFF9C4), Color(0xFFFBC02D)),
                    Color(0xFFF57F17),
                    "Level 3 VIP"
                )
            }
            levelLower == "level 4" -> {
                Triple(
                    listOf(Color(0xFFE0F7FA), Color(0xFF4DD0E1)),
                    Color(0xFF006064),
                    "Level 4 VIP"
                )
            }
            levelLower == "level 5" -> {
                Triple(
                    listOf(Color(0xFFE1BEE7), Color(0xFFBA68C8)),
                    Color(0xFF4A148C),
                    "Level 5 VIP (Legendary)"
                )
            }
            levelLower.contains("gold") -> {
                Triple(
                    listOf(Color(0xFFFFECB3), Color(0xFFFFC107)),
                    Color(0xFF7F6000),
                    "Gold VIP"
                )
            }
            levelLower.contains("silver") -> {
                Triple(
                    listOf(Color(0xFFECEFF1), Color(0xFFB0BEC5)),
                    Color(0xFF37474F),
                    "Silver VIP"
                )
            }
            levelLower.contains("bronze") -> {
                Triple(
                    listOf(Color(0xFFFFE0B2), Color(0xFFFFB74D)),
                    Color(0xFF5D4037),
                    "Bronze VIP"
                )
            }
            else -> {
                Triple(
                    listOf(Color(0xFFE1BEE7), Color(0xFFBA68C8)),
                    Color(0xFF4A148C),
                    vipLevel
                )
            }
        }
    }

    val (bgGradient, contentColor, displayName) = badgeColors
    val icon = when {
        displayName.contains("Level 5") || displayName.contains("Gold") -> "👑"
        displayName.contains("Level 4") || displayName.contains("Silver") -> "✨"
        displayName.contains("Level 3") || displayName.contains("Bronze") -> "⭐"
        displayName.contains("Level 2") -> "⚡"
        displayName.contains("Level 1") -> "🛡️"
        else -> "👤"
    }

    Surface(
        shape = RoundedCornerShape(100.dp),
        shadowElevation = 1.dp,
        color = Color.Transparent,
        modifier = Modifier
            .background(
                brush = Brush.horizontalGradient(bgGradient),
                shape = RoundedCornerShape(100.dp)
            )
            .testTag("membership_badge_${displayName.lowercase().replace(" ", "_")}")
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = icon,
                fontSize = 11.sp
            )
            Text(
                text = displayName.uppercase(),
                fontSize = 9.sp,
                fontWeight = FontWeight.Black,
                color = contentColor,
                letterSpacing = 1.sp
            )
        }
    }
}

const val TOP_EARNER_THRESHOLD = 10000

@Composable
fun TopEarnerBadge(coins: Int, modifier: Modifier = Modifier) {
    if (coins >= TOP_EARNER_THRESHOLD) {
        Surface(
            shape = RoundedCornerShape(100.dp),
            shadowElevation = 1.dp,
            color = Color.Transparent,
            modifier = modifier
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(Color(0xFFFFA000), Color(0xFFFFD54F))
                    ),
                    shape = RoundedCornerShape(100.dp)
                )
                .border(1.dp, Color(0xFFFFD54F), RoundedCornerShape(100.dp))
                .testTag("top_earner_badge")
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "🏆",
                    fontSize = 11.sp
                )
                Text(
                    text = "TOP EARNER",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF5D4037),
                    letterSpacing = 1.sp
                )
            }
        }
    }
}

@Composable
fun BentoHeader(user: UserEntity?, onAvatarClick: () -> Unit, onLogoutClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Welcome back,",
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF44474E),
                letterSpacing = 1.sp
            )
            Text(
                text = user?.name ?: "Valued Member",
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFF001B3D),
                letterSpacing = (-0.5).sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PremiumVIPBadge(
                    vipLevel = user?.vipLevel ?: "Free",
                    isVip = user?.isVip == true
                )
                if (user != null) {
                    TopEarnerBadge(coins = user.coins)
                }
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Avatar Initial Circle
            val initials = remember(user?.name) {
                val parts = user?.name?.split(Regex("\\s+"))?.filter { it.isNotBlank() } ?: emptyList()
                if (parts.isEmpty()) "UR"
                else if (parts.size == 1) parts.first().take(2).uppercase()
                else "${parts.first().first()}${parts[1].first()}".uppercase()
            }

            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFD9E2FF))
                    .border(2.dp, Color.White, CircleShape)
                    .clickable { onAvatarClick() }
                    .wrapContentSize(Alignment.Center)
            ) {
                Text(
                    text = initials,
                    color = Color(0xFF001B3D),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Clean Sign-Out Button
            IconButton(
                onClick = onLogoutClick,
                modifier = Modifier
                    .size(40.dp)
                    .background(Color(0xFFFDE8E8), shape = CircleShape)
                    .testTag("logout_button")
            ) {
                Icon(
                    imageVector = Icons.Default.ExitToApp,
                    contentDescription = "Log Out",
                    tint = Color(0xFFC71515),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

// Circular gold glowing spinner rendering representing live coins
@Composable
fun GlowCoinSpinner() {
    var rotation by remember { mutableStateOf(0f) }
    LaunchedEffect(Unit) {
        while (true) {
            rotation = (rotation + 3f) % 360f
            delay(30)
        }
    }
    Box(
        modifier = Modifier
            .size(48.dp)
            .drawBehind {
                drawCircle(
                    brush = Brush.sweepGradient(listOf(Color(0xFFFFD700), Color(0xFFFFA500), Color(0xFFFFD700))),
                    style = Stroke(width = 8f)
                )
            }
            .wrapContentSize(Alignment.Center)
    ) {
        Icon(
            Icons.Default.Star,
            contentDescription = "Glowing Coin",
            tint = Color(0xFFFFD700),
            modifier = Modifier.size(24.dp)
        )
    }
}

// ==========================================
// WORKSPACE 2: TASKS & AI CHALLENGES
// ==========================================
@Composable
fun TasksWorkspace(viewModel: RewardViewModel) {
    val activeTasks by viewModel.activeTasks.collectAsStateWithLifecycle()
    val completedTaskIds by viewModel.completedTaskIds.collectAsStateWithLifecycle()

    val (standardTasks, specialTasks) = activeTasks.partition { it.id != "task_ai_trivia" }

    var selectedCategory by remember { mutableStateOf("All") }
    val categories = listOf("All", "Survey", "Watch", "Install", "Social", "Special", "VIP Exclusive", "Bonus")

    val filteredStandardTasks = remember(standardTasks, selectedCategory) {
        if (selectedCategory == "All") {
            standardTasks
        } else {
            standardTasks.filter { it.category.equals(selectedCategory, ignoreCase = true) }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        // AI Challenge banner header
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("ai_challenge_header_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "✨ Gemini Interactive AI Bounty",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                    Text(
                        "Answer a custom-generated intelligence challenge by the Gemini 3.5 AI model. Solve correctly to win +100 Coins immediately!",
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.85f)
                    )
                }
            }
        }

        // Gemini Interactive challenge element
        item {
            GeminiInteractiveBountyWidget(viewModel)
        }

        // Standard Social and Activity Tasks Header & Category filters Row
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    "Activity and Sponsored Offers",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(top = 8.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .testTag("task_category_filter_row"),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    categories.forEach { category ->
                        val isSelected = selectedCategory == category
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedCategory = category },
                            label = { Text(category, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFFD9E2FF),
                                selectedLabelColor = Color(0xFF001B3D),
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            modifier = Modifier.testTag("filter_chip_$category")
                        )
                    }
                }
            }
        }

        if (filteredStandardTasks.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                        .testTag("empty_tasks_card"),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("🔍", fontSize = 28.sp)
                        Text(
                            text = "No tasks in '$selectedCategory'",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Try switching to another category or check back later.",
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        } else {
            // Render each filtered standard offer
            items(filteredStandardTasks) { task ->
                val isCompleted = completedTaskIds.contains(task.id)
                val isVipTask = task.id.startsWith("task_vip_")
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = if (isVipTask && !isCompleted) Color(0xFFFFFDF5) else MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp),
                            shape = RoundedCornerShape(14.dp)
                        )
                        .border(
                            width = 1.dp,
                            color = if (isCompleted) Color(0xFF4CAF50).copy(alpha = 0.3f) else if (isVipTask) Color(0xFFFFD700).copy(alpha = 0.5f) else Color.Transparent,
                            shape = RoundedCornerShape(14.dp)
                        )
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                if (isCompleted) Color(0xFFE8F5E9) else if (isVipTask) Color(0xFFFFECB3) else MaterialTheme.colorScheme.primaryContainer,
                                shape = CircleShape
                            )
                            .wrapContentSize(Alignment.Center)
                    ) {
                        Icon(
                            imageVector = if (isCompleted) Icons.Default.Check else Icons.Default.Star,
                            contentDescription = null,
                            tint = if (isCompleted) Color(0xFF4CAF50) else if (isVipTask) Color(0xFFE65100) else MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                task.title,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isCompleted) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f, fill = false)
                            )
                            if (isVipTask) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .background(Color(0xFFFFA500).copy(alpha = 0.2f), shape = RoundedCornerShape(4.dp))
                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                ) {
                                    Text("VIP", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color(0xFFE65100))
                                }
                            }
                        }
                        Text(
                            task.description,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                "Reward: ${task.reward} Coins",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFFA500)
                            )
                            // Clean category badge
                            Box(
                                modifier = Modifier
                                    .background(MaterialTheme.colorScheme.secondaryContainer, shape = RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = task.category.uppercase(),
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }
                    }

                    Button(
                        onClick = { viewModel.triggerTaskCompletion(task.id) },
                        enabled = !isCompleted,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isVipTask) Color(0xFFFFA500) else MaterialTheme.colorScheme.primary,
                            disabledContainerColor = Color(0xFFC8E6C9)
                        ),
                        modifier = Modifier.minimumInteractiveComponentSize(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = if (isCompleted) "Claimed" else "Claim",
                            color = if (isCompleted) Color(0xFF2E7D32) else Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun GeminiInteractiveBountyWidget(viewModel: RewardViewModel) {
    val quiz by viewModel.aiChallengeQuiz.collectAsStateWithLifecycle()
    val isGenerating by viewModel.isGeneratingQuiz.collectAsStateWithLifecycle()
    val selectedOption by viewModel.quizSelectedOption.collectAsStateWithLifecycle()
    val checked by viewModel.quizChecked.collectAsStateWithLifecycle()
    val message by viewModel.quizMessage.collectAsStateWithLifecycle()
    val completedTaskIds by viewModel.completedTaskIds.collectAsStateWithLifecycle()
    val alreadyClaimed = completedTaskIds.contains("task_ai_trivia")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("gemini_trivia_card"),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (quiz == null) {
                // Initial generation state
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        modifier = Modifier.size(40.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        "Quiz Engine is Ready",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Click below to generate a real-time question from Gemini.",
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (alreadyClaimed) {
                        Text(
                            "💡 You completed today's AI challenge! You can keep playing for fun.",
                            color = Color(0xFF4CAF50),
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                    Button(
                        onClick = { viewModel.startGeminiTriviaChallenge() },
                        enabled = !isGenerating,
                        modifier = Modifier.testTag("generate_quiz_button")
                    ) {
                        if (isGenerating) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Invoking Gemini...")
                        } else {
                            Text("Ask Gemini a Riddle")
                        }
                    }
                }
            } else {
                // Interactive quiz active
                quiz?.let { activeQuiz ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Absolute.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "AI Riddle Tracker",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        IconButton(onClick = { viewModel.startGeminiTriviaChallenge() }, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.Refresh, contentDescription = "Regenerate challenge", modifier = Modifier.size(18.dp))
                        }
                    }

                    Text(
                        text = activeQuiz.question,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    // Options list
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        activeQuiz.options.forEachIndexed { index, option ->
                            val isSelected = selectedOption == index
                            val optionBorderColor = when {
                                checked && index == activeQuiz.correctIndex -> Color(0xFF4CAF50)
                                checked && isSelected && index != activeQuiz.correctIndex -> Color(0xFFF44336)
                                isSelected -> MaterialTheme.colorScheme.primary
                                else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                            }
                            val optionBg = when {
                                checked && index == activeQuiz.correctIndex -> Color(0xFFE8F5E9)
                                checked && isSelected && index != activeQuiz.correctIndex -> Color(0xFFFFEBEE)
                                isSelected -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                                else -> Color.Transparent
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(optionBg, RoundedCornerShape(8.dp))
                                    .border(1.dp, optionBorderColor, RoundedCornerShape(8.dp))
                                    .clickable { viewModel.selectQuizOption(index) }
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = { viewModel.selectQuizOption(index) },
                                    enabled = !checked
                                )
                                Text(
                                    text = option,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    // Feedbacks and explanation
                    if (checked) {
                        Text(
                            text = message,
                            fontSize = 13.sp,
                            color = if (selectedOption == activeQuiz.correctIndex) Color(0xFF2E7D32) else Color(0xFFC62828),
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    if (selectedOption == activeQuiz.correctIndex) Color(0xFFE8F5E9) else Color(0xFFFFEBEE),
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(10.dp)
                        )

                        Button(
                            onClick = { viewModel.startGeminiTriviaChallenge() },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Next Riddle")
                        }
                    } else {
                        Button(
                            onClick = { viewModel.verifyQuizAnswer() },
                            enabled = selectedOption != null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("verify_quiz_button"),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Submit Answer")
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// WORKSPACE 3: REFERRALS / SHARE CODES
// ==========================================
@Composable
fun ReferralsWorkspace(viewModel: RewardViewModel) {
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    var referralInput by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        // Invite Banner
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "Refer & Earn Double Coins",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Text(
                        "Give 50 start coins to your friend. Get 100 free bonus coins once they use your code!",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.85f)
                    )
                }
            }
        }

        // Your code block
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("referrals_sharing_card"),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("YOUR PERSONAL REFERRAL CODE", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.secondary)
                    
                    Box(
                        modifier = Modifier
                            .background(
                                MaterialTheme.colorScheme.surfaceColorAtElevation(4.dp),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 24.dp, vertical = 12.dp)
                    ) {
                        Text(
                            text = currentUser?.referralCode ?: "-------",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 2.sp
                        )
                    }

                    Text("Share this code. Others get +50 coins, and you get +100 coins!", fontSize = 11.sp, textAlign = TextAlign.Center)
                }
            }
        }

        // Enter Referral Code Block
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        "Claim Invitation Reward",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Have a referral code from a friend? Enter it below to claim +50 start coins.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = referralInput,
                        onValueChange = { referralInput = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("referral_input_field"),
                        placeholder = { Text("e.g. ALIC843") },
                        label = { Text("Referral Coupon Code") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                        trailingIcon = {
                            if (currentUser?.referredBy != null) {
                                Icon(Icons.Default.Check, contentDescription = "Applied", tint = Color(0xFF4CAF50))
                            }
                        },
                        enabled = currentUser?.referredBy == null
                    )

                    if (currentUser?.referredBy != null) {
                        Text(
                            text = "✅ Code was already applied! Referrer registered: ${currentUser?.referredBy}",
                            color = Color(0xFF2E7D32),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    } else {
                        Button(
                            onClick = {
                                viewModel.submitReferralCode(referralInput)
                                referralInput = ""
                            },
                            enabled = referralInput.isNotBlank(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("apply_referral_button"),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Apply Coupon Code")
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// WORKSPACE 4: WITHDRAWALS & WALLET PAYOUTS
// ==========================================
data class PayoutOption(
    val id: String,
    val name: String,
    val description: String,
    val minCoins: Int,
    val methodType: String,
    val detailPlaceholder: String,
    val iconId: String
)

@Composable
fun WithdrawWorkspace(viewModel: RewardViewModel) {
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val withdrawals by viewModel.withdrawals.collectAsStateWithLifecycle()
    val adminWithdrawals by viewModel.adminWithdrawals.collectAsStateWithLifecycle()

    val payoutMethods = remember {
        listOf(
            PayoutOption("po_bank", "Direct Bank Account Deposit", "$10.00 ACH Wire Transfer", 10000, "Direct Bank Deposit", "Enter Bank Name, Routing # & Account #", "bank"),
            PayoutOption("po_wallet", "Digital Wallet Transfer", "$5.00 Instant Cashout (Venmo/CashApp)", 5000, "Digital Wallet", "Enter Cashtag, Venmo handle, or phone number", "wallet"),
            PayoutOption("po_paypal", "PayPal Balance Transfer", "$5.00 Direct Instant Transfer", 5000, "PayPal", "Enter PayPal email account...", "paypal"),
            PayoutOption("po_amazon", "Amazon Digital Gift Card", "$10.00 Digital Voucher", 10000, "Amazon Voucher", "Enter Delivery Email Address...", "amazon"),
            PayoutOption("po_crypto", "Ethereum Crypto Token", "ETH transfer equivalent", 2500, "Ethereum ETH", "Enter ETH ERC-20 Address...", "crypto"),
            PayoutOption("po_gplay", "Google Play Voucher", "$5.00 Redeem Code", 5000, "Google Play Store", "Enter Email for Gift Card Code...", "google")
        )
    }

    var selectedOption by remember { mutableStateOf(payoutMethods.first()) }
    var detailInput by remember { mutableStateOf("") }
    var amountInputCoins by remember { mutableStateOf("") }
    
    // Payout Workspace modes: "request" or "history"
    var subTab by remember { mutableStateOf("request") }
    // Withdrawal history filters: "All", "Pending", "Approved", "Rejected"
    var historyFilter by remember { mutableStateOf("All") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp)
    ) {
        // 1. Bento Dashboard of Payout Stats
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Wide Top Bento Card: Balance
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFD9E2FF)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                "EXCHANGEABLE COIN RESERVES",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF001B3D),
                                letterSpacing = 1.2.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "${currentUser?.coins ?: 0} Coins",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF001B3D)
                            )
                        }
                        Box(
                            modifier = Modifier
                                .background(Color.White.copy(alpha = 0.6f), shape = RoundedCornerShape(12.dp))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "RM ${String.format("%.2f", (currentUser?.coins ?: 0) / 1000.0)}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF001B3D)
                            )
                        }
                    }
                }

                // Two side-by-side cells for Process & Approved sum
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val pendingSum = withdrawals.filter { it.status.lowercase() == "pending" }.sumOf { it.amount }
                    val approvedSum = withdrawals.filter { it.status.lowercase() == "approved" }.sumOf { it.amount }

                    // Left Side: In Process
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF9C4)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("⏳ ", fontSize = 13.sp)
                                Text(
                                    "In Process",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFC47E00)
                                )
                            }
                            Text(
                                text = java.text.NumberFormat.getIntegerInstance().format(pendingSum),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFFC47E00)
                            )
                            Text(
                                text = "RM ${String.format("%.2f", pendingSum / 1000.0)}",
                                fontSize = 9.sp,
                                color = Color(0xFFC47E00).copy(alpha = 0.8f),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Right Side: Paid Out
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("✅ ", fontSize = 13.sp)
                                Text(
                                    "Paid Out",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF2E7D32)
                                )
                            }
                            Text(
                                text = java.text.NumberFormat.getIntegerInstance().format(approvedSum),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF2E7D32)
                            )
                            Text(
                                text = "RM ${String.format("%.2f", approvedSum / 1000.0)}",
                                fontSize = 9.sp,
                                color = Color(0xFF2E7D32).copy(alpha = 0.8f),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // 2. Bento Segmented Switch Control Row
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFE2E2E6).copy(alpha = 0.6f), shape = RoundedCornerShape(16.dp))
                    .padding(4.dp)
            ) {
                listOf(
                    "request" to "New Request 💸",
                    "history" to "Payout History 📊 (${withdrawals.size})"
                ).forEach { (key, titleText) ->
                    val isChosen = subTab == key
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(
                                if (isChosen) Color.White else Color.Transparent,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable { subTab = key }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = titleText,
                            fontSize = 12.sp,
                            fontWeight = if (isChosen) FontWeight.ExtraBold else FontWeight.Medium,
                            color = if (isChosen) Color(0xFF001B3D) else Color(0xFF44474E)
                        )
                    }
                }
            }
        }

        // CONDITIONAL SECTIONS BASED ON SUB TAB
        if (subTab == "request") {
            // Elegant PayPal account linking display
            item {
                var showPaypalLinkDialog by remember { mutableStateOf(false) }
                var paypalEmailInput by remember { mutableStateOf(currentUser?.paypalEmail ?: "") }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("paypal_account_link_banner"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F0FE)),
                    border = BorderStroke(1.dp, Color(0xFFBCD0F7))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Icon Box with Brand Color (PayPal blue)
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(Color(0xFF003087), shape = RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("P", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Black)
                        }

                        // Text content block
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Linked PayPal Account",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF001B3D)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (currentUser?.paypalEmail.isNullOrBlank()) "No PayPal account linked yet" else currentUser!!.paypalEmail,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (currentUser?.paypalEmail.isNullOrBlank()) Color.DarkGray else Color(0xFF0079C1)
                            )
                        }

                        // Action Button
                        Button(
                            onClick = {
                                paypalEmailInput = currentUser?.paypalEmail ?: ""
                                showPaypalLinkDialog = true
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (currentUser?.paypalEmail.isNullOrBlank()) Color(0xFF0079C1) else Color(0xFF003087)
                            ),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = if (currentUser?.paypalEmail.isNullOrBlank()) "Link Now" else "Manage",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                if (showPaypalLinkDialog) {
                    Dialog(onDismissRequest = { showPaypalLinkDialog = false }) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Link PayPal Account",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 18.sp,
                                        color = Color(0xFF001B3D)
                                    )
                                    IconButton(onClick = { showPaypalLinkDialog = false }) {
                                        Icon(Icons.Default.Close, contentDescription = "Close Dialog")
                                    }
                                }

                                Text(
                                    text = "Store your PayPal email address securely with your CoinReward profile. Verified PayPal addresses enable instant balance cashouts to your pocket!",
                                    fontSize = 12.sp,
                                    lineHeight = 16.sp,
                                    color = Color.Gray
                                )

                                OutlinedTextField(
                                    value = paypalEmailInput,
                                    onValueChange = { paypalEmailInput = it },
                                    label = { Text("PayPal Email Address") },
                                    placeholder = { Text("your.paypal@email.com") },
                                    singleLine = true,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("paypal_email_input_field"),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                    leadingIcon = { Text("✉️", modifier = Modifier.padding(start = 12.dp, end = 6.dp)) }
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    TextButton(onClick = { showPaypalLinkDialog = false }) {
                                        Text("Cancel")
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Button(
                                        onClick = {
                                            viewModel.linkPaypalAccount(paypalEmailInput)
                                            showPaypalLinkDialog = false
                                        },
                                        enabled = paypalEmailInput.isNotBlank(),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF003087))
                                    ) {
                                        Text("Save PayPal Address", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // New Withdrawal request Form Box
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("withdrawals_request_form"),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFE2E2E6))
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            "Initiate New Payout",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF001B3D)
                        )

                        // Scrollable Chip Row for Methods selection
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            payoutMethods.forEach { option ->
                                val isChosen = selectedOption.id == option.id
                                FilterChip(
                                    selected = isChosen,
                                    onClick = {
                                        selectedOption = option
                                        detailInput = if (option.id == "po_paypal") {
                                            currentUser?.paypalEmail ?: ""
                                        } else {
                                            ""
                                        }
                                    },
                                    label = { Text(option.methodType, fontWeight = FontWeight.Bold) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Color(0xFFD9E2FF),
                                        selectedLabelColor = Color(0xFF001B3D)
                                    )
                                )
                            }
                        }

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF7F9FF)),
                            border = BorderStroke(1.dp, Color(0xFFE2E2E6))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = selectedOption.name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = Color(0xFF001B3D)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${selectedOption.description} (Min Requirement: ${selectedOption.minCoins} Coins)",
                                    fontSize = 11.sp,
                                    color = Color(0xFF44474E)
                                )
                            }
                        }

                        OutlinedTextField(
                            value = amountInputCoins,
                            onValueChange = { amountInputCoins = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("withdraw_amount_field"),
                            label = { Text("Deductible Coins Amount") },
                            placeholder = { Text("e.g. ${selectedOption.minCoins}") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )

                        OutlinedTextField(
                            value = detailInput,
                            onValueChange = { detailInput = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("withdraw_details_field"),
                            label = { Text("Recipient Credentials") },
                            placeholder = { Text(selectedOption.detailPlaceholder) },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                        )

                        Button(
                            onClick = {
                                val coins = amountInputCoins.toIntOrNull() ?: 0
                                if (coins < selectedOption.minCoins) {
                                    Toast.makeText(viewModel.getApplication(), "Requested amount cannot be less than standard payout margin of ${selectedOption.minCoins}", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                if ((currentUser?.coins ?: 0) < coins) {
                                    Toast.makeText(viewModel.getApplication(), "Insufficient coins in your account!", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                viewModel.createWithdrawalRequest(coins, selectedOption.methodType, detailInput)
                                amountInputCoins = ""
                                detailInput = ""
                            },
                            enabled = amountInputCoins.isNotBlank() && detailInput.isNotBlank(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF001B3D),
                                contentColor = Color.White
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("submit_withdraw_button"),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text("Exchange Coins for Payout", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }

            // Sandbox Profile notice
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFE2E2E6).copy(alpha = 0.4f), shape = RoundedCornerShape(20.dp))
                        .padding(16.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text("💡", fontSize = 16.sp)
                        Text(
                            text = "Sandbox simulation: Submit a request here, and then switch to the 'Payout History' tab where you can instantly Approve or Reject (Refund) the request as a virtual network administrator!",
                            fontSize = 11.sp,
                            lineHeight = 15.sp,
                            color = Color(0xFF44474E)
                        )
                    }
                }
            }
        } else {
            // HISTORY TAB SELECTED
            // Filtering Chips header row
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Filter Logs:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF44474E),
                        modifier = Modifier.padding(end = 4.dp)
                    )
                    listOf("All", "Pending", "Approved", "Rejected").forEach { filterLabel ->
                        val isChosen = historyFilter == filterLabel
                        FilterChip(
                            selected = isChosen,
                            onClick = { historyFilter = filterLabel },
                            label = { Text(filterLabel, fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFFD9E2FF),
                                selectedLabelColor = Color(0xFF001B3D)
                            ),
                            shape = RoundedCornerShape(100.dp)
                        )
                    }
                }
            }

            // List history filtered withdrawals
            val filteredList = withdrawals.filter {
                historyFilter == "All" || it.status.equals(historyFilter, ignoreCase = true)
            }

            if (filteredList.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Color(0xFFE2E2E6))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("📁", fontSize = 36.sp)
                            Text(
                                text = "No records found.",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF001B3D),
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "There are no withdrawal requests matching the status '$historyFilter' for this user. Tap 'New Request' to file one!",
                                fontSize = 11.sp,
                                color = Color(0xFF44474E),
                                textAlign = TextAlign.Center,
                                lineHeight = 15.sp
                            )
                        }
                    }
                }
            } else {
                items(filteredList) { request ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("history_item_${request.id}"),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Color(0xFFE2E2E6))
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Line 1: Header + Badge status
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val emojiSymbol = when (request.method.lowercase()) {
                                    "paypal" -> "💳"
                                    "amazon voucher", "amazon digital gift card" -> "📦"
                                    "ethereum eth", "crypto token" -> "🪙"
                                    "direct bank deposit", "bank transfer", "bank deposit" -> "🏦"
                                    "digital wallet" -> "📱"
                                    else -> "🎮"
                                }
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(emojiSymbol, fontSize = 20.sp)
                                    Column {
                                        Text(
                                            text = request.method,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Black,
                                            color = Color(0xFF001B3D)
                                        )
                                        Text(
                                            text = "ID: ${request.id.uppercase()}",
                                            fontSize = 9.sp,
                                            fontFamily = FontFamily.Monospace,
                                            color = Color(0xFF44474E),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }

                                val (badgeBg, badgeText, statusEmoji) = when (request.status.lowercase()) {
                                    "approved" -> Triple(Color(0xFFE8F5E9), Color(0xFF2E7D32), "✅")
                                    "rejected" -> Triple(Color(0xFFFFEBEE), Color(0xFFC62828), "❌")
                                    else -> Triple(Color(0xFFFFF9C4), Color(0xFFC47E00), "⏳")
                                }

                                Box(
                                    modifier = Modifier
                                        .background(badgeBg, shape = RoundedCornerShape(100.dp))
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(statusEmoji, fontSize = 10.sp)
                                        Text(
                                            text = request.status.uppercase(),
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = badgeText,
                                            letterSpacing = 0.5.sp
                                        )
                                    }
                                }
                            }

                            HorizontalDivider(thickness = 0.5.dp, color = Color(0xFFE2E2E6))

                            // Line 2: Requested sum info
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "REDEEMED COINS",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF44474E),
                                        letterSpacing = 0.5.sp
                                    )
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(
                                            text = java.text.NumberFormat.getIntegerInstance().format(request.amount),
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.Black,
                                            color = Color(0xFF001B3D)
                                        )
                                        Text("Coins", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF001B3D))
                                    }
                                }

                                Box(
                                    modifier = Modifier
                                        .background(Color(0xFFD9E2FF).copy(alpha = 0.5f), shape = RoundedCornerShape(8.dp))
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = "RM ${String.format("%.2f", request.amount / 1000.0)} Value",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFF001B3D)
                                    )
                                }
                            }

                            // Line 3: Delivery target
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFFF7F9FF), shape = RoundedCornerShape(12.dp))
                                    .padding(10.dp)
                            ) {
                                Text(
                                    text = "PAYMENT DESTINATION",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF44474E),
                                    letterSpacing = 0.5.sp
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = request.details,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF1A1C1E),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Filed on: ${java.text.SimpleDateFormat("MMM dd, yyyy • HH:mm", java.util.Locale.getDefault()).format(request.createdAt)}",
                                    fontSize = 10.sp,
                                    color = Color(0xFF44474E)
                                )
                            }

                            // Line 4: In-context Sandbox state manager
                            if (request.status.lowercase() == "pending") {
                                HorizontalDivider(thickness = 0.5.dp, color = Color(0xFFE2E2E6))
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFFE8DEF8).copy(alpha = 0.35f), shape = RoundedCornerShape(12.dp))
                                        .padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = "🔬 SANDBOX CONSOLE (SIMULATE ADMIN)",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color(0xFF001B3D),
                                        letterSpacing = 1.sp
                                    )
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Button(
                                            onClick = { viewModel.simulateAdminPayoutAction(request.id, approve = true) },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(30.dp)
                                        ) {
                                            Text("Approve Payout ✅", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                        }

                                        Button(
                                            onClick = { viewModel.simulateAdminPayoutAction(request.id, approve = false) },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828)),
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(30.dp)
                                        ) {
                                            Text("Reject (Refund) ❌", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// COMPOSABLE COMPONENT OVERLAYS
// ==========================================

@Composable
fun ProfileSelectorSheet(
    users: List<UserEntity>,
    activeId: String,
    onSwitch: (String) -> Unit,
    onCreateNewClick: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Absolute.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Selected Profiles", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close overlay")
                    }
                }

                Text(
                    text = "Swap profile to perform referrals, checks: copy Referral Code from one, switch to another, apply code on Invite screen, switch back to check coin accumulation!",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 240.dp)
                ) {
                    items(users) { u ->
                        val isActive = u.id == activeId
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    if (isActive) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                                    RoundedCornerShape(8.dp)
                                )
                                .border(
                                    1.dp,
                                    if (isActive) MaterialTheme.colorScheme.primary else Color.Transparent,
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable { onSwitch(u.id) }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Absolute.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(u.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    TopEarnerBadge(coins = u.coins)
                                }
                                Text("${u.email} | Balance: ${u.coins} Coins", fontSize = 11.sp)
                                Text("Ref Code: ${u.referralCode}", fontSize = 10.sp, color = MaterialTheme.colorScheme.primary)
                            }
                            if (isActive) {
                                Icon(Icons.Default.Check, contentDescription = "Active", tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }

                HorizontalDivider()

                Button(
                    onClick = onCreateNewClick,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Add Custom Sandbox Member")
                }
            }
        }
    }
}

@Composable
fun CreateProfileDialog(
    onCreate: (String, String, String) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Register Sandbox Member", fontSize = 18.sp, fontWeight = FontWeight.Bold)

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Display Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email (Sandbox)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = phoneNumber,
                    onValueChange = { phoneNumber = it },
                    label = { Text("Register Number / Phone") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onCreate(name, email, phoneNumber) },
                        enabled = name.isNotBlank() && email.isNotBlank() && phoneNumber.isNotBlank()
                    ) {
                        Text("Create")
                    }
                }
            }
        }
    }
}

// Simulated High Fidelity circular loader Ad Dialog playing
@Composable
fun FullscreenAdPlayer(
    timerSeconds: Int,
    rewardGranted: Int?,
    onDismiss: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    Dialog(
        onDismissRequest = { /* Force user attention, block cancel */ },
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF121212)) // Pure Cinema Black
                .clickable(interactionSource = interactionSource, indication = null) { /* Block clicks */ }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                if (rewardGranted == null) {
                    // Ad is active/playing
                    Text(
                        "SPONSORED PROMOTION ACTIVE",
                        color = Color.Yellow,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )
                    Spacer(modifier = Modifier.height(18.dp))

                    // Ad loading circular countdown indicator
                    Box(
                        modifier = Modifier
                            .size(140.dp)
                            .drawBehind {
                                drawCircle(
                                    color = Color.White.copy(alpha = 0.2f),
                                    style = Stroke(width = 12f)
                                )
                                drawArc(
                                    color = Color(0xFFFF9800),
                                    startAngle = -90f,
                                    sweepAngle = (timerSeconds / 10f) * 360f,
                                    useCenter = false,
                                    style = Stroke(width = 12f)
                                )
                            }
                            .wrapContentSize(Alignment.Center)
                    ) {
                        Text(
                            text = "$timerSeconds",
                            fontSize = 44.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "Viewing advertisement stream safely...",
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                    Text(
                        text = "Do not close the page to guarantee correct anti-cheat logging.",
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.5f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                } else {
                    // Ad completed, reward granted!
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Success",
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.size(72.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        "Advertisement Completed!",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFD700), modifier = Modifier.size(28.dp))
                        Text(
                            text = "+$rewardGranted Coins Added",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFF9800)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                    ) {
                        Text("Collect Reward")
                    }
                }
            }
        }
    }
}

// Extension to styling colors smoothly
fun ColorScheme.infoContainerBrush(): Brush {
    return Brush.horizontalGradient(
        listOf(
            this.primaryContainer.copy(alpha = 0.15f),
            this.secondaryContainer.copy(alpha = 0.15f)
        )
    )
}

@Composable
fun DepositDialog(
    onDeposit: (Double, String) -> Unit,
    onDismiss: () -> Unit
) {
    var depositAmount by remember { mutableStateOf("100.00") }
    var selectedMethod by remember { mutableStateOf("FPX Bank Transfer (Maybank/CIMB/RHB)") }
    
    val methods = listOf(
        "FPX Bank Transfer (Maybank/CIMB/RHB)" to "🏦",
        "Touch 'n Go eWallet" to "🪙",
        "GrabPay Malaysia" to "🟢",
        "Boost Wallet" to "🔴",
        "ShopeePay" to "🟠"
    )

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Deposit Virtual Money (RM)",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF001B3D)
                )
                
                Text(
                    text = "Simulate loading external Ringgit into your wallet to buy Levels 1-5 memberships, unlock tasks, or acquire premium privileges.",
                    fontSize = 12.sp,
                    color = Color.Gray
                )

                // Quick presets
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("10.00", "50.00", "100.00", "200.00", "500.00").forEach { amount ->
                        val isSelected = depositAmount == amount
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(
                                    color = if (isSelected) Color(0xFFD9E2FF) else Color(0xFFF1F3F9),
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) Color(0xFF001B3D) else Color.Transparent,
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .clickable { depositAmount = amount }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "RM $amount",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF001B3D)
                            )
                        }
                    }
                }

                // Custom amount field
                OutlinedTextField(
                    value = depositAmount,
                    onValueChange = { depositAmount = it },
                    label = { Text("Custom Amount (RM)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "Select Payment Account",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF001B3D)
                )

                // Payment methods list
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    methods.forEach { (methodName, emoji) ->
                        val isSelected = selectedMethod == methodName
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    color = if (isSelected) Color(0xFFE8DEF8) else Color(0xFFF7F9FF),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) Color(0xFF6750A4) else Color.Transparent,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable { selectedMethod = methodName }
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(emoji, fontSize = 18.sp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = methodName,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF1E1E24),
                                modifier = Modifier.weight(1f)
                            )
                            if (isSelected) {
                                Text("✅", fontSize = 14.sp)
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = {
                            val amt = depositAmount.toDoubleOrNull() ?: 100.0
                            onDeposit(amt, selectedMethod)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B6A3E)),
                        modifier = Modifier.weight(1.5f)
                    ) {
                        Text("Confirm Deposit")
                    }
                }
            }
        }
    }
}

@Composable
fun MembershipDialog(
    userCoins: Int,
    userUsd: Double,
    onPurchase: (String, Int, Double) -> Unit, // level, priceCoins, priceUsd
    onDismiss: () -> Unit
) {
    var selectedTier by remember { mutableStateOf("Level 1") }

    val tiers = listOf(
        Triple("Level 1", "🛡️ 1.2x Boost + 3 Extra Tasks for 1 Year", Pair(15000, 15.00)),
        Triple("Level 2", "⚡ 1.5x Boost + 3 Extra Tasks for 1 Year", Pair(30000, 30.00)),
        Triple("Level 3", "⭐ 2.0x Boost + 3 Extra Tasks for 1 Year", Pair(45000, 45.00)),
        Triple("Level 4", "✨ 2.5x Boost + 3 Extra Tasks for 1 Year", Pair(60000, 60.00)),
        Triple("Level 5", "👑 3.0x Boost + 3 Extra Tasks for 1 Year", Pair(75000, 75.00))
    )

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .padding(18.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Upgrade Premium Membership",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF001B3D)
                )

                Text(
                    text = "Select an annual level to apply a permanent earnings boost and unlock 3 extra daily high-paying VIP tasks!",
                    fontSize = 11.sp,
                    color = Color.Gray
                )

                // Render physical card options
                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    tiers.forEach { (tierName, perk, pricing) ->
                        val (coinPrice, usdPrice) = pricing
                        val isSelected = selectedTier == tierName
                        
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) Color(0xFFFFF9E6) else Color(0xFFF7F9FF)
                            ),
                            border = BorderStroke(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) Color(0xFFFFA500) else Color(0xFFE2E2E6)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedTier = tierName }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = tierName,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color(0xFF856403) else Color(0xFF001B3D)
                                    )
                                    Text(
                                        text = perk,
                                        fontSize = 10.sp,
                                        color = Color(0xFF333333),
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                                
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "$coinPrice Coins",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFFFA500)
                                    )
                                    Text(
                                        text = "OR RM ${String.format("%.2f", usdPrice)} ($${String.format("%.2f", usdPrice)})",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1B6A3E)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Action Options: Buy with Coins or Buy with RM/Dollar
                val currentTierPricing = tiers.find { it.first == selectedTier }?.third ?: Pair(15000, 15.0)
                val coinPrice = currentTierPricing.first
                val usdPrice = currentTierPricing.second

                val canAffordCoins = userCoins >= coinPrice
                val canAffordUsd = userUsd >= usdPrice

                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = { onPurchase(selectedTier, coinPrice, 0.0) },
                        enabled = canAffordCoins,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFA500))
                    ) {
                        Text("Pay via $coinPrice Coins (Owned: $userCoins)", fontSize = 11.sp)
                    }

                    Button(
                        onClick = { onPurchase(selectedTier, 0, usdPrice) },
                        enabled = canAffordUsd,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B6A3E))
                    ) {
                        Text("Pay via $${String.format("%.2f", usdPrice)} / RM ${String.format("%.2f", usdPrice)} (Owned: $${String.format("%.2f", userUsd)})", fontSize = 11.sp)
                    }
                    
                    if (!canAffordCoins && !canAffordUsd) {
                        Text(
                            text = "⚠️ Insufficient balance in both Coins and Wallet. Please deposit money or earn more coins first!",
                            color = Color.Red,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text("Close Panel")
                }
            }
        }
    }
}

@Composable
fun AuthScreen(
    onLogin: (String, String) -> Unit,
    onRegister: (String, String, String) -> Unit
) {
    var isLoginMode by remember { mutableStateOf(true) }

    // Form inputs state
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF001B3D), Color(0xFF0D2C54))
                )
            )
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 480.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Content
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(Color(0xFFFFD700), shape = CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🪙", fontSize = 24.sp)
                }
                Text(
                    text = "CoinReward",
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = (-0.5).sp
                )
            }

            Text(
                text = if (isLoginMode) "Sign in to start earning coins" else "Create your free earner profile",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )

            // Auth Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Auth Mode Toggle
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF1F3F9), shape = RoundedCornerShape(12.dp))
                            .padding(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(
                                    color = if (isLoginMode) Color.White else Color.Transparent,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable { isLoginMode = true }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Login",
                                fontWeight = FontWeight.Bold,
                                color = if (isLoginMode) Color(0xFF001B3D) else Color.Gray,
                                fontSize = 13.sp
                            )
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(
                                    color = if (!isLoginMode) Color.White else Color.Transparent,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable { isLoginMode = false }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Register",
                                fontWeight = FontWeight.Bold,
                                color = if (!isLoginMode) Color(0xFF001B3D) else Color.Gray,
                                fontSize = 13.sp
                            )
                        }
                    }

                    if (!isLoginMode) {
                        // Registration mode requires Full Name
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Display Name") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("auth_name"),
                            singleLine = true,
                            leadingIcon = { Text("👤", modifier = Modifier.padding(start = 12.dp, end = 6.dp)) }
                        )
                    }

                    // Email Field
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email Address") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("auth_email"),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        leadingIcon = { Text("✉️", modifier = Modifier.padding(start = 12.dp, end = 6.dp)) }
                    )

                    // Phone / Register Number Field
                    OutlinedTextField(
                        value = phoneNumber,
                        onValueChange = { phoneNumber = it },
                        label = { Text("Register Number / Phone") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("auth_register_number"),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        leadingIcon = { Text("📱", modifier = Modifier.padding(start = 12.dp, end = 6.dp)) }
                    )

                    // Action Button
                    Button(
                        onClick = {
                            if (isLoginMode) {
                                onLogin(email, phoneNumber)
                            } else {
                                onRegister(name, email, phoneNumber)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag(if (isLoginMode) "auth_login_submit" else "auth_register_submit"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF001B3D),
                            contentColor = Color.White
                        ),
                        enabled = if (isLoginMode) {
                            email.isNotBlank() && phoneNumber.isNotBlank()
                        } else {
                            name.isNotBlank() && email.isNotBlank() && phoneNumber.isNotBlank()
                        }
                    ) {
                        Text(
                            text = if (isLoginMode) "Sign In Securely" else "Register & Access Wallet",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Interactive demo credential helpers
            if (isLoginMode) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.15f)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "💡 QUICK DEMO AUTO-FILL CREDENTIALS",
                            color = Color(0xFFFFD700),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.sp
                        )

                        Text(
                            text = "Tap a profile block below to instantly autofill and test the login mechanics:",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 10.sp
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Alice Gold Autofill Card
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(10.dp))
                                    .clickable {
                                        email = "alice@coinreward.io"
                                        phoneNumber = "123456"
                                    }
                                    .padding(8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("👑 Alice Gold", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    Text("Reg: 123456", color = Color.White.copy(alpha = 0.6f), fontSize = 9.sp)
                                }
                            }

                            // Bob Builder Autofill Card
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(10.dp))
                                    .clickable {
                                        email = "bob@coinreward.io"
                                        phoneNumber = "654321"
                                    }
                                    .padding(8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("👷 Bob Builder", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    Text("Reg: 654321", color = Color.White.copy(alpha = 0.6f), fontSize = 9.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// WORKSPACE 5: SETTINGS, ABOUT & HELPLINE
// ==========================================

data class SupportTicket(
    val id: Long,
    val category: String,
    val status: String,
    val description: String,
    val timestamp: Long
)

data class ChatMsg(
    val sender: String,
    val text: String,
    val isUser: Boolean,
    val time: String = "Just Now"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsWorkspace(viewModel: RewardViewModel) {
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val logs by viewModel.logs.collectAsStateWithLifecycle()

    // Filter Support ticket list from persistent database logs starting with "[SUPPORT_TICKET]"
    val supportTickets = remember(logs) {
        logs.filter { it.message.startsWith("[SUPPORT_TICKET]") }.map { log ->
            try {
                val clean = log.message.removePrefix("[SUPPORT_TICKET] ")
                val catIndex = clean.indexOf("]")
                val category = if (catIndex != -1) clean.substring(1, catIndex) else "General"
                
                val statusIndex = clean.indexOf("]", catIndex + 1)
                val status = if (statusIndex != -1) clean.substring(catIndex + 3, statusIndex) else "Pending"
                
                val desc = if (statusIndex != -1) clean.substring(statusIndex + 1).trim() else clean
                SupportTicket(log.id, category, status, desc, log.timestamp)
            } catch (e: Exception) {
                SupportTicket(log.id, "General", "Pending", log.message, log.timestamp)
            }
        }
    }

    // Editable profile states
    var editName by remember(currentUser) { mutableStateOf(currentUser?.name ?: "") }
    var editEmail by remember(currentUser) { mutableStateOf(currentUser?.email ?: "") }
    var editPhone by remember(currentUser) { mutableStateOf(currentUser?.phoneNumber ?: "") }
    var editPaypal by remember(currentUser) { mutableStateOf(currentUser?.paypalEmail ?: "") }
    var isEditing by remember { mutableStateOf(false) }

    // Simulator Settings Toggles
    var muteSfx by remember { mutableStateOf(false) }
    var biometricLogin by remember { mutableStateOf(true) }
    var dailyReminder by remember { mutableStateOf(true) }
    var malaysianCurrencyMode by remember { mutableStateOf(false) }

    // Helpline Ticket Forms state
    var selectedCategory by remember { mutableStateOf("Survey Reward Issue") }
    var ticketDescription by remember { mutableStateOf("") }
    val categories = listOf("Survey Reward Issue", "VIP Mode Help", "Payout / Bank Transfer Delay", "Video Ad Crediting", "General Feedback")

    // Helpline Chat dialog triggering state
    var showChatDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // TOP EARNER CONGRATULATIONS SPECIAL FLOATING HEADER
        if (currentUser != null && currentUser!!.coins >= TOP_EARNER_THRESHOLD) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("top_earner_congrats_box"),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFFFF9C4)
                    ),
                    border = BorderStroke(1.5.dp, Color(0xFFFFA000)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("🏆", fontSize = 28.sp)
                        Column {
                            Text(
                                text = "Congratulations, Top Earner!",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF5D4037)
                            )
                            Text(
                                text = "Your total balance of ${currentUser!!.coins} Coins exceeds $TOP_EARNER_THRESHOLD. You have unlocked exclusive priority response on our Malaysia Executive Helpdesk helpline!",
                                fontSize = 11.sp,
                                color = Color(0xFF5D4037).copy(alpha = 0.85f),
                                lineHeight = 15.sp
                            )
                        }
                    }
                }
            }
        }

        // SECTION 1: USER PROFILE & SETTINGS
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("profile_section_card"),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("👤", fontSize = 20.sp)
                            Text("User Profile & Settings", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF001B3D))
                        }
                        if (!isEditing) {
                            IconButton(onClick = { isEditing = true }) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit Profile Info", tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }

                    if (isEditing) {
                        OutlinedTextField(
                            value = editName,
                            onValueChange = { editName = it },
                            label = { Text("Profile Name") },
                            modifier = Modifier.fillMaxWidth().testTag("edit_profile_name"),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = editEmail,
                            onValueChange = { editEmail = it },
                            label = { Text("Email Address") },
                            modifier = Modifier.fillMaxWidth().testTag("edit_profile_email"),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = editPhone,
                            onValueChange = { editPhone = it },
                            label = { Text("Registered Mobile Number") },
                            modifier = Modifier.fillMaxWidth().testTag("edit_profile_phone"),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = editPaypal,
                            onValueChange = { editPaypal = it },
                            label = { Text("Linked PayPal Email") },
                            modifier = Modifier.fillMaxWidth().testTag("edit_profile_paypal"),
                            singleLine = true
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = {
                                    viewModel.updateUserProfile(editName, editEmail, editPhone, editPaypal)
                                    isEditing = false
                                },
                                modifier = Modifier.weight(1f).testTag("save_profile_button"),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Save Profile", fontWeight = FontWeight.Bold)
                            }
                            OutlinedButton(
                                onClick = {
                                    isEditing = false
                                    currentUser?.let {
                                        editName = it.name
                                        editEmail = it.email
                                        editPhone = it.phoneNumber
                                        editPaypal = it.paypalEmail
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Cancel")
                            }
                        }
                    } else {
                        // Display Read-only profile details
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                .padding(12.dp)
                                .fillMaxWidth()
                        ) {
                            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                Text("Full Name", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                                Text(currentUser?.name ?: "N/A", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                Text("Email Address", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                                Text(currentUser?.email ?: "N/A", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                Text("Registered Mobile", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                                Text(currentUser?.phoneNumber ?: "N/A", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                Text("PayPal Target", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                                Text(currentUser?.paypalEmail?.ifBlank { "Not linked yet" } ?: "Not linked yet", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))

                    // Preferences toggle
                    Text("Simulation Preferences", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("🔊", fontSize = 14.sp)
                            Text("Play Sound Effects on Earning Coins", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        }
                        Switch(checked = !muteSfx, onCheckedChange = { muteSfx = !it })
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("🇲🇾", fontSize = 14.sp)
                            Text("Convert Visual Balance into RM Cash", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        }
                        Switch(checked = malaysianCurrencyMode, onCheckedChange = { malaysianCurrencyMode = it })
                    }

                    if (malaysianCurrencyMode && currentUser != null) {
                        val estimatedRM = currentUser!!.coins / 100.0
                        Text(
                            text = "Estimated cash value: RM ${String.format("%.2f", estimatedRM)}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2E7D32),
                            modifier = Modifier.padding(start = 24.dp)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("🛡️", fontSize = 14.sp)
                            Text("Enable Biometric Fingerprint Login", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        }
                        Switch(checked = biometricLogin, onCheckedChange = { biometricLogin = it })
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("📅", fontSize = 14.sp)
                            Text("Receive Daily Offer Multiplier Reminders", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        }
                        Switch(checked = dailyReminder, onCheckedChange = { dailyReminder = it })
                    }
                }
            }
        }

        // SECTION 2: ABOUT US
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("about_us_section_card"),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("✨", fontSize = 20.sp)
                        Text("About Us & Secure Earning", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF001B3D))
                    }
                    Text(
                        text = "CoinReward is Malaysia's premier offline-first mobile micropayouts app. We enable users to complete micro activities like sponsored download tasks, quick personality surveys, and promotional videos to accumulate virtual coin currency, which is fully redeemable for visual digital vouchers or Bank cash transfer.",
                        fontSize = 12.sp,
                        lineHeight = 18.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFE8F5E9), RoundedCornerShape(8.dp))
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("🛡️", fontSize = 16.sp, color = Color(0xFF2E7D32))
                        Text("Room DB Local Transaction Isolation", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                    }

                    Text(
                        text = "To guarantee 100% data secure processing, we maintain an independent SQLite Room ledger database on-device, logging every coin accrual chronologically. This ensures tamper-proof client-side auditing and fast verification.",
                        fontSize = 11.sp,
                        lineHeight = 16.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f)
                    )
                }
            }
        }

        // SECTION 3: HELPLINE & LIVE HELP TICKET SYSTEM
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("helpline_section_card"),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("💼", fontSize = 20.sp)
                            Text("Executive Support Helpline", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF001B3D))
                        }
                        Button(
                            onClick = { showChatDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF001B3D)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("open_live_chat_button")
                        ) {
                            Text("💬 Live Agent", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }

                    Text(
                        text = "Submit a live priority helpline query, or chat directly with our Malaysian digital ledger officer to resolve payout delays or bonus activation.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Ticket Category Filter Chips
                    Text("Support Ticket Category", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        categories.forEach { cat ->
                            val isSelected = selectedCategory == cat
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedCategory = cat },
                                label = { Text(cat, fontSize = 10.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFFD9E2FF),
                                    selectedLabelColor = Color(0xFF001B3D)
                                )
                            )
                        }
                    }

                    // Ticket details description input
                    OutlinedTextField(
                        value = ticketDescription,
                        onValueChange = { ticketDescription = it },
                        label = { Text("Describe your support request...") },
                        placeholder = { Text("e.g. Completed personality survey but did not receive 50 coins bundle.") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .testTag("submit_ticket_description_input")
                    )

                    Button(
                        onClick = {
                            viewModel.submitSupportTicket(selectedCategory, ticketDescription)
                            ticketDescription = ""
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("submit_priority_ticket_button"),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text("⚡", fontSize = 14.sp)
                            Text("EXcute Support Help Request", fontWeight = FontWeight.Bold)
                        }
                    }

                    if (supportTickets.isNotEmpty()) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        Text("Active Helpline Trackers", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            supportTickets.forEach { ticket ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFFF1F3F9), RoundedCornerShape(10.dp))
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(ticket.category, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF001B3D))
                                        Text(ticket.description, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text(
                                            text = java.text.SimpleDateFormat("dd MMM yyyy HH:mm", java.util.Locale.getDefault()).format(java.util.Date(ticket.timestamp)),
                                            fontSize = 9.sp,
                                            color = Color.Gray
                                        )
                                    }
                                    Box(
                                        modifier = Modifier
                                            .background(
                                                color = when (ticket.status) {
                                                    "Pending" -> Color(0xFFFFF3CD)
                                                    "Resolved" -> Color(0xFFD4EDDA)
                                                    else -> Color(0xFFE2E3E5)
                                                },
                                                shape = RoundedCornerShape(4.dp)
                                            )
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = ticket.status.uppercase(),
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = when (ticket.status) {
                                                "Pending" -> Color(0xFF856404)
                                                "Resolved" -> Color(0xFF155724)
                                                else -> Color(0xFF383D41)
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // LIVE HELP LINE CHAT DIALOGUE SIMULATION PANEL
    if (showChatDialog) {
        Dialog(
            onDismissRequest = { showChatDialog = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .fillMaxHeight(0.85f)
                    .clip(RoundedCornerShape(24.dp)),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp
            ) {
                val chatMessages = remember {
                    mutableStateListOf(
                        ChatMsg("Office Assistant Amir", "Slamat sejahtera! Welcome to YTL CoinReward Executive Support Helpdesk.", false),
                        ChatMsg("Office Assistant Amir", "I am Amir, your senior support officer. Drop any coin out queries or payment questions here!", false)
                    )
                }
                var userChatFieldText by remember { mutableStateOf("") }
                val coroutineScope = rememberCoroutineScope()
                val listState = rememberLazyListState()

                LaunchedEffect(chatMessages.size) {
                    if (chatMessages.isNotEmpty()) {
                        listState.animateScrollToItem(chatMessages.size - 1)
                    }
                }

                Column(modifier = Modifier.fillMaxSize()) {
                    // Chat header panel
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF001B3D))
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .background(Color(0xFF4CAF50), CircleShape)
                            )
                            Column {
                                Text("Amir - Executive Helpdesk Officer", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("24/7 Priority Support Desk | Malaysia", color = Color.White.copy(alpha = 0.7f), fontSize = 10.sp)
                            }
                        }
                        IconButton(onClick = { showChatDialog = false }) {
                            Text("❌", color = Color.White, fontSize = 14.sp)
                        }
                    }

                    // Chat transcripts list
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .background(Color(0xFFF5F7FA))
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(chatMessages) { msg ->
                            val isUser = msg.isUser
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
                            ) {
                                Text(
                                    text = msg.sender,
                                    fontSize = 9.sp,
                                    color = Color.Gray,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                                Box(
                                    modifier = Modifier
                                        .background(
                                            color = if (isUser) Color(0xFFD9E2FF) else Color.White,
                                            shape = RoundedCornerShape(
                                                topStart = 16.dp,
                                                topEnd = 16.dp,
                                                bottomStart = if (isUser) 16.dp else 0.dp,
                                                bottomEnd = if (isUser) 0.dp else 16.dp
                                            )
                                        )
                                        .border(
                                            width = 1.dp,
                                            color = if (isUser) Color.Transparent else Color(0xFFE2E4E8),
                                            shape = RoundedCornerShape(
                                                topStart = 16.dp,
                                                topEnd = 16.dp,
                                                bottomStart = if (isUser) 16.dp else 0.dp,
                                                bottomEnd = if (isUser) 0.dp else 16.dp
                                            )
                                        )
                                        .padding(12.dp)
                                        .widthIn(max = 260.dp)
                                ) {
                                    Text(
                                        text = msg.text,
                                        fontSize = 12.sp,
                                        color = if (isUser) Color(0xFF001B3D) else Color.Black,
                                        lineHeight = 16.sp
                                    )
                                }
                            }
                        }
                    }

                    // Chat typing panel
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White)
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = userChatFieldText,
                            onValueChange = { userChatFieldText = it },
                            placeholder = { Text("Ask about cashout, VIP booster or delays...", fontSize = 11.sp) },
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("helpline_chat_input_textfield"),
                            shape = RoundedCornerShape(12.dp)
                        )
                        Button(
                            onClick = {
                                if (userChatFieldText.isNotBlank()) {
                                    val text = userChatFieldText.trim()
                                    chatMessages.add(ChatMsg(currentUser?.name ?: "Valued Member", text, true))
                                    userChatFieldText = ""

                                    coroutineScope.launch {
                                        delay(1000)
                                        val filter = text.lowercase()
                                        val replyMsg = when {
                                            filter.contains("pay") || filter.contains("withdraw") || filter.contains("cash") || filter.contains("bank") -> {
                                                "Our Finance Settlement Center processes CIMB Pay, Touch'n'Go and FPX bank settlements under 12 hours. Credit is fully verified on the Room database."
                                            }
                                            filter.contains("vip") || filter.contains("premium") || filter.contains("membership") -> {
                                                "VIP memberships of Level 1 & 2 provide 2x coin multipliers perpetually! You can activate them instantly in the Home Workspace."
                                            }
                                            filter.contains("survey") || filter.contains("task") || filter.contains("install") -> {
                                                "Sponsor tasks & digital surveys and video reels refresh daily automatically. Completed logs are locked inside our Room table database."
                                            }
                                            filter.contains("helpline") || filter.contains("execute") || filter.contains("help") -> {
                                                "Your active helpline support requests are instantly saved into our SQLite log system and processed sequentially down the manual ledger queue!"
                                            }
                                            else -> {
                                                "Thanks for linking with YTL CoinReward Support! Let me forward your account coordinates to our technical helpdesk queue for instant resolution."
                                            }
                                        }
                                        chatMessages.add(ChatMsg("Office Assistant Amir", replyMsg, false))
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF001B3D)),
                            modifier = Modifier.testTag("send_helpline_chat_message_button")
                        ) {
                            Text("Send", fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}

