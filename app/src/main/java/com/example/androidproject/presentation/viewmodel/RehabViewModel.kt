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
import com.example.androidproject.domain.usecase.GetAIRecommendationUseCase
// import com.example.androidproject.domain.usecase.* // (Hilt가 '실제' UseCase를 주입합니다)
import com.example.androidproject.presentation.main.MainUiState
import com.example.androidproject.presentation.main.TodayExercise
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * [파일 10/11] - '핵심 두뇌'
 * (★수정★) '홈' 화면 '두뇌'(HomeFragment)와 '연결'되기 위해,
 * 'MainUiState'를 '노출'하고 '체크박스' 이벤트를 '처리'합니다.
 */
@HiltViewModel
class RehabViewModel @Inject constructor(
    // (★수정★) 님의 'tree'에 있는 'UseCase'들을 Hilt를 통해 '주입'받습니다.
    private val getAIRecommendationUseCase: GetAIRecommendationUseCase
    // private val getUserUseCase: GetUserUseCase,
    // private val getInjuryUseCase: GetInjuryUseCase,
    // (AddRehabSessionUseCase는 '체크' 시점에 호출됩니다)
    // private val addRehabSessionUseCase: AddRehabSessionUseCase
) : ViewModel() {

    // (★수정★) UI State: 님이 '새로 만든' MainUiState.kt를 '노출'합니다.
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
                // (★수정★) 님의 'tree'에 있는 'GetAIRecommendationUseCase'를 '호출'합니다!
                // (GetAIRecommendationUseCase가 User/Injury/Exercise/Diet를 모두 반환한다고 '가정'합니다.)
                // (만약 UseCase가 분리되어 있다면, 각각 호출해야 합니다.)

                // (시뮬레이션)
                // val user = getUserUseCase()
                // val aiResult = getAIRecommendationUseCase(user.id)
                // val injury = user.currentInjuryId?.let { getInjuryUseCase(it) }

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
     * (★수정★)
     * '두뇌'(HomeFragment)가 '체크박스' 클릭을 '알려주면' 호출되는 함수입니다.
     * (팀원 1의 가이드라인 원칙 1, 4)
     */
    fun toggleExerciseCompletion(exerciseId: String) {
        viewModelScope.launch {
            // (시뮬레이션)
            // 1. (★필수★) UseCase를 호출하여 서버/DB에 '운동 기록'(RehabSession)을 '생성'합니다.
            //    (님의 'tree'에 'AddRehabSessionUseCase'가 있습니다.)
            //    (님의 '로드맵'에 따라, 'dateTime'에는 'java.util.Date()'를 사용해야 합니다.)
            //
            //    val session = RehabSession(
            //        id = "...", userId = "...", exerciseId = exerciseId,
            //        dateTime = java.util.Date(), // (★중요★) 님의 '가이드라인'
            //        sets = 3, reps = 10, successStatus = "성공", userFeedback = null
            //    )
            //    val result = addRehabSessionUseCase(session)


            // 2. (성공 시) 로컬 UI 상태 '즉시' 업데이트
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

    /**
     * (★수정★) '두뇌'(HomeFragment)가 '오류' 메시지를 '처리'한 후 호출합니다.
     */
    fun clearErrorMessage() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}