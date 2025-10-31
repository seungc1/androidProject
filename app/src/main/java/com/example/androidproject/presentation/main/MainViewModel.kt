package com.example.androidproject.presentation.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.androidproject.domain.model.Diet
import com.example.androidproject.domain.model.Exercise
import com.example.androidproject.domain.model.Injury
import com.example.androidproject.domain.model.User
// import com.example.androidproject.domain.usecase.* // (주입받을 UseCase들)
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    // (가상) Domain Layer의 UseCase들을 Hilt를 통해 주입받습니다.
    // private val getUserUseCase: GetUserUseCase,
    // private val getInjuryUseCase: GetInjuryUseCase,
    // private val getTodayExercisesUseCase: GetTodayExercisesUseCase,
    // private val getTodayDietsUseCase: GetTodayDietsUseCase,
    // private val toggleExerciseCompletionUseCase: ToggleExerciseCompletionUseCase
) : ViewModel() {

    // UI State
    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    init {
        // ViewModel이 생성될 때 메인 화면에 필요한 모든 데이터를 로드합니다.
        loadMainDashboardData()
    }

    private fun loadMainDashboardData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                // (시뮬레이션) 실제로는 UseCase를 호출하여 데이터를 가져옵니다.
                // val user = getUserUseCase()
                // val injury = user.currentInjuryId?.let { getInjuryUseCase(it) }
                // val exercises = getTodayExercisesUseCase(user.id)
                // val diets = getTodayDietsUseCase(user.id)

                // --- 더미 데이터 (시뮬레이션) ---
                kotlinx.coroutines.delay(1000) // 1초 로딩 딜레이

                val dummyUser = User(
                    id = "user01", name = "김재활", gender = "남성", age = 30,
                    heightCm = 175, weightKg = 70.5, activityLevel = "활동적",
                    fitnessGoal = "근육 증가", allergyInfo = listOf("땅콩"),
                    preferredDietType = "일반", targetCalories = 2500,
                    currentInjuryId = "injury01"
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
                    todayExercises = dummyExercises.map { TodayExercise(it, false) }, // UI용 모델로 변환
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
     * 사용자가 운동 Todo 리스트의 체크박스를 클릭했을 때 호출됩니다.
     */
    fun toggleExerciseCompletion(exerciseId: String) {
        viewModelScope.launch {
            // (시뮬레이션)
            // 1. UseCase를 호출하여 서버/DB에 완료 상태를 기록 (RehabSession 생성)
            //    val result = toggleExerciseCompletionUseCase(exerciseId)

            // 2. (성공 시) 로컬 UI 상태 즉시 업데이트 (Optimistic Update)
            _uiState.update { currentState ->
                val updatedExercises = currentState.todayExercises.map {
                    if (it.exercise.id == exerciseId) {
                        it.copy(isCompleted = !it.isCompleted) // 완료 상태 토글
                    } else {
                        it
                    }
                }
                currentState.copy(todayExercises = updatedExercises)
            }
        }
    }
}
