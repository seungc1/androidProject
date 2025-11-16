package com.example.androidproject.presentation.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible // (★필수★) 'isVisible' 확장 함수 import
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels // (★ 수정 ★) 'viewModels' -> 'activityViewModels'
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

/**
 * [파일 9/11] - '기록' 화면 '두뇌'
 * (★ 수정 ★) '데이터가 없을 때' '안내' '문구'(emptyViewHistory)를
 * '표시'하는 '로직'을 'observeUiState'에 '추가'합니다.
 */
@AndroidEntryPoint
class HistoryFragment : Fragment() {

    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!

    // (★ 수정 ★) Hilt 범위 충돌을 해결하기 위해 'viewModels' -> 'activityViewModels'로 변경
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

        // (이하 코드는 우리가 이전에 완성한 상태입니다)
        setupRecyclerView()
        setupCalendarListener()
        observeUiState()

        // '처음' 화면이 '표시'될 때 '오늘 날짜'의 '기록'을 '요청'합니다.
        viewModel.loadHistory(Date(binding.calendarView.date))
    }

    private fun setupRecyclerView() {
        historyAdapter = HistoryAdapter()
        binding.historyRecyclerView.adapter = historyAdapter
    }

    private fun setupCalendarListener() {
        binding.calendarView.setOnDateChangeListener { view, year, month, dayOfMonth ->

            val selectedCalendar = Calendar.getInstance().apply {
                set(year, month, dayOfMonth)
            }
            val selectedDate = selectedCalendar.time

            // (★핵심★) '핵심 두뇌'(ViewModel)에게 '기록'을 '가져오라고' '요청'
            viewModel.loadHistory(selectedDate)
        }
    }

    /**
     * (★ 수정 ★) '핵심 두뇌'(ViewModel)의 'UI 상태'를 '관찰'합니다.
     * '데이터' '목록'이 '비어있으면' 'emptyViewHistory'를 '표시'합니다.
     */
    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {

                // 'viewModel'의 'historyUiState'를 '구독'합니다.
                viewModel.historyUiState.collectLatest { state ->

                    // 1. '로딩 스피너' '표시' 여부
                    binding.loadingProgressBar.isVisible = state.isLoading

                    // 2. '데이터가 있는지' '확인'
                    val hasHistory = state.historyItems.isNotEmpty()

                    // 3. (★ 핵심 ★) '데이터가 있고' '로딩이 끝나면' -> '목록' '표시'
                    binding.historyRecyclerView.isVisible = hasHistory && !state.isLoading

                    // 4. (★ 핵심 ★) '데이터가 없고' '로딩이 끝나면' -> '안내 문구' '표시'
                    binding.emptyViewHistory.isVisible = !hasHistory && !state.isLoading

                    // 5. '데이터' '업데이트'
                    historyAdapter.submitList(state.historyItems)

                    // 6. '오류' '처리' (기존과 동일)
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
        _binding = null // 메모리 누수 방지
    }
}