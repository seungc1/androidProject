package com.example.androidproject.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.androidproject.domain.model.Diet
import com.example.androidproject.domain.model.Exercise
import com.example.androidproject.domain.model.Injury
import com.example.androidproject.domain.model.User
import com.example.androidproject.domain.usecase.GetAIRecommendationUseCase
import com.example.androidproject.presentation.main.MainUiState
import com.example.androidproject.presentation.main.TodayExercise
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RehabViewModel @Inject constructor(
    private val getAIRecommendationUseCase: GetAIRecommendationUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    init {
        loadMainDashboardData()
    }

    private fun loadMainDashboardData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                kotlinx.coroutines.delay(1000) // 1초 로딩 딜레이

                val dummyUser = User(
                    id = "user01", name = "김재활", gender = "남성", age = 30,
                    heightCm = 175, weightKg = 70.5, activityLevel = "활동적",
                    fitnessGoal = "근육 증가", allergyInfo = listOf("땅콩"),
                    preferredDietType = "일반", targetCalories = 2500,
                    currentInjuryId = "injury01",
                    preferredDietaryTypes = listOf("일반식", "저염식"), // 예시 값
                    equipmentAvailable = listOf("덤벨", "밴드"), // 예시 값
                    currentPainLevel = 4, // 예시 값 (1-10)
                    additionalNotes = "부상 회복에 집중하고 싶습니다." // 예시 값
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

                _uiState.value = MainUiState(
                    isLoading = false,
                    userName = dummyUser.name,
                    currentInjuryName = dummyInjury.name,
                    todayExercises = dummyExercises.map { TodayExercise(it, false) },
                    recommendedDiets = dummyDiets,
                    errorMessage = null
                )

            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "데이터 로드 실패: ${e.message}") }
            }
        }
    }

    fun toggleExerciseCompletion(exerciseId: String) {
        viewModelScope.launch {
            _uiState.update { currentState ->
                val updatedExercises = currentState.todayExercises.map {
                    if (it.exercise.id == exerciseId) {
                        it.copy(isCompleted = !it.isCompleted)
                    } else {
                        it
                    }
                }
                currentState.copy(todayExercises = updatedExercises)
            }
        }
    }

    fun clearErrorMessage() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}