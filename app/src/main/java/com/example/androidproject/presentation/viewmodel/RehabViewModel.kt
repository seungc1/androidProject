package com.example.androidproject.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.androidproject.domain.model.Diet
import com.example.androidproject.domain.model.DietRecommendation // (★추가★)
import com.example.androidproject.domain.model.Exercise
import com.example.androidproject.domain.model.ExerciseRecommendation // (★추가★)
import com.example.androidproject.domain.model.Injury
import com.example.androidproject.domain.model.User
import com.example.androidproject.domain.model.DietSession
import com.example.androidproject.domain.model.RehabSession
import com.example.androidproject.domain.usecase.GetAIRecommendationUseCase
import com.example.androidproject.presentation.history.HistoryItem
import com.example.androidproject.presentation.main.MainUiState
import com.example.androidproject.presentation.main.TodayExercise
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch // (★추가★)
import kotlinx.coroutines.flow.collect // (★추가★)
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Date
import java.util.UUID
import javax.inject.Inject

// (데이터 클래스 HistoryUiState, DietDetailUiState는 수정 없음)
data class HistoryUiState(
    val isLoading: Boolean = false,
    val historyItems: List<HistoryItem> = emptyList(),
    val errorMessage: String? = null
)
data class DietDetailUiState(
    val isLoading: Boolean = false,
    val diet: Diet? = null,
    val alternatives: List<String> = emptyList(),
    val errorMessage: String? = null
)


