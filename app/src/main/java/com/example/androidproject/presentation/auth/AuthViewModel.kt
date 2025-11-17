package com.example.androidproject.presentation.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.example.androidproject.domain.usecase.LoginUseCase

/**
 * [새 파일 1/6] - '인증' (Auth) 핵심 두뇌
 * 경로: presentation/auth/AuthViewModel.kt
 * '로그인' 및 '로그인 상태 확인' 로직을 '전담'합니다.
 */

// '로그인' '결과'를 '표시'하기 위한 '상태'
sealed class LoginState {
    object Loading : LoginState()
    object Success : LoginState()
    data class Error(val message: String) : LoginState()
}

// '시작' 화면('Splash')에서 '로그인' '상태'를 '확인'하기 위한 '상태'
sealed class AuthState {
    object Loading : AuthState()
    object Authenticated : AuthState() // '로그인' 됨
    object Unauthenticated : AuthState() // '로그인' 안 됨
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase // ✅ [추가] UseCase 주입
    // (미래) private val checkLoginStatusUseCase: CheckLoginStatusUseCase
) : ViewModel() {

    // 'LoginActivity'가 '관찰'할 '로그인' '결과'
    private val _loginState = MutableLiveData<LoginState>()
    val loginState: LiveData<LoginState> = _loginState

    // 'SplashActivity'가 '관찰'할 '로그인' '상태'
    private val _authState = MutableLiveData<AuthState>()
    val authState: LiveData<AuthState> = _authState

    /**
     * 'SplashActivity'가 '호출'합니다.
     * (시뮬레이션: 1초 후, '로그인 안 됨' 상태를 '반환')
     */
    fun checkLoginStatus() {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            delay(1000) // (시뮬레이션) 1초간 '상태' '확인' (예: 토큰 검사)

            // (★ 테스트 ★) '로그인'이 '안 된' '상태'를 '강제'로 '반환'
            _authState.value = AuthState.Unauthenticated

            // (미래: '자동 로그인' '테스트'를 '하려면' '아래' '주석'을 '해제'하고 '위' '코드를' '주석 처리')
            // _authState.value = AuthState.Authenticated
        }
    }

    /**
     * 'LoginActivity'가 '호출'합니다.
     * (시뮬레이션: 'test' / '1234'일 경우 '성공')
     */
    fun login(username: String, password: String) {
        viewModelScope.launch {
            _loginState.value = LoginState.Loading
            delay(500) // (시뮬레이션) 0.5초간 '로그인' '시도'

            // 로그인 시도 UseCase 호출
            val isSuccess = loginUseCase(username, password)

            if (isSuccess) {
                _loginState.value = LoginState.Success
            } else {
                _loginState.value = LoginState.Error("비밀번호가 일치하지 않습니다.")
            }

        }
    }
}