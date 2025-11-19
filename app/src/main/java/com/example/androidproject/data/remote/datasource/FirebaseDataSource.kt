package com.example.androidproject.data.remote.datasource

import com.example.androidproject.domain.model.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
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
        val email = if (user.id.contains("@")) user.id else "${user.id}$DUMMY_DOMAIN"
        val authResult = auth.createUserWithEmailAndPassword(email, user.password).await()
        val uid = authResult.user?.uid ?: throw Exception("UID 생성 실패")
        saveUserToFirestore(uid, user)
        return uid
    }

    suspend fun login(id: String, password: String): String {
        val email = if (id.contains("@")) id else "$id$DUMMY_DOMAIN"
        val authResult = auth.signInWithEmailAndPassword(email, password).await()
        return authResult.user?.uid ?: throw Exception("로그인 실패")
    }

    /**
     * ★★★ [추가] 구글 ID 토큰으로 Firebase 인증 ★★★
     */
    suspend fun signInWithGoogle(idToken: String): String {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        val authResult = auth.signInWithCredential(credential).await()
        return authResult.user?.uid ?: throw Exception("구글 로그인 실패")
    }

    suspend fun updateUser(user: User) {
        val uid = getUid(user.id)
        saveUserToFirestore(uid, user)
    }

    // 유저 정보 저장 로직 중복 제거를 위한 헬퍼 함수
    private suspend fun saveUserToFirestore(uid: String, user: User) {
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
    }

    suspend fun getUser(uid: String): User? {
        val snapshot = firestore.collection("users").document(uid).get().await()
        if (!snapshot.exists()) return null

        val data = snapshot.data ?: return null

        // Firestore Map -> User 객체 수동 변환
        return try {
            User(
                id = data["originalId"] as? String ?: "",
                password = "",
                name = data["name"] as? String ?: "",
                gender = data["gender"] as? String ?: "",
                age = (data["age"] as? Number)?.toInt() ?: 0,
                heightCm = (data["heightCm"] as? Number)?.toInt() ?: 0,
                weightKg = (data["weightKg"] as? Double) ?: 0.0,
                activityLevel = data["activityLevel"] as? String ?: "",
                fitnessGoal = data["fitnessGoal"] as? String ?: "",
                allergyInfo = (data["allergyInfo"] as? List<String>) ?: emptyList(),
                preferredDietType = data["preferredDietType"] as? String ?: "",
                preferredDietaryTypes = (data["preferredDietaryTypes"] as? List<String>) ?: emptyList(),
                equipmentAvailable = (data["equipmentAvailable"] as? List<String>) ?: emptyList(),
                currentPainLevel = (data["currentPainLevel"] as? Number)?.toInt() ?: 0,
                additionalNotes = data["additionalNotes"] as? String,
                targetCalories = (data["targetCalories"] as? Number)?.toInt(),
                currentInjuryId = data["currentInjuryId"] as? String
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * ★★★ [수정] 서버 권한/네트워크 오류에 대한 try-catch 추가 (오류를 다시 던짐) ★★★
     */
    suspend fun checkUserExistsRemote(username: String): Boolean {
        return try {
            // 'users' 컬렉션에서 'originalId'가 일치하는 문서가 있는지 쿼리
            val snapshot = firestore.collection("users")
                .whereEqualTo("originalId", username)
                .limit(1)
                .get()
                .await()
            return !snapshot.isEmpty
        } catch (e: Exception) {
            // FirebaseFirestoreException (PERMISSION_DENIED 등) 발생 시, 오류를 던져 ViewModel에서 NetworkError로 처리하게 함
            throw e
        }
    }


    // ------------------------------------------------------------------------
    // 2. 부상 정보 (Injury)
    // ------------------------------------------------------------------------

    suspend fun upsertInjury(userId: String, injury: Injury) {
        val uid = getUid(userId)
        val data = hashMapOf(
            "id" to injury.id,
            "name" to injury.name,
            "bodyPart" to injury.bodyPart,
            "severity" to injury.severity,
            "description" to injury.description
        )
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
            "dateTime" to session.dateTime,
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
                    dateTime = doc.getDate("dateTime") ?: Date(),
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
        return snapshot.documents.mapNotNull { doc ->
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

        workouts.forEach { workout ->
            val docRef = collectionRef.document(workout.scheduledDate.replace(" ", "_"))

            val data = hashMapOf(
                "scheduledDate" to workout.scheduledDate,
                "exercises" to workout.exercises
            )
            batch.set(docRef, data)
        }
        batch.commit().await()
    }

    // [삭제됨] 중복된 clearWorkouts 함수 제거

    suspend fun getWorkouts(userId: String): List<ScheduledWorkout> {
        val uid = getUid(userId)
        val snapshot = getUserDocRef(uid).collection("scheduled_workouts").get().await()

        return snapshot.documents.mapNotNull { doc ->
            val date = doc.getString("scheduledDate") ?: return@mapNotNull null
            val exercisesList = doc.get("exercises") as? List<Map<String, Any>> ?: emptyList()

            val exercises = exercisesList.map { map ->
                ExerciseRecommendation(
                    name = map["name"] as? String ?: "",
                    description = map["description"] as? String ?: "",
                    bodyPart = map["bodyPart"] as? String ?: "",
                    sets = (map["sets"] as? Number)?.toInt() ?: 0,
                    reps = (map["reps"] as? Number)?.toInt() ?: 0,
                    difficulty = map["difficulty"] as? String ?: "",
                    aiRecommendationReason = map["aiRecommendationReason"] as? String,
                    imageUrl = map["imageUrl"] as? String
                )
            }
            ScheduledWorkout(date, exercises)
        }
    }

    // ★★★ [유지] 하나만 남긴 clearWorkouts 함수 ★★★
    // [디버그 로그 추가]
    suspend fun clearWorkouts(userId: String) {
        android.util.Log.d("DEBUG_DELETE", "FirebaseDataSource: Firestore에서 루틴 삭제 시작 (User: $userId)")
        val uid = getUid(userId)
        val collectionRef = getUserDocRef(uid).collection("scheduled_workouts")
        val snapshot = collectionRef.get().await()

        android.util.Log.d("DEBUG_DELETE", "FirebaseDataSource: 삭제할 문서 개수: ${snapshot.documents.size}")

        val batch = firestore.batch()
        snapshot.documents.forEach { batch.delete(it.reference) }
        batch.commit().await()

        android.util.Log.d("DEBUG_DELETE", "FirebaseDataSource: Firestore 삭제 완료")
    }

// -----------------------------------------------------------------------
// 6. AI 분석 캐시 (Analysis Cache)
// ------------------------------------------------------------------------

    /**
     * 주간 분석 결과를 Firebase 캐시에 저장합니다.
     * @param cacheId 주간 분석 기간의 시작 날짜 (YYYYMMDD)
     */
    suspend fun saveAnalysisCache(userId: String, cacheId: String, result: AIAnalysisResult) {
        val uid = getUid(userId)
        val data = hashMapOf(
            "summary" to result.summary,
            "strengths" to result.strengths,
            "areasForImprovement" to result.areasForImprovement,
            "personalizedTips" to result.personalizedTips,
            "nextStepsRecommendation" to result.nextStepsRecommendation,
            "disclaimer" to result.disclaimer,
            "timestamp" to Date() // 캐시 저장 시간
        )
        getUserDocRef(uid).collection("analysis_cache").document(cacheId).set(data).await()
    }

    /**
     * Firebase 캐시에서 주간 분석 결과를 가져옵니다. (주간 분석 시작 날짜 기준)
     */
    suspend fun getAnalysisCache(userId: String, cacheId: String): AIAnalysisResult? {
        val uid = getUid(userId)
        val snapshot = getUserDocRef(uid).collection("analysis_cache").document(cacheId).get().await()

        if (!snapshot.exists()) return null

        val data = snapshot.data ?: return null

        @Suppress("UNCHECKED_CAST")
        return AIAnalysisResult(
            summary = data["summary"] as? String ?: "",
            strengths = data["strengths"] as? List<String> ?: emptyList(),
            areasForImprovement = data["areasForImprovement"] as? List<String> ?: emptyList(),
            personalizedTips = data["personalizedTips"] as? List<String> ?: emptyList(),
            nextStepsRecommendation = data["nextStepsRecommendation"] as? String ?: "",
            disclaimer = data["disclaimer"] as? String ?: ""
        )
    }
    // ------------------------------------------------------------------------
    // Helper Methods
    // ------------------------------------------------------------------------

    private fun getUid(userId: String): String {
        val currentUser = auth.currentUser
        if (currentUser != null) return currentUser.uid

        throw Exception("사용자가 로그인되어 있지 않습니다.")
    }
    suspend fun getInjury(injuryId: String): Injury? {
        val currentUser = auth.currentUser ?: return null
        val snapshot = firestore.collection("users").document(currentUser.uid)
            .collection("injuries").document(injuryId).get().await()

        if (!snapshot.exists()) return null

        return Injury(
            id = snapshot.getString("id") ?: "",
            name = snapshot.getString("name") ?: "",
            bodyPart = snapshot.getString("bodyPart") ?: "",
            severity = snapshot.getString("severity") ?: "",
            description = snapshot.getString("description") ?: ""
        )
    }

    private fun getUserDocRef(uid: String) = firestore.collection("users").document(uid)
}