package com.example.androidproject.presentation.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.androidproject.databinding.FragmentHistoryBinding
import com.example.androidproject.presentation.viewmodel.HistoryViewModel
import com.prolificinteractive.materialcalendarview.CalendarDay
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.HashSet
import org.threeten.bp.LocalDate
import org.threeten.bp.ZoneId
import org.threeten.bp.DateTimeUtils
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.flow.first
import com.example.androidproject.domain.usecase.GetDailyHistoryUseCase
import com.example.androidproject.data.local.SessionManager
import com.example.androidproject.domain.usecase.GetWeeklyAnalysisUseCase
import com.example.androidproject.domain.repository.UserRepository
import com.example.androidproject.data.ExerciseCatalog
import android.util.Log
import java.util.Date

@AndroidEntryPoint
class HistoryFragment : Fragment() {

    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HistoryViewModel by viewModels()

    // [제거됨] private lateinit var historyAdapter: HistoryAdapter

    // ★★★ [유지/재사용] 필요한 의존성 주입 ★★★
    @Inject
    lateinit var getDailyHistoryUseCase: GetDailyHistoryUseCase
    @Inject
    lateinit var sessionManager: SessionManager
    @Inject
    lateinit var getWeeklyAnalysisUseCase: GetWeeklyAnalysisUseCase
    @Inject
    lateinit var userRepository: UserRepository
    // ★★★ ★★★


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // [삭제됨] setupRecyclerView()
        setupCalendarListener()
        // [삭제됨] setupSwipeToRefresh()
        observeUiState()
        observeRecordedDates()

        val today = CalendarDay.today()
        binding.calendarView.setCurrentDate(today)
        binding.calendarView.setSelectedDate(today)

