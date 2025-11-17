package com.example.androidproject.presentation.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.example.androidproject.MainActivity
import com.example.androidproject.R
import com.example.androidproject.databinding.ActivityLoginBinding
import dagger.hilt.android.AndroidEntryPoint

/**
 * [새 파일 5/6] - '로그인' 화면 '두뇌'
 * 경로: presentation/auth/LoginActivity.kt
 * '사용자' '입력'을 '받아' 'AuthViewModel'에게 '로그인'을 '요청'합니다.
 */
@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    // 'AuthViewModel'을 '주입'
    private val viewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1. '로그인' 버튼 '클릭 리스너'
        binding.loginButton.setOnClickListener {
            val username = binding.usernameEditText.text.toString()
            val password = binding.passwordEditText.text.toString()
            viewModel.login(username, password)
        }

        // 2. 'ViewModel'의 '로그인' '결과'를 '관찰'
        observeLoginState()
    }

    private fun observeLoginState() {
        viewModel.loginState.observe(this) { state ->
            when (state) {
                is LoginState.Loading -> {
                    // '로딩' 중: '버튼' '비활성화' 및 '스피너' '표시'
                    binding.loginLoadingSpinner.isVisible = true
                    binding.loginButton.isEnabled = false
                }
                is LoginState.Success -> {
                    // '성공': 'MainActivity'로 '이동'
                    binding.loginLoadingSpinner.isVisible = false
                    Toast.makeText(this, "로그인 성공!", Toast.LENGTH_SHORT).show()

                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish() // '로그인' 화면은 '종료'
                }
                is LoginState.Error -> {
                    // '실패': '에러' '메시지' '표시'
                    binding.loginLoadingSpinner.isVisible = false
                    binding.loginButton.isEnabled = true
                    Toast.makeText(this, state.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}