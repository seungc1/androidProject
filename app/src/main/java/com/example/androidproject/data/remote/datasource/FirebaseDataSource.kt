package com.example.androidproject.data.remote.datasource

import com.example.androidproject.domain.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirebaseDataSource @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {
    // (★핵심★) 가짜 도메인 상수
    private val DUMMY_DOMAIN = "@rehabai.com"

    // 회원가입 (Authentication + Firestore 저장)
    suspend fun signUp(user: User): String {
        // 1. 아이디를 이메일 형식으로 변환 (예: "test" -> "test@rehabai.com")
        val email = if (user.id.contains("@")) user.id else "${user.id}$DUMMY_DOMAIN"

        // 2. Firebase Auth 생성
        val authResult = auth.createUserWithEmailAndPassword(email, user.password).await()
        val uid = authResult.user?.uid ?: throw Exception("UID 생성 실패")

        // 3. Firestore에 유저 정보 저장 (User 객체를 Map으로 변환)
        val userMap = hashMapOf(
            "uid" to uid,
            "originalId" to user.id, // 원래 아이디도 저장해둠
            "name" to user.name,
            "gender" to user.gender,
            "age" to user.age,
            "heightCm" to user.heightCm,
            "weightKg" to user.weightKg,
            "activityLevel" to user.activityLevel,
            "fitnessGoal" to user.fitnessGoal,
            "allergyInfo" to user.allergyInfo,
            "preferredDietType" to user.preferredDietType,
            "preferredDietaryTypes" to user.preferredDietaryTypes,
            "equipmentAvailable" to user.equipmentAvailable,
            "currentPainLevel" to user.currentPainLevel,
            "additionalNotes" to user.additionalNotes,
            "targetCalories" to user.targetCalories,
            "currentInjuryId" to user.currentInjuryId
        )

        firestore.collection("users").document(uid).set(userMap).await()
        return uid
    }

    // 로그인
    suspend fun login(id: String, password: String): String {
        // 1. 아이디를 이메일 형식으로 변환
        val email = if (id.contains("@")) id else "$id$DUMMY_DOMAIN"

        // 2. Firebase Auth 로그인 시도
        val authResult = auth.signInWithEmailAndPassword(email, password).await()
        return authResult.user?.uid ?: throw Exception("로그인 실패")
    }

    // (참고) Firestore에서 유저 데이터 가져오기 - 나중에 필요하면 사용
    suspend fun getUserProfile(uid: String): Map<String, Any>? {
        val snapshot = firestore.collection("users").document(uid).get().await()
        return snapshot.data
    }
}