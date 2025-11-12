package com.example.androidproject.presentation.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast // (★추가★) '오류' 메시지 '표시'를 위해 import
import androidx.core.view.isVisible // (★추가★) 'isVisible' 확장 함수 import
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels // (★추가★) 'by viewModels()' Hilt 주입을 위해 import
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope // (★추가★) 'lifecycleScope' import
import androidx.lifecycle.repeatOnLifecycle // (★추가★) 'repeatOnLifecycle' import
import com.example.androidproject.databinding.FragmentHistoryBinding // (★필수★) '실제 UI'의 ViewBinding import
// (★추가★) '핵심 두뇌' ViewModel import
import com.example.androidproject.presentation.viewmodel.RehabViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest // (★추가★) 'collectLatest' import
import kotlinx.coroutines.launch // (★추가★) 'launch' import
import java.util.Date // (★추가★) 'Date' import
import java.util.Calendar // (★추가★) Calendar import

/**
 * [파일 9/11] - '기록' 화면 '두뇌'
 * (★완성★) '핵심 두뇌'(RehabViewModel)와 '목록 관리자'(HistoryAdapter)를
 * '최종 연결'합니다.
 */
@AndroidEntryPoint // (님의 '팀원 1 가이드라인' 원칙 2)
class HistoryFragment : Fragment() {

    // (★필수★) ViewBinding 설정
    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!

    // (★필수★) '핵심 두뇌' ViewModel '주입' (가이드라인 2)
    private val viewModel: RehabViewModel by viewModels()

    // (★추가★) '기록' 목록 관리자(Adapter) 인스턴스 선언
    private lateinit var historyAdapter: HistoryAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // (★필수★) '실제 UI' (fragment_history.xml)를 연결
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // (★완성★) '팀원 1의 로드맵 Phase 4' 작업 시작

        // 1. '목록 관리자'와 'RecyclerView'를 '연결'합니다.
        setupRecyclerView()

        // 2. '달력'의 '날짜 선택' 이벤트를 '감지'합니다.
        setupCalendarListener()

        // 3. '핵심 두뇌'(ViewModel)의 'UI 상태'를 '관찰'합니다. (가이드라인 3)
        observeUiState()

        // 4. (★중요★) '처음' 화면이 '표시'될 때 '오늘 날짜'의 '기록'을 '요청'합니다.
        viewModel.loadHistory(Date(binding.calendarView.date)) // 'Date()' 대신 '달력'의 '현재 날짜' 사용
    }

    /**
     * (★추가★) 1. '목록 관리자'(HistoryAdapter)를 '설정'합니다.
     */
    private fun setupRecyclerView() {
        // 'HistoryAdapter'의 '새 인스턴스'를 '생성'합니다.
        historyAdapter = HistoryAdapter()

        // 'fragment_history.xml'의 'historyRecyclerView'에 'adapter'를 '연결'합니다.
        // (LayoutManager는 XML의 'app:layoutManager' 속성이 '자동'으로 '설정'해줍니다.)
        binding.historyRecyclerView.adapter = historyAdapter
    }

    /**
     * (★추가★) 2. '달력'의 '날짜 변경' 이벤트를 '설정'합니다.
     */
    private fun setupCalendarListener() {
        binding.calendarView.setOnDateChangeListener { view, year, month, dayOfMonth ->

            // 1. 선택된 날짜 (year, month, dayOfMonth)로 'Calendar' 객체를 만듭니다.
            val selectedCalendar = Calendar.getInstance().apply {
                set(year, month, dayOfMonth)
            }

            // 2. (가이드라인 4) 'java.util.Date' 객체로 '변환'합니다.
            val selectedDate = selectedCalendar.time

            // (디버그용) 날짜가 잘 선택되었는지 'Toast' 메시지를 띄웁니다.
            // val formattedDate = SimpleDateFormat("yyyy년 MM월 dd일", Locale.KOREA).format(selectedDate)
            // Toast.makeText(context, "$formattedDate 기록을 로드합니다.", Toast.LENGTH_SHORT).show()

            // 3. (★핵심★) '핵심 두뇌'(ViewModel)에게 "이 '날짜'의 '기록'을 '가져와'!"라고 '요청'합니다.
            // (가이드라인 1: ViewModel만 참조)
            viewModel.loadHistory(selectedDate)
        }
    }

    /**
     * (★추가★) 3. '핵심 두뇌'(ViewModel)의 'UI 상태'를 '관찰'합니다.
     * (가이드라인 3: Flow 관찰)
     */
    private fun observeUiState() {
        // 'Fragment'의 'View' 생명주기(STARTED)에 맞춰 '안전하게' 'Flow'를 '관찰'합니다.
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {

                // 'viewModel'의 'historyUiState'를 '구독'합니다.
                viewModel.historyUiState.collectLatest { state ->
                    // (가) '로딩 스피너'의 '표시'/'숨김' 상태를 '업데이트'합니다.
                    // (참고: 님의 fragment_history.xml에는 로딩 스피너가 빠져있습니다.
                    //  추후 fragment_home.xml을 참고하여 'loadingProgressBar'를 추가하는 것을 권장합니다.)
                    // binding.loadingProgressBar.isVisible = state.isLoading

                    // (나) '목록 관리자'에게 '새로운 기록 목록'을 '제출'합니다.
                    // (DiffUtil이 '자동'으로 '변경된' 항목만 '새로고침'합니다.)
                    historyAdapter.submitList(state.historyItems)

                    // (다) '오류' 메시지가 '있는' 경우 '토스트'를 '표시'합니다.
                    state.errorMessage?.let { message ->
                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                        // '오류'를 '처리'했음을 'ViewModel'에 '알립니다'.
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