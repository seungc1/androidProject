package com.example.androidproject.presentation.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.androidproject.databinding.FragmentHistoryBinding
import com.example.androidproject.presentation.viewmodel.RehabViewModel
import com.prolificinteractive.materialcalendarview.CalendarDay // (★ 추가 ★) '새' '달력' import
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Date
import java.util.Calendar
import java.util.HashSet // (★ 추가 ★)

/**
 * [수정 파일 6/6] - '기록' 화면 '두뇌'
 * (★ 수정 ★) '기본' '달력' -> '새' '라이브러리' '달력'('MaterialCalendarView')
 * '로직'으로 '교체'하고, '날짜' '색상' '데코레이터'를 '적용'합니다.
 */
@AndroidEntryPoint
class HistoryFragment : Fragment() {

    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!

    private val viewModel: RehabViewModel by activityViewModels()

    private lateinit var historyAdapter: HistoryAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupCalendarListener()
        setupSwipeToRefresh()
        observeUiState() // '선택'한 '날'의 '기록' '목록' '관찰'
        observeRecordedDates() // (★ 추가 ★) '모든' '기록' '날짜' '관찰' (색상 변경용)

        // (★ 수정 ★) '처음' '표시'될 '날짜'를 '오늘'로 '설정'
        val today = CalendarDay.today()
        binding.calendarView.setCurrentDate(today)
        binding.calendarView.setSelectedDate(today) // '오늘' '날짜' '선택'

        // (★ 수정 ★) 'ViewModel'의 '데이터' '로드'를 '기다릴' '필요'가 '없으므로',
        // '오늘' '날짜' '기록'과 '분석'을 '즉시' '호출'
        // (단, 'loadAllSessionDates'는 'loadMainDashboardData' '내부'에서 '호출'되므로 '여기서' '호출' '불필요')
        viewModel.loadHistory(today.date)
        viewModel.fetchWeeklyAnalysis()
    }

    private fun setupRecyclerView() {
        historyAdapter = HistoryAdapter()
        binding.historyRecyclerView.adapter = historyAdapter
    }

    // (기존 '새로고침' 로직 - 수정 없음)
    private fun setupSwipeToRefresh() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.fetchWeeklyAnalysis()
        }
    }

    /**
     * (★ 수정 ★)
     * '새' '달력' '라이브러리'의 '클릭' '리스너'('setOnDateChangedListener')로 '변경'
     */
    private fun setupCalendarListener() {
        binding.calendarView.setOnDateChangedListener { widget, date, selected ->
            // 'date'는 'CalendarDay' '타입'입니다.
            // 'CalendarDay'를 'java.util.Date'로 '변환'합니다.
            val selectedDate = date.date

            // (★ 핵심 ★) '핵심 두뇌'(ViewModel)에게 '기록'을 '가져오라고' '요청'
            viewModel.loadHistory(selectedDate)
        }
    }

    // (기존 'UI 상태' '관찰' 로직 - 수정 없음)
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
                    binding.historyRecyclerView.isVisible = hasHistory && !state.isLoading
                    binding.emptyViewHistory.isVisible = !hasHistory && !state.isLoading

                    historyAdapter.submitList(state.historyItems)

                    state.errorMessage?.let { message ->
                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                        viewModel.clearHistoryErrorMessage()
                    }
                }
            }
        }
    }

    /**
     * (★ 추가 ★)
     * '기록이 있는' '모든' '날짜' '목록'을 '관찰'하고, '달력'에 '색상' '데코레이터'를 '적용'
     */
    private fun observeRecordedDates() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {

                viewModel.recordedDates.collectLatest { recordedDaysSet ->
                    // (데이터가 '있을' '때'만 '데코레이터'를 '적용')
                    if (recordedDaysSet.isNotEmpty()) {

                        // '달력'의 '모든' '날짜'를 '가져옵니다'.
                        val currentMonth = binding.calendarView.currentDate
                        val calendar = Calendar.getInstance()
                        calendar.set(currentMonth.year, currentMonth.month - 1, 1) // (월은 0부터 시작)
                        val maxDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)

                        val allDaysInMonth = HashSet<CalendarDay>()
                        for (i in 1..maxDay) {
                            calendar.set(Calendar.DAY_OF_MONTH, i)
                            allDaysInMonth.add(CalendarDay.from(calendar))
                        }

                        // '데코레이터' '적용'
                        binding.calendarView.addDecorators(
                            DisabledDateDecorator(allDaysInMonth, recordedDaysSet),
                            EnabledDateDecorator(recordedDaysSet)
                        )
                    }
                }
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // 메모리 누수 방지
    }
}