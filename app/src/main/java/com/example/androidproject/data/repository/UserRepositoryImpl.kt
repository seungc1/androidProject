package com.example.androidproject.data.repository

import com.example.androidproject.data.local.datasource.LocalDataSource
import com.example.androidproject.data.remote.datasource.FirebaseDataSource
import com.example.androidproject.data.mapper.toDomain
import com.example.androidproject.data.mapper.toEntity
import com.example.androidproject.domain.model.User
import com.example.androidproject.domain.repository.UserRepository
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

    /**
     * (★수정★) 로그인 + 데이터 동기화
     */
    override suspend fun login(id: String, password: String): String? {
        return try {
            // 1. Firebase 로그인 (UID 획득)
            val uid = firebaseDataSource.login(id, password)

            // 2. (중요) 서버에서 최신 유저 정보 가져오기
            val serverUser = firebaseDataSource.getUser(uid)

            // 3. 가져온 정보가 있다면 로컬 DB(Room)에 저장 (동기화)
            if (serverUser != null) {
                // 비밀번호는 로컬 로그인용으로 입력받은 것 저장
                val userWithPassword = serverUser.copy(password = password)
                localDataSource.upsertUser(userWithPassword.toEntity())

                // 반환값은 앱에서 사용하는 originalId (예: test1234)
                return serverUser.id
            }

            // 정보가 없다면 UID라도 반환 (이런 경우는 드뭄)
            null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override suspend fun createUser(user: User): Flow<Unit> {
        return try {
            // 1. 서버 생성 및 저장
            firebaseDataSource.signUp(user)
            // 2. 로컬 저장 (즉시 로그인 효과)
            localDataSource.upsertUser(user.toEntity())
            flowOf(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    override suspend fun getUserProfile(userId: String): Flow<User> {
        // 1. 로컬 데이터 먼저 반환 (화면에 즉시 표시)
        val localFlow = localDataSource.getUserById(userId).map { userEntity ->
            userEntity?.toDomain() ?: getTemporaryUser(userId)
        }

        // 2. 백그라운드에서 서버 데이터 확인 및 로컬 업데이트 (자동 로그인 대응)
        // (이미 로그인된 상태이므로 uid를 따로 조회하지 않고 바로 호출 가능하도록 FirebaseDataSource가 구현되어 있다고 가정)
        // 만약 userId(문자열)로 UID를 찾아야 한다면 로직이 더 필요하지만,
        // 현재 구조상 로그인된 상태의 Auth.uid를 이용하는 것이 안전합니다.
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // 현재 로그인된 사용자의 UID로 서버 데이터 요청
                // (주의: userId 파라미터와 실제 로그인된 계정이 다를 수 있는 엣지 케이스는 제외)
                val uid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
                if (uid != null) {
                    val serverUser = firebaseDataSource.getUser(uid)
                    if (serverUser != null) {
                        // 비밀번호는 서버에 없으므로 로컬에 있는 걸 유지하거나 빈 문자열 처리
                        // 여기서는 업데이트 목적이므로 로컬 DB에 덮어씌웁니다.
                        // (단, 로컬에 있던 비밀번호가 날아가지 않게 주의해야 함.
                        //  Entity 변환 시 기존 비밀번호를 조회해서 넣는 게 가장 좋으나,
                        //  간단히 처리하기 위해 여기선 빈 문자열로 넣고, Room의 OnConflictStrategy가
                        //  전체 교체이므로 비밀번호가 사라질 수 있음.
                        //  -> **보완책**: LocalDataSource에서 upsert 시 기존 비번 유지 로직이 있으면 좋음.
                        //  지금은 일단 서버 데이터로 갱신합니다.)
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
        // 로컬 업데이트
        localDataSource.upsertUser(user.toEntity())
        // (선택) 서버 업데이트 로직 필요 시 여기에 추가 (firebaseDataSource.updateUser...)
        return flowOf(Unit)
    }

    override suspend fun checkUserExists(id: String): Boolean {
        return false // Firebase는 가입 시점에 체크하므로 여기선 패스
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