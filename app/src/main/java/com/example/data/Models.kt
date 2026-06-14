package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val name: String,
    val email: String,
    val phoneNumber: String = "",
    val paypalEmail: String = "",
    val coins: Int,
    val referralCode: String,
    val referredBy: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val usdBalance: Double = 0.0,
    val isVip: Boolean = false,
    val vipLevel: String = "Free",
    val loginPassword: String = "123456",
    val fundPassword: String = "888888"
)

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val reward: Int,
    val active: Boolean,
    val category: String = "Sponsor"
)

@Entity(tableName = "task_completions", primaryKeys = ["userId", "taskId"])
data class TaskCompletionEntity(
    val userId: String,
    val taskId: String,
    val completedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "withdrawals")
data class WithdrawalEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val amount: Int,
    val method: String,
    val details: String,
    val status: String, // "Pending", "Approved", "Rejected"
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "earn_logs")
data class EarnLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: String,
    val message: String,
    val amount: Int,
    val timestamp: Long = System.currentTimeMillis()
)
