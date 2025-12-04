package com.dataDoctor.rehabai.presentation.history

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
import com.dataDoctor.rehabai.databinding.FragmentHistoryBinding
import com.dataDoctor.rehabai.presentation.viewmodel.HistoryViewModel
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
import com.dataDoctor.rehabai.domain.usecase.GetDailyHistoryUseCase
import com.dataDoctor.rehabai.data.local.SessionManager
import com.dataDoctor.rehabai.domain.usecase.GetWeeklyAnalysisUseCase
import com.dataDoctor.rehabai.domain.repository.UserRepository
import com.dataDoctor.rehabai.data.ExerciseCatalog
import androidx.navigation.fragment.findNavController // [추가]
import androidx.recyclerview.widget.LinearLayoutManager // [추가]
import android.util.Log

@AndroidEntryPoint
class HistoryFragment : Fragment() {

    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HistoryViewModel by viewModels()

    private lateinit var historyAdapter: HistoryAdapter // [복구]

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

        setupRecyclerView() // [복구]
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

    private fun setupRecyclerView() {
        historyAdapter = HistoryAdapter { item ->
            // 클릭 이벤트 처리
            when (item) {
                is HistoryItem.Diet -> {
                    // 식단 상세 화면으로 이동 (session.id를 전달하여 ViewModel에서 조회)
                    val action = HistoryFragmentDirections.actionHistoryFragmentToDietDetailFragment(item.session.id)
                    findNavController().navigate(action)
                }
                is HistoryItem.Exercise -> {
                    // 운동 상세는 현재 별도 화면이 없으므로 토스트 메시지 등 처리 (선택 사항)
                    // Toast.makeText(context, "운동 상세: ${item.session.exerciseId}", Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.historyRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = historyAdapter
        }
    }

    private fun setupCalendarListener() {
        binding.calendarView.setOnDateChangedListener { _, date, _ ->
            loadDailyHistory(date.date)
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

                    // [복구] RecyclerView 데이터 업데이트
                    historyAdapter.submitList(state.historyItems)
                    binding.emptyHistoryTextView.isVisible = !state.isLoading && state.historyItems.isEmpty()
                    binding.historyRecyclerView.isVisible = !state.isLoading && state.historyItems.isNotEmpty()
                    binding.loadingProgressBar.isVisible = state.isLoading

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

    // ★★★ [수정] ViewModel을 통한 데이터 로드 ★★★
    private fun loadDailyHistory(date: LocalDate) {
        // ViewModel에 로드 요청 (UI 업데이트는 observeUiState에서 처리)
        viewModel.loadHistory(date)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}