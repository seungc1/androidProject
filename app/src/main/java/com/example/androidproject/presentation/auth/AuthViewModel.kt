// 파일 경로: app/src/main/java/com/example/androidproject/presentation/auth/AuthViewModel.kt
package com.example.androidproject.presentation.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.androidproject.data.local.SessionManager
import com.example.androidproject.domain.model.User
import com.example.androidproject.domain.usecase.CheckUserExistsUseCase
import com.example.androidproject.domain.usecase.GoogleLoginUseCase
import com.example.androidproject.domain.usecase.LoginUseCase
import com.example.androidproject.domain.usecase.SignupUseCase
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

// --- 기존 상태 클래스 정의 ---
sealed class LoginState {
    object Loading : LoginState()
    data class Success(val userId: String) : LoginState()
    data class Error(val message: String) : LoginState()
}

sealed class AuthState {
    object Loading : AuthState()
    data class Authenticated(val userId: String) : AuthState()
    object Unauthenticated : AuthState()
}

sealed class SignupState {
    object Loading : SignupState()
    data class Success(val userId: String) : SignupState()
    data class Error(val message: String) : SignupState()
}

// --- ★★★ 신규 상태 클래스 정의 (NetworkError 추가) ★★★
sealed class CheckDuplicationState {
    object Idle : CheckDuplicationState()
    object Loading : CheckDuplicationState()
    data class Available(val username: String) : CheckDuplicationState() // 사용 가능 (성공)
    object Exists : CheckDuplicationState() // 중복됨 (실패)
    object Invalid : CheckDuplicationState() // 입력 무효 (실패)
    object NetworkError : CheckDuplicationState() // ★★★ 네트워크/권한 오류 ★★★
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val checkUserExistsUseCase: CheckUserExistsUseCase,
    private val signupUseCase: SignupUseCase,
    private val googleLoginUseCase: GoogleLoginUseCase,
    private val sessionManager: SessionManager
) : ViewModel() {

    // LiveData
    private val _loginState = MutableLiveData<LoginState>()
    val loginState: LiveData<LoginState> = _loginState

    private val _authState = MutableLiveData<AuthState>()
    val authState: LiveData<AuthState> = _authState

    private val _signupState = MutableLiveData<SignupState>()
    val signupState: LiveData<SignupState> = _signupState

    // ★★★ 신규 LiveData ★★★
    private val _duplicationState = MutableLiveData<CheckDuplicationState>(CheckDuplicationState.Idle)
    val duplicationState: LiveData<CheckDuplicationState> = _duplicationState


    /**
     * 로그인 상태 확인
     */
    fun checkLoginStatus() {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val userId = sessionManager.getUserId()

            if (userId != null) {
                val currentUser = FirebaseAuth.getInstance().currentUser
                if (currentUser != null) {
                    _authState.value = AuthState.Authenticated(userId)
                } else {
                    sessionManager.clearSession()
                    _authState.value = AuthState.Unauthenticated
                }
            } else {
                _authState.value = AuthState.Unauthenticated
            }
        }
    }

    /**
     * 일반 로그인
     */
    fun login(username: String, password: String) {
        viewModelScope.launch {
            _loginState.value = LoginState.Loading
            val userId = loginUseCase(username, password)
            if (userId != null) {
                sessionManager.saveUserId(userId)
                _loginState.value = LoginState.Success(userId)
            } else {
                _loginState.value = LoginState.Error("로그인 실패")
            }
        }
    }

    /**
     * 구글 로그인 요청
     */
    fun googleLogin(idToken: String) {
        viewModelScope.launch {
            _loginState.value = LoginState.Loading
            val userId = googleLoginUseCase(idToken)
            if (userId != null) {
                sessionManager.saveUserId(userId)
                _loginState.value = LoginState.Success(userId)
            } else {
                _loginState.value = LoginState.Error("구글 로그인 실패")
            }
        }
    }

    /**
     * ★★★ [수정] 아이디 중복 검사 로직 (오류 처리 추가) ★★★
     */
    fun checkUsernameDuplication(username: String) {
        viewModelScope.launch {
            _duplicationState.value = CheckDuplicationState.Loading

            // 기본 유효성 검사 (빈 값, 길이 등)
            if (username.isBlank() || username.length < 4) {
                _duplicationState.value = CheckDuplicationState.Invalid
                return@launch
            }

            try {
                // DB 조회 (Local + Remote)
                val exists = checkUserExistsUseCase(username)

                if (exists) {
                    _duplicationState.value = CheckDuplicationState.Exists
                } else {
                    _duplicationState.value = CheckDuplicationState.Available(username)
                }
            } catch (e: Exception) {
                // ★★★ Firebase 권한/네트워크 오류 처리 ★★★
                _duplicationState.value = CheckDuplicationState.NetworkError
            }
        }
    }

    /**
     * 회원가입 로직
     */
    fun signup(username: String, password: String, passwordConfirm: String) {
        viewModelScope.launch {
            _signupState.value = SignupState.Loading

            if (password != passwordConfirm) {
                _signupState.value = SignupState.Error("PASSWORD_MISMATCH")
                return@launch
            }
            // 비밀번호 길이 검사 (strings.xml에 맞춰 6자리로 조정)
            if (password.length < 6) {
                _signupState.value = SignupState.Error("SHORT_PASSWORD")
                return@launch
            }

            // ★★★ 주의: 여기서 중복 확인 로직은 삭제합니다.
            // 대신, UI(SignupActivity)에서 'verifiedUsername' 플래그를 통해
            // 미리 중복 확인을 완료했는지 검증해야 합니다.

            val newUser = User(
                id = username,
                password = password,
                name = "신규 사용자",
                gender = "미설정",
                age = 0, heightCm = 0, weightKg = 0.0, activityLevel = "낮음",
                fitnessGoal = "재활", allergyInfo = emptyList(),
                preferredDietType = "일반", preferredDietaryTypes = emptyList(),
                equipmentAvailable = emptyList(), currentPainLevel = 0,
                additionalNotes = null, targetCalories = null, currentInjuryId = null
            )

            try {
                signupUseCase(newUser).first()
                sessionManager.saveUserId(newUser.id)
                _signupState.value = SignupState.Success(newUser.id)
            } catch (e: Exception) {
                // Firebase 중복 오류 발생 시 "UNKNOWN_ERROR"가 아닌 "USER_EXISTS"를 반환하도록 변경
                // FirebaseDataSource에서 User ID로 가입할 때 이미 로컬에 있다면 "USER_EXISTS"로 처리합니다.
                // 만약 Firebase Auth에서 중복 오류가 나면 여기서 잡고 USER_EXISTS로 처리하는 로직이 필요합니다.
                val errorMessage = if (e.message?.contains("The email address is already in use") == true) "USER_EXISTS" else "UNKNOWN_ERROR"
                _signupState.value = SignupState.Error(errorMessage)
            }
        }
    }
}