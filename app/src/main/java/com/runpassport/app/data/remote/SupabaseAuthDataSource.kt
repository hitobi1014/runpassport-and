package com.runpassport.app.data.remote

import android.util.Log
import com.runpassport.app.data.model.User
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.from
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SupabaseAuthDataSource @Inject constructor(
    private val supabaseClientProvider: SupabaseClientProvider
) {
    private val client get() = supabaseClientProvider.client

    /**
     * Supabase users 테이블에서 모든 사용자 목록을 가져옴
     */
    suspend fun fetchUsers(): List<User> {
        Log.d("SupabaseAuthDataSource", "fetchUsers() 시작")
        return try {
            val users = client.from("users")
                .select()
                .decodeList<User>()
            Log.d("SupabaseAuthDataSource", "fetchUsers() 성공: ${users.size}명 조회됨")
            users.forEach { user ->
                Log.d("SupabaseAuthDataSource", "  - User: id=${user.id}, email=${user.email}, nickname=${user.nickname}, displayName=${user.displayName}")
            }
            users
        } catch (e: Exception) {
            Log.e("SupabaseAuthDataSource", "fetchUsers() 실패", e)
            throw e
        }
    }

    /**
     * 테스트 계정 로그인 (이메일/비밀번호)
     * Supabase Auth 사용
     */
    suspend fun signInWithEmail(email: String, password: String): Result<String> {
        return try {
            client.auth.signInWith(Email) {
                this.email = email
                this.password = password
            }

            // Supabase 3.x에서는 세션이 자동으로 저장됨
            val token = client.auth.currentAccessTokenOrNull()
                ?: return Result.failure(Exception("액세스 토큰을 가져올 수 없음"))

            Result.success(token)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

}