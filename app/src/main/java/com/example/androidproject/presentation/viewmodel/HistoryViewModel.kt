package com.example.androidproject.presentation.viewmodel

import android.util.Log // (★추가★) 디버깅용 로그
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.androidproject.data.local.SessionManager
import com.example.androidproject.domain.model.AIAnalysisResult
import com.example.androidproject.domain.repository.DietSessionRepository
import com.example.androidproject.domain.repository.RehabSessionRepository
import com.example.androidproject.domain.repository.UserRepository
import com.example.androidproject.domain.usecase.GetWeeklyAnalysisUseCase
import com.example.androidproject.presentation.history.HistoryItem
import com.prolificinteractive.materialcalendarview.CalendarDay
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.threeten.bp.DateTimeUtils
import org.threeten.bp.LocalDate
import org.threeten.bp.ZoneId
import javax.inject.Inject

data class HistoryUiState(
    val isLoading: Boolean = false,
    val historyItems: List<HistoryItem> = emptyList(),
    val errorMessage: String? = null,
    val isAnalyzing: Boolean = false,
    val analysisResult: AIAnalysisResult? = null
)

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val rehabSessionRepository: RehabSessionRepository,
    private val dietSessionRepository: DietSessionRepository,
    private val getWeeklyAnalysisUseCase: GetWeeklyAnalysisUseCase,
    private val userRepository: UserRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _historyUiState = MutableStateFlow(HistoryUiState())
    val historyUiState: StateFlow<HistoryUiState> = _historyUiState.asStateFlow()

    private val _recordedDates = MutableStateFlow<Set<CalendarDay>>(emptySet())
    val recordedDates: StateFlow<Set<CalendarDay>> = _recordedDates.asStateFlow()

    init {
        loadRecordedDates()
    }

    fun loadHistory(date: LocalDate) {
        val userId = sessionManager.getUserId()
        if (userId == null) {
            Log.e("HistoryViewModel", "사용자 ID를 찾을 수 없습니다.")
            return
        }

        viewModelScope.launch {
            _historyUiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                // 1. 선택한 날짜의 00:00:00 (Start)
                val startDate = DateTimeUtils.toDate(date.atStartOfDay(ZoneId.systemDefault()).toInstant())

                // 2. 다음 날짜의 00:00:00 (End) -> 쿼리에서 '<' (미만) 조건을 쓰므로 24시간 전체 커버
                val endDate = DateTimeUtils.toDate(date.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant())

                Log.d("HistoryViewModel", "데이터 조회: $userId, $startDate ~ $endDate")

                val rehabFlow = rehabSessionRepository.getRehabSessionsBetween(userId, startDate, endDate)
                val dietFlow = dietSessionRepository.getDietSessionsBetween(userId, startDate, endDate)

                // 두 데이터를 결합하여 최신순으로 정렬
                combine(rehabFlow, dietFlow) { rehabSessions, dietSessions ->
                    Log.d("HistoryViewModel", "조회 결과 - 운동: ${rehabSessions.size}개, 식단: ${dietSessions.size}개")

                    val exerciseItems = rehabSessions.map { HistoryItem.Exercise(it) }
                    val dietItems = dietSessions.map { HistoryItem.Diet(it) }
                    (exerciseItems + dietItems).sortedByDescending { it.dateTime }
                }.collect { historyItems ->
                    _historyUiState.update { it.copy(isLoading = false, historyItems = historyItems) }
                }
            } catch (e: Exception) {
                Log.e("HistoryViewModel", "데이터 로드 오류", e)
                _historyUiState.update { it.copy(isLoading = false, errorMessage = "로드 실패: ${e.message}") }
            }
        }
    }

    fun fetchWeeklyAnalysis() {
        val userId = sessionManager.getUserId() ?: return

        viewModelScope.launch {
            _historyUiState.update { it.copy(isAnalyzing = true, analysisResult = null) }
            try {
                val user = userRepository.getUserProfile(userId).first()
                getWeeklyAnalysisUseCase(user)
                    .catch { e ->
                        _historyUiState.update {
                            it.copy(
                                isAnalyzing = false,
                                analysisResult = AIAnalysisResult(
                                    "분석 실패", emptyList(), emptyList(), emptyList(), "오류", "오류: ${e.message}"
                                )
                            )
                        }
                    }
                    .collect { result ->
                        _historyUiState.update { it.copy(isAnalyzing = false, analysisResult = result) }
                    }
            } catch (e: Exception) {
                _historyUiState.update { it.copy(isAnalyzing = false) }
            }
        }
    }

    fun loadRecordedDates() {
        val userId = sessionManager.getUserId() ?: return
        viewModelScope.launch {
            val rehabDates = rehabSessionRepository.getRehabHistory(userId).first().map { it.dateTime }
            val dietDates = dietSessionRepository.getDietHistory(userId).first().map { it.dateTime }

            // 오늘 날짜도 포함 (UX상 저장 직후 반응성을 위해)
            val allDates = (rehabDates + dietDates + java.util.Date()).distinct()

            val calendarDays = allDates.map {
                val instant = DateTimeUtils.toInstant(it)
                CalendarDay.from(instant.atZone(ZoneId.systemDefault()).toLocalDate())
            }.toSet()

            _recordedDates.value = calendarDays
        }
    }

    fun clearErrorMessage() {
        _historyUiState.update { it.copy(errorMessage = null) }
    }
}