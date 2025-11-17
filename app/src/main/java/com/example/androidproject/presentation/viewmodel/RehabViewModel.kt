package com.example.androidproject.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.androidproject.data.mapper.toDomain
import com.example.androidproject.domain.model.*
import com.example.androidproject.domain.usecase.GetWeeklyAnalysisUseCase
import com.example.androidproject.domain.usecase.AddRehabSessionUseCase
import com.example.androidproject.domain.repository.WorkoutRoutineRepository
import com.example.androidproject.domain.repository.UserRepository
import com.example.androidproject.domain.repository.InjuryRepository
import com.example.androidproject.domain.repository.DietRepository
import com.example.androidproject.domain.repository.RehabSessionRepository
import com.example.androidproject.domain.repository.DietSessionRepository
import com.example.androidproject.presentation.history.HistoryItem
import com.example.androidproject.presentation.main.MainUiState
import com.example.androidproject.presentation.main.TodayExercise
import com.prolificinteractive.materialcalendarview.CalendarDay
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Calendar
import java.util.Locale
import java.util.UUID
import javax.inject.Inject
import org.threeten.bp.DateTimeUtils
import org.threeten.bp.LocalDate
import org.threeten.bp.ZoneId
import org.threeten.bp.temporal.ChronoUnit


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
    private val getWeeklyAnalysisUseCase: GetWeeklyAnalysisUseCase,
    private val addRehabSessionUseCase: AddRehabSessionUseCase,
    private val workoutRoutineRepository: WorkoutRoutineRepository, // (AI 루틴 캐싱용)
    private val userRepository: UserRepository,                   // (사용자 DB)
    private val injuryRepository: InjuryRepository,                 // (부상 DB)
    private val dietRepository: DietRepository,                     // (식단 사전 DB)
    private val rehabSessionRepository: RehabSessionRepository,     // (운동 기록 DB)
    private val dietSessionRepository: DietSessionRepository      // (식단 기록 DB)
) : ViewModel() {

    // (StateFlow 선언부 수정 없음)
    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    private val _historyUiState = MutableStateFlow(HistoryUiState())
    val historyUiState: StateFlow<HistoryUiState> = _historyUiState.asStateFlow()

    private val _dietDetailState = MutableStateFlow(DietDetailUiState())
    val dietDetailState: StateFlow<DietDetailUiState> = _dietDetailState.asStateFlow()

    private val _recordedDates = MutableStateFlow<Set<CalendarDay>>(emptySet())
    val recordedDates: StateFlow<Set<CalendarDay>> = _recordedDates.asStateFlow()


    private val _currentUser = MutableStateFlow<User?>(null)
    private val _currentInjury = MutableStateFlow<Injury?>(null)

    // (★참고★: ProfileEditFragment 호환성을 위해 남겨둔 임시 변수)
    lateinit var dummyUser: User
    lateinit var dummyInjury: Injury


    init {
        // (★수정★) 앱이 시작되면 '로그인'된 '사용자' 정보를 DB에서 불러옵니다.
        // (현재는 'user01'로 하드코딩. Splash/Login 화면에서 이 ID를 받아와야 함)
        loadUserAndInjury("user01")
    }

    /**
     * 🚨 [추가] DB에서 현재 사용자 정보와 부상 정보를 로드합니다.
     */
    private fun loadUserAndInjury(userId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            // 1. DB에서 사용자 정보 로드
            _currentUser.value = userRepository.getUserProfile(userId).first()
            dummyUser = _currentUser.value!! // (임시 호환성)

            // 2. 사용자의 '현재 부상 ID'로 부상 정보 로드
            val injuryId = _currentUser.value?.currentInjuryId
            if (injuryId != null) {
                // (★수정★) .first()를 사용하여 Flow가 완료될 때까지 기다림
                _currentInjury.value = injuryRepository.getInjuryById(injuryId).first()
                dummyInjury = _currentInjury.value ?: createEmptyInjury() // (임시 호환성)
            } else {
                dummyInjury = createEmptyInjury() // (임시 호환성)
            }

            // 3. 사용자/부상 정보 로드가 '완료'된 후, AI 루틴을 '처음' 로드합니다.
            loadMainDashboardData(forceReload = false)
        }
    }

    /**
     * (★수정★) AI 루틴 로드 로직 (DB 캐시 우선)
     */
    fun loadMainDashboardData(forceReload: Boolean) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            // (Check 1) 강제 리로드가 아니고, 기존 루틴(ViewModel 메모리)이 있다면
            if (!forceReload && _uiState.value.fullRoutine.isNotEmpty()) {
                val todayExercises = filterTodayExercises(_uiState.value.fullRoutine)
                // (Check 2) '오늘의 운동'이 있다면 (루틴이 유효함)
                if (todayExercises.isNotEmpty()) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            todayExercises = todayExercises
                        )
                    }
                    loadAllSessionDates(_currentUser.value!!.id)
                    return@launch // AI 호출 없이 함수 종료
                }
            }

            // (AI 호출)
            try {
                val user = _currentUser.value
                val injury = _currentInjury.value

                // (사용자 정보가 없으면 AI 호출 중단)
                if (user == null) {
                    _uiState.update { it.copy(isLoading = false, userName = "") } // (빈 화면 표시)
                    return@launch
                }

            try {
                dummyUser = User(
                    id = userId, // (★ 수정 ★) '로그인'한 'userId' '사용'
                    password = "1234", // ('DB' '연동' '전' '임시' '비밀번호')
                    name = "김재활 (로그인 됨)", // (이름 '수정')
                    gender = "남성", age = 30,
                    heightCm = 175, weightKg = 70.5, activityLevel = "활동적",
                    fitnessGoal = "근육 증가", allergyInfo = listOf("땅콩", "새우"),
                    preferredDietType = "일반", targetCalories = 2500,
                    currentInjuryId = "injury01",
                    preferredDietaryTypes = listOf("일반식", "저염식"),
                    equipmentAvailable = listOf("덤벨", "밴드"),
                    currentPainLevel = 4,
                    additionalNotes = "부상 회복에 집중하고 싶습니다."
                )
                dummyInjury = Injury(
                    id = "injury01", name = "손목 염좌", bodyPart = "손목",
                    severity = "경미", description = "가벼운 통증이 있는 상태"
                )

                getAIRecommendationUseCase(userId, dummyInjury)
                workoutRoutineRepository.getWorkoutRoutine(forceReload, user, injury)
                    .catch { e ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                userName = user.name,
                                errorMessage = "AI 루틴 생성 중 오류 발생: ${e.message}"
                            )
                        }
                    }
                    .collect { aiResult -> // (aiResult는 이제 DB 또는 API에서 옴)

                        val diets = aiResult.recommendedDiets.map { it.toDomain() }
                        dietRepository.upsertDiets(diets)

                        _uiState.value = MainUiState(
                            isLoading = false,
                            userName = user.name,
                            currentInjuryName = injury?.name,
                            currentInjuryArea = injury?.bodyPart,
                            fullRoutine = aiResult.scheduledWorkouts,
                            todayExercises = filterTodayExercises(aiResult.scheduledWorkouts),
                            recommendedDiets = diets,
                            errorMessage = null
                        )

                        loadAllSessionDates(dummyUser.id)
                        loadAllSessionDates(user.id)
                    }

            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "데이터 로드 실패: ${e.message}") }
            }
        }
    }

    // (filterTodayExercises, toTodayExerciseList 수정 없음)
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

    // (setExerciseCompleted 수정 없음)
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

    // (saveRehabSessionDetails - DB 저장 로직으로 수정)
    fun saveRehabSessionDetails(exerciseId: String, rating: Int, notes: String) {
        viewModelScope.launch {
            val user = _currentUser.value ?: return@launch

            // (AI 추천값에서 sets/reps를 가져오도록 수정)
            val exercise = _uiState.value.todayExercises.find { it.exercise.id == exerciseId }?.exercise

            val session = RehabSession(
                id = UUID.randomUUID().toString(),
                userId = user.id,
                exerciseId = exerciseId,
                dateTime = Date(),
                sets = exercise?.sets ?: 3, // (AI 추천값 or 기본값)
                reps = exercise?.reps ?: 10, // (AI 추천값 or 기본값)
                durationMinutes = 15, // (임시)
                notes = notes,
                userRating = rating
            )

            addRehabSessionUseCase(session).collect()
            loadAllSessionDates(user.id) // (달력 새로고침)
            setExerciseCompleted(exerciseId, true)
        }
    }
    fun clearErrorMessage() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    // (★ 수정 ★) 'loadHistory' - '더미' 데이터 '제거', '실제' 'DB' '조회'
    fun loadHistory(date: LocalDate) {
    // (loadHistory - 'LocalDate' '타입' '사용' '유지')
    fun loadHistory(date: LocalDate) {
        viewModelScope.launch {
            _historyUiState.update { it.copy(isLoading = true, errorMessage = null) }
            val user = _currentUser.value ?: return@launch

            try {
                // 1. 'LocalDate' -> 'Date' (Start)
                val startDate = DateTimeUtils.toDate(date.atStartOfDay(ZoneId.systemDefault()).toInstant())
                // 2. 'LocalDate' + 1 day -> 'Date' (End)
                val endDate = DateTimeUtils.toDate(date.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant())

                // 3. 'DB'에서 '날짜 범위'로 '실제' '기록' '조회'
                val rehabFlow = rehabSessionRepository.getRehabSessionsBetween(user.id, startDate, endDate)
                val dietFlow = dietSessionRepository.getDietSessionsBetween(user.id, startDate, endDate)

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
                    emptyList()
                }
                // 4. '운동'과 '식단' 기록을 '조합'
                combine(rehabFlow, dietFlow) { rehabSessions, dietSessions ->
                    val exerciseItems = rehabSessions.map { HistoryItem.Exercise(it) }
                    val dietItems = dietSessions.map { HistoryItem.Diet(it) }

                    (exerciseItems + dietItems).sortedByDescending { it.dateTime }
                }.collect { historyItems ->
                    _historyUiState.update {
                        it.copy(isLoading = false, historyItems = historyItems)
                    }
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

    // (fetchWeeklyAnalysis - dummyUser -> user 수정)
    fun fetchWeeklyAnalysis() {
        viewModelScope.launch {
            val user = _currentUser.value ?: return@launch

            _historyUiState.update { it.copy(isAnalyzing = true, analysisResult = null) }
            try {
                getWeeklyAnalysisUseCase(user)
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

    // (createErrorAnalysisResult - 수정 없음)
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

    // (★ 수정 ★) 'loadDietDetails' - 'DB'에서 '직접' '조회'
    // (loadDietDetails, clearDietDetailErrorMessage - 'min' 버전 유지, 동일함)
    fun loadDietDetails(dietId: String) {
        viewModelScope.launch {
            _dietDetailState.update { it.copy(isLoading = true, errorMessage = null, alternatives = emptyList()) }
            try {
                // 'dietRepository'를 통해 DB에서 '직접' '조회'
                val foundDiet = dietRepository.getDietById(dietId).first()

                if (foundDiet == null) {
                    throw Exception("선택한 식단(ID: $dietId)을 찾을 수 없습니다.")
                }
                _dietDetailState.update { it.copy(diet = foundDiet) }

                // (이하 대체 식품 더미 로직은 유지)
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

    // (clearDietDetailErrorMessage - 수정 없음)
    fun clearDietDetailErrorMessage() {
        _dietDetailState.update { it.copy(errorMessage = null) }
    }

    /**
     * (★수정★) '개인정보'가 '저장'되면, 'User'와 'Injury'를 'DB에 저장'합니다.
     */
    // (updateUserProfile - 'forceReload' '값' '수정')
    fun updateUserProfile(updatedUser: User, updatedInjuryName: String, updatedInjuryArea: String) {
        viewModelScope.launch {
            val user = _currentUser.value ?: return@launch

            // 1. 'Injury' 정보를 'DB에 저장'
            val newInjury = Injury(
                id = _currentInjury.value?.id ?: "injury_${user.id}", // (ID가 없으면 새로 생성)
                name = updatedInjuryName,
                bodyPart = updatedInjuryArea,
                severity = _currentInjury.value?.severity ?: "경미", // (임시)
                description = _currentInjury.value?.description ?: "정보 없음" // (임시)
            )
            // (★ 수정 ★) 'loadMainDashboardData' -> 'loadDataForUser'
            loadDataForUser(updatedUser.id, forceReload = true)
            injuryRepository.upsertInjury(newInjury, user.id)

            // 2.  'User' 정보도 'DB에 저장' (새 부상 ID 포함)
            val userToUpdate = updatedUser.copy(currentInjuryId = newInjury.id)
            userRepository.updateUserProfile(userToUpdate)

            // 3. ViewModel의 '현재' '상태' '업데이트'
            _currentUser.value = userToUpdate
            _currentInjury.value = newInjury
            dummyUser = userToUpdate // (임시 호환성)
            dummyInjury = newInjury // (임시 호환성)

            // 4. (유지) AI 루틴을 '강제로' '재생성'합니다.
            loadMainDashboardData(forceReload = true)
        }
    }

    // (loadAllSessionDates - 'threeten' '사용' '유지')
    /**
     * (★ 수정 ★) 'loadAllSessionDates' - '더미' '데이터' '제거', '실제' 'DB' '조회'
     */
    fun loadAllSessionDates(userId: String) {
        viewModelScope.launch {
            // 1. 'DB'에서 '실제' '기록' '조회'
            val rehabDates = rehabSessionRepository.getRehabHistory(userId).first().map { it.dateTime }
            val dietDates = dietSessionRepository.getDietHistory(userId).first().map { it.dateTime }

            val recordedUtilDates = (rehabDates + dietDates).distinct() // (중복 제거)

            val recordedDaysSet = HashSet<CalendarDay>()
            // (★중요★) 'saveRehabSessionDetails'에서 '방금' '저장'한 '오늘' 날짜 '추가'
            // (DB에서 '직접' '조회'하면 이 코드는 '필요 없습니다')
            recordedUtilDates.add(Date())

            // (★ 핵심 ★) 'java.util.Date' '목록'을 'CalendarDay' '목록'으로 '변환'
            val recordedDaysSet = HashSet<CalendarDay>() // 'HashSet' '사용'
            // 2. (★ 핵심 ★) 'java.util.Date' '목록'을 'CalendarDay' '목록'으로 '변환'
            val recordedDaysSet = HashSet<CalendarDay>()
            recordedUtilDates.forEach { utilDate ->
                // (★ 수정 ★) 'java.util.Date' -> 'threeten.LocalDate' -> 'CalendarDay' (API 24 '호환')
                val instant = DateTimeUtils.toInstant(utilDate)
                val localDate = instant.atZone(ZoneId.systemDefault()).toLocalDate()
                recordedDaysSet.add(CalendarDay.from(localDate))
            }

            _recordedDates.value = recordedDaysSet
        }
    }

    // (임시 호환성)
    private fun createEmptyInjury(): Injury {
        return Injury(id = "temp", name = "없음", bodyPart = "없음", severity = "없음", description = "")
    }
}