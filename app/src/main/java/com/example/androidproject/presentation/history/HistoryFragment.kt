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

@AndroidEntryPoint
class HistoryFragment : Fragment() {

    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!

    // (★수정★) sharedViewModel 대신 전용 ViewModel 사용
    private val viewModel: HistoryViewModel by viewModels()

    private lateinit var historyAdapter: HistoryAdapter

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

    private fun setupCalendarListener() {
        binding.calendarView.setOnDateChangedListener { _, date, _ ->
            viewModel.loadHistory(date.date)
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
                    binding.historyRecyclerView.isVisible = hasHistory && !state.isLoading

                    // ★★★ ID를 emptyStateTextView로 변경합니다. ★★★
                    binding.historyEmptyMessageTextView.isVisible = !hasHistory && !state.isLoading

                    historyAdapter.submitList(state.historyItems)

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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}