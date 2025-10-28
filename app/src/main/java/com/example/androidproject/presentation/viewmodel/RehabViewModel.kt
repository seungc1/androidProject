// app/src/main/java/com/example/androidproject/presentation/viewmodel/RehabViewModel.kt
package com.example.androidproject.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.androidproject.domain.model.Exercise
import com.example.androidproject.domain.model.Injury
import com.example.androidproject.domain.usecase.GetRecommendedRehabUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

// Hilt가 이 ViewModel에 의존성을 주입하도록 지시합니다.
@HiltViewModel
class RehabViewModel @Inject constructor(
    private val getRecommendedRehabUseCase: GetRecommendedRehabUseCase // Hilt를 통해 Use Case 주입
) : ViewModel() {

    // 추천 운동 목록을 LiveData로 관리하여 UI에서 관찰할 수 있도록 합니다.
    private val _recommendedExercises = MutableLiveData<List<Exercise>>()
    val recommendedExercises: LiveData<List<Exercise>> get() = _recommendedExercises

    // 로딩 상태를 LiveData로 관리 (선택 사항)
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    // 오류 메시지를 LiveData로 관리 (선택 사항)
    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> get() = _errorMessage


    // 사용자 부상 정보를 받아 추천 운동을 가져오는 함수
    fun fetchRecommendedExercises(injury: Injury) {
        viewModelScope.launch {
            _isLoading.value = true // 로딩 시작
            _errorMessage.value = null // 에러 초기화

            try {
                // Use Case를 호출하여 비즈니스 로직 실행 및 결과 가져오기
                val exercises = getRecommendedRehabUseCase(injury)
                _recommendedExercises.value = exercises // LiveData 업데이트
            } catch (e: Exception) {
                _errorMessage.value = "운동 추천 중 오류 발생: ${e.localizedMessage}" // 오류 처리
            } finally {
                _isLoading.value = false // 로딩 종료
            }
        }
    }
}