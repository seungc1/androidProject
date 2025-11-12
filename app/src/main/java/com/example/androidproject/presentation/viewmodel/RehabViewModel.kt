package com.example.androidproject.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
// (★수정★) 님의 'tree'에 있는 '필드값'과 'UseCase'를 import 합니다.
import com.example.androidproject.domain.model.Diet
import com.example.androidproject.domain.model.Exercise
import com.example.androidproject.domain.model.Injury
import com.example.androidproject.domain.model.User
// (★추가★) '기록' 화면에서 사용할 '필드값' 모델 import
import com.example.androidproject.domain.model.DietSession
import com.example.androidproject.domain.model.RehabSession
import com.example.androidproject.domain.usecase.GetAIRecommendationUseCase
// import com.example.androidproject.domain.usecase.* // (Hilt가 '실제' UseCase를 주입합니다)
// (★추가★) '기록' 화면에서 사용할 'HistoryItem' import
import com.example.androidproject.presentation.history.HistoryItem
import com.example.androidproject.presentation.main.MainUiState
import com.example.androidproject.presentation.main.TodayExercise
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first // (★ 추가 ★) 'first()' import
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Date
import java.util.UUID
import javax.inject.Inject

// (★기존★) '기록' 화면(HistoryFragment)을 위한 UI 상태
data class HistoryUiState(
    val isLoading: Boolean = false,
    val historyItems: List<HistoryItem> = emptyList(),
    val errorMessage: String? = null
)

// (★ 추가 ★) '식단 상세' 화면(DietDetailFragment)을 위한 UI 상태
data class DietDetailUiState(
    val isLoading: Boolean = false,
    val diet: Diet? = null,
    val alternatives: List<String> = emptyList(),
    val errorMessage: String? = null
)


