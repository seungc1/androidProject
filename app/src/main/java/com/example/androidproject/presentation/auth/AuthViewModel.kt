package com.example.androidproject.presentation.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.androidproject.data.local.SessionManager
import com.example.androidproject.domain.model.User
import com.example.androidproject.domain.usecase.CheckUserExistsUseCase
import com.example.androidproject.domain.usecase.GoogleLoginUseCase // (★ 추가)
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
    private val googleLoginUseCase: GoogleLoginUseCase, // (★ 추가) 주입
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
     * (★ 추가 ★) 구글 로그인 요청
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
     * 회원가입
     */
    fun signup(username: String, password: String, passwordConfirm: String) {
        viewModelScope.launch {
            _signupState.value = SignupState.Loading

            if (password != passwordConfirm) {
                _signupState.value = SignupState.Error("PASSWORD_MISMATCH")
                return@launch
            }
            if (password.length < 6) {
                _signupState.value = SignupState.Error("SHORT_PASSWORD")
                return@launch
            }
            if (checkUserExistsUseCase(username)) {
                _signupState.value = SignupState.Error("USER_EXISTS")
                return@launch
            }

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
                _signupState.value = SignupState.Error("UNKNOWN_ERROR")
            }
        }
    }
}