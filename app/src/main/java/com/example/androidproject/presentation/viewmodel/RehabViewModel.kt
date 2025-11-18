package com.example.androidproject.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.androidproject.data.mapper.toDomain
import com.example.androidproject.domain.model.*
import com.example.androidproject.domain.usecase.GetWeeklyAnalysisUseCase
import com.example.androidproject.domain.usecase.AddRehabSessionUseCase
// (★ DEV ★) 'min'의 UseCase 대신 'dev'의 캐시 Repository를 사용합니다.
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
    // (★ 병합 ★) 'dev' 브랜치의 '모든' Repository와 UseCase를 주입받습니다.
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

    // (★ DEV ★) '실제' DB 데이터를 담을 변수
    private val _currentUser = MutableStateFlow<User?>(null)
    private val _currentInjury = MutableStateFlow<Injury?>(null)

    // (★ 병합 ★) 'min' 브랜치의 'ProfileEditFragment' 호환성을 위한 임시 변수
    lateinit var dummyUser: User
    lateinit var dummyInjury: Injury

    // (★ 병합 ★) 'min' 브랜치 '요구사항'에 따라 'init' 블록 '삭제'
    // init { ... }

    /**
     * (★ 병합 ★) 'min' 브랜치의 'MainActivity' '진입점' 함수 '추가'
     * '로그인' '성공' '시' '이' '함수'가 '호출'됩니다.
     */
    fun loadDataForUser(userId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            // (★ 병합 ★) 'dev' 브랜치의 '실제' 'DB 로드' '로직'을 '호출'
            loadUserAndInjury(userId)
        }
    }

    /**
     * (★ 병합 ★) 'dev' 브랜치의 '실제' 'DB 로드' '함수' (private 유지)
     */
    private fun loadUserAndInjury(userId: String) {
        viewModelScope.launch {
            // 1. DB에서 사용자 정보 로드
            _currentUser.value = userRepository.getUserProfile(userId).first()
            dummyUser = _currentUser.value!! // (임시 호환성)

            // 2. 사용자의 '현재 부상 ID'로 부상 정보 로드
            val injuryId = _currentUser.value?.currentInjuryId
            if (injuryId != null) {
                _currentInjury.value = injuryRepository.getInjuryById(injuryId).first()
                dummyInjury = _currentInjury.value ?: createEmptyInjury() // (임시 호환성)
            } else {
                _currentInjury.value = null
                dummyInjury = createEmptyInjury() // (임시 호환성)
            }

            // 3. 사용자/부상 정보 로드가 '완료'된 후, AI 루틴을 '처음' 로드합니다.
            loadMainDashboardData(forceReload = false)
        }
    }

    /**
     * (★ 병합 ★) 'dev' 브랜치의 'AI 루틴 로드' '함수' (수정 완료)
     * 'min' 브랜치의 '잘못' '병합'된 '코드를' '모두' '제거'했습니다.
     */
    fun loadMainDashboardData(forceReload: Boolean) {
        viewModelScope.launch {
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
                    if(_currentUser.value != null) {
                        loadAllSessionDates(_currentUser.value!!.id)
                    }
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

                // (★ DEV ★) 'min'의 'GetAIRecommendationUseCase' 대신 'WorkoutRoutineRepository' 사용
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

                        // (★ DEV ★) AI가 추천한 식단을 'Diet 사전' DB에 저장
                        val diets = aiResult.recommendedDiets.map { it.toDomain() }
                        dietRepository.upsertDiets(diets)

                        _uiState.value = MainUiState(
                            isLoading = false,
                            userName = user.name,
                            currentInjuryName = injury?.name,
                            currentInjuryArea = injury?.bodyPart,
                            fullRoutine = aiResult.scheduledWorkouts,
                            todayExercises = filterTodayExercises(aiResult.scheduledWorkouts),
                            recommendedDiets = diets, // (DB에 저장된 Diet 객체 리스트)
                            errorMessage = null
                        )
                        loadAllSessionDates(user.id)
                    }

            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "데이터 로드 실패: ${e.message}") }
            }
        }
    }

    // (★ 병합 ★) 'min' 브랜치의 'Mapper' '함수' ('dev' '버전'과 '동일'하여 '유지')
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

    // (★ 병합 ★) 'min' 브랜치의 'setExerciseCompleted' '버그' '수정' ('dev' '버전' '사용')
    private fun setExerciseCompleted(exerciseId: String, isCompleted: Boolean) {
        _uiState.update { currentState ->
            val updatedExercises = currentState.todayExercises.map {
                if (it.exercise.id == exerciseId) {
                    it.copy(isCompleted = isCompleted) // '!' (토글)이 아닌 'isCompleted' '값' '직접' '할당'
                } else {
                    it
                }
            }
            currentState.copy(todayExercises = updatedExercises)
        }
    }

    // (★ 병합 ★) 'dev' 브랜치의 '실제' 'DB 저장' '로직' '사용'
    fun saveRehabSessionDetails(exerciseId: String, rating: Int, notes: String) {
        viewModelScope.launch {
            val user = _currentUser.value ?: return@launch

            val exercise = _uiState.value.todayExercises.find { it.exercise.id == exerciseId }?.exercise

            val session = RehabSession(
                id = UUID.randomUUID().toString(),
                userId = user.id, // (★ 'dummyUser.id' '대신' '실제' 'user.id' '사용')
                exerciseId = exerciseId,
                dateTime = Date(),
                sets = exercise?.sets ?: 3,
                reps = exercise?.reps ?: 10,
                durationMinutes = 15,
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

    /**
     * (★ 병합 ★) 'dev' 브랜치의 '실제' 'DB 조회' '로직' '사용'
     * 'Conflicting declarations' '오류' '해결' (중복 함수 '제거')
     */
    fun loadHistory(date: LocalDate) {
        viewModelScope.launch {
            _historyUiState.update { it.copy(isLoading = true, errorMessage = null) }
            val user = _currentUser.value ?: return@launch

            try {
                // 'LocalDate' -> 'Date' 변환 ('threeten' 라이브러리 사용)
                val startDate = DateTimeUtils.toDate(date.atStartOfDay(ZoneId.systemDefault()).toInstant())
                val endDate = DateTimeUtils.toDate(date.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant())

                // 'DB'에서 '날짜 범위'로 '실제' '기록' '조회'
                val rehabFlow = rehabSessionRepository.getRehabSessionsBetween(user.id, startDate, endDate)
                val dietFlow = dietSessionRepository.getDietSessionsBetween(user.id, startDate, endDate)

                // '운동'과 '식단' 기록을 '조합'
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

    // (★ 병합 ★) 'dev' 브랜치의 '실제' 'User' '객체' '사용'
    fun fetchWeeklyAnalysis() {
        viewModelScope.launch {
            val user = _currentUser.value ?: return@launch // (dummyUser -> _currentUser.value)

            _historyUiState.update { it.copy(isAnalyzing = true, analysisResult = null) }
            try {
                getWeeklyAnalysisUseCase(user) // '실제' user 객체 전달
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

    // (★ 병합 ★) 'dev' 브랜치의 '실제' 'DB 조회' '로직' '사용'
    fun loadDietDetails(dietId: String) {
        viewModelScope.launch {
            _dietDetailState.update { it.copy(isLoading = true, errorMessage = null, alternatives = emptyList()) }
            try {
                val foundDiet = dietRepository.getDietById(dietId).first()

                if (foundDiet == null) {
                    throw Exception("선택한 식단(ID: $dietId)을 찾을 수 없습니다.")
                }
                _dietDetailState.update { it.copy(diet = foundDiet) }

                // (대체 식품 더미 로직은 유지)
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
     * (★ 병합 ★) 'dev' 브랜치의 '실제' 'DB 저장' '로직' '사용'
     * 'loadDataForUser' '참조' '오류' '해결'
     */
    fun updateUserProfile(updatedUser: User, updatedInjuryName: String, updatedInjuryArea: String) {
        viewModelScope.launch {
            val user = _currentUser.value ?: return@launch

            // 1. 'Injury' 정보를 'DB에 저장'
            val newInjury = Injury(
                id = _currentInjury.value?.id ?: "injury_${user.id}",
                name = updatedInjuryName,
                bodyPart = updatedInjuryArea,
                severity = _currentInjury.value?.severity ?: "경미",
                description = _currentInjury.value?.description ?: "정보 없음"
            )
            injuryRepository.upsertInjury(newInjury, user.id)

            // 2. 'User' 정보도 'DB에 저장' (새 부상 ID 포함)
            val userToUpdate = updatedUser.copy(currentInjuryId = newInjury.id)
            userRepository.updateUserProfile(userToUpdate)

            // 3. ViewModel의 '현재' '상태' '업데이트'
            _currentUser.value = userToUpdate
            _currentInjury.value = newInjury
            dummyUser = userToUpdate // (임시 호환성)
            dummyInjury = newInjury // (임시 호환성)

            // 4. (★ 수정 ★) 'loadDataForUser' '대신' 'loadMainDashboardData' '호출'
            loadMainDashboardData(forceReload = true)
        }
    }

    /**
     * (★ 병합 ★) 'dev' 브랜치의 '실제' 'DB 조회' '로직' '사용'
     * 'Conflicting declarations' '오류' '해결' (중복 함수 '제거')
     * 'Unresolved reference 'add'' '오류' '해결' (잘못된 'Date()' '추가' '로직' '제거')
     */
    fun loadAllSessionDates(userId: String) {
        viewModelScope.launch {
            // 1. 'DB'에서 '실제' '기록' '조회'
            val rehabDates = rehabSessionRepository.getRehabHistory(userId).first().map { it.dateTime }
            val dietDates = dietSessionRepository.getDietHistory(userId).first().map { it.dateTime }

            val recordedUtilDates = (rehabDates + dietDates).distinct() // (중복 제거)

            // 2. 'java.util.Date' '목록'을 'CalendarDay' '목록'으로 '변환'
            val recordedDaysSet = HashSet<CalendarDay>()
            recordedUtilDates.forEach { utilDate ->
                val instant = DateTimeUtils.toInstant(utilDate)
                val localDate = instant.atZone(ZoneId.systemDefault()).toLocalDate()
                recordedDaysSet.add(CalendarDay.from(localDate))
            }

            _recordedDates.value = recordedDaysSet
        }
    }

    // (★ 병합 ★) 'dev' 브랜치의 'Helper' '함수' '추가'
    private fun createEmptyInjury(): Injury {
        return Injury(id = "temp", name = "없음", bodyPart = "없음", severity = "없음", description = "")
    }
}