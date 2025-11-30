// 파일 경로: app/src/main/java/com/example/androidproject/presentation/auth/SignupActivity.kt
package com.dataDoctor.rehabai.presentation.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import com.dataDoctor.rehabai.MainActivity
import com.dataDoctor.rehabai.R
import com.dataDoctor.rehabai.databinding.ActivitySignupBinding
import dagger.hilt.android.AndroidEntryPoint

/**
 * [수정 파일 3/4] '회원가입' '화면' '두뇌'
 * (★ 수정 ★) 아이디 중복 확인 버튼 로직 및 UI 상태 관리 추가
 */
@AndroidEntryPoint
class SignupActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignupBinding
    private val viewModel: AuthViewModel by viewModels()

    // ★★★ [신규] 중복 확인이 완료된 아이디를 저장하는 상태 변수 ★★★
    private var verifiedUsername: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1. 아이디 입력 변경 감지 리스너 (변경 시 중복 확인 상태 초기화)
        binding.usernameEditText.addTextChangedListener {
            verifiedUsername = null
            clearUsernameMessages()
        }

        // 2. ★★★ 중복 확인 버튼 클릭 리스너 ★★★
        binding.checkDuplicationButton.setOnClickListener {
            val username = binding.usernameEditText.text.toString()
            viewModel.checkUsernameDuplication(username)
        }

        // 3. '회원가입' 버튼 클릭 리스너
        binding.signupButton.setOnClickListener {
            // ★★★ 중복 확인 필수 검증 ★★★
            val currentUsername = binding.usernameEditText.text.toString()
            if (currentUsername != verifiedUsername) {
                // 중복 확인이 안 되었거나, 확인 후 아이디를 변경했을 경우
                binding.usernameInputLayout.error = "회원가입을 위해 아이디 중복 확인이 필요합니다."
                binding.usernameInputLayout.requestFocus()
                return@setOnClickListener
            }

            clearErrors(excludeUsername = true) // 중복 확인은 통과했으므로 제외

            val password = binding.passwordEditText.text.toString()
            val passwordConfirm = binding.passwordConfirmEditText.text.toString()

            viewModel.signup(currentUsername, password, passwordConfirm)
        }

        // 4. ViewModel의 결과 관찰
        observeSignupState()
        observeDuplicationState()
    }

    private fun clearErrors(excludeUsername: Boolean = false) {
        if (!excludeUsername) {
            binding.usernameInputLayout.error = null
        }
        binding.passwordInputLayout.error = null
        binding.passwordConfirmInputLayout.error = null
    }

    private fun clearUsernameMessages() {
        binding.usernameInputLayout.error = null
        binding.usernameInputLayout.helperText = null
    }

    // ★★★ 아이디 중복 확인 결과 처리 ★★★
    private fun observeDuplicationState() {
        viewModel.duplicationState.observe(this) { state ->
            // 로딩 상태에서만 버튼 비활성화 (UX 향상)
            binding.checkDuplicationButton.isEnabled = state != CheckDuplicationState.Loading
            binding.signupButton.isEnabled = state != CheckDuplicationState.Loading

            when (state) {
                is CheckDuplicationState.Loading -> {
                    clearUsernameMessages()
                }
                is CheckDuplicationState.Available -> {
                    // 성공: 사용 가능
                    verifiedUsername = state.username
                    binding.usernameInputLayout.helperText = "✅ 사용 가능한 아이디입니다."
                    binding.usernameInputLayout.isHelperTextEnabled = true
                    Toast.makeText(this, "사용 가능한 아이디입니다.", Toast.LENGTH_SHORT).show()
                }
                is CheckDuplicationState.Exists -> {
                    // ★★★ 수정: 경고 메시지 출력 ★★★
                    verifiedUsername = null
                    binding.usernameInputLayout.error = getString(R.string.signup_error_user_exists)
                    binding.usernameInputLayout.requestFocus()
                    Toast.makeText(this, getString(R.string.signup_error_user_exists), Toast.LENGTH_SHORT).show()
                }
                is CheckDuplicationState.Invalid -> {
                    // 실패: 유효하지 않은 입력
                    verifiedUsername = null
                    binding.usernameInputLayout.error = "아이디를 4자 이상 입력해 주세요."
                    binding.usernameInputLayout.requestFocus()
                }
                is CheckDuplicationState.NetworkError -> {
                    // 서버 확인 오류 (PERMISSION_DENIED 등)
                    verifiedUsername = null
                    binding.usernameInputLayout.error = "서버 확인 불가 (권한 오류)"
                    Toast.makeText(this, "Firebase 권한 오류: Firestore 규칙을 확인해주세요.", Toast.LENGTH_LONG).show()
                }
                is CheckDuplicationState.Idle -> {}
            }
        }
    }

    private fun observeSignupState() {
        viewModel.signupState.observe(this) { state ->
            when (state) {
                is SignupState.Loading -> {
                    binding.signupLoadingSpinner.isVisible = true
                    binding.signupButton.isEnabled = false
                    binding.checkDuplicationButton.isEnabled = false
                    clearErrors()
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
                    binding.checkDuplicationButton.isEnabled = true

                    // (★ 수정 ★) 에러 종류에 따라 해당 입력창에 에러 표시
                    when (state.message) {
                        "SHORT_PASSWORD" -> {
                            binding.passwordInputLayout.error = getString(R.string.signup_error_short_password)
                            binding.passwordInputLayout.requestFocus()
                        }
                        "PASSWORD_MISMATCH" -> {
                            binding.passwordConfirmInputLayout.error = getString(R.string.signup_error_password_mismatch)
                            binding.passwordConfirmInputLayout.requestFocus()
                        }
                        "USER_EXISTS" -> {
                            // 로컬 DB를 통과했더라도 Firebase에서 중복으로 확인된 경우
                            binding.usernameInputLayout.error = getString(R.string.signup_error_user_exists)
                            binding.usernameInputLayout.requestFocus()
                            verifiedUsername = null
                        }
                        else -> {
                            // UNKNOWN_ERROR: Firebase 오류나 네트워크 오류 등
                            Toast.makeText(this, getString(R.string.signup_error_unknown), Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
        }
    }
}