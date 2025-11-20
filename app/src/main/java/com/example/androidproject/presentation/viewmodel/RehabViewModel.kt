// 파일 경로: app/src/main/java/com/example/androidproject/presentation/viewmodel/RehabViewModel.kt
package com.example.androidproject.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.androidproject.data.local.SessionManager
import com.example.androidproject.data.local.datasource.LocalDataSource
import com.example.androidproject.data.mapper.toDomain
import com.example.androidproject.data.mapper.toEntity // ★★★ [수정] toEntity import 직접 추가 ★★★
import com.example.androidproject.domain.model.*
import com.example.androidproject.domain.repository.*
import com.example.androidproject.domain.usecase.AddDietSessionUseCase
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
                        _currentUser.value?.let { loadAllSessionDates(it.id) }
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

            // ★★★ [수정] 세션 저장 후, 완료 상태도 업데이트/저장하도록 호출 ★★★
            setExerciseCompleted(exerciseId, true)
        }
    }

    // ★★★ [수정] 운동 완료 상태를 DB에 영구 저장하는 로직 포함 ★★★
    fun setExerciseCompleted(exerciseId: String, isCompleted: Boolean) {
        viewModelScope.launch {
            val user = _currentUser.value ?: return@launch
            val fullRoutine = _uiState.value.fullRoutine

            // 1. 인메모리 및 전체 루틴 상태 업데이트
            val updatedFullRoutine = fullRoutine.map { scheduledWorkout ->
                val todayString = SimpleDateFormat("M월 d일 (E)", Locale.KOREA).format(Date())
                val normalize = { s: String -> s.replace(" ", "").trim() }

                // 오늘 날짜 루틴인 경우에만 상태 업데이트
                if (normalize(scheduledWorkout.scheduledDate).contains(normalize(todayString))) {
                    val updatedExercises = scheduledWorkout.exercises.map { exerciseRec ->
                        // isCompleted 필드가 ExerciseRecommendation 모델에 있다고 가정
                        if (exerciseRec.name == exerciseId) {
                            exerciseRec.copy(isCompleted = isCompleted)
                        } else {
                            exerciseRec
                        }
                    }
                    scheduledWorkout.copy(exercises = updatedExercises)
                } else {
                    scheduledWorkout
                }
            }

            // 2. Room DB 및 Firebase에 영구 저장 (새로운 Repository 함수 호출)
            workoutRoutineRepository.upsertWorkoutRoutineState(user.id, updatedFullRoutine)

            // 3. UI State의 todayExercises만 따로 업데이트 (UI 갱신용)
            val updatedTodayExercises = filterTodayExercises(updatedFullRoutine)

            _uiState.update {
                it.copy(
                    todayExercises = updatedTodayExercises,
                    fullRoutine = updatedFullRoutine // 전체 루틴도 업데이트
                )
            }
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
            localDataSource.clearAllData()
            sessionManager.clearSession()
            _currentUser.value = null
            _currentInjury.value = null
        }
    }

    /**
     * ★★★ [수정] 테스트 기록 생성 시 운동/식단 정의 데이터 추가 ★★★
     */
    fun createTestHistory() {
        viewModelScope.launch {
            val user = _currentUser.value ?: return@launch
            _uiState.update { it.copy(isLoading = true) }

            val calendar = Calendar.getInstance()

            // 1. 운동 및 식단 정의 데이터 (Lookup Data)
            val exerciseDefinitions = listOf(
                Exercise(id = "가벼운 스트레칭", name = "가벼운 스트레칭", description = "전신 이완", bodyPart = "전신", difficulty = "초급", precautions = null, sets = null, reps = null, aiRecommendationReason = null, imageName = "neck_lateral_flexion"),
                Exercise(id = "의자 스쿼트", name = "의자 스쿼트", description = "하체 근력", bodyPart = "하체", difficulty = "초급", precautions = null, sets = null, reps = null, aiRecommendationReason = null, imageName = "chair_squat_sit_to_stand"),
                Exercise(id = "벽 짚고 팔굽혀펴기", name = "벽 짚고 팔굽혀펴기", description = "상체 근력", bodyPart = "상체", difficulty = "초급", precautions = null, sets = null, reps = null, aiRecommendationReason = null, imageName = "wall_push_up"),
                Exercise(id = "제자리 걷기", name = "제자리 걷기", description = "유산소", bodyPart = "전신", difficulty = "초급", precautions = null, sets = null, reps = null, aiRecommendationReason = null, imageName = "marching_in_place")
            )
            val dietDefinitions = listOf(
                Diet(id = "닭가슴살 샐러드", mealType = "점심", foodName = "닭가슴살 샐러드", quantity = 1.0, unit = "인분", calorie = 300, protein = 30.0, fat = 10.0, carbs = 20.0, ingredients = listOf("닭가슴살", "채소"), preparationTips = null),
                Diet(id = "현미밥과 나물", mealType = "저녁", foodName = "현미밥과 나물", quantity = 1.0, unit = "인분", calorie = 450, protein = 15.0, fat = 5.0, carbs = 70.0, ingredients = listOf("현미", "나물"), preparationTips = null),
                Diet(id = "고구마와 우유", mealType = "아침", foodName = "고구마와 우유", quantity = 1.0, unit = "세트", calorie = 250, protein = 10.0, fat = 5.0, carbs = 40.0, ingredients = listOf("고구마", "우유"), preparationTips = null),
                Diet(id = "연어 스테이크", mealType = "저녁", foodName = "연어 스테이크", quantity = 1.0, unit = "인분", calorie = 500, protein = 40.0, fat = 30.0, carbs = 10.0, ingredients = listOf("연어", "버터"), preparationTips = null)
            )

            // 2. 정의 데이터를 로컬 DB에 먼저 저장
            localDataSource.upsertExercises(exerciseDefinitions.map { it.toEntity() })
            dietRepository.upsertDiets(dietDefinitions)

            // 3. 세션 기록 생성 및 저장 (과거 7일)
            for (i in 1..7) {
                calendar.add(Calendar.DAY_OF_YEAR, -1) // 하루씩 과거로 이동
                val date = calendar.time

                // 운동 기록 생성
                val rehabSession = RehabSession(
                    id = UUID.randomUUID().toString(),
                    userId = user.id,
                    exerciseId = exerciseDefinitions.random().name, // 정의된 ID 사용
                    dateTime = date,
                    sets = (2..4).random(),
                    reps = (10..15).random(),
                    durationMinutes = (15..40).random(),
                    userRating = (3..5).random(),
                    notes = "테스트 기록: $i 일 전 운동 완료"
                )
                addRehabSessionUseCase(rehabSession).collect()

                // 식단 기록 생성
                val dietSession = DietSession(
                    id = UUID.randomUUID().toString(),
                    userId = user.id,
                    dietId = dietDefinitions.random().id, // 정의된 ID 사용
                    dateTime = date,
                    actualQuantity = 1.0,
                    actualUnit = "인분",
                    userSatisfaction = (3..5).random(),
                    notes = "테스트 기록: ${dietDefinitions.random().foodName} 섭취"
                )
                addDietSessionUseCase(dietSession).collect()
            }

            _uiState.update { it.copy(isLoading = false) }
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
            sets = sets, reps = reps, aiRecommendationReason = aiRecommendationReason,
            imageName = null
        ),
        isCompleted = isCompleted
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