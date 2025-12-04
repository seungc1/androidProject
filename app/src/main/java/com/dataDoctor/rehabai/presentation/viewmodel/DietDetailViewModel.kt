package com.dataDoctor.rehabai.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dataDoctor.rehabai.domain.model.Diet
import com.dataDoctor.rehabai.domain.repository.DietRepository
import com.dataDoctor.rehabai.domain.repository.DietSessionRepository // [추가]
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DietDetailUiState(
    val isLoading: Boolean = false,
    val diet: Diet? = null,
    // [추가] AI 추천 식단인지 여부
    val isAiRecommendation: Boolean = false,
    val photoUrl: String? = null,
    val alternatives: List<String> = emptyList(),
    val errorMessage: String? = null
)

@HiltViewModel
class DietDetailViewModel @Inject constructor(
    private val dietRepository: DietRepository,
    private val dietSessionRepository: DietSessionRepository // [추가]
) : ViewModel() {

    private val _dietDetailState = MutableStateFlow(DietDetailUiState())
    val dietDetailState: StateFlow<DietDetailUiState> = _dietDetailState.asStateFlow()

    fun loadDietDetails(id: String) {
        viewModelScope.launch {
            _dietDetailState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                // 1. 먼저 Diet (AI 추천 식단)에서 검색
                val foundDiet = dietRepository.getDietById(id).first()

                if (foundDiet != null) {
                    // AI 추천 식단인 경우
                    _dietDetailState.update {
                        it.copy(
                            diet = foundDiet,
                            isAiRecommendation = true, // AI 모드 활성화
                            photoUrl = null // AI 추천은 사진이 없음 (또는 기본 이미지)
                        )
                    }
                    
                    // (복구) AI 추천 대체 식품 로직
                    // 실제로는 AI API를 호출하거나 DB에서 가져와야 하지만, 일단 더미 데이터로 복구
                    val dummyAlternatives = when (id) {
                        "d001" -> listOf("대체: 그릭 요거트와 견과류", "대체: 통밀빵과 아보카도")
                        "d002" -> listOf("대체: 두부 샐러드", "대체: 연어 스테이크와 채소 구이")
                        else -> listOf("추천할 만한 대체 식품이 없습니다.")
                    }
                    _dietDetailState.update { it.copy(isLoading = false, alternatives = dummyAlternatives) }

                } else {
                    // 2. 없으면 DietSession (기록)에서 검색
                    val foundSession = dietSessionRepository.getDietSessionById(id).first()
                    if (foundSession != null) {
                        // Session을 Diet 객체로 변환하여 표시 (임시 매핑)
                        val sessionDiet = Diet(
                            id = foundSession.id,
                            mealType = "기록된 식단", // 세션에는 mealType이 없을 수 있음
                            foodName = foundSession.foodName ?: "이름 없음",
                            quantity = foundSession.actualQuantity,
                            unit = foundSession.actualUnit,
                            calorie = 0, // 세션에는 칼로리 정보가 없을 수 있음
                            protein = 0.0,
                            fat = 0.0,
                            carbs = 0.0,
                            ingredients = emptyList(),
                            preparationTips = foundSession.notes,
                            aiRecommendationReason = null
                        )
                        // [수정] photoUrl도 함께 업데이트, AI 모드 비활성화
                        _dietDetailState.update {
                            it.copy(
                                diet = sessionDiet,
                                photoUrl = foundSession.photoUrl,
                                isAiRecommendation = false,
                                isLoading = false
                            )
                        }
                    } else {
                        throw Exception("식단 정보를 찾을 수 없습니다.")
                    }
                }
            } catch (e: Exception) {
                _dietDetailState.update { it.copy(isLoading = false, errorMessage = "로드 실패: ${e.message}") }
            }
        }
    }

    fun clearErrorMessage() {
        _dietDetailState.update { it.copy(errorMessage = null) }
    }
}