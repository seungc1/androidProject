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
import kotlinx.coroutines.flow.first
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

// (★기존★) '식단 상세' 화면(DietDetailFragment)을 위한 UI 상태
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

    // --- '식단 상세' 화면 (Diet Detail) 상태 관리 ---
    private val _dietDetailState = MutableStateFlow(DietDetailUiState())
    val dietDetailState: StateFlow<DietDetailUiState> = _dietDetailState.asStateFlow()

    // (★ 수정 ★) 'ProfileFragment'가 '참조'할 '더미' 사용자 정보
    lateinit var dummyUser: User

    // (★ 추가 ★) '더미' 부상 정보도 'ProfileFragment'에서 '참조'할 수 있도록 노출
    lateinit var dummyInjury: Injury


    init {
        loadMainDashboardData()
    }

    private fun loadMainDashboardData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                // --- 더미 데이터 (시뮬레이션) ---
                kotlinx.coroutines.delay(1000) // 1초 로딩 딜레이

                // (★ 수정 ★) 'val dummyUser' -> 'dummyUser' (전역 변수에 '할당')
                // (★ 수정 ★) 'heightCm'을 'Int'로 변경 (User.kt 모델과 '일치')
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
                // (★ 수정 ★) 'val dummyInjury' -> 'dummyInjury' (전역 변수에 '할당')
                dummyInjury = Injury(
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
                    // (★ 수정 ★) 'uiState'에 '환부(부위)'도 '저장'
                    currentInjuryName = dummyInjury.name,
                    currentInjuryArea = dummyInjury.bodyPart, // (★ 추가 ★)
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


    // --- '식단 상세' 화면 (Diet Detail) 기능 (수정 없음) ---

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


    // (★ 수정 ★) --- '개인정보' 화면 (Profile) 기능 ---

    /**
     * (★ 수정 ★)
     * '두뇌'(ProfileEditFragment)가 '저장' 버튼을 '누르면' '호출'됩니다.
     * '환부(부위)'와 '질환명'을 '업데이트'하여 'UI State'를 '갱신'합니다.
     */
    fun updateUserProfile(updatedUser: User, updatedInjuryName: String, updatedInjuryArea: String) {
        viewModelScope.launch {
            // 1. (시뮬레이션) ViewModel의 '더미' 데이터를 '업데이트'합니다.
            dummyUser = updatedUser
            dummyInjury = dummyInjury.copy(
                name = updatedInjuryName, // '질환명' 업데이트
                bodyPart = updatedInjuryArea  // '환부(부위)' 업데이트
            )

            // 2. (시뮬레이션) '홈' 탭과 '개인정보' 탭이 '공유'하는 'UI State'도 '업데이트'합니다.
            // (UI State를 '관찰'하는 모든 '화면'을 '새로고침'하기 위함입니다.)
            _uiState.update { currentState ->
                currentState.copy(
                    userName = updatedUser.name,
                    currentInjuryName = updatedInjuryName, // '업데이트'된 '질환명'을 '반영'
                    currentInjuryArea = updatedInjuryArea  // '업데이트'된 '환부(부위)'를 '반영'
                )
            }

            // (실제 앱에서는 이 'updatedUser' 객체를
            // 'updateUserProfileUseCase(updatedUser)'로 '전달'하여 'DB'에 '저장'합니다.)
            // (부상 정보도 'updateInjuryUseCase' 등으로 '저장'해야 합니다.)
        }
    }

} // (★ 중요 ★) 클래스의 '닫는 괄호'

//아래는 입력된 정보가 없을 떄

