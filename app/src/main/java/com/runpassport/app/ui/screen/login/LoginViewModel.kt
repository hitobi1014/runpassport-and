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
    val password: String = "",
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
                            user.email
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
     * 비밀번호 변경
     */
    fun onPasswordChanged(password: String) {
        _uiState.update { it.copy(password = password) }
    }

    fun onErrorMessageShown() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    /**
     * 로그인 버튼 클릭 (문제 2에서 구현)
     */
    fun onLoginClick() {
        Log.d("LoginViewModel", "로그인 버튼 클릭")
        val selectedAccount = _uiState.value.selectedAccount ?: return
        Log.d(
            "LoginViewModel",
            "선택된 계정: ${_uiState.value.selectedAccount}, map: ${_uiState.value.accountsMap[selectedAccount]}"
        )
        val user = _uiState.value.accountsMap[selectedAccount] ?: return
        val password = _uiState.value.password // 실제 입력된 비밀번호

        // 비밀번호 입력 검증
        if (password.isBlank()) {
            _uiState.update { it.copy(errorMessage = "비밀번호를 입력해주세요") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            authRepository.login(user.email, password)
                .onSuccess { accessToken ->
                    Log.d("LoginViewModel", "로그인 성공 발급토큰: ${accessToken}, user: ${user.email}")
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