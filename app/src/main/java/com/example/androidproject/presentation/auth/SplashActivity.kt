package com.example.androidproject.presentation.auth

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.androidproject.MainActivity
import com.example.androidproject.databinding.ActivitySplashBinding
import dagger.hilt.android.AndroidEntryPoint

/**
 * [수정 파일 1/2] - '스플래시(시작)' 화면 '두뇌'
 * (★ 수정 ★) '자동 로그인' '성공' '시' 'MainActivity'에 'userId'를 '전달'
 */
@AndroidEntryPoint
class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding
    private val viewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        observeAuthState()
        viewModel.checkLoginStatus()
    }

    private fun observeAuthState() {
        viewModel.authState.observe(this) { state ->
            when (state) {
                is AuthState.Loading -> {
                    // (로딩 중)
                }
                is AuthState.Authenticated -> {
                    // '로그인' 됨: 'MainActivity'로 '이동'
                    // (★ 수정 ★) 'userId'를 'Intent'에 '담아' '전달'
                    navigateTo(MainActivity::class.java, state.userId)
                }
                is AuthState.Unauthenticated -> {
                    // '로그인' 안 됨: 'LoginActivity'로 '이동'
                    navigateTo(LoginActivity::class.java, null)
                }
            }
        }
    }

    /**
     * (★ 수정 ★) 'userId'를 '전달'하는 'navigateTo' '함수'
     */
    private fun navigateTo(activityClass: Class<*>, userId: String?) {
        val intent = Intent(this, activityClass).apply {
            // (★ 추가 ★) 'userId'가 '있다면' 'Intent'에 '추가'
            if (userId != null) {
                putExtra("USER_ID", userId)
            }
            // (★ 추가 ★) '뒤로가기' '버튼' '시' '로그인' '화면'이 '다시' '뜨지' '않도록' '스택' '정리'
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }
}