//package com.example.androidproject.presentation.viewmodel
//
//import androidx.lifecycle.LiveData
//import androidx.lifecycle.MutableLiveData
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.viewModelScope
//// (★수정★) 님의 'tree'에 있는 '필드값'과 'UseCase'를 import 합니다.
//import com.example.androidproject.domain.model.Diet
//import com.example.androidproject.domain.model.Exercise
//import com.example.androidproject.domain.model.Injury
//import com.example.androidproject.domain.model.User
//// (★추가★) '기록' 화면에서 사용할 '필드값' 모델 import
//import com.example.androidproject.domain.model.DietSession
//import com.example.androidproject.domain.model.RehabSession
//import com.example.androidproject.domain.usecase.GetAIRecommendationUseCase
//// import com.example.androidproject.domain.usecase.* // (Hilt가 '실제' UseCase를 주입합니다)
//// (★추가★) '기록' 화면에서 사용할 'HistoryItem' import
//import com.example.androidproject.presentation.history.HistoryItem
//import com.example.androidproject.presentation.main.MainUiState
//import com.example.androidproject.presentation.main.TodayExercise
//import dagger.hilt.android.lifecycle.HiltViewModel
//import kotlinx.coroutines.flow.MutableStateFlow
//import kotlinx.coroutines.flow.StateFlow
//import kotlinx.coroutines.flow.asStateFlow
//import kotlinx.coroutines.flow.first
//import kotlinx.coroutines.flow.update
//import kotlinx.coroutines.launch
//import java.util.Date
//import java.util.UUID
//import javax.inject.Inject
//
//// (★기존★) '기록' 화면(HistoryFragment)을 위한 UI 상태
//data class HistoryUiState(
//    val isLoading: Boolean = false,
//    val historyItems: List<HistoryItem> = emptyList(),
//    val errorMessage: String? = null
//)
//
//// (★기존★) '식단 상세' 화면(DietDetailFragment)을 위한 UI 상태
//data class DietDetailUiState(
//    val isLoading: Boolean = false,
//    val diet: Diet? = null,
//    val alternatives: List<String> = emptyList(),
//    val errorMessage: String? = null
//)
//
//
//@HiltViewModel
//class RehabViewModel @Inject constructor(
//    private val getAIRecommendationUseCase: GetAIRecommendationUseCase,
//    // (★추가★) '저장' 시 '시뮬레이션'을 위해 'UseCase' '주입' (주석 처리)
//    // private val addRehabSessionUseCase: AddRehabSessionUseCase
//    // (★추가★) '기록' 화면을 위한 UseCase (시뮬레이션)
//    // private val getHistoryUseCase: GetHistoryUseCase
//) : ViewModel() {
//
//    // --- '홈' 화면 (Main) 상태 관리 ---
//    private val _uiState = MutableStateFlow(MainUiState())
//    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()
//
//    // --- '기록' 화면 (History) 상태 관리 ---
//    private val _historyUiState = MutableStateFlow(HistoryUiState())
//    val historyUiState: StateFlow<HistoryUiState> = _historyUiState.asStateFlow()
//
//    // --- '식단 상세' 화면 (Diet Detail) 상태 관리 ---
//    private val _dietDetailState = MutableStateFlow(DietDetailUiState())
//    val dietDetailState: StateFlow<DietDetailUiState> = _dietDetailState.asStateFlow()
//
//    // (★ 수정 ★) 'ProfileFragment'가 '참조'할 '더미' 사용자 정보
//    // (테스트를 위해 '초기화' 코드를 'init' 밖으로 '이동')
//    lateinit var dummyUser: User
//    lateinit var dummyInjury: Injury
//
//
//    init {
//        loadMainDashboardData()
//    }
//
//    /**
//     * (★ 테스트 수정 ★)
//     * '빈 화면' '테스트'를 위해, '더미 데이터' '생성' '로직'을 '모두' '주석 처리'하고
//     * '로딩'만 '종료'하도록 '임시' '수정'합니다.
//     */
//    private fun loadMainDashboardData() {
//        viewModelScope.launch {
//            _uiState.update { it.copy(isLoading = true) }
//
//            try {
//                // --- (★ 테스트 ★) '더미 데이터' '생성' '로직' '전부' '주석 처리' ---
//                // kotlinx.coroutines.delay(1000)
//                /*
//                dummyUser = User(
//                    id = "user01", name = "김재활", gender = "남성", age = 30,
//                    heightCm = 175, weightKg = 70.5, ...
//                )
//                dummyInjury = Injury(
//                    id = "injury01", name = "손목 염좌", bodyPart = "손목", ...
//                )
//                val dummyExercises = listOf( ... )
//                val dummyDiets = listOf( ... )
//                */
//                // --- '주석 처리' '끝' ---
//
//                // (★ 테스트 ★) '데이터가 없는' '로딩 완료' '상태'를 '즉시' '방출'
//                _uiState.value = MainUiState(
//                    isLoading = false,
//                    userName = "", // (★핵심★) '이름'을 '비워'둡니다.
//                    currentInjuryName = null,
//                    currentInjuryArea = null,
//                    todayExercises = emptyList(), // (★핵심★) '운동' '목록'을 '비워'둡니다.
//                    recommendedDiets = emptyList(), // (★핵심★) '식단' '목록'을 '비워'둡니다.
//                    errorMessage = null
//                )
//
//                // (★ 테스트 ★) 'dummyUser'가 '초기화'되지 '않아' '발생'하는 '오류'를 '막기' 위해
//                // '빈' '객체'를 '할당'합니다. (ProfileEditFragment가 참조하기 때문)
//                dummyUser = User("", "", "", 0, 0, 0.0, "", "", emptyList(), "", emptyList(), emptyList(), 0, null, null, null)
//                dummyInjury = Injury("", "", "", "", "")
//
//
//            } catch (e: Exception) {
//                _uiState.update { it.copy(isLoading = false, errorMessage = "데이터 로드 실패: ${e.message}") }
//            }
//        }
//    }
//
//    // --- (이하 '홈', '기록', '식단 상세', '개인정보' 기능은 '수정 없음') ---
//
//    private fun setExerciseCompleted(exerciseId: String, isCompleted: Boolean) {
//        _uiState.update { currentState ->
//            val updatedExercises = currentState.todayExercises.map {
//                if (it.exercise.id == exerciseId) {
//                    it.copy(isCompleted = isCompleted)
//                } else {
//                    it
//                }
//            }
//            currentState.copy(todayExercises = updatedExercises)
//        }
//    }
//
//    fun saveRehabSessionDetails(exerciseId: String, rating: Int, notes: String) {
//        viewModelScope.launch {
//            val session = RehabSession(
//                id = UUID.randomUUID().toString(),
//                userId = "user01",
//                exerciseId = exerciseId,
//                dateTime = Date(),
//                sets = 3, reps = 10, durationMinutes = 15,
//                notes = notes,
//                userRating = rating
//            )
//            setExerciseCompleted(exerciseId, true)
//        }
//    }
//
//    fun clearErrorMessage() {
//        _uiState.update { it.copy(errorMessage = null) }
//    }
//
//    fun loadHistory(date: Date) {
//        viewModelScope.launch {
//            _historyUiState.update { it.copy(isLoading = true, errorMessage = null) }
//            try {
//                kotlinx.coroutines.delay(500)
//
//                // (★ 테스트 ★) '기록' 탭의 '빈 화면'도 '테스트'하려면
//                // '아래' 'dummyHistoryItems' '블록'을 '주석 처리'하세요.
//                val dummyHistoryItems = listOf(
//                    HistoryItem.Exercise(
//                        RehabSession(
//                            id = "session001", userId = "user01", exerciseId = "ex001",
//                            dateTime = date,
//                            sets = 3, reps = 10, durationMinutes = 15,
//                            notes = "조금 아팠음",
//                            userRating = 3
//                        )
//                    ),
//                    HistoryItem.Diet(
//                        DietSession(
//                            id = "dietSession001", userId = "user01", dietId = "d001",
//                            dateTime = date,
//                            actualQuantity = 1.0, actualUnit = "그릇",
//                            userSatisfaction = 5
//                        )
//                    )
//                )
//
//                _historyUiState.update {
//                    it.copy(isLoading = false, historyItems = dummyHistoryItems) // (★ 테스트 ★) 'dummyHistoryItems' -> 'emptyList()'로 '변경'
//                }
//
//            } catch (e: Exception) {
//                _historyUiState.update {
//                    it.copy(isLoading = false, errorMessage = "기록 로드 실패: ${e.message}")
//                }
//            }
//        }
//    }
//
//    fun clearHistoryErrorMessage() {
//        _historyUiState.update { it.copy(errorMessage = null) }
//    }
//
//    fun loadDietDetails(dietId: String) {
//        viewModelScope.launch {
//            _dietDetailState.update { it.copy(isLoading = true, errorMessage = null, alternatives = emptyList()) }
//
//            try {
//                // (★ 테스트 ★) 'uiState'가 '비어'있으므로, '이' '함수'는 '오류'를 '발생'시킵니다.
//                // '식단 상세' 탭의 '빈 화면' '테스트'는 'loadMainDashboardData'를 '복구'한 '후'에 '진행'해야 합니다.
//                val currentUiState = _uiState.first()
//                val foundDiet = currentUiState.recommendedDiets.find { it.id == dietId }
//
//                if (foundDiet == null) {
//                    throw Exception("선택한 식단(ID: $dietId)을 찾을 수 없습니다.")
//                }
//
//                _dietDetailState.update { it.copy(diet = foundDiet) }
//
//                kotlinx.coroutines.delay(500)
//
//                val dummyAlternatives = when (dietId) {
//                    "d001" -> listOf("대체: 그릭 요거트와 견과류", "대체: 통밀빵과 아보카도")
//                    "d002" -> listOf("대체: 두부 샐러드", "대체: 연어 스테이크와 채소 구이")
//                    else -> listOf("추천할 만한 대체 식품이 없습니다.")
//                }
//
//                _dietDetailState.update {
//                    it.copy(isLoading = false, alternatives = dummyAlternatives)
//                }
//
//            } catch (e: Exception) {
//                _dietDetailState.update {
//                    it.copy(isLoading = false, errorMessage = "대체 식품 로드 실패: ${e.message}")
//                }
//            }
//        }
//    }
//
//    fun clearDietDetailErrorMessage() {
//        _dietDetailState.update { it.copy(errorMessage = null) }
//    }
//
//    fun updateUserProfile(updatedUser: User, updatedInjuryName: String, updatedInjuryArea: String) {
//        viewModelScope.launch {
//            dummyUser = updatedUser
//            dummyInjury = dummyInjury.copy(
//                name = updatedInjuryName,
//                bodyPart = updatedInjuryArea
//            )
//            _uiState.update { currentState ->
//                currentState.copy(
//                    userName = updatedUser.name,
//                    currentInjuryName = updatedInjuryName,
//                    currentInjuryArea = updatedInjuryArea
//                )
//            }
//        }
//    }
//
//}