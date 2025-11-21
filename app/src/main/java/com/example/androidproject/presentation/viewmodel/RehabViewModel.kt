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
import android.util.Log

@HiltViewModel
class RehabViewModel @Inject constructor(
    // [수정 완료] rehabSessionRepository가 생성자에 주입되어야 합니다.
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
        if (_uiState.value.fullRoutine.isNotEmpty() && _currentUser.value != null) {
            return // 이미 데이터가 있으면 로딩 스킵
        }
        viewModelScope.launch {
            // 초기 로딩은 true로 시작
            _uiState.update { it.copy(isLoading = true, isRoutineLoading = true) }
            startDataObservation(userId)
        }
    }

    private fun startDataObservation(userId: String) {
        viewModelScope.launch {
            userRepository.getUserProfile(userId).collectLatest { user ->
                _currentUser.value = user

                // 1. 프로필 로드 완료 후 전체 로딩(isLoading) 해제 -> 기본 UI 즉시 표시
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        userName = user.name,
                        isProfileComplete = user.name != "신규 사용자"
                    )
                }

                if (user.currentInjuryId != null) {
                    observeInjury(user.currentInjuryId!!)
                } else {
                    _currentInjury.value = null
                }

                if (_uiState.value.fullRoutine.isEmpty()) {
                    // 2. AI 루틴 로드는 별도의 로딩 상태(isRoutineLoading) 하에 시작
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

            // 1. 루틴 로드 시작 시점에 isRoutineLoading을 true로 설정 (운동/식단 영역 스피너 시작)
            _uiState.update { it.copy(isRoutineLoading = true) }

            // [핵심] 2. 오늘 날짜의 완료된 운동 기록을 DB에서 가져옵니다.
            val todaySessions = fetchTodayCompletedSessions(user.id)
            Log.d("REHAB_LOG", "로드 시점: 오늘 완료된 세션 수: ${todaySessions.size}")

            try {
                val injury = _currentInjury.value
                workoutRoutineRepository.getWorkoutRoutine(forceReload, user, injury)
                    .catch { e ->
                        _uiState.update {
                            it.copy(
                                isRoutineLoading = false, // AI 오류 발생 시 로딩 해제
                                errorMessage = "AI 루틴 오류: ${e.message}"
                            )
                        }
                    }
                    .collect { aiResult ->

                        // 1. 전체 식단을 DB에 저장 (flatten)
                        val allDiets = aiResult.scheduledDiets.flatMap { it.meals }.map { it.toDomain() }
                        dietRepository.upsertDiets(allDiets)

                        // [핵심] 3. AI 결과에 완료 기록을 반영합니다.
                        val updatedTodayExercises = filterTodayExercises(aiResult.scheduledWorkouts, todaySessions)

                        Log.d("REHAB_LOG", "AI 로드: 오늘의 운동 ${updatedTodayExercises.size}개 중 완료 ${updatedTodayExercises.count { it.isCompleted }}개 표시.")

                        _uiState.update {
                            it.copy(
                                isRoutineLoading = false, // AI 로드 완료 -> 로딩 해제
                                userName = user.name,
                                currentInjuryName = injury?.name,
                                currentInjuryArea = injury?.bodyPart,
                                fullRoutine = aiResult.scheduledWorkouts,
                                todayExercises = updatedTodayExercises,
                                recommendedDiets = filterTodayDiets(aiResult.scheduledDiets),
                                isProfileComplete = isComplete
                            )
                        }
                    }
            } catch (e: Exception) {
                _uiState.update { it.copy(isRoutineLoading = false, errorMessage = "데이터 로드 실패: ${e.message}") }
                Log.e("REHAB_LOG", "loadMainDashboardData 실패: ${e.message}")
            }
        }
    }

    fun saveRehabSessionDetails(exerciseId: String, rating: Int, notes: String, isCompleted: Boolean) {
        viewModelScope.launch {
            val user = _currentUser.value ?: return@launch
            val exercise = _uiState.value.todayExercises.find { it.exercise.id == exerciseId }?.exercise

            // ★★★ [수정 핵심] 중복 방지를 위한 고유 ID 생성 ★★★
            // 형식: USER_ID_YYYYMMDD_EXERCISE_ID
            val dateFormat = SimpleDateFormat("yyyyMMdd", Locale.US)
            val todayDateString = dateFormat.format(Date())

            // Note: isCompleted가 false일 때도 기록을 남길 수 있도록 ID는 유지합니다.
            val uniqueSessionId = "${user.id}_${todayDateString}_${exerciseId}"

            // 1. RehabSession 객체 생성 (rating과 notes를 포함)
            val session = RehabSession(
                id = uniqueSessionId, // ★★★ 고유 ID 사용 ★★★
                userId = user.id,
                exerciseId = exerciseId,
                dateTime = Date(),
                sets = exercise?.sets ?: 3,
                reps = exercise?.reps ?: 10,
                durationMinutes = 15,
                // 운동을 '완수하지 못했음'을 표시할 때 rating을 0으로 설정하여 분석에서 제외
                userRating = if (isCompleted) rating else 0,
                notes = notes
            )
            Log.d("REHAB_LOG_SAVE", "저장 요청: ID=${uniqueSessionId}, isCompleted=$isCompleted")

            // 2. 기록 저장 (Local + Firebase)
            // LocalDataSource와 FirebaseDataSource는 OnConflictStrategy.REPLACE(덮어쓰기)를 사용하므로
            // ID가 같으면 덮어써져 중복 저장이 방지됩니다.
            addRehabSessionUseCase(session).collect()

            // 3. UI 상태 업데이트 (체크박스 상태 반영)
            setExerciseCompleted(exerciseId, isCompleted)
        }
    }

    private fun setExerciseCompleted(exerciseId: String, isCompleted: Boolean) {
        _uiState.update { currentState ->
            val updatedExercises = currentState.todayExercises.map {
                // isCompleted 인자 값에 따라 체크박스 상태를 업데이트
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

            // UI를 즉시 업데이트하고 로딩 스피너만 보이도록 설정
            _uiState.update {
                it.copy(
                    isLoading = false, // 메인 화면은 로딩 해제
                    isRoutineLoading = true, // 운동 컨텐츠는 로딩 시작
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
                _uiState.update { it.copy(isRoutineLoading = false, errorMessage = "저장 실패: ${e.message}") }
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

    // createTestHistory 함수는 요청에 따라 삭제했습니다.
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

    // [핵심 함수 1] 오늘 하루 동안 완료한 세션 목록을 DB에서 가져옵니다.
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

        return try {
            val sessions = rehabSessionRepository.getRehabSessionsBetween(userId, startOfDay, endOfDay).first()
            Log.d("REHAB_LOG_FETCH", "DB 조회 성공 (오늘): 세션 ${sessions.size}개 발견.")
            sessions
        } catch (e: Exception) {
            Log.e("REHAB_LOG_FETCH", "DB 조회 실패 (오늘): ${e.message}")
            emptyList()
        }
    }

    // [핵심 함수 2] AI 계획과 완료 기록을 비교하여 isCompleted 상태를 복원합니다.
    private fun filterTodayExercises(
        fullRoutine: List<ScheduledWorkout>,
        completedSessions: List<RehabSession>
    ): List<TodayExercise> {
        val todayString = SimpleDateFormat("M월 d일 (E)", Locale.KOREA).format(Date())
        val normalize = { s: String -> s.replace(" ", "").trim() }

        Log.d("REHAB_LOG_MAP", "=== 매핑 시작 (오늘의 날짜: $todayString) ===")

        Log.d("REHAB_LOG_MAP", "=== 매핑 시작 (오늘의 날짜: $todayString) ===")

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
            // 1. AI 추천 운동 이름(aiRec.name)을 기반으로 카탈로그에서 고유 ID를 찾습니다.
            val matchingCatalogExercise = ExerciseCatalog.allExercises.find { it.name == aiRec.name }

            if (matchingCatalogExercise != null) {
                android.util.Log.d("RehabDebug", "Match found in Catalog: ${matchingCatalogExercise.name}")
                // 2. [핵심 비교] 완료된 세션 목록(completedSessions)에 현재 운동의 고유 ID가 있는지 확인합니다.
                val isCompleted = completedSessions.any { session -> session.exerciseId == matchingCatalogExercise.id }

                Log.d("REHAB_LOG_MAP", " - 운동명: ${aiRec.name}, ID: ${matchingCatalogExercise.id}, 완료 상태(isCompleted): $isCompleted")

                // isCompleted 상태를 포함하여 TodayExercise 객체를 생성합니다.
                TodayExercise(
                    // ExerciseCatalog의 원본 Exercise 객체를 복사하고 AI의 sets/reps를 덮어씁니다.
                    exercise = matchingCatalogExercise.copy(
                        sets = aiRec.sets,
                        reps = aiRec.reps
                    ),
                    isCompleted = isCompleted
                )
            } else {
                Log.e("REHAB_LOG_MAP", " - 오류: 카탈로그에 없는 운동 ${aiRec.name}가 AI 추천에 포함됨. 무시됨.")
                android.util.Log.e("RehabDebug", "NO MATCH in Catalog for: '${aiRec.name}'")
                null
            }
        }
    }

    // ✅ [복구] 오늘 식단 필터링 함수
    // ✅ [복구] 오늘 식단 필터링 함수
    private fun filterTodayDiets(scheduledDiets: List<ScheduledDiet>): List<Diet> {
        val todayDateString = SimpleDateFormat("M월 d일 (E)", Locale.KOREA).format(Date())
        android.util.Log.d("DIET_DEBUG", "Today's date string: '$todayDateString'")
        
        scheduledDiets.forEach { 
            android.util.Log.d("DIET_DEBUG", "Available scheduled date: '${it.scheduledDate}'") 
        }

        val todayDiet = scheduledDiets.find { it.scheduledDate.contains(todayDateString) }
            ?: scheduledDiets.firstOrNull().also { 
                android.util.Log.w("DIET_DEBUG", "Exact match failed. Falling back to first available: ${it?.scheduledDate}") 
            }
        
        if (todayDiet != null) {
            android.util.Log.d("DIET_DEBUG", "Match found (or fallback)! Diets count: ${todayDiet.meals.size}")
        } else {
            android.util.Log.w("DIET_DEBUG", "No matching diet found for today and list is empty.")
        }

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