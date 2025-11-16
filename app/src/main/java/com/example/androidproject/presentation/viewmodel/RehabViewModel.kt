package com.example.androidproject.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.androidproject.domain.model.Diet
import com.example.androidproject.domain.model.DietRecommendation
import com.example.androidproject.domain.model.Exercise
import com.example.androidproject.domain.model.ExerciseRecommendation
import com.example.androidproject.domain.model.Injury
import com.example.androidproject.domain.model.User
import com.example.androidproject.domain.model.DietSession
import com.example.androidproject.domain.model.RehabSession
import com.example.androidproject.domain.usecase.GetAIRecommendationUseCase
import com.example.androidproject.presentation.history.HistoryItem
import com.example.androidproject.presentation.main.MainUiState
import com.example.androidproject.presentation.main.TodayExercise
import com.example.androidproject.domain.model.AIAnalysisResult
import com.example.androidproject.domain.model.ScheduledWorkout
import com.example.androidproject.domain.usecase.GetWeeklyAnalysisUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Calendar
import java.util.Locale
import java.util.UUID
import javax.inject.Inject

data class HistoryUiState(
    val isLoading: Boolean = false,
    val historyItems: List<HistoryItem> = emptyList(),
    val errorMessage: String? = null,
    val isAnalyzing: Boolean = false,
    val analysisResult: AIAnalysisResult? = null
)
data class DietDetailUiState(
    val isLoading: Boolean = false,
    val diet: Diet? = null,
    val alternatives: List<String> = emptyList(),
    val errorMessage: String? = null
)


