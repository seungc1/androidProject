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
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Date
import java.util.Calendar

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


        // 화면이 처음 열릴 때 '오늘 날짜 기록'과 'AI 주간 분석'을  호출합니다.
        viewModel.loadHistory(Date(binding.calendarView.date))
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
        binding.calendarView.setOnDateChangeListener { view, year, month, dayOfMonth ->
            val selectedCalendar = Calendar.getInstance().apply {
                set(year, month, dayOfMonth)
            }
            val selectedDate = selectedCalendar.time

            // (날짜별 기록만 새로 불러옴)
            viewModel.loadHistory(selectedDate)
        }
    }

    // UI 상태 관찰 로직은 AI 리포트가 오면 알아서 표시하도록 이미 구현되어 있습니다.
    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {

                viewModel.historyUiState.collectLatest { state ->

                    // 1. (일반 로딩) 날짜별 기록 로딩 스피너
                    binding.loadingProgressBar.isVisible = state.isLoading

                    // 2. (AI 로딩) SwipeRefresh 스피너
                    binding.swipeRefreshLayout.isRefreshing = state.isAnalyzing

                    // 3. (AI 리포트) AI 분석 결과가 있으면 카드 표시
                    if (state.analysisResult != null) {
                        binding.analysisCard.isVisible = true
                        binding.analysisSummaryTextView.text = state.analysisResult.summary
                    } else {
                        binding.analysisCard.isVisible = false
                    }

                    // 4. (기록 목록)
                    val hasHistory = state.historyItems.isNotEmpty()
                    binding.historyRecyclerView.isVisible = hasHistory && !state.isLoading
                    binding.emptyViewHistory.isVisible = !hasHistory && !state.isLoading

                    // 5. (데이터)
                    historyAdapter.submitList(state.historyItems)

                    // 6. (오류)
                    state.errorMessage?.let { message ->
                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                        viewModel.clearHistoryErrorMessage()
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