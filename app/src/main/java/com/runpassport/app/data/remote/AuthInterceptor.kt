package com.runpassport.app.data.remote

import com.runpassport.app.data.local.TokenManager
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthInterceptor @Inject constructor(
    private val tokenManager: TokenManager
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        // 토큰 가져오기 (runBlocking은 Interceptor가 suspend 함수를 지원하지 않아서 사용)
        val token = runBlocking {
            tokenManager.getAccessToken()
        }

        // 토큰이 있으면 Authorization 헤더 추가
        val newRequest = if (token != null) {
            originalRequest.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        } else {
            originalRequest
        }

        return chain.proceed(newRequest)
    }
}