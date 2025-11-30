// 파일 경로: app/src/main/java/com/example/androidproject/data/repository/UserRepositoryImpl.kt
package com.dataDoctor.rehabai.data.repository

import com.dataDoctor.rehabai.data.local.datasource.LocalDataSource
import com.dataDoctor.rehabai.data.remote.datasource.FirebaseDataSource
import com.dataDoctor.rehabai.data.mapper.toDomain
import com.dataDoctor.rehabai.data.mapper.toEntity
import com.dataDoctor.rehabai.domain.model.User
import com.dataDoctor.rehabai.domain.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val localDataSource: LocalDataSource,
    private val firebaseDataSource: FirebaseDataSource
) : UserRepository {

    override suspend fun login(id: String, password: String): String? {
        return try {
            val uid = firebaseDataSource.login(id, password)
            val serverUser = firebaseDataSource.getUser(uid)

            if (serverUser != null) {
                val userWithPassword = serverUser.copy(password = password)
                localDataSource.upsertUser(userWithPassword.toEntity())
                return serverUser.id
            }
            null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // (★ 추가 ★) 구글 로그인 구현
    override suspend fun loginWithGoogle(idToken: String): String? {
        return try {
            // 1. Firebase 인증 (UID 획득)
            val uid = firebaseDataSource.signInWithGoogle(idToken)

            // 2. Firestore에서 유저 정보 확인
            var serverUser = firebaseDataSource.getUser(uid)

            // 3. 신규 유저라면 DB에 초기 정보 생성 (회원가입 처리)
            if (serverUser == null) {
                val newUser = User(
                    id = FirebaseAuth.getInstance().currentUser?.email ?: "google_user",
                    password = "", // 소셜 로그인은 비밀번호 없음
                    name = "신규 사용자", // 이 이름 덕분에 로그인 후 프로필 입력 화면으로 이동함
                    gender = "미설정",
                    age = 0, heightCm = 0, weightKg = 0.0, activityLevel = "낮음",
                    fitnessGoal = "재활", allergyInfo = emptyList(),
                    preferredDietType = "일반", preferredDietaryTypes = emptyList(),
                    equipmentAvailable = emptyList(), currentPainLevel = 0,
                    additionalNotes = null, targetCalories = null, currentInjuryId = null
                )
                firebaseDataSource.signUp(newUser) // Firestore 저장 (set)
                serverUser = newUser
            }

            // 4. 로컬 DB 동기화
            localDataSource.upsertUser(serverUser.toEntity())

            // 5. 사용자 ID 반환
            serverUser.id
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override suspend fun createUser(user: User): Flow<Unit> {
        return try {
            firebaseDataSource.signUp(user)
            localDataSource.upsertUser(user.toEntity())
            flowOf(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    override suspend fun getUserProfile(userId: String): Flow<User> {
        val localFlow = localDataSource.getUserById(userId).map { userEntity ->
            userEntity?.toDomain() ?: getTemporaryUser(userId)
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val uid = FirebaseAuth.getInstance().currentUser?.uid
                if (uid != null) {
                    val serverUser = firebaseDataSource.getUser(uid)
                    if (serverUser != null) {
                        localDataSource.upsertUser(serverUser.toEntity())
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        return localFlow
    }

    override suspend fun updateUserProfile(user: User): Flow<Unit> {
        return try {
            firebaseDataSource.updateUser(user)
            localDataSource.upsertUser(user.toEntity())
            flowOf(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    /**
     * ★★★ [수정] 로컬 DB와 Firebase를 모두 조회하여 중복 확인 ★★★
     */
    override suspend fun checkUserExists(id: String): Boolean {
        // 1. Local DB 확인 (빠른 체크)
        val localExists = localDataSource.getUserCountById(id) > 0
        android.util.Log.d("DUPLICATION_CHECK", "Local check for '$id': $localExists")

        if (localExists) return true

        // 2. Firebase Firestore 확인 (서버 체크)
        val remoteExists = firebaseDataSource.checkUserExistsRemote(id)
        android.util.Log.d("DUPLICATION_CHECK", "Remote check for '$id': $remoteExists")
        
        return remoteExists
    }

    private fun getTemporaryUser(userId: String): User {
        return User(
            id = userId, password = "", name = "로딩 중...", gender = "", age = 0,
            heightCm = 0, weightKg = 0.0, activityLevel = "",
            fitnessGoal = "", allergyInfo = emptyList(),
            preferredDietType = "", targetCalories = 0,
            currentInjuryId = null, preferredDietaryTypes = emptyList(),
            equipmentAvailable = emptyList(), currentPainLevel = 0, additionalNotes = null
        )
    }
}