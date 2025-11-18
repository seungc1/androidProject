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
 * [수정 파일 3/8] - '로그인' 화면 '두뇌'
 * (★ 수정 ★) '회원가입' '버튼'('signupButton') '클릭' '리스너' '추가'
 */
@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val viewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1. '로그인' 버튼 '클릭 리스너' (수정 없음)
        binding.loginButton.setOnClickListener {
            val username = binding.usernameEditText.text.toString()
            val password = binding.passwordEditText.text.toString()
            viewModel.login(username, password)
        }

        // (★ 추가 ★) 2. '회원가입' 버튼 '클릭 리스너'
        binding.signupButton.setOnClickListener {
            val intent = Intent(this, SignupActivity::class.java)
            startActivity(intent)
            // ('finish()' '호출' '안 함' - '회원가입' '후' '이' '화면'으로 '돌아올' '수' '있음')
        }

        // 3. 'ViewModel'의 '로그인' '결과'를 '관찰'
        observeLoginState()
    }

    private fun observeLoginState() {
        viewModel.loginState.observe(this) { state ->
            when (state) {
                is LoginState.Loading -> {
                    binding.loginLoadingSpinner.isVisible = true
                    binding.loginButton.isEnabled = false
                }
                is LoginState.Success -> {
                    binding.loginLoadingSpinner.isVisible = false
                    Toast.makeText(this, "로그인 성공!", Toast.LENGTH_SHORT).show()

                    // (★ 수정 ★) '성공'한 'userId'를 'Intent'에 '담아' '전달'
                    val intent = Intent(this, MainActivity::class.java).apply {
                        putExtra("USER_ID", state.userId)
                        // '이전' '화면'('Splash', 'Login')을 '모두' '종료'
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    }
                    startActivity(intent)
                    finish()
                }
                is LoginState.Error -> {
                    binding.loginLoadingSpinner.isVisible = false
                    binding.loginButton.isEnabled = true
                    // (★ 수정 ★) '요청'하신 '새' '오류' '메시지' '사용'
                    Toast.makeText(this, getString(R.string.login_failed_message), Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}