package com.runpassport.app.ui.screen.login

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.runpassport.app.data.local.TokenManager
import com.runpassport.app.data.model.User
import com.runpassport.app.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LoginUiState(
    val accounts: List<String> = emptyList(),
    val accountsMap: Map<String, User> = emptyMap(), // 추가: display → User 매핑
    val selectedAccount: String? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isLoginSuccess: Boolean = false
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    init {
        loadTestAccounts()
    }

    /**
     * Supabase에서 테스트 계정 목록 로드
     */
    private fun loadTestAccounts() {
        Log.d("LoginViewModel", "loadTestAccounts() 시작")
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            Log.d("LoginViewModel", "로딩 상태 업데이트")

            authRepository.getTestAccounts()
                .onSuccess { users ->
                    Log.d("LoginViewModel", "Repository에서 ${users.size}명 수신")

                    // display label과 User 객체를 매핑
                    val accountsMap = users.associateBy(
                        keySelector = { user ->
                            "${user.nickname ?: user.email} - ${user.displayName ?: "사용자"}"
                        },
                        valueTransform = { it }
                    )

                    Log.d("LoginViewModel", "accountsMap 생성 완료: ${accountsMap.size}개")
                    accountsMap.keys.forEach { label ->
                        Log.d("LoginViewModel", "  - 계정 라벨: $label")
                    }

                    _uiState.update {
                        it.copy(
                            accounts = accountsMap.keys.toList(),
                            accountsMap = accountsMap,
                            isLoading = false,
                            errorMessage = null
                        )
                    }
                    Log.d("LoginViewModel", "UI 상태 업데이트 완료")
                }
                .onFailure { error ->
                    Log.e("LoginViewModel", "계정 목록 로드 실패", error)
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "계정 목록을 불러올 수 없습니다: ${error.message}"
                        )
                    }
                }
        }
    }

    /**
     * 사용자가 드롭다운에서 계정 선택
     */
    fun onAccountSelected(account: String) {
        _uiState.update { it.copy(selectedAccount = account) }
    }

    /**
     * 로그인 버튼 클릭 (문제 2에서 구현)
     */
    fun onLoginClick() {
        val selectedAccount = _uiState.value.selectedAccount ?: return
        val user = _uiState.value.accountsMap[selectedAccount] ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            // 테스트 환경에서는 비밀번호가 사전에 정해져 있다고 가정 => TODO 필요없음
            // 실제 프로덕션에서는 비밀번호 입력 필드 필요
            val testPassword = "test1234"

            authRepository.login(user.email, testPassword)
                .onSuccess { accessToken ->
                    // 토큰 저장
                    tokenManager.saveAccessToken(accessToken)

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isLoginSuccess = true
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "로그인 실패 :${error.message}"
                        )
                    }
                }
        }
    }
}