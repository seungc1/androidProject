package com.example.androidproject.data.remote.datasource

import com.example.androidproject.domain.model.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import java.util.Date
import javax.inject.Inject

class FirebaseDataSource @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {
    // (★핵심★) 가짜 도메인 상수
    private val DUMMY_DOMAIN = "@rehabai.com"

    // ------------------------------------------------------------------------
    // 1. 인증 (Auth) & 유저 (User)
    // ------------------------------------------------------------------------

    suspend fun signUp(user: User): String {
        // 1. 아이디를 이메일 형식으로 변환
        val email = if (user.id.contains("@")) user.id else "${user.id}$DUMMY_DOMAIN"

        // 2. Firebase Auth 생성
        val authResult = auth.createUserWithEmailAndPassword(email, user.password).await()
        val uid = authResult.user?.uid ?: throw Exception("UID 생성 실패")

        // 3. Firestore에 유저 정보 저장 (User 객체 -> Map)
        val userMap = hashMapOf(
            "uid" to uid,
            "originalId" to user.id,
            "name" to user.name,
            "gender" to user.gender,
            "age" to user.age,
            "heightCm" to user.heightCm,
            "weightKg" to user.weightKg,
            "activityLevel" to user.activityLevel,
            "fitnessGoal" to user.fitnessGoal,
            "allergyInfo" to user.allergyInfo, // List는 Firestore 배열로 저장됨
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

    suspend fun login(id: String, password: String): String {
        val email = if (id.contains("@")) id else "$id$DUMMY_DOMAIN"
        val authResult = auth.signInWithEmailAndPassword(email, password).await()
        return authResult.user?.uid ?: throw Exception("로그인 실패")
    }

    // ------------------------------------------------------------------------
    // 2. 부상 정보 (Injury)
    // ------------------------------------------------------------------------

    suspend fun upsertInjury(userId: String, injury: Injury) {
        val uid = getUid(userId) // 실제 userId(문자열)를 이용해 UID를 찾거나, 현재 로그인한 UID 사용
        val data = hashMapOf(
            "id" to injury.id,
            "name" to injury.name,
            "bodyPart" to injury.bodyPart,
            "severity" to injury.severity,
            "description" to injury.description
        )
        // users/{uid}/injuries/{injuryId} 경로에 저장
        getUserDocRef(uid).collection("injuries").document(injury.id).set(data).await()
    }

    suspend fun getInjuries(userId: String): List<Injury> {
        val uid = getUid(userId)
        val snapshot = getUserDocRef(uid).collection("injuries").get().await()
        return snapshot.documents.mapNotNull { doc ->
            Injury(
                id = doc.getString("id") ?: return@mapNotNull null,
                name = doc.getString("name") ?: "",
                bodyPart = doc.getString("bodyPart") ?: "",
                severity = doc.getString("severity") ?: "",
                description = doc.getString("description") ?: ""
            )
        }
    }

    // ------------------------------------------------------------------------
    // 3. 재활 운동 기록 (RehabSession)
    // ------------------------------------------------------------------------

    suspend fun addRehabSession(session: RehabSession) {
        val uid = getUid(session.userId)
        val data = hashMapOf(
            "id" to session.id,
            "userId" to session.userId,
            "exerciseId" to session.exerciseId,
            "dateTime" to session.dateTime, // Firestore는 Date 타입을 Timestamp로 저장함
            "sets" to session.sets,
            "reps" to session.reps,
            "durationMinutes" to session.durationMinutes,
            "userRating" to session.userRating,
            "notes" to session.notes
        )
        getUserDocRef(uid).collection("rehab_sessions").document(session.id).set(data).await()
    }

    suspend fun getRehabHistory(userId: String): List<RehabSession> {
        val uid = getUid(userId)
        val snapshot = getUserDocRef(uid).collection("rehab_sessions")
            .orderBy("dateTime", Query.Direction.DESCENDING)
            .get().await()

        return snapshot.documents.mapNotNull { doc ->
            try {
                RehabSession(
                    id = doc.getString("id") ?: "",
                    userId = doc.getString("userId") ?: "",
                    exerciseId = doc.getString("exerciseId") ?: "",
                    dateTime = doc.getDate("dateTime") ?: Date(), // Timestamp -> Date 변환
                    sets = doc.getLong("sets")?.toInt() ?: 0,
                    reps = doc.getLong("reps")?.toInt() ?: 0,
                    durationMinutes = doc.getLong("durationMinutes")?.toInt(),
                    userRating = doc.getLong("userRating")?.toInt(),
                    notes = doc.getString("notes")
                )
            } catch (e: Exception) { null }
        }
    }

    suspend fun getRehabSessionsBetween(userId: String, startDate: Date, endDate: Date): List<RehabSession> {
        val uid = getUid(userId)
        val snapshot = getUserDocRef(uid).collection("rehab_sessions")
            .whereGreaterThanOrEqualTo("dateTime", startDate)
            .whereLessThanOrEqualTo("dateTime", endDate)
            .get().await()
        // (매핑 로직은 위와 동일하므로 생략 또는 함수로 분리 가능)
        return snapshot.documents.mapNotNull { doc ->
            // ... 위와 동일한 매핑 ...
            RehabSession(
                id = doc.getString("id") ?: "",
                userId = doc.getString("userId") ?: "",
                exerciseId = doc.getString("exerciseId") ?: "",
                dateTime = doc.getDate("dateTime") ?: Date(),
                sets = doc.getLong("sets")?.toInt() ?: 0,
                reps = doc.getLong("reps")?.toInt() ?: 0,
                durationMinutes = doc.getLong("durationMinutes")?.toInt(),
                userRating = doc.getLong("userRating")?.toInt(),
                notes = doc.getString("notes")
            )
        }
    }

    // ------------------------------------------------------------------------
    // 4. 식단 기록 (DietSession)
    // ------------------------------------------------------------------------

    suspend fun addDietSession(session: DietSession) {
        val uid = getUid(session.userId)
        val data = hashMapOf(
            "id" to session.id,
            "userId" to session.userId,
            "dietId" to session.dietId,
            "dateTime" to session.dateTime,
            "actualQuantity" to session.actualQuantity,
            "actualUnit" to session.actualUnit,
            "userSatisfaction" to session.userSatisfaction,
            "notes" to session.notes
        )
        getUserDocRef(uid).collection("diet_sessions").document(session.id).set(data).await()
    }

    suspend fun getDietHistory(userId: String): List<DietSession> {
        val uid = getUid(userId)
        val snapshot = getUserDocRef(uid).collection("diet_sessions")
            .orderBy("dateTime", Query.Direction.DESCENDING)
            .get().await()

        return snapshot.documents.mapNotNull { doc ->
            DietSession(
                id = doc.getString("id") ?: "",
                userId = doc.getString("userId") ?: "",
                dietId = doc.getString("dietId") ?: "",
                dateTime = doc.getDate("dateTime") ?: Date(),
                actualQuantity = doc.getDouble("actualQuantity") ?: 0.0,
                actualUnit = doc.getString("actualUnit") ?: "",
                userSatisfaction = doc.getLong("userSatisfaction")?.toInt(),
                notes = doc.getString("notes")
            )
        }
    }

    // ------------------------------------------------------------------------
    // 5. AI 루틴 (ScheduledWorkout)
    // ------------------------------------------------------------------------

    suspend fun upsertWorkouts(userId: String, workouts: List<ScheduledWorkout>) {
        val uid = getUid(userId)
        val batch = firestore.batch()
        val collectionRef = getUserDocRef(uid).collection("scheduled_workouts")

        // 기존 루틴 삭제 (선택 사항: 덮어쓰기 정책에 따라 다름)
        // 여기선 덮어쓰기 방식으로 각 날짜별 문서를 생성
        workouts.forEach { workout ->
            // 날짜 문자열 등을 문서 ID로 사용하거나 자동 ID 사용
            // 여기선 "scheduledDate"를 문서 ID로 사용해 중복 방지 추천 (예: "2025-11-17")
            // 하지만 날짜 포맷이 한글일 수 있으므로 안전하게 자동 ID 사용하거나 인덱스 사용
            val docRef = collectionRef.document(workout.scheduledDate.replace(" ", "_")) // 간단한 ID 생성

            val data = hashMapOf(
                "scheduledDate" to workout.scheduledDate,
                "exercises" to workout.exercises // List<ExerciseRecommendation>은 Firestore가 자동으로 배열(Map)로 변환해줌
            )
            batch.set(docRef, data)
        }
        batch.commit().await()
    }

    suspend fun getWorkouts(userId: String): List<ScheduledWorkout> {
        val uid = getUid(userId)
        val snapshot = getUserDocRef(uid).collection("scheduled_workouts").get().await()

        return snapshot.documents.mapNotNull { doc ->
            val date = doc.getString("scheduledDate") ?: return@mapNotNull null
            // Firestore 배열 -> List<Map> -> List<ExerciseRecommendation> 변환
            val exercisesList = doc.get("exercises") as? List<Map<String, Any>> ?: emptyList()

            val exercises = exercisesList.map { map ->
                ExerciseRecommendation(
                    name = map["name"] as? String ?: "",
                    description = map["description"] as? String ?: "",
                    bodyPart = map["bodyPart"] as? String ?: "",
                    sets = (map["sets"] as? Long)?.toInt() ?: 0,
                    reps = (map["reps"] as? Long)?.toInt() ?: 0,
                    difficulty = map["difficulty"] as? String ?: "",
                    aiRecommendationReason = map["aiRecommendationReason"] as? String,
                    imageUrl = map["imageUrl"] as? String
                )
            }
            ScheduledWorkout(date, exercises)
        }
    }

    suspend fun clearWorkouts(userId: String) {
        val uid = getUid(userId)
        val collectionRef = getUserDocRef(uid).collection("scheduled_workouts")
        val snapshot = collectionRef.get().await()
        val batch = firestore.batch()
        snapshot.documents.forEach { batch.delete(it.reference) }
        batch.commit().await()
    }

    suspend fun getUser(uid: String): User? {
        val snapshot = firestore.collection("users").document(uid).get().await()
        if (!snapshot.exists()) return null

        val data = snapshot.data ?: return null

        // Firestore Map -> User 객체 수동 변환
        return try {
            User(
                id = data["originalId"] as? String ?: "",
                password = "", // 비밀번호는 서버에서 가져오지 않음 (보안)
                name = data["name"] as? String ?: "",
                gender = data["gender"] as? String ?: "",
                age = (data["age"] as? Long)?.toInt() ?: 0,
                heightCm = (data["heightCm"] as? Long)?.toInt() ?: 0,
                weightKg = (data["weightKg"] as? Double) ?: 0.0,
                activityLevel = data["activityLevel"] as? String ?: "",
                fitnessGoal = data["fitnessGoal"] as? String ?: "",
                allergyInfo = (data["allergyInfo"] as? List<String>) ?: emptyList(),
                preferredDietType = data["preferredDietType"] as? String ?: "",
                preferredDietaryTypes = (data["preferredDietaryTypes"] as? List<String>) ?: emptyList(),
                equipmentAvailable = (data["equipmentAvailable"] as? List<String>) ?: emptyList(),
                currentPainLevel = (data["currentPainLevel"] as? Long)?.toInt() ?: 0,
                additionalNotes = data["additionalNotes"] as? String,
                targetCalories = (data["targetCalories"] as? Long)?.toInt(),
                currentInjuryId = data["currentInjuryId"] as? String
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }


    // ------------------------------------------------------------------------
    // Helper Methods
    // ------------------------------------------------------------------------

    /**
     * 현재 로그인된 사용자의 UID를 가져오거나,
     * 전달받은 userId(우리의 커스텀 ID)를 이용해 Firestore에서 UID를 조회하는 로직이 필요할 수 있음.
     * * (여기서는 간단히 현재 로그인된 유저의 UID를 반환하도록 구현)
     * 만약 다른 기기에서 로그인했다면 auth.currentUser가 null이 아닐 것임.
     */
    private fun getUid(userId: String): String {
        // 1. 현재 로그인된 유저가 있으면 그 UID 사용 (가장 정확)
        val currentUser = auth.currentUser
        if (currentUser != null) return currentUser.uid

        // 2. 만약 비로그인 상태에서 이 함수가 호출된다면 예외 발생
        // (실제 앱에서는 로그인 후에만 데이터 접근하므로 이럴 일은 적음)
        throw Exception("사용자가 로그인되어 있지 않습니다.")
    }

    private fun getUserDocRef(uid: String) = firestore.collection("users").document(uid)
}