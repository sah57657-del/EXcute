package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE id = :userId")
    fun getUser(userId: String): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE id = :userId")
    suspend fun getUserSync(userId: String): UserEntity?

    @Query("SELECT * FROM users WHERE referralCode = :code")
    suspend fun getUserByReferralCodeSync(code: String): UserEntity?

    @Query("SELECT * FROM users")
    suspend fun getAllUsersSync(): List<UserEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Query("UPDATE users SET coins = :coins WHERE id = :userId")
    suspend fun updateCoins(userId: String, coins: Int)
}

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks WHERE active = 1")
    fun getAllActiveTasks(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE id = :taskId")
    suspend fun getTaskSync(taskId: String): TaskEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity)
}

@Dao
interface TaskCompletionDao {
    @Query("SELECT taskId FROM task_completions WHERE userId = :userId")
    fun getCompletedTaskIdsForUser(userId: String): Flow<List<String>>

    @Query("SELECT COUNT(*) > 0 FROM task_completions WHERE userId = :userId AND taskId = :taskId")
    suspend fun isTaskCompletedSync(userId: String, taskId: String): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCompletion(completion: TaskCompletionEntity)
}

@Dao
interface WithdrawalDao {
    @Query("SELECT * FROM withdrawals WHERE userId = :userId ORDER BY createdAt DESC")
    fun getWithdrawalsForUser(userId: String): Flow<List<WithdrawalEntity>>

    @Query("SELECT * FROM withdrawals ORDER BY createdAt DESC")
    fun getAllWithdrawals(): Flow<List<WithdrawalEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWithdrawal(withdrawal: WithdrawalEntity)

    @Query("UPDATE withdrawals SET status = :status WHERE id = :id")
    suspend fun updateWithdrawalStatusSync(id: String, status: String)
}

@Dao
interface EarnLogDao {
    @Query("SELECT * FROM earn_logs WHERE userId = :userId ORDER BY timestamp DESC")
    fun getLogsForUser(userId: String): Flow<List<EarnLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: EarnLogEntity)
}
