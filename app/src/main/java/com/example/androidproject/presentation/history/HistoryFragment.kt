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
import com.prolificinteractive.materialcalendarview.CalendarDay
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
// (★ 삭제 ★) 'java.util.Date' '삭제'
// import java.util.Date
import java.util.Calendar
// (★ 수정 ★) 'kotlin.collections.Set' '대신' 'java.util.Set' '사용' '통일'
import java.util.HashSet
import java.util.Set
// (★ 추가 ★) 'threeten' '날짜' '타입' 'import'
import org.threeten.bp.LocalDate

/**
 * [수정 파일 6/6] - '기록' 화면 '두뇌'
 * (★ 수정 ★) 'java.util.Date' -> 'org.threeten.bp.LocalDate'로 '타입' '변경'
 * (★ 수정 ★) 'Set' '타입' '불일치' '오류' '수정'
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
        observeUiState()
        observeRecordedDates()

        // (★ 수정 ★) 'CalendarDay.today().date' ('LocalDate')를 '사용'
        val today = CalendarDay.today()
        binding.calendarView.setCurrentDate(today)
        binding.calendarView.setSelectedDate(today)

        // (★ 수정 ★) 'today.date' ('LocalDate')를 '전달'
        viewModel.loadHistory(today.date)
        viewModel.fetchWeeklyAnalysis()
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

    /**
     * (★ 수정 ★) '리스너' '파라미터' '`date`'의 '타입'이 '`CalendarDay`' '임을' '명시'
     */
    private fun setupCalendarListener() {
        binding.calendarView.setOnDateChangedListener { widget, date: CalendarDay, selected ->
            // (★ 수정 ★) 'date.date' ('LocalDate')를 '직접' '전달'
            val selectedLocalDate = date.date

            viewModel.loadHistory(selectedLocalDate)
        }
    }

    // (observeUiState - 수정 없음)
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
     * (★ 수정 ★) 'HashSet' '타입' '불일치' '오류' '수정'
     */
    private fun observeRecordedDates() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {

                // 'recordedDaysSet'은 'Set<CalendarDay>' '타입'
                viewModel.recordedDates.collectLatest { recordedDaysSet ->
                    if (recordedDaysSet.isNotEmpty()) {

                        val currentMonth = binding.calendarView.currentDate

                        // (★ 수정 ★) 'allDaysInMonth'의 '타입'을 'HashSet'으로 '명시'
                        val allDaysInMonth = HashSet<CalendarDay>()
                        val calendar = Calendar.getInstance()
                        // (★ 수정 ★) '새' '달력' '라이브러리'의 '월'은 '1'부터 '시작'
                        calendar.set(currentMonth.year, currentMonth.month - 1, 1)
                        val maxDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)

                        for (i in 1..maxDay) {
                            // (★ 수정 ★) 'LocalDate' '대신' 'Calendar' '객체' '사용'
                            calendar.set(Calendar.DAY_OF_MONTH, i)
                            allDaysInMonth.add(CalendarDay.from(calendar))
                        }

                        // (★ 수정 ★) 'recordedDaysSet'을 'HashSet'으로 '변환'하여 '전달'
                        // (DateColorDecorators '파일'을 'Set'으로 '바꿨기' '때문에' '변환' '불필요')
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