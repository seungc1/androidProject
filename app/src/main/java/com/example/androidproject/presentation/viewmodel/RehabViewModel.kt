package com.example.androidproject.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.androidproject.domain.model.AIAnalysisResult
import com.example.androidproject.domain.model.Diet
import com.example.androidproject.domain.model.DietRecommendation
import com.example.androidproject.domain.model.Exercise
import com.example.androidproject.domain.model.ExerciseRecommendation
import com.example.androidproject.domain.model.Injury
import com.example.androidproject.domain.model.RehabSession
import com.example.androidproject.domain.model.ScheduledWorkout
import com.example.androidproject.domain.model.ScheduledDiet
import com.example.androidproject.domain.model.User
import com.example.androidproject.domain.usecase.AddDietSessionUseCase // ✅ 추가
import com.example.androidproject.domain.usecase.AddRehabSessionUseCase // ✅ 추가
import com.example.androidproject.domain.usecase.GetAIRecommendationUseCase
import com.example.androidproject.domain.usecase.GetDailyHistoryUseCase // ✅ 추가 (GetDailyHistoryUseCase.kt 파일이 있어야 합니다!)
import com.example.androidproject.domain.usecase.GetWeeklyAnalysisUseCase
import com.example.androidproject.presentation.history.HistoryItem
import com.example.androidproject.presentation.main.MainUiState
import com.example.androidproject.presentation.main.TodayExercise
import com.prolificinteractive.materialcalendarview.CalendarDay
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.threeten.bp.DateTimeUtils
import org.threeten.bp.LocalDate
import org.threeten.bp.ZoneId
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID
import javax.inject.Inject



