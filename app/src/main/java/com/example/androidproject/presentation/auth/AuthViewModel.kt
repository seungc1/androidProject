package com.example.androidproject.presentation.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.androidproject.data.local.SessionManager
import com.example.androidproject.domain.model.User
import com.example.androidproject.domain.usecase.CheckUserExistsUseCase
import com.example.androidproject.domain.usecase.LoginUseCase
import com.example.androidproject.domain.usecase.SignupUseCase
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

// --- 상태 클래스 정의 ---
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

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val checkUserExistsUseCase: CheckUserExistsUseCase,
    private val signupUseCase: SignupUseCase,
    private val sessionManager: SessionManager
) : ViewModel() {

    // LiveData
    private val _loginState = MutableLiveData<LoginState>()
    val loginState: LiveData<LoginState> = _loginState

    private val _authState = MutableLiveData<AuthState>()
    val authState: LiveData<AuthState> = _authState

    private val _signupState = MutableLiveData<SignupState>()
    val signupState: LiveData<SignupState> = _signupState

    /**
     * [수정됨] 로그인 상태 확인
     * 로컬에 ID가 있더라도, 실제 Firebase 인증 세션이 유효한지 '더블 체크' 합니다.
     */
    fun checkLoginStatus() {
        viewModelScope.launch {
            _authState.value = AuthState.Loading

            // 1. 로컬 세션(SharedPreferences) 확인
            val userId = sessionManager.getUserId()

            if (userId != null) {
                // 2. (★중요★) 서버(Firebase) 인증 상태 확인
                // 로컬에 기록이 있어도, 서버에서 삭제되었거나 토큰이 만료되었으면 로그아웃 처리
                val currentUser = FirebaseAuth.getInstance().currentUser

                if (currentUser != null) {
                    // 인증 정보 유효함 -> 로그인 성공
                    _authState.value = AuthState.Authenticated(userId)
                } else {
                    // 인증 정보가 없음 (삭제됨/만료됨) -> 로그아웃 처리 및 로컬 데이터 정리
                    sessionManager.clearSession()
                    _authState.value = AuthState.Unauthenticated
                }
            } else {
                // 로컬 정보 없음 -> 비로그인 상태
                _authState.value = AuthState.Unauthenticated
            }
        }
    }

    /**
     * 로그인
     */
    fun login(username: String, password: String) {
        viewModelScope.launch {
            _loginState.value = LoginState.Loading
            // loginUseCase 내부에서 Firebase 로그인 + DB 조회를 수행
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
     * 회원가입
     */
    fun signup(username: String, password: String, passwordConfirm: String) {
        viewModelScope.launch {
            _signupState.value = SignupState.Loading

            // 1. 비밀번호 일치 검사
            if (password != passwordConfirm) {
                _signupState.value = SignupState.Error("PASSWORD_MISMATCH")
                return@launch
            }
            // 2. 길이 검사
            if (password.length < 4) {
                _signupState.value = SignupState.Error("SHORT_PASSWORD")
                return@launch
            }
            // 3. 아이디 중복 검사 (Firebase는 가입 시 자동 체크하므로 로컬은 패스)
            if (checkUserExistsUseCase(username)) {
                _signupState.value = SignupState.Error("USER_EXISTS")
                return@launch
            }

            // 4. 유저 객체 생성
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

            // 5. 가입 요청
            try {
                signupUseCase(newUser).first()
                // 가입 성공 시 자동 로그인 처리
                sessionManager.saveUserId(newUser.id)
                _signupState.value = SignupState.Success(newUser.id)
            } catch (e: Exception) {
                _signupState.value = SignupState.Error("UNKNOWN_ERROR")
            }
        }
    }
}