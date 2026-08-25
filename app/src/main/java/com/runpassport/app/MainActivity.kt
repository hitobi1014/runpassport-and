package com.runpassport.app

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.runpassport.app.ui.screen.login.LoginRoute
import com.runpassport.app.ui.theme.RunpassportTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RunpassportTheme {
                LoginRoute(
                    onLoginSuccess = {
                        Log.d("MainActivity", "로그인 성공 후 홈으로 이동")
                        // TODO 홈 화면으로 이동 => Navigation 연결 후
                    }
                )
            }
        }
    }
}