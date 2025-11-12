package com.example.androidproject.presentation.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
// (★ 수정 ★) 'by viewModels' -> 'by activityViewModels'로 '변경'
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
import java.util.Calendar // (이 import는 이미 있을 수 있습니다)

/**
 * [파일 9/11] - '기록' 화면 '두뇌'
 * (★ 수정 ★) 'Hilt' 충돌을 '해결'하기 위해 'by viewModels()'를
 * 'by activityViewModels()'로 '변경'합니다.
 */
@AndroidEntryPoint
class HistoryFragment : Fragment() {

    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!

    // (★ 수정 ★) Hilt 범위 충돌을 해결하기 위해 'viewModels' -> 'activityViewModels'로 변경
    // 이렇게 하면 '홈', '상세', '기록' 탭이 '모두' '동일한' ViewModel 인스턴스를 '공유'합니다.
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

            viewModel.loadHistory(selectedDate)
        }
    }

    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {

                viewModel.historyUiState.collectLatest { state ->

                    // (참고: fragment_history.xml에 loadingProgressBar가 없습니다)
                    // binding.loadingProgressBar.isVisible = state.isLoading

                    historyAdapter.submitList(state.historyItems)

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