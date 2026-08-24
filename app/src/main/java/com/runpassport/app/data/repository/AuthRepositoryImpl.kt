package com.runpassport.app.data.repository

import android.util.Log
import com.runpassport.app.data.model.User
import com.runpassport.app.data.remote.SupabaseAuthDataSource
import com.runpassport.app.domain.repository.AuthRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val supabaseAuthDataSource: SupabaseAuthDataSource
) : AuthRepository {

    override suspend fun getTestAccounts(): Result<List<User>> {
        Log.d("AuthRepository", "getTestAccounts() 시작")
        return try {
            val users = supabaseAuthDataSource.fetchUsers()
            Log.d("AuthRepository", "getTestAccounts() 성공: ${users.size}명")
            Result.success(users)
        } catch (e: Exception) {
            Log.e("AuthRepository", "getTestAccounts() 실패", e)
            Result.failure(e)
        }
    }

    override suspend fun login(
        email: String,
        password: String
    ): Result<String> {
        return supabaseAuthDataSource.signInWithEmail(email, password)
    }
}