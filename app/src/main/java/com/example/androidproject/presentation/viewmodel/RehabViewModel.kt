package com.example.androidproject.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.androidproject.data.local.SessionManager
import com.example.androidproject.data.local.datasource.LocalDataSource
import com.example.androidproject.domain.model.*
import com.example.androidproject.domain.repository.*
import com.example.androidproject.domain.usecase.AddDietSessionUseCase // (★ 추가)
import com.example.androidproject.domain.usecase.AddRehabSessionUseCase
import com.example.androidproject.presentation.main.MainUiState
import com.example.androidproject.presentation.main.TodayExercise
import com.prolificinteractive.materialcalendarview.CalendarDay
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.threeten.bp.DateTimeUtils
import org.threeten.bp.ZoneId
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class RehabViewModel @Inject constructor(
    private val addRehabSessionUseCase: AddRehabSessionUseCase,
    private val addDietSessionUseCase: AddDietSessionUseCase,
    private val workoutRoutineRepository: WorkoutRoutineRepository,
    private val userRepository: UserRepository,
    private val injuryRepository: InjuryRepository,
    private val dietRepository: DietRepository,
    private val sessionManager: SessionManager,
    private val localDataSource: LocalDataSource
) : ViewModel() {

    // region [StateFlow Definitions]
    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    private val _recordedDates = MutableStateFlow<Set<CalendarDay>>(emptySet())
    val recordedDates: StateFlow<Set<CalendarDay>> = _recordedDates.asStateFlow()

    // Internal State
    private val _currentUser = MutableStateFlow<User?>(null)
    private val _currentInjury = MutableStateFlow<Injury?>(null)

    // Legacy Support (화면 표시용 임시 변수)
    lateinit var dummyUser: User
    lateinit var dummyInjury: Injury
    // endregion

    // region [Initialization & Entry Point]
    fun loadDataForUser(userId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            loadUserAndInjury(userId)
        }
    }

    private fun loadUserAndInjury(userId: String) {
        viewModelScope.launch {
            // 1. 사용자 정보 '지속 관찰' (collectLatest)
            userRepository.getUserProfile(userId).collectLatest { user ->
                _currentUser.value = user
                dummyUser = user

                // 2. 부상 정보 로드
                val injuryId = user.currentInjuryId
                if (injuryId != null) {
                    // 부상 정보도 '지속 관찰'하여 동기화 시 자동 갱신
                    injuryRepository.getInjuryById(injuryId).collectLatest { injury ->
                        _currentInjury.value = injury
                        dummyInjury = injury ?: createEmptyInjury()

                        // 데이터가 준비되면 대시보드 로드
                        loadMainDashboardData(forceReload = false)
                    }
                } else {
                    _currentInjury.value = null
                    dummyInjury = createEmptyInjury()
                    loadMainDashboardData(forceReload = false)
                }
            }
        }
    }
    // endregion

    // region [Dashboard Feature]
    fun loadMainDashboardData(forceReload: Boolean) {
        viewModelScope.launch {
            val user = _currentUser.value ?: return@launch

            // 프로필 완료 여부 체크 (기본 이름 "신규 사용자"인지 확인)
            val isComplete = user.name != "신규 사용자"

            // 1. 이미 로드된 루틴이 있고 강제 리로드가 아니면 재사용
            if (!forceReload && _uiState.value.fullRoutine.isNotEmpty()) {
                val todayExercises = filterTodayExercises(_uiState.value.fullRoutine)
                if (todayExercises.isNotEmpty()) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            todayExercises = todayExercises,
                            isProfileComplete = isComplete
                        )
                    }
                    _currentUser.value?.let { loadAllSessionDates(it.id) }
                    return@launch
                }
            }

            // 2. AI 루틴 가져오기
            try {
                val injury = _currentInjury.value

                workoutRoutineRepository.getWorkoutRoutine(forceReload, user, injury)
                    .catch { e ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                userName = user.name,
                                isProfileComplete = isComplete,
                                errorMessage = "AI 루틴 오류: ${e.message}"
                            )
                        }
                    }
                    .collect { aiResult ->
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
                            isProfileComplete = isComplete
                        )
                        loadAllSessionDates(user.id)
                    }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "데이터 로드 실패: ${e.message}") }
            }
        }
    }

    fun saveRehabSessionDetails(exerciseId: String, rating: Int, notes: String) {
        viewModelScope.launch {
            val user = _currentUser.value ?: return@launch
            val exercise =
                _uiState.value.todayExercises.find { it.exercise.id == exerciseId }?.exercise

            val session = RehabSession(
                id = UUID.randomUUID().toString(),
                userId = user.id,
                exerciseId = exerciseId,
                dateTime = Date(),
                sets = exercise?.sets ?: 3,
                reps = exercise?.reps ?: 10,
                durationMinutes = 15,
                notes = notes,
                userRating = rating
            )

            addRehabSessionUseCase(session).collect()
            loadAllSessionDates(user.id)
            setExerciseCompleted(exerciseId, true)
        }
    }

    private fun setExerciseCompleted(exerciseId: String, isCompleted: Boolean) {
        _uiState.update { currentState ->
            val updatedExercises = currentState.todayExercises.map {
                if (it.exercise.id == exerciseId) it.copy(isCompleted = isCompleted) else it
            }
            currentState.copy(todayExercises = updatedExercises)
        }
    }

    private fun loadAllSessionDates(userId: String) {
        // (대시보드 달력용 - 필요시 구현. 현재는 HistoryViewModel에서 담당)
    }
    // endregion

    // region [Profile Feature]
    fun updateUserProfile(
        updatedUser: User,
        updatedInjuryName: String,
        updatedInjuryArea: String
    ) {
        viewModelScope.launch {
            val user = _currentUser.value ?: return@launch

            val newInjury = Injury(
                id = _currentInjury.value?.id ?: "injury_${user.id}",
                name = updatedInjuryName,
                bodyPart = updatedInjuryArea,
                severity = _currentInjury.value?.severity ?: "경미",
                description = _currentInjury.value?.description ?: "정보 없음"
            )
            injuryRepository.upsertInjury(newInjury, user.id)

            val userToUpdate = updatedUser.copy(currentInjuryId = newInjury.id)
            userRepository.updateUserProfile(userToUpdate)

            _currentUser.value = userToUpdate
            _currentInjury.value = newInjury
            dummyUser = userToUpdate
            dummyInjury = newInjury

            loadMainDashboardData(forceReload = true)
        }
    }

    fun logout() {
        viewModelScope.launch {
            // 1. 로컬 DB 데이터 싹 지우기
            localDataSource.clearAllData()

            // 2. 세션 정보 지우기
            sessionManager.clearSession()

            // 3. 상태 초기화
            _currentUser.value = null
            _currentInjury.value = null
        }
    }

    // ------------------------------------------------------------------------
    // (★ 추가 ★) 테스트 데이터 생성 치트키 함수
    // ------------------------------------------------------------------------
    fun createTestHistory() {
        viewModelScope.launch {
            val user = _currentUser.value ?: return@launch
            _uiState.update { it.copy(isLoading = true) } // 로딩 표시

            val calendar = Calendar.getInstance()

            // 지난 7일간의 데이터 생성
            for (i in 1..7) {
                calendar.add(Calendar.DAY_OF_YEAR, -1) // 하루씩 뒤로 감
                val date = calendar.time

                // 1. 운동 기록 생성 (랜덤 점수)
                val rehabSession = RehabSession(
                    id = UUID.randomUUID().toString(),
                    userId = user.id,
                    exerciseId = "test_exercise", // 테스트용 ID
                    dateTime = date,
                    sets = 3,
                    reps = 12,
                    durationMinutes = (20..50).random(),
                    userRating = (3..5).random(), // 3~5점 사이 랜덤
                    notes = "테스트 운동 기록 ($i 일전)"
                )
                addRehabSessionUseCase(rehabSession).collect()

                // 2. 식단 기록 생성 (랜덤 만족도)
                val dietSession = DietSession(
                    id = UUID.randomUUID().toString(),
                    userId = user.id,
                    dietId = "test_diet", // 테스트용 ID
                    dateTime = date,
                    actualQuantity = 1.0,
                    actualUnit = "인분",
                    userSatisfaction = (2..5).random(), // 2~5점 사이 랜덤
                    notes = "테스트 식단 기록 ($i 일전)"
                )
                addDietSessionUseCase(dietSession).collect()
            }

            _uiState.update { it.copy(isLoading = false) }
            // 완료 후 필요하다면 달력 갱신 등을 호출
        }
    }
    // endregion

    // region [Helpers & Utils]
    fun clearErrorMessage() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    private fun createEmptyInjury() =
        Injury(id = "temp", name = "없음", bodyPart = "없음", severity = "없음", description = "")

    private fun filterTodayExercises(fullRoutine: List<ScheduledWorkout>): List<TodayExercise> {
        val todayString = SimpleDateFormat("M월 d일 (E)", Locale.KOREA).format(Date())
        return fullRoutine.find { it.scheduledDate.contains(todayString) }
            ?.exercises?.map { it.toTodayExercise() } ?: emptyList()
    }

    private fun ExerciseRecommendation.toTodayExercise() = TodayExercise(
        exercise = Exercise(
            id = name, name = name, description = description, bodyPart = bodyPart,
            difficulty = difficulty, videoUrl = imageUrl, precautions = null,
            sets = sets, reps = reps, aiRecommendationReason = aiRecommendationReason
        ),
        isCompleted = false
    )

    private fun DietRecommendation.toDomain() = Diet(
        id = foodItems?.joinToString() ?: UUID.randomUUID().toString(),
        mealType = mealType,
        foodName = foodItems?.joinToString(", ") ?: "이름 없는 식단",
        quantity = 1.0, unit = "인분",
        calorie = calories?.toInt() ?: 0,
        protein = proteinGrams ?: 0.0, fat = fats ?: 0.0, carbs = carbs ?: 0.0,
        ingredients = ingredients ?: emptyList(),
        preparationTips = null, aiRecommendationReason = aiRecommendationReason
    )
    // endregion
}