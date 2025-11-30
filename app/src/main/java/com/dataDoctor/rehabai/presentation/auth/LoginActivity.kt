package com.dataDoctor.rehabai.presentation.auth

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.dataDoctor.rehabai.MainActivity
import com.dataDoctor.rehabai.R
import com.dataDoctor.rehabai.databinding.ActivityLoginBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val viewModel: AuthViewModel by viewModels()

    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                val idToken = account.idToken

                // ★★★ [수정] 디버그 로그 추가 (ID 토큰 존재 여부 확인) ★★★
                android.util.Log.d("GOOGLE_AUTH", "Google Sign-In 성공: Email=${account.email}")

                if (idToken != null) {
                    android.util.Log.d("GOOGLE_AUTH", "ID Token 획득: ${idToken.substring(0, 10)}... (토큰 길이: ${idToken.length})")
                    viewModel.googleLogin(idToken)
                } else {
                    android.util.Log.e("GOOGLE_AUTH", "ID Token이 null입니다. GSO 설정(requestIdToken)과 SHA-1을 확인하세요.")
                    Toast.makeText(this, "구글 ID 토큰을 가져오지 못했습니다.", Toast.LENGTH_SHORT).show()
                }
            } catch (e: ApiException) {
                // 구글 로그인 API 에러 처리 (API 코드를 확인하여 원인 진단)
                android.util.Log.e("GOOGLE_AUTH", "Google Sign-In 실패. 상태 코드: ${e.statusCode}, 메시지: ${e.message}", e)
                Toast.makeText(this, "구글 로그인 실패: ${e.statusCode} (${e.message})", Toast.LENGTH_LONG).show()
                e.printStackTrace()
            }
        } else {
            android.util.Log.w("GOOGLE_AUTH", "Google Sign-In 취소됨. Result Code: ${result.resultCode}")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // XML 오류가 해결되면 binding도 정상적으로 생성됩니다.
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.loginButton.setOnClickListener {
            val username = binding.usernameEditText.text.toString()
            val password = binding.passwordEditText.text.toString()
            viewModel.login(username, password)
        }

        binding.googleLoginButton.setOnClickListener {
            startGoogleSignIn()
        }

        binding.signupButton.setOnClickListener {
            val intent = Intent(this, SignupActivity::class.java)
            startActivity(intent)
        }

        observeLoginState()
    }

    private fun startGoogleSignIn() {
        val webClientId = try {
            // (★수정★) 리소스 참조 오류 가능성이 있으므로 try-catch 블록 내부에서 호출
            getString(R.string.default_web_client_id)
        } catch (e: Exception) {
            // 리소스를 찾지 못할 경우 에러를 출력 (앱 크래시 방지)
            Toast.makeText(this, "오류: Google Client ID를 찾을 수 없습니다. Firebase 설정을 확인하세요.", Toast.LENGTH_LONG).show()
            e.printStackTrace()
            return
        }

        // ★★★ [수정] Web Client ID를 로그로 출력하여 확인 ★★★
        android.util.Log.d("GOOGLE_AUTH", "사용된 Web Client ID: $webClientId")

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(webClientId)
            .requestEmail()
            .build()

        val googleSignInClient = GoogleSignIn.getClient(this, gso)
        googleSignInClient.signOut().addOnCompleteListener {
            android.util.Log.d("GOOGLE_AUTH", "기존 구글 세션 로그아웃 후 로그인 시작.")
            googleSignInLauncher.launch(googleSignInClient.signInIntent)
        }
    }

    private fun observeLoginState() {
        viewModel.loginState.observe(this) { state ->
            when (state) {
                is LoginState.Loading -> {
                    binding.loginLoadingSpinner.isVisible = true
                    binding.loginButton.isEnabled = false
                    binding.googleLoginButton.isEnabled = false
                }
                is LoginState.Success -> {
                    binding.loginLoadingSpinner.isVisible = false
                    Toast.makeText(this, "로그인 성공!", Toast.LENGTH_SHORT).show()

                    val intent = Intent(this, MainActivity::class.java).apply {
                        putExtra("USER_ID", state.userId)
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    }
                    startActivity(intent)
                    finish()
                }
                is LoginState.Error -> {
                    binding.loginLoadingSpinner.isVisible = false
                    binding.loginButton.isEnabled = true
                    binding.googleLoginButton.isEnabled = true
                    Toast.makeText(this, state.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}