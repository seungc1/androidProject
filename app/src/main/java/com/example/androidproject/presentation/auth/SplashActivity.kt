package com.example.androidproject.presentation.auth

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.androidproject.MainActivity
import com.example.androidproject.databinding.ActivitySplashBinding
import dagger.hilt.android.AndroidEntryPoint

/**
 * [새 파일 3/6] - '스플래시(시작)' 화면 '두뇌'
 * 경로: presentation/auth/SplashActivity.kt
 * 앱의 '새로운' '시작 지점'(Launcher)입니다.
 * '로그인' '상태'를 '확인'하고 '적절한' '화면'으로 '이동'시킵니다.
 */
@AndroidEntryPoint
class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding
    private val viewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1. 'ViewModel'의 '로그인' '상태' '확인' '결과'를 '관찰'
        observeAuthState()

        // 2. 'ViewModel'에게 '로그인' '상태' '확인'을 '요청'
        viewModel.checkLoginStatus()
    }

    private fun observeAuthState() {
        viewModel.authState.observe(this) { state ->
            when (state) {
                is AuthState.Loading -> {
                    // '로딩' 중 (아무것도 하지 않고 '스피너'만 '표시')
                }
                is AuthState.Authenticated -> {
                    // '로그인' 됨: 'MainActivity'로 '이동'
                    navigateTo(MainActivity::class.java)
                }
                is AuthState.Unauthenticated -> {
                    // '로그인' 안 됨: 'LoginActivity'로 '이동'
                    navigateTo(LoginActivity::class.java)
                }
            }
        }
    }

    /**
     * '목표' 'Activity'로 '이동'하고 '현재' 'Activity'를 '종료'
     */
    private fun navigateTo(activityClass: Class<*>) {
        val intent = Intent(this, activityClass)
        startActivity(intent)
        finish()
    }
}