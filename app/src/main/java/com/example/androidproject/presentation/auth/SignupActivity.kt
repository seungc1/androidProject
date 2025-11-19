package com.example.androidproject.presentation.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.example.androidproject.MainActivity
import com.example.androidproject.R
import com.example.androidproject.databinding.ActivitySignupBinding
import dagger.hilt.android.AndroidEntryPoint

/**
 * [수정 파일 3/4] '회원가입' '화면' '두뇌'
 * (★ 수정 ★) 에러 발생 시 Toast 대신 '입력창'에 '빨간색 경고' 표시
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
            // 버튼 클릭 시 기존 에러 메시지 초기화 (깔끔하게 다시 검사)
            clearErrors()

            val username = binding.usernameEditText.text.toString()
            val password = binding.passwordEditText.text.toString()
            val passwordConfirm = binding.passwordConfirmEditText.text.toString()

            viewModel.signup(username, password, passwordConfirm)
        }

        // 2. 'ViewModel'의 '회원가입' '결과' '관찰'
        observeSignupState()
    }

    private fun clearErrors() {
        binding.usernameInputLayout.error = null
        binding.passwordInputLayout.error = null
        binding.passwordConfirmInputLayout.error = null
    }

    private fun observeSignupState() {
        viewModel.signupState.observe(this) { state ->
            when (state) {
                is SignupState.Loading -> {
                    binding.signupLoadingSpinner.isVisible = true
                    binding.signupButton.isEnabled = false
                    clearErrors() // 로딩 중에는 에러 메시지 숨김
                }
                is SignupState.Success -> {
                    binding.signupLoadingSpinner.isVisible = false
                    clearErrors()

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

                    // (★ 수정 ★) 에러 종류에 따라 '해당 입력창'에 빨간색 에러 표시
                    when (state.message) {
                        "SHORT_PASSWORD" -> {
                            binding.passwordInputLayout.error = getString(R.string.signup_error_short_password)
                            binding.passwordInputLayout.requestFocus() // 사용자가 바로 수정할 수 있게 포커스 이동
                        }
                        "PASSWORD_MISMATCH" -> {
                            binding.passwordConfirmInputLayout.error = getString(R.string.signup_error_password_mismatch)
                            binding.passwordConfirmInputLayout.requestFocus()
                        }
                        "USER_EXISTS" -> {
                            binding.usernameInputLayout.error = getString(R.string.signup_error_user_exists)
                            binding.usernameInputLayout.requestFocus()
                        }
                        else -> {
                            // 그 외 알 수 없는 에러는 Toast로 표시
                            Toast.makeText(this, getString(R.string.signup_error_unknown), Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
        }
    }
}