@HiltViewModel
class RehabViewModel @Inject constructor(
    private val getAIRecommendationUseCase: GetAIRecommendationUseCase,
    // (★추가★) '저장' 시 '시뮬레이션'을 위해 'UseCase' '주입' (주석 처리)
    // private val addRehabSessionUseCase: AddRehabSessionUseCase
    // (★추가★) '기록' 화면을 위한 UseCase (시뮬레이션)
    // private val getHistoryUseCase: GetHistoryUseCase
) : ViewModel() {

    // --- '홈' 화면 (Main) 상태 관리 ---
    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    // --- '기록' 화면 (History) 상태 관리 ---
    private val _historyUiState = MutableStateFlow(HistoryUiState())
    val historyUiState: StateFlow<HistoryUiState> = _historyUiState.asStateFlow()

    // (★ 추가 ★) --- '식단 상세' 화면 (Diet Detail) 상태 관리 ---
    private val _dietDetailState = MutableStateFlow(DietDetailUiState())
    val dietDetailState: StateFlow<DietDetailUiState> = _dietDetailState.asStateFlow()


    init {
        loadMainDashboardData()
    }

    private fun loadMainDashboardData() {
        // ... (기존 loadMainDashboardData 함수 내용은 수정 없음) ...
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                // --- 더미 데이터 (시뮬레이션) ---
                kotlinx.coroutines.delay(1000) // 1초 로딩 딜레이

                val dummyUser = User(
                    id = "user01", name = "김재활", gender = "남성", age = 30,
                    heightCm = 175, weightKg = 70.5, activityLevel = "활동적",
                    fitnessGoal = "근육 증가", allergyInfo = listOf("땅콩"),
                    preferredDietType = "일반", targetCalories = 2500,
                    currentInjuryId = "injury01",
                    preferredDietaryTypes = listOf("일반식", "저염식"),
                    equipmentAvailable = listOf("덤벨", "밴드"),
                    currentPainLevel = 4,
                    additionalNotes = "부상 회복에 집중하고 싶습니다."
                )
                val dummyInjury = Injury(
                    id = "injury01", name = "손목 염좌", bodyPart = "손목",
                    severity = "경미", description = "가벼운 통증이 있는 상태"
                )
                val dummyExercises = listOf(
                    Exercise(
                        id = "ex001", name = "손목 스트레칭 (가볍게)",
                        description = "손목을 부드럽게 돌려줍니다.", bodyPart = "손목",
                        difficulty = "초급", videoUrl = null, precautions = "통증이 느껴지면 중단",
                        aiRecommendationReason = "경미한 손목 염좌 회복에 도움"
                    ),
                    Exercise(
                        id = "ex002", name = "가벼운 스쿼트",
                        description = "...", bodyPart = "하체",
                        difficulty = "초급", videoUrl = null, precautions = null,
                        aiRecommendationReason = "전반적인 근력 유지"
                    )
                )
                val dummyDiets = listOf(
                    Diet(
                        id = "d001", mealType = "아침", foodName = "오트밀과 블루베리",
                        quantity = 1.0, unit = "그릇", calorie = 350, protein = 10.0,
                        fat = 5.0, carbs = 60.0, ingredients = listOf("오트밀", "블루베리", "우유"),
                        preparationTips = "오트밀을 우유에 불려 드세요.",
                        aiRecommendationReason = "균형잡힌 탄수화물과 항산화제 제공"
                    ),
                    Diet(
                        id = "d002", mealType = "점심", foodName = "닭가슴살 샐러드",
                        quantity = 200.0, unit = "g", calorie = 450, protein = 40.0,
                        fat = 15.0, carbs = 20.0, ingredients = listOf("닭가슴살", "양상추", "토마토"),
                        preparationTips = "닭가슴살은 굽거나 삶아서 준비",
                        aiRecommendationReason = "근육 회복에 필요한 고단백 식단"
                    )
                )
                // --- 더미 데이터 끝 ---

                _uiState.value = MainUiState(
                    isLoading = false,
                    userName = dummyUser.name,
                    currentInjuryName = dummyInjury.name,
                    todayExercises = dummyExercises.map { TodayExercise(it, false) },
                    recommendedDiets = dummyDiets,
                    errorMessage = null
                )

            } catch (e: Exception) {
                // (시뮬레이션) 에러 처리
                _uiState.update { it.copy(isLoading = false, errorMessage = "데이터 로드 실패: ${e.message}") }
            }
        }
    }

    // --- '홈' 화면 (Main) 기능 (수정 없음) ---

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
            val session = RehabSession(
                id = UUID.randomUUID().toString(),
                userId = "user01",
                exerciseId = exerciseId,
                dateTime = Date(),
                sets = 3, reps = 10, durationMinutes = 15,
                notes = notes,
                userRating = rating
            )
            // (실제 연동)
            // val result = addRehabSessionUseCase(session)

            setExerciseCompleted(exerciseId, true)
        }
    }

    fun clearErrorMessage() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    // --- '기록' 화면 (History) 기능 (수정 없음) ---

    fun loadHistory(date: Date) {
        // ... (기존 loadHistory 함수 내용은 수정 없음) ...
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


    // (★ 추가 ★) --- '식단 상세' 화면 (Diet Detail) 기능 ---

    /**
     * (★ 추가 ★)
     * '두뇌'(DietDetailFragment)가 '호출'하면, 'AI'에게 '대체 식품'을 '요청' (시뮬레이션)
     */
    fun loadDietDetails(dietId: String) {
        viewModelScope.launch {
            _dietDetailState.update { it.copy(isLoading = true, errorMessage = null, alternatives = emptyList()) }

            try {
                // 1. '홈' 화면이 '이미' '가지고 있는' '식단' 정보를 '먼저' '찾습니다'.
                val currentUiState = _uiState.first() // '홈' 화면의 '현재' 상태를 '가져옵니다'.
                val foundDiet = currentUiState.recommendedDiets.find { it.id == dietId }

                if (foundDiet == null) {
                    throw Exception("선택한 식단(ID: $dietId)을 찾을 수 없습니다.")
                }

                // 2. '식단' 정보는 '즉시' '업데이트'합니다.
                _dietDetailState.update { it.copy(diet = foundDiet) }

                // 3. (시뮬레이션) 'AI'에게 '대체 식품'을 '요청'하는 '척' 0.5초 '딜레이'
                kotlinx.coroutines.delay(500)

                // (시뮬레이션) 'AI'가 'ID'에 따라 '다른' '대체 식품'을 '제안'했다고 '가정'합니다.
                val dummyAlternatives = when (dietId) {
                    "d001" -> listOf("대체: 그릭 요거트와 견과류", "대체: 통밀빵과 아보카도")
                    "d002" -> listOf("대체: 두부 샐러드", "대체: 연어 스테이크와 채소 구이")
                    else -> listOf("추천할 만한 대체 식품이 없습니다.")
                }

                // 4. '대체 식품' '목록'을 '업데이트'합니다.
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

    /**
     * (★ 추가 ★) '두뇌'(DietDetailFragment)가 '오류' 메시지를 '처리'한 후 호출합니다.
     */
    fun clearDietDetailErrorMessage() {
        _dietDetailState.update { it.copy(errorMessage = null) }
    }
}