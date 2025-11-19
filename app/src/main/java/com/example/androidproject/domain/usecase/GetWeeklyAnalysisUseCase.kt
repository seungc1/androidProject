package com.example.androidproject.domain.usecase

import com.example.androidproject.data.remote.datasource.FirebaseDataSource // 🚨 (1) Import 추가
import com.example.androidproject.domain.model.AIAnalysisResult
import com.example.androidproject.domain.model.RehabData
import com.example.androidproject.domain.model.User
import com.example.androidproject.domain.repository.AIApiRepository
import com.example.androidproject.domain.repository.DietSessionRepository
import com.example.androidproject.domain.repository.RehabSessionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow // 🚨 (2) Flow 빌더 사용을 위해 import
import kotlinx.coroutines.flow.catch
import java.util.Calendar
import java.util.Date
import javax.inject.Inject
import java.text.SimpleDateFormat // 🚨 (3) SimpleDateFormat Import
import java.util.Locale

/**
 * 지난 7일간의 기록을 수집하여 AI에게 분석을 요청하는 Use Case (★ 캐싱 로직 추가 ★)
 */
class GetWeeklyAnalysisUseCase @Inject constructor(
    private val rehabSessionRepository: RehabSessionRepository,
    private val dietSessionRepository: DietSessionRepository,
    private val aiApiRepository: AIApiRepository,
    private val firebaseDataSource: FirebaseDataSource
) {
    suspend operator fun invoke(user: User): Flow<AIAnalysisResult> = flow {

        val calendar = Calendar.getInstance()
        val endDate = calendar.time
        calendar.add(Calendar.DAY_OF_YEAR, -7)
        val startDate = calendar.time

        // Cache ID: 분석 기간의 시작 날짜를 기준으로 YYYYMMDD 형식으로 생성
        val dateFormat = SimpleDateFormat("yyyyMMdd", Locale.US)
        val cacheId = dateFormat.format(startDate)

        // 2. 캐시 확인 (Firebase)
        val cachedResult = firebaseDataSource.getAnalysisCache(user.id, cacheId)
        if (cachedResult != null) {
            android.util.Log.d("AnalysisCache", "Cache HIT for week $cacheId. Returning cached result.")
            emit(cachedResult)
            return@flow // 캐시가 있으면 API 호출 없이 종료
        }
        android.util.Log.d("AnalysisCache", "Cache MISS for week $cacheId. Calling AI API.")


        // 3. 캐시가 없는 경우, AI 분석에 필요한 데이터 준비
        val rehabSessions = rehabSessionRepository.getRehabSessionsBetween(user.id, startDate, endDate).first()
        val dietSessions = dietSessionRepository.getDietSessionsBetween(user.id, startDate, endDate).first()

        val rehabData = RehabData(
            userId = user.id,
            userProfile = user,
            pastRehabSessions = rehabSessions,
            pastDietSessions = dietSessions,
            currentPainLevel = user.currentPainLevel
        )

        // 4. AI Repository에 분석 요청
        aiApiRepository.analyzeRehabProgress(rehabData)
            .catch { e ->
                android.util.Log.e("AnalysisCache", "AI API error: ${e.message}")
                // 오류 시 임시 오류 객체 반환
                emit(AIAnalysisResult("분석 실패: ${e.message}", emptyList(), emptyList(), emptyList(), "오류", "오류 발생"))
            }
            .collect { aiResult ->
                // 5. AI 결과 수신 후 캐시에 저장
                firebaseDataSource.saveAnalysisCache(user.id, cacheId, aiResult)
                android.util.Log.d("AnalysisCache", "AI Result saved to cache: $cacheId")

                // 6. 결과 반환
                emit(aiResult)
            }
    }
}