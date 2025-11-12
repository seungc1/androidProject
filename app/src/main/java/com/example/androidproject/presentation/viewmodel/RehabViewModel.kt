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
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Date // (★추가★) 님의 '가이드라인' 원칙 4 (java.util.Date) import
import javax.inject.Inject

// (★추가★) '기록' 화면(HistoryFragment)을 위한 UI 상태 데이터 클래스
data class HistoryUiState(
    val isLoading: Boolean = false,
    val historyItems: List<HistoryItem> = emptyList(),
    val errorMessage: String? = null
)

/**
 * [파일 10/11] - '핵심 두뇌'
 * (★수정★) '홈' 화면 '두뇌'(HomeFragment)와 '연결'되기 위해,
 * 'MainUiState'를 '노출'하고 '체크박스' 이벤트를 '처리'합니다.
 *
 * (★추가★) '기록' 화면 '두뇌'(HistoryFragment)와 '연결'되기 위해,
 * 'HistoryUiState'를 '노출'하고 '기록 로드' 이벤트를 '처리'합니다.
 */
@HiltViewModel
class RehabViewModel @Inject constructor(
    // (★수정★) 님의 'tree'에 있는 'UseCase'들을 Hilt를 통해 '주입'받습니다.
    private val getAIRecommendationUseCase: GetAIRecommendationUseCase
    // private val getUserUseCase: GetUserUseCase,
    // private val getInjuryUseCase: GetInjuryUseCase,
    // (AddRehabSessionUseCase는 '체크' 시점에 호출됩니다)
    // private val addRehabSessionUseCase: AddRehabSessionUseCase
    // (★추가★) '기록' 화면을 위한 UseCase (시뮬레이션)
    // private val getHistoryUseCase: GetHistoryUseCase
) : ViewModel() {

    // --- '홈' 화면 (Main) 상태 관리 ---
    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()


    // (★추가★) --- '기록' 화면 (History) 상태 관리 ---
    private val _historyUiState = MutableStateFlow(HistoryUiState())
    val historyUiState: StateFlow<HistoryUiState> = _historyUiState.asStateFlow()


    init {
        // ViewModel이 생성될 때 메인 화면에 필요한 모든 데이터를 로드합니다.
        loadMainDashboardData()
    }

    private fun loadMainDashboardData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                // ... (기존 '홈' 화면 더미 데이터 로직 - 생략) ...

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
            // (★수정★) 님의 '실제' 모델 필드명('notes', 'userRating')에 맞게 수정합니다.
            //    val session = RehabSession(
            //        id = "...", userId = "...", exerciseId = exerciseId,
            //        dateTime = java.util.Date(), // (★중요★) 님의 '가이드라인'
            //        sets = 3, reps = 10, durationMinutes = 15,
            //        notes = "자동 기록됨", // (★수정★)
            //        userRating = 0        // (★수정★)
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


    // (★추가★) --- '기록' 화면 (History) 기능 ---

    /**
     * (★추가★)
     * '두뇌'(HistoryFragment)가 '날짜'를 '선택'하면 호출되는 함수입니다.
     * (팀원 1의 가이드라인 원칙 1, 4)
     */
    fun loadHistory(date: Date) {
        viewModelScope.launch {
            _historyUiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                // (시뮬레이션) UseCase를 호출하여 '선택된 날짜'(date)의 '기록'을 '가져옵니다'.
                // val historyItems = getHistoryUseCase(date)

                // --- '기록' 더미 데이터 (시뮬레이션) ---
                kotlinx.coroutines.delay(500) // 0.5초 로딩 딜레이

                // (★중요★) 'HistoryAdapter'가 'HistoryItem'을 '요구'하므로,
                // 'RehabSession'과 'DietSession'을 'HistoryItem.Exercise'와 'HistoryItem.Diet'으로 '포장'합니다.

                // (★수정★) 님의 '실제' 모델 필드명('notes', 'userRating')과 타입('Int?')에 맞게 수정합니다.
                val dummyHistoryItems = listOf(
                    HistoryItem.Exercise(
                        RehabSession(
                            id = "session001", userId = "user01", exerciseId = "ex001", // (임시) 나중에 '운동 이름'으로 변환 필요
                            dateTime = date, // (★핵심★) '선택된 날짜'로 설정
                            sets = 3, reps = 10, durationMinutes = 15,
                            notes = "조금 아팠음", // (★수정★) 'successStatus' -> 'notes'
                            userRating = 3         // (★수정★) 'userFeedback' -> 'userRating' (Int)
                        )
                    ),
                    HistoryItem.Diet(
                        DietSession(
                            id = "dietSession001", userId = "user01", dietId = "d001", // (임시) 나중에 '음식 이름'으로 변환 필요
                            dateTime = date, // (★핵심★) '선택된 날짜'로 설정
                            actualQuantity = 1.0, actualUnit = "그릇",
                            userSatisfaction = 5 // (★수정★) "만족"(String) -> 5 (Int?)
                        )
                    ),
                    HistoryItem.Exercise(
                        RehabSession(
                            id = "session002", userId = "user01", exerciseId = "ex002",
                            dateTime = date,
                            sets = 5, reps = 15, durationMinutes = 20,
                            notes = "완료",    // (★수정★) 'successStatus' -> 'notes'
                            userRating = 5      // (★수정★) 'userFeedback' -> 'userRating' (Int)
                        )
                    )
                )
                // --- 더미 데이터 끝 ---

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

    /**
     * (★추가★) '두뇌'(HistoryFragment)가 '오류' 메시지를 '처리'한 후 호출합니다.
     */
    fun clearHistoryErrorMessage() {
        _historyUiState.update { it.copy(errorMessage = null) }
    }
}