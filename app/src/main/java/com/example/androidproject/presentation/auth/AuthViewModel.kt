package com.example.androidproject.presentation.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.androidproject.data.local.SessionManager // (★ 추가 ★)
import com.example.androidproject.domain.model.User
import com.example.androidproject.domain.usecase.CheckUserExistsUseCase
import com.example.androidproject.domain.usecase.LoginUseCase
import com.example.androidproject.domain.usecase.SignupUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * [수정 파일 1/4] - '인증' (Auth) 핵심 두뇌
 * (★ 수정 ★) 'SessionManager'를 '주입'받아 '자동 로그인' '로직' '처리'
 */

// (LoginState, AuthState, SignupState '정의'는 '이전'과 '동일')
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
    private val sessionManager: SessionManager // (★ 추가 ★) '세션 관리자' '주입'
) : ViewModel() {

    // (LiveData 선언부 - 수정 없음)
    private val _loginState = MutableLiveData<LoginState>()
    val loginState: LiveData<LoginState> = _loginState
    private val _authState = MutableLiveData<AuthState>()
    val authState: LiveData<AuthState> = _authState
    private val _signupState = MutableLiveData<SignupState>()
    val signupState: LiveData<SignupState> = _signupState

    /**
     * (★ 수정 ★) 'SplashActivity'가 '호출'
     * '저장'된 'userId'를 '확인'하여 '자동 로그인' '처리'
     */
    fun checkLoginStatus() {
        viewModelScope.launch {
            _authState.value = AuthState.Loading

            // '세션' '관리자'에게 'ID' '요청'
            val userId = sessionManager.getUserId()

            if (userId != null) {
                // 'ID'가 '있으면' -> '자동 로그인' '성공'
                _authState.value = AuthState.Authenticated(userId)
            } else {
                // 'ID'가 '없으면' -> '로그인' '필요'
                _authState.value = AuthState.Unauthenticated
            }
        }
    }

    /**
     * (★ 수정 ★) '로그인' '성공' '시' 'userId'를 '저장'
     */
    fun login(username: String, password: String) {
        viewModelScope.launch {
            _loginState.value = LoginState.Loading
            val userId = loginUseCase(username, password)
            if (userId != null) {
                // (★ 추가 ★) '로그인' '성공' '시' 'ID'를 '저장'
                sessionManager.saveUserId(userId)
                _loginState.value = LoginState.Success(userId)
            } else {
                _loginState.value = LoginState.Error("로그인 실패")
            }
        }
    }

    /**
     * (★ 수정 ★) '회원가입' '성공' '시' 'userId'를 '저장'
     */
    fun signup(username: String, password: String, passwordConfirm: String) {
        viewModelScope.launch {
            _signupState.value = SignupState.Loading

            // (비밀번호 '일치' '검사')
            if (password != passwordConfirm) {
                _signupState.value = SignupState.Error("PASSWORD_MISMATCH")
                return@launch
            }
            // (길이 '검사')
            if (password.length < 4) {
                _signupState.value = SignupState.Error("SHORT_PASSWORD")
                return@launch
            }
            // ('아이디' '중복' '검사')
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
                // (★ 추가 ★) '회원가입' '성공' '시' 'ID'를 '저장'
                sessionManager.saveUserId(newUser.id)
                _signupState.value = SignupState.Success(newUser.id)
            } catch (e: Exception) {
                _signupState.value = SignupState.Error("UNKNOWN_ERROR")
            }
        }
    }
}