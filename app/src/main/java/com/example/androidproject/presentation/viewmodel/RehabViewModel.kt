package com.example.androidproject.presentation.viewmodel

import com.example.androidproject.data.ExerciseCatalog
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.androidproject.data.local.SessionManager
import com.example.androidproject.data.local.datasource.LocalDataSource
import com.example.androidproject.data.mapper.toDomain
import com.example.androidproject.domain.model.*
import com.example.androidproject.domain.repository.*
import com.example.androidproject.domain.usecase.AddRehabSessionUseCase
import com.example.androidproject.domain.usecase.AddDietSessionUseCase
import com.example.androidproject.data.remote.datasource.FirebaseDataSource
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
    // ★★★ [수정] 여기에 rehabSessionRepository를 추가했습니다 ★★★
    private val rehabSessionRepository: RehabSessionRepository,
    private val addRehabSessionUseCase: AddRehabSessionUseCase,
    private val workoutRoutineRepository: WorkoutRoutineRepository,
    private val addDietSessionUseCase: AddDietSessionUseCase,
    private val userRepository: UserRepository,
    private val injuryRepository: InjuryRepository,
    private val dietRepository: DietRepository,
    private val sessionManager: SessionManager,
    private val localDataSource: LocalDataSource,
    private val firebaseDataSource: FirebaseDataSource
) : ViewModel() {

    // region [StateFlow Definitions]
    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    private val _recordedDates = MutableStateFlow<Set<CalendarDay>>(emptySet())
    val recordedDates: StateFlow<Set<CalendarDay>> = _recordedDates.asStateFlow()

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _currentInjury = MutableStateFlow<Injury?>(null)
    val currentInjury: StateFlow<Injury?> = _currentInjury.asStateFlow()

    // Legacy Support
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

    private fun startDataObservation(userId: String) {
        viewModelScope.launch {
            userRepository.getUserProfile(userId).collectLatest { user ->
                _currentUser.value = user

                if (user.currentInjuryId != null) {
                    observeInjury(user.currentInjuryId!!)
                } else {
                    _currentInjury.value = null
                }

                if (_uiState.value.fullRoutine.isEmpty()) {
                    loadMainDashboardData(forceReload = false)
                }
            }
        }
    }

    private var currentInjuryJob: kotlinx.coroutines.Job? = null

    private fun observeInjury(injuryId: String) {
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

            // [핵심] 오늘 날짜의 완료된 운동 기록을 가져옵니다.
            val todaySessions = fetchTodayCompletedSessions(user.id)

            if (!forceReload && _uiState.value.fullRoutine.isNotEmpty()) {
                // [핵심] 가져온 기록을 필터 함수에 전달하여 체크 상태를 반영합니다.
                val todayExercises = filterTodayExercises(_uiState.value.fullRoutine, todaySessions)
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
                        // 1. 전체 식단을 DB에 저장 (flatten)
                        val allDiets = aiResult.scheduledDiets.flatMap { it.meals }.map { it.toDomain() }
                        dietRepository.upsertDiets(allDiets)

                        _uiState.value = MainUiState(
                            isLoading = false,
                            userName = user.name,
                            currentInjuryName = injury?.name,
                            currentInjuryArea = injury?.bodyPart,
                            fullRoutine = aiResult.scheduledWorkouts,
                            // [핵심] 여기서도 완료 기록(todaySessions)을 반영합니다.
                            todayExercises = filterTodayExercises(aiResult.scheduledWorkouts, todaySessions),
                            recommendedDiets = filterTodayDiets(aiResult.scheduledDiets), // ✅ [수정] 오늘 식단만 필터링
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
    fun updateUserProfile(updatedUser: User, updatedInjuryName: String, updatedInjuryArea: String) {
        viewModelScope.launch {
            val user = _currentUser.value ?: return@launch

            _uiState.update {
                it.copy(
                    isLoading = true,
                    fullRoutine = emptyList(),
                    todayExercises = emptyList()
                )
            }

            try {
                val newInjury = Injury(
                    id = _currentInjury.value?.id ?: "injury_${user.id}",
                    name = updatedInjuryName,
                    bodyPart = updatedInjuryArea,
                    severity = _currentInjury.value?.severity ?: "경미",
                    description = _currentInjury.value?.description ?: "정보 없음"
                )
                injuryRepository.upsertInjury(newInjury, user.id)

                val userToUpdate = updatedUser.copy(currentInjuryId = newInjury.id)
                userRepository.updateUserProfile(userToUpdate).collect()

                _currentUser.value = userToUpdate
                _currentInjury.value = newInjury

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

    fun deleteAllUserData() {
        viewModelScope.launch {
            val userId = _currentUser.value?.id ?: return@launch

            _uiState.update { it.copy(isLoading = true) }

            try {
                localDataSource.clearAllData()
                firebaseDataSource.clearAllRehabData(userId)

                sessionManager.clearSession()
                _currentUser.value = null
                _currentInjury.value = null
                _uiState.update { MainUiState(isLoading = false, isProfileComplete = false) }

            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "데이터 삭제 실패: ${e.message}") }
            }
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

    // region [Diet Recording]
    fun recordDiet(
        foodName: String,
        photoUri: android.net.Uri?,
        mealType: String,
        quantity: Double,
        unit: String,
        satisfaction: Int
    ) {
        viewModelScope.launch {
            val user = _currentUser.value
            android.util.Log.d("DIET_RECORD", "recordDiet called: user=${user?.id}, foodName=$foodName")
            
            if (user == null) {
                android.util.Log.e("DIET_RECORD", "User is null, cannot save diet")
                return@launch
            }

            try {
                // 사진 URI를 문자열로 저장 (실제로는 파일로 복사하거나 Firebase Storage에 업로드해야 함)
                val photoPath = photoUri?.toString()

                val dietSession = DietSession(
                    id = UUID.randomUUID().toString(),
                    userId = user.id,
                    dietId = "user_recorded_${System.currentTimeMillis()}", // 사용자 기록은 특별한 ID
                    dateTime = Date(),
                    actualQuantity = quantity,
                    actualUnit = unit,
                    userSatisfaction = satisfaction,
                    notes = "사용자가 직접 기록한 식단",
                    foodName = foodName, // [추가] 사용자 입력 음식 이름
                    photoUrl = photoPath // [추가] 사진 경로
                )

                android.util.Log.d("DIET_RECORD", "Calling addDietSessionUseCase with session: ${dietSession.id}")
                addDietSessionUseCase(dietSession).collect {
                    android.util.Log.d("DIET_RECORD", "Diet session saved successfully: ${dietSession.id}")
                }
            } catch (e: Exception) {
                android.util.Log.e("DIET_RECORD", "Error saving diet: ${e.message}", e)
                e.printStackTrace()
                _uiState.update { it.copy(errorMessage = "식단 기록 실패: ${e.message}") }
            }
        }
    }
    // endregion

    // region [Helpers & Utils]
    fun clearErrorMessage() { _uiState.update { it.copy(errorMessage = null) } }

    private fun createEmptyInjury() = Injury(id = "temp", name = "없음", bodyPart = "없음", severity = "없음", description = "")

    // [추가] 오늘 하루 동안 완료한 세션 목록을 가져오는 함수
    private suspend fun fetchTodayCompletedSessions(userId: String): List<RehabSession> {
        val calendar = Calendar.getInstance()

        // 오늘의 시작 시간 (00:00:00.000)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfDay = calendar.time

        // 오늘의 끝 시간 (23:59:59.999)
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        val endOfDay = calendar.time

        // 주입받은 rehabSessionRepository를 사용하여 DB 조회
        return try {
            rehabSessionRepository.getRehabSessionsBetween(userId, startOfDay, endOfDay).first()
        } catch (e: Exception) {
            emptyList()
        }
    }

    // [수정] completedSessions(DB기록)과 비교하여 isCompleted 설정
    private fun filterTodayExercises(
        fullRoutine: List<ScheduledWorkout>,
        completedSessions: List<RehabSession>
    ): List<TodayExercise> {
        val todayString = SimpleDateFormat("M월 d일 (E)", Locale.KOREA).format(Date())
        val normalize = { s: String -> s.replace(" ", "").trim() }

        android.util.Log.d("RehabDebug", "Today: $todayString (Normalized: ${normalize(todayString)})")
        android.util.Log.d("RehabDebug", "FullRoutine Size: ${fullRoutine.size}")
        fullRoutine.forEach { 
            android.util.Log.d("RehabDebug", "Routine Date: ${it.scheduledDate} (Normalized: ${normalize(it.scheduledDate)})")
        }

        val todayWorkout = fullRoutine.find {
            normalize(it.scheduledDate).contains(normalize(todayString))
        }

        if (todayWorkout == null) {
            android.util.Log.d("RehabDebug", "No matching workout found for today.")
            return emptyList()
        }

        android.util.Log.d("RehabDebug", "Found workout for today. Exercises: ${todayWorkout.exercises.size}")

        return todayWorkout.exercises.mapNotNull { aiRec ->
            android.util.Log.d("RehabDebug", "Processing AI Exercise: '${aiRec.name}'")
            val matchingCatalogExercise = ExerciseCatalog.allExercises.find { it.name == aiRec.name }

            if (matchingCatalogExercise != null) {
                android.util.Log.d("RehabDebug", "Match found in Catalog: ${matchingCatalogExercise.name}")
                // ★ [핵심] DB 기록 중에 현재 운동 ID와 일치하는 것이 있는지 확인
                val isDone = completedSessions.any { it.exerciseId == matchingCatalogExercise.id }

                TodayExercise(
                    exercise = Exercise(
                        id = matchingCatalogExercise.id,
                        name = aiRec.name,
                        description = aiRec.description,
                        bodyPart = aiRec.bodyPart,
                        difficulty = aiRec.difficulty,
                        precautions = matchingCatalogExercise.precautions,
                        sets = aiRec.sets,
                        reps = aiRec.reps,
                        aiRecommendationReason = aiRec.aiRecommendationReason,
                        imageName = matchingCatalogExercise.imageName
                    ),
                    isCompleted = isDone // ★ DB 상태 반영
                )
            } else {
                android.util.Log.e("RehabDebug", "NO MATCH in Catalog for: '${aiRec.name}'")
                null
            }
        }
    }

    // ✅ [복구] 오늘 식단 필터링 함수
    private fun filterTodayDiets(scheduledDiets: List<ScheduledDiet>): List<Diet> {
        val todayDateString = SimpleDateFormat("M월 d일 (E)", Locale.KOREA).format(Date())
        val todayDiet = scheduledDiets.find { it.scheduledDate.contains(todayDateString) }
        return todayDiet?.meals?.toDietList() ?: emptyList()
    }

    private fun List<DietRecommendation>.toDietList(): List<Diet> {
        return this.map { it.toDomain() }
    }

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