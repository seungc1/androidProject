package com.example.androidproject.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.androidproject.data.local.SessionManager
import com.example.androidproject.data.local.datasource.LocalDataSource
import com.example.androidproject.data.mapper.toDomain
import com.example.androidproject.domain.model.*
import com.example.androidproject.domain.repository.*
import com.example.androidproject.domain.usecase.AddRehabSessionUseCase
import com.example.androidproject.domain.usecase.AddDietSessionUseCase
import com.example.androidproject.presentation.main.MainUiState
import com.example.androidproject.presentation.main.TodayExercise
import com.prolificinteractive.materialcalendarview.CalendarDay
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
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
    private val addDietSessionUseCase: AddDietSessionUseCase,
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

    // (★수정★) StateFlow로 변경하여 UI가 항상 최신값을 바라보게 함
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _currentInjury = MutableStateFlow<Injury?>(null)
    val currentInjury: StateFlow<Injury?> = _currentInjury.asStateFlow()

    // Legacy Support (삭제하거나 currentUser.value로 대체하는 것이 좋지만 호환성 유지)
    // getter를 사용하여 항상 최신 값을 반환하도록 변경
    val dummyUser: User get() = _currentUser.value ?: User(id="", password="", name="로딩중", gender="", age=0, heightCm=0, weightKg=0.0, activityLevel="", fitnessGoal="", allergyInfo=emptyList(), preferredDietType="", preferredDietaryTypes=emptyList(), equipmentAvailable=emptyList(), currentPainLevel=0)
    val dummyInjury: Injury get() = _currentInjury.value ?: createEmptyInjury()
    // endregion

    // region [Initialization & Entry Point]
    fun loadDataForUser(userId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            startDataObservation(userId)
        }
    }

    /**
     * (★수정★) 데이터를 '일회성'이 아닌 '지속적'으로 관찰합니다.
     */
    private fun startDataObservation(userId: String) {
        // 1. 사용자 정보 관찰
        viewModelScope.launch {
            userRepository.getUserProfile(userId).collectLatest { user ->
                _currentUser.value = user

                // 사용자 정보가 로드되면, 그 안의 injuryId로 부상 정보 관찰 시작
                if (user.currentInjuryId != null) {
                    observeInjury(user.currentInjuryId!!)
                } else {
                    _currentInjury.value = null
                }

                // 대시보드 데이터 로드 (최초 1회 또는 필요시)
                if (_uiState.value.fullRoutine.isEmpty()) {
                    loadMainDashboardData(forceReload = false)
                }
            }
        }
    }

    private var currentInjuryJob: kotlinx.coroutines.Job? = null

    private fun observeInjury(injuryId: String) {
        // 기존 관찰 작업이 있다면 취소 (중복 방지)
        currentInjuryJob?.cancel()
        currentInjuryJob = viewModelScope.launch {
            injuryRepository.getInjuryById(injuryId).collectLatest { injury ->
                _currentInjury.value = injury
            }
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
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        todayExercises = todayExercises,
                        isProfileComplete = isComplete
                    )
                }
                return@launch
            }

            try {
                val injury = _currentInjury.value
                // (주의) injury가 null이어도 루틴 생성은 시도함
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
    // endregion

    // region [Profile Feature]
    // [수정] 프로필 업데이트 시 기존 루틴 UI 초기화 및 강제 리로드
    fun updateUserProfile(updatedUser: User, updatedInjuryName: String, updatedInjuryArea: String) {
        viewModelScope.launch {
            val user = _currentUser.value ?: return@launch

            // 1. 로딩 시작 및 기존 루틴 UI에서 제거 (사용자에게 갱신됨을 알림)
            _uiState.update {
                it.copy(
                    isLoading = true,
                    fullRoutine = emptyList(), // 기존 데이터 화면에서 삭제
                    todayExercises = emptyList()
                )
            }

            try {
                // 2. 부상 정보 생성 및 저장
                val newInjury = Injury(
                    id = _currentInjury.value?.id ?: "injury_${user.id}",
                    name = updatedInjuryName,
                    bodyPart = updatedInjuryArea,
                    severity = _currentInjury.value?.severity ?: "경미",
                    description = _currentInjury.value?.description ?: "정보 없음"
                )
                injuryRepository.upsertInjury(newInjury, user.id)

                // 3. 사용자 정보 업데이트 (부상 ID 연결)
                val userToUpdate = updatedUser.copy(currentInjuryId = newInjury.id)
                userRepository.updateUserProfile(userToUpdate).collect()

                android.util.Log.d("DEBUG_DELETE", "ViewModel: 프로필 업데이트 완료. 강제 리로드(Force Reload) 요청 시작")
                // 4. 로컬 상태 즉시 업데이트
                _currentUser.value = userToUpdate
                _currentInjury.value = newInjury

                // 5. [핵심] 강제 리로드 요청 (기존 DB 데이터 삭제 후 AI 재요청)
                loadMainDashboardData(forceReload = true)

            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "저장 실패: ${e.message}") }
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            localDataSource.clearAllData()
            sessionManager.clearSession()
            _currentUser.value = null
            _currentInjury.value = null
        }
    }

    fun createTestHistory() {
        viewModelScope.launch {
            val user = _currentUser.value ?: return@launch
            _uiState.update { it.copy(isLoading = true) }

            try {
                val calendar = Calendar.getInstance()
                val exerciseNames = listOf("가벼운 스트레칭", "의자 스쿼트", "벽 짚고 팔굽혀펴기", "제자리 걷기")
                val dietNames = listOf("닭가슴살 샐러드", "현미밥과 나물", "고구마와 우유", "연어 스테이크")

                for (i in 1..7) {
                    calendar.add(Calendar.DAY_OF_YEAR, -1)
                    val date = calendar.time

                    val rehabSession = RehabSession(
                        id = UUID.randomUUID().toString(),
                        userId = user.id,
                        exerciseId = exerciseNames.random(),
                        dateTime = date,
                        sets = (2..4).random(),
                        reps = (10..15).random(),
                        durationMinutes = (15..40).random(),
                        userRating = (3..5).random(),
                        notes = "테스트 기록: $i 일 전 운동 완료"
                    )
                    addRehabSessionUseCase(rehabSession).collect()

                    val dietSession = DietSession(
                        id = UUID.randomUUID().toString(),
                        userId = user.id,
                        dietId = "diet_${i}",
                        dateTime = date,
                        actualQuantity = 1.0,
                        actualUnit = "인분",
                        userSatisfaction = (3..5).random(),
                        notes = "테스트 기록: ${dietNames.random()} 섭취"
                    )
                    addDietSessionUseCase(dietSession).collect()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }
    // endregion

    // region [Helpers & Utils]
    fun clearErrorMessage() { _uiState.update { it.copy(errorMessage = null) } }

    private fun createEmptyInjury() = Injury(id = "temp", name = "없음", bodyPart = "없음", severity = "없음", description = "")

    private fun filterTodayExercises(fullRoutine: List<ScheduledWorkout>): List<TodayExercise> {
        val todayString = SimpleDateFormat("M월 d일 (E)", Locale.KOREA).format(Date())
        val normalize = { s: String -> s.replace(" ", "").trim() }

        return fullRoutine.find {
            normalize(it.scheduledDate).contains(normalize(todayString))
        }?.exercises?.map { it.toTodayExercise() } ?: emptyList()
    }

    private fun ExerciseRecommendation.toTodayExercise() = TodayExercise(
        exercise = Exercise(
            id = name, name = name, description = description, bodyPart = bodyPart,
            difficulty = difficulty, precautions = null,
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