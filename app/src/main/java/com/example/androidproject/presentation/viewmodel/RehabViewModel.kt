package com.example.androidproject.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.androidproject.data.local.SessionManager
import com.example.androidproject.data.local.datasource.LocalDataSource // (★필수★) import 확인
import com.example.androidproject.data.mapper.toDomain
import com.example.androidproject.domain.model.*
import com.example.androidproject.domain.repository.*
import com.example.androidproject.domain.usecase.AddRehabSessionUseCase
import com.example.androidproject.presentation.main.MainUiState
import com.example.androidproject.presentation.main.TodayExercise
import com.prolificinteractive.materialcalendarview.CalendarDay
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
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
    private val addRehabSessionUseCase: AddRehabSessionUseCase,
    private val workoutRoutineRepository: WorkoutRoutineRepository,
    private val userRepository: UserRepository,
    private val injuryRepository: InjuryRepository,
    private val dietRepository: DietRepository,
    private val sessionManager: SessionManager,
    // (★ 추가 ★) 'localDataSource'를 생성자에 '주입'합니다.
    private val localDataSource: LocalDataSource
) : ViewModel() {

    // region [StateFlow Definitions]
    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    private val _recordedDates = MutableStateFlow<Set<CalendarDay>>(emptySet())
    val recordedDates: StateFlow<Set<CalendarDay>> = _recordedDates.asStateFlow()

    // Internal State -> Public Exposure
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _currentInjury = MutableStateFlow<Injury?>(null)
    val currentInjury: StateFlow<Injury?> = _currentInjury.asStateFlow()

    // Legacy Support
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
            val user = userRepository.getUserProfile(userId).first()
            _currentUser.value = user
            dummyUser = user

            val injuryId = user.currentInjuryId
            if (injuryId != null) {
                val injury = injuryRepository.getInjuryById(injuryId).first()
                _currentInjury.value = injury
                dummyInjury = injury ?: createEmptyInjury()
            } else {
                _currentInjury.value = null
                dummyInjury = createEmptyInjury()
            }

            loadMainDashboardData(forceReload = false)
        }
    }
    // endregion

    // region [Dashboard Feature]
    fun loadMainDashboardData(forceReload: Boolean) {
        viewModelScope.launch {
            val user = _currentUser.value ?: return@launch
            val isComplete = user.name != "신규 사용자"

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
            val exercise = _uiState.value.todayExercises.find { it.exercise.id == exerciseId }?.exercise

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
        // (필요 시 구현)
    }
    // endregion

    // region [Profile Feature]
    fun updateUserProfile(updatedUser: User, updatedInjuryName: String, updatedInjuryArea: String) {
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
            // (★이제 정상 작동함★) 주입받은 localDataSource 사용
            localDataSource.clearAllData()
            sessionManager.clearSession()
            _currentUser.value = null
            _currentInjury.value = null
        }
    }

    fun createTestHistory() {
        // (테스트 데이터 생성 로직 유지)
    }
    // endregion

    // region [Helpers & Utils]
    fun clearErrorMessage() { _uiState.update { it.copy(errorMessage = null) } }

    private fun createEmptyInjury() = Injury(id = "temp", name = "없음", bodyPart = "없음", severity = "없음", description = "")

    private fun filterTodayExercises(fullRoutine: List<ScheduledWorkout>): List<TodayExercise> {
        // 1. 앱이 생성하는 오늘 날짜 포맷
        val todayString = SimpleDateFormat("M월 d일 (E)", Locale.KOREA).format(Date())

        // 2. 날짜 매칭 (공백 제거 후 비교로 유연성 확보)
        val normalize = { s: String -> s.replace(" ", "").trim() }

        return fullRoutine.find {
            normalize(it.scheduledDate).contains(normalize(todayString))
        }?.exercises?.map { it.toTodayExercise() } ?: emptyList()
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