@HiltViewModel
class RehabViewModel @Inject constructor(
    private val getAIRecommendationUseCase: GetAIRecommendationUseCase,
    private val getWeeklyAnalysisUseCase: GetWeeklyAnalysisUseCase,
    // ✅ [수정] DB 연동을 위한 UseCase들 주입
    private val addRehabSessionUseCase: AddRehabSessionUseCase,
    private val addDietSessionUseCase: AddDietSessionUseCase,
    private val getDailyHistoryUseCase: GetDailyHistoryUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    private val _historyUiState = MutableStateFlow(HistoryUiState())
    val historyUiState: StateFlow<HistoryUiState> = _historyUiState.asStateFlow()

    private val _dietDetailState = MutableStateFlow(DietDetailUiState())
    val dietDetailState: StateFlow<DietDetailUiState> = _dietDetailState.asStateFlow()

    private val _recordedDates = MutableStateFlow<Set<CalendarDay>>(emptySet())
    val recordedDates: StateFlow<Set<CalendarDay>> = _recordedDates.asStateFlow()

    // (더미 데이터는 프로필 화면 호환성을 위해 일단 유지)
    lateinit var dummyUser: User
    lateinit var dummyInjury: Injury

    init {
        loadMainDashboardData(forceReload = false)
    }

    private fun loadMainDashboardData(forceReload: Boolean) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            // (기존) 이미 로드된 데이터가 있으면 재사용
            if (!forceReload && _uiState.value.fullRoutine.isNotEmpty()) {
                val todayExercises = filterTodayExercises(_uiState.value.fullRoutine)
                if (todayExercises.isNotEmpty()) {
                    _uiState.update { it.copy(isLoading = false, todayExercises = todayExercises) }
                    return@launch
                }
            }

            try {
                // (기존) 더미 유저 생성 (나중에 UserRepository와 연결해야 함)
                dummyUser = User(
                    id = "user01", password = "password", name = "김재활", gender = "남성", age = 30,
                    heightCm = 175, weightKg = 70.5, activityLevel = "활동적",
                    fitnessGoal = "근육 증가", allergyInfo = listOf("땅콩", "새우"),
                    preferredDietType = "일반", targetCalories = 2500,
                    currentInjuryId = "injury01", preferredDietaryTypes = listOf("일반식", "저염식"),
                    equipmentAvailable = listOf("덤벨", "밴드"), currentPainLevel = 4,
                    additionalNotes = "부상 회복에 집중하고 싶습니다."
                )
                dummyInjury = Injury("injury01", "손목 염좌", "손목", "경미", "가벼운 통증")

                // (기존) AI 추천 받기
                getAIRecommendationUseCase(dummyUser.id, dummyInjury)
                    .catch { e -> _uiState.update { it.copy(isLoading = false, errorMessage = "AI 오류: ${e.message}") } }
                    .collect { aiResult ->
                        _uiState.value = MainUiState(
                            isLoading = false, userName = dummyUser.name,
                            currentInjuryName = dummyInjury.name, currentInjuryArea = dummyInjury.bodyPart,
                            fullRoutine = aiResult.scheduledWorkouts,
                            todayExercises = filterTodayExercises(aiResult.scheduledWorkouts),
                            recommendedDiets = filterTodayDiets(aiResult.scheduledDiets),
                            errorMessage = null
                        )
                    }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "로드 실패: ${e.message}") }
            }
        }
    }

    // (기존) 헬퍼 함수들
    private fun filterTodayExercises(fullRoutine: List<ScheduledWorkout>): List<TodayExercise> {
        val todayDateString = SimpleDateFormat("M월 d일 (E)", Locale.KOREA).format(Date())
        val todayWorkout = fullRoutine.find { it.scheduledDate.contains(todayDateString) }
        return todayWorkout?.exercises?.toTodayExerciseList() ?: emptyList()
    }

    private fun filterTodayDiets(scheduledDiets: List<ScheduledDiet>): List<Diet> {
        val todayDateString = SimpleDateFormat("M월 d일 (E)", Locale.KOREA).format(Date())
        val todayDiet = scheduledDiets.find { it.scheduledDate.contains(todayDateString) }
        return todayDiet?.meals?.toDietList() ?: emptyList()
    }

    private fun List<ExerciseRecommendation>.toTodayExerciseList(): List<TodayExercise> {
        return this.map { rec ->
            val exercise = Exercise(
                id = rec.name, name = rec.name, description = rec.description,
                bodyPart = rec.bodyPart, difficulty = rec.difficulty, videoUrl = rec.imageUrl,
                precautions = null, sets = rec.sets, reps = rec.reps, aiRecommendationReason = rec.aiRecommendationReason
            )
            TodayExercise(exercise = exercise, isCompleted = false)
        }
    }

    private fun List<DietRecommendation>.toDietList(): List<Diet> {
        return this.map { rec ->
            Diet(
                id = rec.foodItems?.joinToString() ?: UUID.randomUUID().toString(),
                mealType = rec.mealType, foodName = rec.foodItems?.joinToString(", ") ?: "식단",
                quantity = 1.0, unit = "인분", calorie = rec.calories?.toInt() ?: 0,
                protein = rec.proteinGrams ?: 0.0, fat = rec.fats ?: 0.0, carbs = rec.carbs ?: 0.0,
                ingredients = rec.ingredients ?: emptyList(), preparationTips = null, aiRecommendationReason = rec.aiRecommendationReason
            )
        }
    }

    // ✅ [수정] '저장' 버튼 클릭 시 '진짜 DB'에 저장하도록 변경
    fun saveRehabSessionDetails(exerciseId: String, rating: Int, notes: String) {
        viewModelScope.launch {
            val _session = RehabSession(
                id = UUID.randomUUID().toString(),
                userId = "user01", // (임시 ID - 나중에 실제 로그인 ID로 교체)
                exerciseId = exerciseId,
                dateTime = Date(),
                sets = 3, reps = 10, durationMinutes = 15,
                notes = notes,
                userRating = rating
            )

            try {
                // ✅ [수정] 주석 해제! -> 진짜 DB에 저장
                addRehabSessionUseCase(_session).collect { }
                setExerciseCompleted(exerciseId, true)
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "저장 실패: ${e.message}") }
            }
        }
    }

    // ✅ [수정] '기록' 탭 로드 시 '진짜 DB'에서 데이터를 가져오도록 변경
    fun loadHistory(date: Date) {
        viewModelScope.launch {
            _historyUiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                // ✅ [수정] 더미 데이터 삭제하고 GetDailyHistoryUseCase 호출
                getDailyHistoryUseCase("user01", date)
                    .collect { (rehabList, dietList) ->
                        val rehabItems = rehabList.map { HistoryItem.Exercise(it) }
                        val dietItems = dietList.map { HistoryItem.Diet(it) }
                        val allItems = (rehabItems + dietItems).sortedByDescending { it.dateTime }

                        _historyUiState.update {
                            it.copy(isLoading = false, historyItems = allItems)
                        }
                    }
            } catch (e: Exception) {
                _historyUiState.update {
                    it.copy(isLoading = false, errorMessage = "기록 로드 실패: ${e.message}")
                }
            }
        }
    }

    // (LocalDate 오버로딩)
    fun loadHistory(date: LocalDate) {
        val selectedDate = DateTimeUtils.toDate(date.atStartOfDay(ZoneId.systemDefault()).toInstant())
        loadHistory(selectedDate)
    }

    // (나머지 함수들은 그대로 유지)
    private fun setExerciseCompleted(exerciseId: String, isCompleted: Boolean) {
        _uiState.update { currentState ->
            val updatedExercises = currentState.todayExercises.map {
                if (it.exercise.id == exerciseId) it.copy(isCompleted = isCompleted) else it
            }
            currentState.copy(todayExercises = updatedExercises)
        }
    }

    fun clearErrorMessage() { _uiState.update { it.copy(errorMessage = null) } }
    fun clearHistoryErrorMessage() { _historyUiState.update { it.copy(errorMessage = null) } }
    fun clearDietDetailErrorMessage() { _dietDetailState.update { it.copy(errorMessage = null) } }

    fun updateUserProfile(updatedUser: User, updatedInjuryName: String, updatedInjuryArea: String) {
        viewModelScope.launch {
            dummyUser = updatedUser
            dummyInjury = dummyInjury.copy(name = updatedInjuryName, bodyPart = updatedInjuryArea)
            loadMainDashboardData(forceReload = true)
        }
    }

    fun loadDietDetails(dietId: String) {
        viewModelScope.launch {
            _dietDetailState.update { it.copy(isLoading = true, alternatives = emptyList()) }
            val diet = _uiState.value.recommendedDiets.find { it.id == dietId }
            if (diet != null) {
                _dietDetailState.update { it.copy(diet = diet, isLoading = false) }
            }
        }
    }

    fun fetchWeeklyAnalysis() {
        viewModelScope.launch {
            _historyUiState.update { it.copy(isAnalyzing = true) }
            try {
                getWeeklyAnalysisUseCase(dummyUser).collect { result ->
                    _historyUiState.update { it.copy(isAnalyzing = false, analysisResult = result) }
                }
            } catch (e: Exception) {
                _historyUiState.update { it.copy(isAnalyzing = false) }
            }
        }
    }

    fun loadAllSessionDates(userId: String) {}
}