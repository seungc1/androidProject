package com.example.androidproject.data.local

import android.content.SharedPreferences
import javax.inject.Inject
import javax.inject.Singleton

/**
 * [새 파일 1/2]
 * SharedPreferences를 사용하여 '로그인' '상태'('userId')를 '저장'/'로드'하는 '세션' '관리자'
 */
@Singleton
class SessionManager @Inject constructor(
    // (Hilt가 'SessionModule'에서 'SharedPreferences' '객체'를 '주입'해 줌)
    private val prefs: SharedPreferences
) {

    companion object {
        private const val KEY_USER_ID = "user_id"
    }

    /**
     * '로그인' '성공' '시' 'userId'를 '저장'
     */
    fun saveUserId(userId: String) {
        prefs.edit().putString(KEY_USER_ID, userId).apply()
    }

    /**
     * '자동' '로그인' '시' '저장'된 'userId'를 '로드'
     */
    fun getUserId(): String? {
        return prefs.getString(KEY_USER_ID, null)
    }

    /**
     * (미래 '로그아웃' '기능'을 '위해' '필요'함)
     */
    fun clearSession() {
        prefs.edit().remove(KEY_USER_ID).apply()
    }
}