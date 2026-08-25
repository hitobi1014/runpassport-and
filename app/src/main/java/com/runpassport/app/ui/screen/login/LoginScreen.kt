package com.runpassport.app.ui.screen.login

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.runpassport.app.ui.theme.Blue
import com.runpassport.app.ui.theme.Gray600
import com.runpassport.app.ui.theme.LightGray


/**
 * Stateful Composable (ViewModel과 연결)
 * 실제 앱에서 사용
 */
@Composable
fun LoginRoute(
    onLoginSuccess: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LoginViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    if (uiState.isLoginSuccess) {
        onLoginSuccess()
    }

    // errorMessage 생길 때마다 Snackbar 띄움, 다 보여준 뒤 상태 초기화
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.onErrorMessageShown()
        }
    }
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        // 기존 LoginScreen 호출 (Stateless)
        LoginScreen(
            modifier = Modifier.padding(padding),
            accounts = uiState.accounts,
            selectedAccount = uiState.selectedAccount,
            password = uiState.password,
            onAccountSelected = viewModel::onAccountSelected,
            onPasswordChanged = viewModel::onPasswordChanged,
            onLoginClick = viewModel::onLoginClick,
//            modifier = modifier
        )
    }

    // 로딩/에러 표시 (선택사항)
    if (uiState.isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.3f)) // 반투명 스크림
                .pointerInput(Unit) {}, // 터치 이벤트 통과x
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }
}

@Composable
fun LoginScreen(
    accounts: List<String>,
    selectedAccount: String?,
    password: String,
    onAccountSelected: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onLoginClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Spacer(modifier = Modifier.height(80.dp))

        // 로고 아이콘 (임시로 placeholder)
        Box(
            modifier = Modifier
                .size(80.dp)
                .background(Blue, RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            // 실제 앱에서는 Icon 또는 Image로 교체
            Text("📈", style = MaterialTheme.typography.headlineMedium)
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "RunPassport",
            style = MaterialTheme.typography.headlineMedium
        )

        Text(
            text = "전국 러닝 스탬프 투어",
            style = MaterialTheme.typography.bodySmall,
            color = Gray600
        )

        Spacer(modifier = Modifier.height(40.dp))

        // 테스트 계정 선택 섹션
        Text(
            text = "테스트 계정 선택",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.align(Alignment.Start)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 드롭다운
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(LightGray, RoundedCornerShape(8.dp))
                .clickable { expanded = true }
                .padding(16.dp)
        ) {
            androidx.compose.foundation.layout.Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = selectedAccount ?: "계정을 선택하세요",
                    style = MaterialTheme.typography.bodyMedium
                )
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = "드롭다운"
                )
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                accounts.forEach { account ->
                    DropdownMenuItem(
                        text = { Text(account) },
                        onClick = {
                            onAccountSelected(account)
                            expanded = false
                        }
                    )
                }
            }
        }

        // 비밀번호 입력 필드
        OutlinedTextField(
            value = password,
            onValueChange = onPasswordChanged,
            label = { Text("비밀번호") },
            placeholder = { Text("비밀번호를 입력하세요") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = { onLoginClick() }
            ),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        // 로그인 버튼
        Button(
            onClick = onLoginClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Blue
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                text = "로그인",
                style = MaterialTheme.typography.bodyLarge
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "친구관광공사 프로젝트 샘플 데모입니다.\n실제 인증 절차 없이 계정을 선택해 체험하세요",
            style = MaterialTheme.typography.bodySmall,
            color = Gray600,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
    }
}

//@Preview(showBackground = true, showSystemUi = true)
//@Composable
//fun LoginScreenPreview() {
//    RunpassportTheme {
//        LoginScreen(
//            accounts = listOf(
//                "러너001 - 김도운 (서울)",
//                "러너002 - 이준호 (부산)",
//                "러너003 - 박서연 (제주)"
//            ),
//            selectedAccount = "러너001 - 김도운 (서울)",
//            onAccountSelected = {},
//            onLoginClick = {}
//        )
//    }
//}