@HiltViewModel
class RehabViewModel @Inject constructor(
    private val getAIRecommendationUseCase: GetAIRecommendationUseCase
    // ... (주석 처리된 다른 UseCase들) ...
) : ViewModel() {

    // (StateFlow 선언부 수정 없음)
    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    private val _historyUiState = MutableStateFlow(HistoryUiState())
    val historyUiState: StateFlow<HistoryUiState> = _historyUiState.asStateFlow()

    private val _dietDetailState = MutableStateFlow(DietDetailUiState())
    val dietDetailState: StateFlow<DietDetailUiState> = _dietDetailState.asStateFlow()

    lateinit var dummyUser: User
    lateinit var dummyInjury: Injury

    init {
        loadMainDashboardData()
    }

    // (★★★★★ 여기가 핵심 수정 사항 ★★★★★)
    private fun loadMainDashboardData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                // 1. (유지) 프로필/부상 정보는 아직 로그인/선택 기능이 없으므로
                //    'ProfileFragment'와 'UseCase'에서 사용할 더미 데이터를 생성합니다.
                dummyUser = User(
                    id = "user01", name = "김재활", gender = "남성", age = 30,
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

                // 2. (★수정★) 'dummyExercises'와 'dummyDiets' 리스트 생성 '삭제'
                //    대신 'GetAIRecommendationUseCase'를 '호출'합니다.

                // 3단계에서 설정한 Build Variant에 따라
                // 'debug' 모드면 FakeAIApiRepository가,
                // 'release' 모드면 AIApiRepositoryImpl이 자동 실행됩니다.
                getAIRecommendationUseCase(dummyUser.id, dummyInjury)
                    .catch { e ->
                        // 4. (★추가★) UseCase (Flow) 실행 중 오류 처리
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                userName = dummyUser.name, // 이름은 표시
                                errorMessage = "AI 추천을 불러오는 중 오류 발생: ${e.message}"
                            )
                        }
                    }
                    .collect { aiResult ->
                        // 5. (★추가★) UseCase가 성공적으로 AI응답(aiResult)을 가져온 경우
                        //    Domain 모델(AIResult)을 UI 모델(MainUiState)로 '매핑(변환)'합니다.
                        _uiState.value = MainUiState(
                            isLoading = false,
                            userName = dummyUser.name,
                            currentInjuryName = dummyInjury.name,
                            currentInjuryArea = dummyInjury.bodyPart,
                            // (★핵심★) AI 추천 모델을 UI 모델로 변환
                            todayExercises = aiResult.recommendedExercises.toTodayExerciseList(),
                            recommendedDiets = aiResult.recommendedDiets.toDietList(),
                            errorMessage = null
                        )
                    }

            } catch (e: Exception) {
                // (유지) 더미 데이터 생성 중 발생할 수 있는 예외 처리 (거의 발생 안 함)
                _uiState.update { it.copy(isLoading = false, errorMessage = "데이터 로드 실패: ${e.message}") }
            }
        }
    }

    // (★★★★★ 이하 Mapper 함수 2개 추가 ★★★★★)

    /**
     * [AIRecommendationResult]의 List<ExerciseRecommendation>을
     * [MainUiState]의 List<TodayExercise>로 변환합니다.
     */
    /**
     * [AIRecommendationResult]의 List<ExerciseRecommendation>을
     * [MainUiState]의 List<TodayExercise>로 변환합니다.
     */
    private fun List<ExerciseRecommendation>.toTodayExerciseList(): List<TodayExercise> {
        return this.map { rec ->
            // AI 모델(ExerciseRecommendation)을 Domain 모델(Exercise)로 변환
            val exercise = Exercise(
                id = rec.name, // (AI가 ID를 주지 않으므로 이름으로 임시 ID 사용)
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
            // Domain 모델(Exercise)을 UI 모델(TodayExercise)로 래핑
            TodayExercise(
                exercise = exercise,
                isCompleted = false // (기본값은 '미완료')
            )
        }
    }

    /**
     * [AIRecommendationResult]의 List<DietRecommendation>을
     * [MainUiState]의 List<Diet>로 변환합니다.
     */
    private fun List<DietRecommendation>.toDietList(): List<Diet> {
        return this.map { rec ->
            // AI 모델(DietRecommendation)을 Domain 모델(Diet)로 변환
            Diet(
                id = rec.foodItems.joinToString(), // (AI가 ID를 주지 않으므로 음식 이름 조합으로 임시 ID 사용)
                mealType = rec.mealType,
                foodName = rec.foodItems.joinToString(", "),
                quantity = 1.0, // (AI가 '양'을 주지 않았으므로 기본값 1.0)
                unit = "인분",  // (AI가 '단위'를 주지 않았으므로 기본값 '인분')
                calorie = rec.calories?.toInt() ?: 0,
                protein = rec.proteinGrams ?: 0.0,
                fat = rec.fats ?: 0.0,
                carbs = rec.carbs ?: 0.0,
                ingredients = rec.ingredients,
                preparationTips = null, // (AI가 준 tips가 있다면 여기에 매핑)
                aiRecommendationReason = rec.aiRecommendationReason
            )
        }
    }


    // --- (이하 나머지 함수들은 수정 없음) ---

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

    fun saveRehabSessionDetails(exerciseId: String, rating: Int, notes: String) {
        viewModelScope.launch {
            val _session = RehabSession(
                id = UUID.randomUUID().toString(),
                userId = "user01",
                exerciseId = exerciseId,
                dateTime = Date(),
                sets = 3, reps = 10, durationMinutes = 15,
                notes = notes,
                userRating = rating
            )
            // (실제 연동)
            // val result = addRehabSessionUseCase(_session)

            setExerciseCompleted(exerciseId, true)
        }
    }

    fun clearErrorMessage() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    // ... (loadHistory, clearHistoryErrorMessage 함수 수정 없음) ...
    fun loadHistory(date: Date) {
        viewModelScope.launch {
            _historyUiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                kotlinx.coroutines.delay(500)
                val dummyHistoryItems = listOf(
                    HistoryItem.Exercise(
                        RehabSession(
                            id = "session001", userId = "user01", exerciseId = "ex001",
                            dateTime = date,
                            sets = 3, reps = 10, durationMinutes = 15,
                            notes = "조금 아팠음",
                            userRating = 3
                        )
                    ),
                    HistoryItem.Diet(
                        DietSession(
                            id = "dietSession001", userId = "user01", dietId = "d001",
                            dateTime = date,
                            actualQuantity = 1.0, actualUnit = "그릇",
                            userSatisfaction = 5
                        )
                    ),
                    HistoryItem.Exercise(
                        RehabSession(
                            id = "session002", userId = "user01", exerciseId = "ex002",
                            dateTime = date,
                            sets = 5, reps = 15, durationMinutes = 20,
                            notes = "완료",
                            userRating = 5
                        )
                    )
                )
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
            _uiState.update { currentState ->
                currentState.copy(
                    userName = updatedUser.name,
                    currentInjuryName = updatedInjuryName,
                    currentInjuryArea = updatedInjuryArea
                )
            }
        }
    }
}