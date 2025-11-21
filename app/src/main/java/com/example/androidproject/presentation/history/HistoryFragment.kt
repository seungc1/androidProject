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
import kotlinx.coroutines.flow.filterNotNull
import com.example.androidproject.domain.usecase.GetDailyHistoryUseCase
import com.example.androidproject.data.local.SessionManager
import com.example.androidproject.domain.usecase.GetWeeklyAnalysisUseCase
import com.example.androidproject.domain.repository.UserRepository
import com.example.androidproject.data.ExerciseCatalog
import android.util.Log

@AndroidEntryPoint
class HistoryFragment : Fragment() {

    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!

    // (★수정★) sharedViewModel 대신 전용 ViewModel 사용
    private val viewModel: HistoryViewModel by viewModels()

    private lateinit var historyAdapter: HistoryAdapter

    // ★★★ [추가] 테스트용 의존성 주입 ★★★
    @Inject
    lateinit var getDailyHistoryUseCase: GetDailyHistoryUseCase
    @Inject
    lateinit var sessionManager: SessionManager
    @Inject
    lateinit var getWeeklyAnalysisUseCase: GetWeeklyAnalysisUseCase
    @Inject
    lateinit var userRepository: UserRepository
    // ★★★ [추가] 테스트용 의존성 주입 끝 ★★★


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupCalendarListener()
        setupSwipeToRefresh()
        observeUiState()
        observeRecordedDates()

        val today = CalendarDay.today()
        binding.calendarView.setCurrentDate(today)
        binding.calendarView.setSelectedDate(today)

        // 메인 영역 로드 (Flow.first() 기반으로 수정됨)
        viewModel.loadHistory(today.date)
        viewModel.fetchWeeklyAnalysis()

        // ★★★ [추가] 테스트 영역 로드 ★★★
        loadDailyHistoryTest(today.date)
        loadWeeklyAnalysisTest()
    }

    override fun onResume() {
        super.onResume()
        // [추가] 탭으로 돌아올 때마다 기록된 날짜 새로고침 (다른 탭에서 식단/운동 기록 후)
        viewModel.loadRecordedDates()

        // 2. 현재 달력에서 선택된 날짜의 기록을 다시 로드하여 최신 상태 반영
        val selectedDate = binding.calendarView.selectedDate ?: CalendarDay.today()
        viewModel.loadHistory(selectedDate.date)

        // ★★★ [추가] 테스트 영역도 갱신 ★★★
        loadDailyHistoryTest(selectedDate.date)
        loadWeeklyAnalysisTest()
    }


    private fun setupRecyclerView() {
        historyAdapter = HistoryAdapter()
        binding.historyRecyclerView.adapter = historyAdapter
    }

    private fun setupSwipeToRefresh() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.fetchWeeklyAnalysis()
        }
    }

    private fun setupCalendarListener() {
        binding.calendarView.setOnDateChangedListener { _, date, _ ->
            viewModel.loadHistory(date.date)
            loadDailyHistoryTest(date.date) // [추가] 날짜 변경 시 테스트 영역 갱신
        }
    }

    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.historyUiState.collectLatest { state ->
                    binding.loadingProgressBar.isVisible = state.isLoading

                    binding.swipeRefreshLayout.isRefreshing = state.isAnalyzing

                    if (state.analysisResult != null) {
                        binding.analysisCard.isVisible = true
                        binding.analysisSummaryTextView.text = state.analysisResult.summary
                    } else {
                        binding.analysisCard.isVisible = false
                    }

                    val hasHistory = state.historyItems.isNotEmpty()
                    android.util.Log.d("HISTORY_FRAG", "hasHistory: $hasHistory, isLoading: ${state.isLoading}, items: ${state.historyItems.size}")

                    // --- 수정 시작: 목록 및 빈 화면 메시지 가시성 로직 개선 ---
                    // RecyclerView는 데이터가 있고 로딩/분석 중이 아닐 때만 보입니다.
                    binding.historyRecyclerView.isVisible = hasHistory && !state.isLoading
                    android.util.Log.d("HISTORY_FRAG", "RecyclerView visibility: ${binding.historyRecyclerView.isVisible}")

                    // 빈 메시지는 데이터가 없고 로딩 중이 아닐 때만 보입니다.
                    binding.historyEmptyMessageTextView.isVisible = !hasHistory && !state.isLoading
                    // --- 수정 종료 ---

                    historyAdapter.submitList(state.historyItems)
                    android.util.Log.d("HISTORY_FRAG", "Submitted ${state.historyItems.size} items to adapter")

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

    // ★★★ [추가] 테스트용 함수 1: 선택된 날짜의 기록 로드 (ProfileFragment 로직과 동일) ★★★
    private fun loadDailyHistoryTest(date: LocalDate) {
        val userId = sessionManager.getUserId()
        val textView = binding.testHistoryRecordsTextView

        if (userId.isNullOrEmpty()) {
            textView.text = "로그인된 사용자 정보가 없습니다."
            return
        }

        viewLifecycleOwner.lifecycleScope.launch {
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
                Log.e("HistoryFragment", "테스트 기록 로드 실패: ${e.message}", e)
                textView.text = "테스트 기록 로드 중 오류 발생: ${e.message}"
            }
        }
    }

    // ★★★ [추가] 테스트용 함수 2: 주간 AI 분석 리포트 로드 (ProfileFragment 로직과 동일) ★★★
    private fun loadWeeklyAnalysisTest() {
        val userId = sessionManager.getUserId()
        val summaryTextView = binding.testAnalysisSummaryTextView

        if (userId.isNullOrEmpty()) {
            summaryTextView.text = "사용자 정보가 없어 분석을 로드할 수 없습니다."
            return
        }

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                // Flow.filterNotNull().first() 대신, repository에서 직접 first()를 호출하여 user 객체를 가져옵니다.
                val user = userRepository.getUserProfile(userId).filterNotNull().first()

                getWeeklyAnalysisUseCase(user)
                    .collectLatest { result ->
                        summaryTextView.text = "요약: ${result.summary}"
                        binding.testAnalysisStrengthsTextView.text = result.strengths.joinToString("\n") { "• $it" }.ifEmpty { "내용 없음" }
                        binding.testAnalysisImprovementTextView.text = result.areasForImprovement.joinToString("\n") { "• $it" }.ifEmpty { "내용 없음" }
                        binding.testAnalysisTipsTextView.text = result.personalizedTips.joinToString("\n") { "• $it" }.ifEmpty { "내용 없음" }
                        binding.testAnalysisNextStepsTextView.text = "다음 단계 권장 사항: ${result.nextStepsRecommendation}"
                    }
            } catch (e: Exception) {
                Log.e("HistoryFragment", "주간 분석 로드 실패: ${e.message}", e)
                summaryTextView.text = "AI 분석 로드 중 오류 발생: ${e.message}"
                binding.testAnalysisStrengthsTextView.text = "오류로 인해 상세 분석을 불러올 수 없습니다."
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}