        // 메인 기록 로드
        loadDailyHistory(today.date)
        viewModel.fetchWeeklyAnalysis()
    }

    override fun onResume() {
        super.onResume()
        // 1. 기록된 날짜 새로고침
        viewModel.loadRecordedDates()

        // 2. 현재 달력에서 선택된 날짜의 기록을 다시 로드하여 최신 상태 반영
        val selectedDate = binding.calendarView.selectedDate ?: CalendarDay.today()
        loadDailyHistory(selectedDate.date)
    }

    private fun setupCalendarListener() {
        binding.calendarView.setOnDateChangedListener { _, date, _ ->
            loadDailyHistory(date.date) // [수정] 메인 로직 호출
        }
    }

    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.historyUiState.collectLatest { state ->

                    // [삭제됨] binding.swipeRefreshLayout.isRefreshing = state.isAnalyzing

                    // AI 분석 카드 (analysisCard) 관리 및 상세 데이터 바인딩
                    if (state.analysisResult != null) {
                        binding.analysisCard.isVisible = true

                        // 1. 요약 정보
                        binding.analysisSummaryTextView.text = state.analysisResult.summary

                        // 2. ★★★ [수정/추가] 상세 분석 필드 바인딩 ★★★
                        binding.analysisStrengthsTextView.text =
                            state.analysisResult.strengths.joinToString("\n") { "• $it" }.ifEmpty { "내용 없음" }
                        binding.analysisImprovementTextView.text =
                            state.analysisResult.areasForImprovement.joinToString("\n") { "• $it" }.ifEmpty { "내용 없음" }
                        binding.analysisTipsTextView.text =
                            state.analysisResult.personalizedTips.joinToString("\n") { "• $it" }.ifEmpty { "내용 없음" }
                        binding.analysisNextStepsTextView.text =
                            "다음 단계 권장 사항: ${state.analysisResult.nextStepsRecommendation}"
                        // ★★★ ★★★

                    } else {
                        binding.analysisCard.isVisible = false
                    }

                    // [삭제됨] RecyclerView 관련 로직 제거

                    state.errorMessage?.let { message ->
                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                        viewModel.clearErrorMessage()
                    }
                }
            }
        }
    }

    private fun observeRecordedDates() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.recordedDates.collectLatest { recordedDaysSet ->
                    if (recordedDaysSet.isNotEmpty()) {
                        val currentMonth = binding.calendarView.currentDate
                        val allDaysInMonth = HashSet<CalendarDay>()
                        val maxDay = currentMonth.date.lengthOfMonth()

                        for (i in 1..maxDay) {
                            allDaysInMonth.add(CalendarDay.from(currentMonth.year, currentMonth.month, i))
                        }

                        binding.calendarView.addDecorators(
                            DisabledDateDecorator(allDaysInMonth, recordedDaysSet),
                            EnabledDateDecorator(recordedDaysSet)
                        )
                    }
                }
            }
        }
    }

    // ★★★ [수정/주력] 메인 기록 로드 함수 (이전 loadDailyHistoryTest 로직 사용) ★★★
    private fun loadDailyHistory(date: LocalDate) {
        val userId = sessionManager.getUserId()
        val textView = binding.historyRecordsTextView // [수정] 메인 ID 사용

        if (userId.isNullOrEmpty()) {
            textView.text = "로그인된 사용자 정보가 없습니다."
            return
        }

        viewLifecycleOwner.lifecycleScope.launch {
            // 로딩 스피너 수동 제어 시작
            binding.loadingProgressBar.isVisible = true

            try {
                val localDate = DateTimeUtils.toDate(date.atStartOfDay(ZoneId.systemDefault()).toInstant())
                textView.text = "선택 날짜 ${SimpleDateFormat("M월 d일 (E)", Locale.KOREA).format(localDate)} 기록 로드 중..."

                // 선택된 날짜의 데이터 로드 (Flow.first()를 사용하여 즉시 결과 획득)
                val (rehabSessions, dietSessions) = getDailyHistoryUseCase(userId, localDate).first()

                val output = StringBuilder()
                output.append("--- 운동 기록 (${rehabSessions.size}개) ---\n")
                if (rehabSessions.isEmpty()) {
                    output.append("기록된 운동이 없습니다.\n")
                } else {
                    rehabSessions.sortedBy { it.dateTime }.forEach { session ->
                        val exerciseName = ExerciseCatalog.allExercises
                            .find { it.id == session.exerciseId }
                            ?.name ?: "알 수 없는 운동 (${session.exerciseId})"
                        val ratingText = when (session.userRating) {
                            5 -> "매우 좋음" 4 -> "좋음" 3 -> "보통" 2 -> "힘듦" 1 -> "나쁨" else -> "평가 없음"
                        }
                        val time = SimpleDateFormat("a h:mm", Locale.KOREA).format(session.dateTime)
                        output.append("• [운동] $time: $exerciseName (${session.sets}세트, ${session.reps}회) / 평점: $ratingText\n")
                    }
                }

                output.append("\n--- 식단 기록 (${dietSessions.size}개) ---\n")
                if (dietSessions.isEmpty()) {
                    output.append("기록된 식단이 없습니다.\n")
                } else {
                    dietSessions.sortedBy { it.dateTime }.forEach { session ->
                        val foodName = session.foodName ?: "알 수 없는 식단"
                        val satisfactionText = when (session.userSatisfaction) {
                            5 -> "매우 만족" 4 -> "만족" 3 -> "보통" 2 -> "불만족" 1 -> "매우 불만족" else -> "평가 없음"
                        }
                        val time = SimpleDateFormat("a h:mm", Locale.KOREA).format(session.dateTime)
                        output.append("• [식단] $time: $foodName (${session.actualQuantity}${session.actualUnit}) / 만족도: $satisfactionText\n")
                    }
                }
                textView.text = output.toString()

            } catch (e: Exception) {
                Log.e("HistoryFragment", "메인 기록 로드 실패: ${e.message}", e)
                textView.text = "기록 로드 중 오류 발생: ${e.message}"
            } finally {
                binding.loadingProgressBar.isVisible = false // 로딩 완료
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}