@HiltViewModel
class RehabViewModel @Inject constructor(
    private val getAIRecommendationUseCase: GetAIRecommendationUseCase,
    private val getWeeklyAnalysisUseCase: GetWeeklyAnalysisUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    private val _historyUiState = MutableStateFlow(HistoryUiState())
    val historyUiState: StateFlow<HistoryUiState> = _historyUiState.asStateFlow()

    private val _dietDetailState = MutableStateFlow(DietDetailUiState())
    val dietDetailState: StateFlow<DietDetailUiState> = _dietDetailState.asStateFlow()

    lateinit var dummyUser: User
    lateinit var dummyInjury: Injury

    init {
        loadMainDashboardData(forceReload = false)
    }

    /**
     * (★수정★) AI 루틴 로드 로직
     * 1. forceReload=true (프로필 변경) -> 무조건 AI 호출
     * 2. forceReload=false (일반 로드)
     * 2a. 기존 루틴 O, '오늘 운동' O -> 기존 루틴 사용
     * 2b. 기존 루틴 X, 또는 '오늘 운동' X (루틴 만료) -> AI 호출 (버그 수정)
     */
    private fun loadMainDashboardData(forceReload: Boolean) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            // 1. (Check 1) 강제 리로드가 아니고, 기존 루틴이 있다면
            if (!forceReload && _uiState.value.fullRoutine.isNotEmpty()) {
                // 2. (Check 2) '오늘의 운동'을 필터링해 봅니다.
                val todayExercises = filterTodayExercises(_uiState.value.fullRoutine)

                // 3. (Check 3) '오늘의 운동'이 있다면 (루틴이 유효함)
                if (todayExercises.isNotEmpty()) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            todayExercises = todayExercises
                        )
                    }
                    return@launch // AI 호출 없이 함수 종료
                }
                // (만약 'todayExercises'가 비어있다면, 루틴이 만료된 것이므로
                // 아래 4단계로 넘어가서 AI를 호출합니다.)
            }

            // 4. (AI 호출)
            // A) forceReload = true (프로필 수정 시)
            // B) fullRoutine이 비어있을 때 (첫 실행 시)
            // C) '오늘의 운동'이 비어있을 때 (루틴이 만료된 경우)
            try {
                // (유지) 더미 데이터 생성
                dummyUser = User(
                    id = "user01", name = "김재활", gender = "남성", age = 30,
                    heightCm = 175, weightKg = 70.5, activityLevel = "활동적",
                    fitnessGoal = "근육 증가", allergyInfo = listOf("땅콩", "새우"),
                    preferredDietType = "일반", targetCalories = 2500,
                    currentInjuryId = "injury01",
                    preferredDietaryTypes = listOf("일반식", "저염식"),
                    equipmentAvailable = listOf("덤벨", "밴드"),
                    currentPainLevel = 4,
                    additionalNotes = "부상 회복에 집중하고 싶습니다. 특히 손목에 부담이 가지 않는 운동을 선호합니다."
                )
                dummyInjury = Injury(
                    id = "injury01", name = "손목 염좌", bodyPart = "손목",
                    severity = "경미", description = "가벼운 통증이 있는 상태"
                )

                // 5. (유지) GetAIRecommendationUseCase 호출
                getAIRecommendationUseCase(dummyUser.id, dummyInjury)
                    .catch { e ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                userName = dummyUser.name,
                                errorMessage = "AI 루틴 생성 중 오류 발생: ${e.message}"
                            )
                        }
                    }
                    .collect { aiResult ->
                        // 6. (유지) AI가 반환한 '전체 루틴'과 '오늘의 운동'을 UI 상태에 저장
                        _uiState.value = MainUiState(
                            isLoading = false,
                            userName = dummyUser.name,
                            currentInjuryName = dummyInjury.name,
                            currentInjuryArea = dummyInjury.bodyPart,
                            fullRoutine = aiResult.scheduledWorkouts,
                            todayExercises = filterTodayExercises(aiResult.scheduledWorkouts),
                            recommendedDiets = aiResult.recommendedDiets.toDietList(),
                            errorMessage = null
                        )
                    }

            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "데이터 로드 실패: ${e.message}") }
            }
        }
    }

    /**
     * (유지) '전체 루틴'에서 '오늘 날짜' 필터링
     */
    private fun filterTodayExercises(fullRoutine: List<ScheduledWorkout>): List<TodayExercise> {
        val todayDateString = SimpleDateFormat("M월 d일 (E)", Locale.KOREA).format(Date())

        val todayWorkout = fullRoutine.find {
            it.scheduledDate.contains(todayDateString)
        }

        return todayWorkout?.exercises?.toTodayExerciseList() ?: emptyList()
    }


    /**
     * (유지) ExerciseRecommendation -> TodayExercise 변환 매퍼
     */
    private fun List<ExerciseRecommendation>.toTodayExerciseList(): List<TodayExercise> {
        return this.map { rec ->
            val exercise = Exercise(
                id = rec.name,
                name = rec.name,
                description = rec.description,
                bodyPart = rec.bodyPart,
                difficulty = rec.difficulty,
                videoUrl = rec.imageUrl,
                precautions = null,
                sets = rec.sets,
                reps = rec.reps,
                aiRecommendationReason = rec.aiRecommendationReason
            )
            TodayExercise(
                exercise = exercise,
                isCompleted = false
            )
        }
    }

    /**
     * (유지) DietRecommendation -> Diet 변환 매퍼 (null-safe 처리 포함)
     */
    private fun List<DietRecommendation>.toDietList(): List<Diet> {
        return this.map { rec ->
            Diet(
                id = rec.foodItems?.joinToString() ?: UUID.randomUUID().toString(),
                mealType = rec.mealType,
                foodName = rec.foodItems?.joinToString(", ") ?: "이름 없는 식단",
                quantity = 1.0,
                unit = "인분",
                calorie = rec.calories?.toInt() ?: 0,
                protein = rec.proteinGrams ?: 0.0,
                fat = rec.fats ?: 0.0,
                carbs = rec.carbs ?: 0.0,
                ingredients = rec.ingredients ?: emptyList(),
                preparationTips = null,
                aiRecommendationReason = rec.aiRecommendationReason
            )
        }
    }


    // --- (이하 '홈', '기록', '식단 상세' 기능은 수정 없음) ---

    private fun setExerciseCompleted(exerciseId: String, isCompleted: Boolean) {
        _uiState.update { currentState ->
            val updatedExercises = currentState.todayExercises.map {
                if (it.exercise.id == exerciseId) {
                    it.copy(isCompleted = !it.isCompleted)
                } else {
                    it
                }
            }
            currentState.copy(todayExercises = updatedExercises)
        }
    }

    fun saveRehabSessionDetails(exerciseId: String, rating: Int, notes: String) {
        viewModelScope.launch {
            val _session = RehabSession(
                id = UUID.randomUUID().toString(),
                userId = "user01",
                exerciseId = exerciseId,
                dateTime = Date(),
                sets = 3, reps = 10, durationMinutes = 15,
                notes = notes,
                userRating = rating
            )
            // (실제 연동 시 주석 해제)
            // addRehabSessionUseCase(_session)

            setExerciseCompleted(exerciseId, true)
        }
    }

    fun clearErrorMessage() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun loadHistory(date: Date) {
        viewModelScope.launch {
            _historyUiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                kotlinx.coroutines.delay(500)
                val dummyHistoryItems = listOf(
                    HistoryItem.Exercise(
                        RehabSession(
                            id = "session001", userId = "user01", exerciseId = "ex001",
                            dateTime = date,
                            sets = 3, reps = 10, durationMinutes = 15,
                            notes = "조금 아팠음",
                            userRating = 3
                        )
                    ),
                    HistoryItem.Diet(
                        DietSession(
                            id = "dietSession001", userId = "user01", dietId = "d001",
                            dateTime = date,
                            actualQuantity = 1.0, actualUnit = "그릇",
                            userSatisfaction = 5
                        )
                    )
                )
                _historyUiState.update {
                    it.copy(isLoading = false, historyItems = dummyHistoryItems)
                }
            } catch (e: Exception) {
                _historyUiState.update {
                    it.copy(isLoading = false, errorMessage = "기록 로드 실패: ${e.message}")
                }
            }
        }
    }

    fun clearHistoryErrorMessage() {
        _historyUiState.update { it.copy(errorMessage = null) }
    }

    fun fetchWeeklyAnalysis() {
        viewModelScope.launch {
            _historyUiState.update { it.copy(isAnalyzing = true, analysisResult = null) }
            try {
                getWeeklyAnalysisUseCase(dummyUser)
                    .catch { e ->
                        _historyUiState.update {
                            it.copy(
                                isAnalyzing = false,
                                analysisResult = createErrorAnalysisResult("AI 분석 실패: ${e.message}")
                            )
                        }
                    }
                    .collect { result ->
                        _historyUiState.update {
                            it.copy(isAnalyzing = false, analysisResult = result)
                        }
                    }
            } catch (e: Exception) {
                _historyUiState.update {
                    it.copy(
                        isAnalyzing = false,
                        analysisResult = createErrorAnalysisResult("분석 준비 중 오류: ${e.message}")
                    )
                }
            }
        }
    }

    private fun createErrorAnalysisResult(message: String): AIAnalysisResult {
        return AIAnalysisResult(
            summary = message,
            strengths = emptyList(),
            areasForImprovement = emptyList(),
            personalizedTips = emptyList(),
            nextStepsRecommendation = "오류로 인해 분석을 완료할 수 없습니다.",
            disclaimer = "오류 발생"
        )
    }

    fun loadDietDetails(dietId: String) {
        viewModelScope.launch {
            _dietDetailState.update { it.copy(isLoading = true, errorMessage = null, alternatives = emptyList()) }
            try {
                val currentUiState = _uiState.first()
                val foundDiet = currentUiState.recommendedDiets.find { it.id == dietId }

                if (foundDiet == null) {
                    throw Exception("선택한 식단(ID: $dietId)을 찾을 수 없습니다.")
                }
                _dietDetailState.update { it.copy(diet = foundDiet) }
                kotlinx.coroutines.delay(500)
                val dummyAlternatives = when (dietId) {
                    "d001" -> listOf("대체: 그릭 요거트와 견과류", "대체: 통밀빵과 아보카도")
                    "d002" -> listOf("대체: 두부 샐러드", "대체: 연어 스테이크와 채소 구이")
                    else -> listOf("추천할 만한 대체 식품이 없습니다.")
                }
                _dietDetailState.update {
                    it.copy(isLoading = false, alternatives = dummyAlternatives)
                }
            } catch (e: Exception) {
                _dietDetailState.update {
                    it.copy(isLoading = false, errorMessage = "대체 식품 로드 실패: ${e.message}")
                }
            }
        }
    }

    fun clearDietDetailErrorMessage() {
        _dietDetailState.update { it.copy(errorMessage = null) }
    }


    /**
     * (유지) '개인정보'가 '저장'되면, AI 루틴을 '강제로' '재생성'합니다.
     */
    fun updateUserProfile(updatedUser: User, updatedInjuryName: String, updatedInjuryArea: String) {
        viewModelScope.launch {
            dummyUser = updatedUser
            dummyInjury = dummyInjury.copy(
                name = updatedInjuryName,
                bodyPart = updatedInjuryArea
            )

            loadMainDashboardData(forceReload = true)
        }
    }
}