package com.example.androidproject.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.androidproject.domain.model.AIRecommendationResult // ✨ 새로 추가 ✨
import com.example.androidproject.domain.model.Injury
import com.example.androidproject.domain.model.Exercise // AIRecommendationResult 안에 Exercise와 Diet가 있으므로 필요한 경우
import com.example.androidproject.domain.model.Diet // AIRecommendationResult 안에 Exercise와 Diet가 있으므로 필요한 경우
import com.example.androidproject.domain.usecase.GetAIRecommendationUseCase // ✨ Use Case 변경 ✨
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest // ✨ Flow를 수집하기 위해 필요 ✨
import kotlinx.coroutines.launch
import javax.inject.Inject

// Hilt가 이 ViewModel에 의존성을 주입하도록 지시합니다.
@HiltViewModel
class RehabViewModel @Inject constructor(
    // ⭐ GetRecommendedRehabUseCase 대신 GetAIRecommendationUseCase를 주입받습니다. ⭐
    private val getAIRecommendationUseCase: GetAIRecommendationUseCase
) : ViewModel() {

    // ⭐ AI 추천 결과를 담는 LiveData (운동과 식단 모두 포함) ⭐
    private val _aiRecommendationResult = MutableLiveData<AIRecommendationResult>()
    val aiRecommendationResult: LiveData<AIRecommendationResult> get() = _aiRecommendationResult

    // (필요하다면) 운동 목록만 따로 추출하여 UI에 전달할 LiveData
    private val _recommendedExercises = MutableLiveData<List<Exercise>>()
    val recommendedExercises: LiveData<List<Exercise>> get() = _recommendedExercises

    // (필요하다면) 식단 목록만 따로 추출하여 UI에 전달할 LiveData
    private val _recommendedDiets = MutableLiveData<List<Diet>>()
    val recommendedDiets: LiveData<List<Diet>> get() = _recommendedDiets

    // 로딩 상태를 LiveData로 관리
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    // 오류 메시지를 LiveData로 관리
    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> get() = _errorMessage


    // 사용자 ID와 부상 정보를 받아 AI 추천을 가져오는 함수
    // ⭐ injury: Injury만으로는 부족하며, userId도 필요합니다. ⭐
    fun fetchAIRecommendations(userId: String, injury: Injury?) { // injury는 nullable로 변경
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                // ⭐ GetAIRecommendationUseCase를 호출하고 Flow를 수집합니다. ⭐
                getAIRecommendationUseCase(userId, injury).collectLatest { result ->
                    _aiRecommendationResult.value = result // 전체 AI 추천 결과 업데이트
                    _recommendedExercises.value = result.recommendedExercises // 운동 목록만 추출
                    _recommendedDiets.value = result.recommendedDiets // 식단 목록만 추출
                }
            } catch (e: Exception) {
                _errorMessage.value = "AI 추천 가져오는 중 오류 발생: ${e.localizedMessage}"
                // 오류 발생 시 기존 데이터 초기화 (선택 사항)
                _aiRecommendationResult.value = null
                _recommendedExercises.value = emptyList()
                _recommendedDiets.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    // ⭐ 추가: 특정 운동의 상세 정보를 가져오는 함수 (필요하다면) ⭐
    // 이 기능은 GetExerciseDetailUseCase가 구현되어야 가능합니다.
    /*
    private val _exerciseDetail = MutableLiveData<Exercise>()
    val exerciseDetail: LiveData<Exercise> get() = _exerciseDetail

    fun fetchExerciseDetail(exerciseId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                // getExerciseDetailUseCase를 주입받아 사용
                getExerciseDetailUseCase(exerciseId).collectLatest { detail ->
                    _exerciseDetail.value = detail
                }
            } catch (e: Exception) {
                _errorMessage.value = "운동 상세 정보 가져오는 중 오류 발생: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    */
}