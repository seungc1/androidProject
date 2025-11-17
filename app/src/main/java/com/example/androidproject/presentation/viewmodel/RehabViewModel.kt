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
import com.example.androidproject.domain.usecase.AddRehabSessionUseCase
import com.prolificinteractive.materialcalendarview.CalendarDay
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
import org.threeten.bp.DateTimeUtils
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import org.threeten.bp.ZoneId

// (데이터 클래스 HistoryUiState, DietDetailUiState는 수정 없음)
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
    private val getWeeklyAnalysisUseCase: GetWeeklyAnalysisUseCase,
    private val addRehabSessionUseCase: AddRehabSessionUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    private val _historyUiState = MutableStateFlow(HistoryUiState())
    val historyUiState: StateFlow<HistoryUiState> = _historyUiState.asStateFlow()

    private val _dietDetailState = MutableStateFlow(DietDetailUiState())
    val dietDetailState: StateFlow<DietDetailUiState> = _dietDetailState.asStateFlow()

    private val _recordedDates = MutableStateFlow<Set<CalendarDay>>(emptySet())
    val recordedDates: StateFlow<Set<CalendarDay>> = _recordedDates.asStateFlow()


    lateinit var dummyUser: User
    lateinit var dummyInjury: Injury

    init {
        // (★참고★: DB 연결 후, 'dummyUser'는 '로그인' 시점에 설정되어야 합니다.)
        loadMainDashboardData(forceReload = false)
    }

    fun loadMainDashboardData(forceReload: Boolean) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            if (!forceReload && _uiState.value.fullRoutine.isNotEmpty()) {
                val todayExercises = filterTodayExercises(_uiState.value.fullRoutine)
                if (todayExercises.isNotEmpty()) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            todayExercises = todayExercises
                        )
                    }
                    loadAllSessionDates(dummyUser.id) // (DB 캐싱 구현 시 이 위치가 적절)
                    return@launch
                }
            }

            try {
                // (★임시★: 이 더미 데이터는 '로그인' 기능이 완성되면 '제거'하고
                // 'userRepository.getUserProfile'을 통해 'DB'에서 가져와야 합니다.)
                dummyUser = User(
                    id = "user01", password = "1234", name = "김재활", gender = "남성", age = 30,
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
                // (--- 임시 데이터 끝 ---)

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
                        // (★임시★: AI가 루틴을 로드할 때마다 날짜를 계산 - DB 연동 후 변경 필요)
                        loadAllSessionDates(dummyUser.id)
                    }

            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "데이터 로드 실패: ${e.message}") }
            }
        }
    }

    // (filterTodayExercises, Mapper 함수 2개 - 수정 없음)
    private fun filterTodayExercises(fullRoutine: List<ScheduledWorkout>): List<TodayExercise> {
        val todayDateString = SimpleDateFormat("M월 d일 (E)", Locale.KOREA).format(Date())
        val todayWorkout = fullRoutine.find {
            it.scheduledDate.contains(todayDateString)
        }
        return todayWorkout?.exercises?.toTodayExerciseList() ?: emptyList()
    }
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


    private fun setExerciseCompleted(exerciseId: String, isCompleted: Boolean) {
        _uiState.update { currentState ->
            val updatedExercises = currentState.todayExercises.map {
                if (it.exercise.id == exerciseId) {
                    it.copy(isCompleted = isCompleted)
                } else {
                    it
                }
            }
            currentState.copy(todayExercises = updatedExercises)
        }
    }

    /**
     * (★수정★) 'saveRehabSessionDetails' - UseCase를 '실제' '호출'합니다.
     */
    fun saveRehabSessionDetails(exerciseId: String, rating: Int, notes: String) {
        viewModelScope.launch {
            val session = RehabSession(
                id = UUID.randomUUID().toString(),
                userId = dummyUser.id, // (★수정★) "user01" -> dummyUser.id
                exerciseId = exerciseId,
                dateTime = Date(),
                sets = 3, reps = 10, durationMinutes = 15, // (★개선 필요★: 이 값도 AI 추천값으로)
                notes = notes,
                userRating = rating
            )

            // 🚨 [수정] UseCase를 '실제' '호출'하여 DB에 '저장'합니다.
            // (경고가 사라졌습니다)
            addRehabSessionUseCase(session).collect()

            // (★중요★) 운동 '기록'이 '저장'되었으므로,
            // '기록' 탭의 '달력'을 '새로고침'하여 '오늘' 날짜에 '점'을 '표시'합니다.
            loadAllSessionDates(dummyUser.id)

            // (기존) UI '체크' 상태 '업데이트'
            setExerciseCompleted(exerciseId, true)
        }
    }

    fun clearErrorMessage() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    // (★ 수정 ★) 'loadHistory' - 'LocalDate'를 'Date'로 '변환'하는 '로직' '추가'
    fun loadHistory(date: LocalDate) { // (★ 수정 ★) 'Date' -> 'LocalDate'
        viewModelScope.launch {
            _historyUiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                kotlinx.coroutines.delay(500) // (시뮬레이션 딜레이)

                // (★ 수정 ★) 'LocalDate' -> 'java.util.Date'로 '변환' (API 24 '호환')
                val selectedDate = DateTimeUtils.toDate(date.atStartOfDay(ZoneId.systemDefault()).toInstant())
                val calendar = Calendar.getInstance().apply { time = selectedDate }
                val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)

                // (★수정★: 이 로직은 'GetHistoryUseCase'로 '이동'되어야 합니다. 지금은 '더미')
                // (팀원이 DB를 연결했으므로, 'addRehabSessionUseCase'로 '저장'한 '데이터'를 '조회'해야 합니다)
                val dummyHistoryItems = if (dayOfMonth == 5 || dayOfMonth == 10 || dayOfMonth == 15) {
                    listOf(
                        HistoryItem.Exercise(
                            RehabSession(
                                id = "session001", userId = "user01", exerciseId = "ex001",
                                dateTime = selectedDate,
                                sets = 3, reps = 10, durationMinutes = 15,
                                notes = "조금 아팠음",
                                userRating = 3
                            )
                        ),
                        HistoryItem.Diet(
                            DietSession(
                                id = "dietSession001", userId = "user01", dietId = "d001",
                                dateTime = selectedDate,
                                actualQuantity = 1.0, actualUnit = "그릇",
                                userSatisfaction = 5
                            )
                        )
                    )
                } else {
                    // (★중요★) 'saveRehabSessionDetails'로 '저장'한 '데이터'가 '표시'되려면
                    // 'GetHistoryUseCase'를 '만들고' 'Repository'를 '호출'해야 합니다.
                    emptyList()
                }

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

    // (fetchWeeklyAnalysis, createErrorAnalysisResult - 수정 없음)
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

    fun loadAllSessionDates(userId: String) {
        viewModelScope.launch {

            val recordedUtilDates = mutableListOf<Date>()
            val calendar = Calendar.getInstance()

            calendar.set(Calendar.DAY_OF_MONTH, 5); recordedUtilDates.add(calendar.time)
            calendar.set(Calendar.DAY_OF_MONTH, 10); recordedUtilDates.add(calendar.time)
            calendar.set(Calendar.DAY_OF_MONTH, 15); recordedUtilDates.add(calendar.time)

            // (★중요★) 'saveRehabSessionDetails'에서 '방금' '저장'한 '오늘' 날짜 '추가'
            // (DB에서 '직접' '조회'하면 이 코드는 '필요 없습니다')
            recordedUtilDates.add(Date())

            // (★ 핵심 ★) 'java.util.Date' '목록'을 'CalendarDay' '목록'으로 '변환'
            val recordedDaysSet = HashSet<CalendarDay>() // 'HashSet' '사용'
            recordedUtilDates.forEach { utilDate ->
                // (★ 수정 ★) 'java.util.Date' -> 'threeten.LocalDate' -> 'CalendarDay' (API 24 '호환')
                val instant = DateTimeUtils.toInstant(utilDate)
                val localDate = instant.atZone(ZoneId.systemDefault()).toLocalDate()
                recordedDaysSet.add(CalendarDay.from(localDate))
            }

            _recordedDates.value = recordedDaysSet
        }
    }
}