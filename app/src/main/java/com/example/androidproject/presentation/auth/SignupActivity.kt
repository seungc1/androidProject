package com.example.androidproject.presentation.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.example.androidproject.MainActivity
import com.example.androidproject.R // (★ 추가 ★) 'R.string' '참조'
import com.example.androidproject.databinding.ActivitySignupBinding
import dagger.hilt.android.AndroidEntryPoint

/**
 * [수정 파일 3/4] '회원가입' '화면' '두뇌'
 * (★ 수정 ★) '비밀번호' '확인' '값'을 '읽어' 'ViewModel'에 '전달'
 */
@AndroidEntryPoint
class SignupActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignupBinding
    private val viewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1. '회원가입' '버튼' '클릭' '리스너'
        binding.signupButton.setOnClickListener {
            val username = binding.usernameEditText.text.toString()
            val password = binding.passwordEditText.text.toString()
            // (★ 수정 ★) '비밀번호' '확인' '값' '읽기'
            val passwordConfirm = binding.passwordConfirmEditText.text.toString()

            // (★ 수정 ★) 'ViewModel'에 '3개'의 '값' '전달'
            viewModel.signup(username, password, passwordConfirm)
        }

        // 2. 'ViewModel'의 '회원가입' '결과' '관찰'
        observeSignupState()
    }

    private fun observeSignupState() {
        viewModel.signupState.observe(this) { state ->
            when (state) {
                is SignupState.Loading -> {
                    binding.signupLoadingSpinner.isVisible = true
                    binding.signupButton.isEnabled = false
                }
                is SignupState.Success -> {
                    binding.signupLoadingSpinner.isVisible = false
                    Toast.makeText(this, "회원가입 성공! 로그인되었습니다.", Toast.LENGTH_SHORT).show()

                    val intent = Intent(this, MainActivity::class.java).apply {
                        putExtra("USER_ID", state.userId)
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    }
                    startActivity(intent)
                    finish()
                }
                is SignupState.Error -> {
                    binding.signupLoadingSpinner.isVisible = false
                    binding.signupButton.isEnabled = true

                    // (★ 수정 ★) 'ViewModel'이 '전달'한 '에러' '메시지'를 '표시'
                    val errorMessage = when (state.message) {
                        "USER_EXISTS" -> getString(R.string.signup_error_user_exists)
                        "SHORT_PASSWORD" -> getString(R.string.signup_error_short_password)
                        "PASSWORD_MISMATCH" -> getString(R.string.signup_error_password_mismatch)
                        else -> getString(R.string.signup_error_unknown)
                    }
                    Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}