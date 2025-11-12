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
// (★추가★) '저장' 시 '시뮬레이션'을 위해 'AddRehabSessionUseCase' import
// import com.example.androidproject.domain.usecase.AddRehabSessionUseCase
// (★추가★) '기록' 화면에서 사용할 'HistoryItem' import
import com.example.androidproject.presentation.history.HistoryItem
import com.example.androidproject.presentation.main.MainUiState
import com.example.androidproject.presentation.main.TodayExercise
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Date
import java.util.UUID // (★추가★) '시뮬레이션' 저장을 위해 'UUID' import
import javax.inject.Inject

// ... (HistoryUiState data class는 그대로) ...
data class HistoryUiState(
    val isLoading: Boolean = false,
    val historyItems: List<HistoryItem> = emptyList(),
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
                    todayExercises = dummyExercises.map { TodayExercise(it, false) }, // (★핵심★) UI용 모델로 변환
                    recommendedDiets = dummyDiets,
                    errorMessage = null
                )

            } catch (e: Exception) {
                // (시뮬레이션) 에러 처리
                _uiState.update { it.copy(isLoading = false, errorMessage = "데이터 로드 실패: ${e.message}") }
            }
        }
    }

    /**
     * (★수정★) 'toggleExerciseCompletion' -> 'setExerciseCompleted'
     * '홈' 화면의 '운동 완료' '상태'를 '업데이트'합니다. (토글이 아닌 '설정')
     */
    private fun setExerciseCompleted(exerciseId: String, isCompleted: Boolean) {
        _uiState.update { currentState ->
            val updatedExercises = currentState.todayExercises.map {
                if (it.exercise.id == exerciseId) {
                    // (★수정★) '토글'(!it.isCompleted) 대신 '설정'(isCompleted)
                    it.copy(isCompleted = isCompleted)
                } else {
                    it
                }
            }
            currentState.copy(todayExercises = updatedExercises)
        }
    }

    /**
     * (★추가★) (Goal 2)
     * '상세' 화면(ExerciseDetailFragment)이 '저장' 버튼을 '누르면' '호출'됩니다.
     */
    fun saveRehabSessionDetails(exerciseId: String, rating: Int, notes: String) {
        viewModelScope.launch {
            // (시뮬레이션)
            // 1. (★필수★) UseCase를 호출하여 '운동 기록'(RehabSession)을 '생성'합니다.
            //    (님의 '필드값' 모델에 맞춰 'notes'와 'userRating'을 '사용'합니다.)

            val session = RehabSession(
                id = UUID.randomUUID().toString(), // 임시 ID 생성
                userId = "user01", // (임시) '현재' 사용자 ID
                exerciseId = exerciseId,
                dateTime = Date(), // (★중요★) '지금' 시간 (가이드라인 4)
                sets = 3, // (임시) 기본값
                reps = 10, // (임시) 기본값
                durationMinutes = 15, // (임시) 기본값
                notes = notes, // (★핵심★) '상세' 화면에서 '입력'받은 '후기'
                userRating = rating  // (★핵심★) '상세' 화면에서 '입력'받은 '만족도'
            )
            // (실제 연동)
            // val result = addRehabSessionUseCase(session)

            // 2. (★핵심★) '저장'에 '성공'했으므로, '홈' 화면 UI '상태'를 '업데이트'합니다.
            // 'setExerciseCompleted'를 '호출'하여 '체크' 표시를 '켭니다'. (Goal 3)
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
                kotlinx.coroutines.delay(500) // 0.5초 로딩 딜레이

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
}