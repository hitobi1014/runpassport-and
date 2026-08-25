package com.runpassport.app.domain.repository

import com.runpassport.app.data.model.User

interface AuthRepository {
    suspend fun getTestAccounts(): Result<List<User>>
    suspend fun login(email: String, password: String): Result<String>
}