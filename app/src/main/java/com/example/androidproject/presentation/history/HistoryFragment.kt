package com.example.androidproject.presentation.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels // (★필수★) '핵심 두뇌'를 '주입'받기 위해 import
import com.example.androidproject.databinding.FragmentHistoryBinding // (★필수★) '실제 UI'의 ViewBinding import
import com.example.androidproject.presentation.viewmodel.RehabViewModel // (★필수★) '핵심 두뇌' import
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * [파일 9/11] - '기록' 화면 '두뇌'
 * (★최종본★) '달력'(CalendarView)을 '클릭'했을 때
 * '날짜'를 '감지'하는 '연결' 코드가 '추가'된 버전입니다.
 * (팀원 1의 로드맵 Phase 4)
 */
@AndroidEntryPoint // (님의 '팀원 1 가이드라인' 원칙 2)
class HistoryFragment : Fragment() {

    // (★필수★) ViewBinding 설정
    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!

    // (★필수★) '기록' 탭의 '데이터'를 '관리'할 '핵심 두뇌'(ViewModel)를 '주입'받습니다.
    // (님의 '팀원 1 가이드라인' 원칙 2)
    private val viewModel: RehabViewModel by viewModels()

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

        // (★필수★) '달력'에 '날짜 선택 리스너'를 '연결'합니다.
        setupCalendarListener()

        // (성준민)
        // 나중에 이 곳에서 viewModel.historyResults.observe(...) (관찰) 코드를 추가하여
        // 님이 '터치'한 '날짜'의 '기록'('필드값')을 '목록 관리자(Adapter)'에
        // '연결'하게 됩니다. (팀원 1의 로드맵 Phase 4)
        // (예: binding.historyRecyclerView.adapter = historyAdapter)
    }

    /**
     * (★필수★)
     * 님이 요청하신 '달력'(calendarView)을 '터치'하면 '날짜'를 '감지'하는 '리스너'입니다.
     */
    private fun setupCalendarListener() {
        binding.calendarView.setOnDateChangeListener { view, year, month, dayOfMonth ->

            // 1. 선택된 날짜 (year, month, dayOfMonth)로 'Calendar' 객체를 만듭니다.
            val selectedCalendar = Calendar.getInstance().apply {
                set(year, month, dayOfMonth)
            }

            // 2. (님의 '팀원 1 가이드라인' 원칙 4) 'java.util.Date' 객체로 '변환'합니다.
            val selectedDate = selectedCalendar.time

            // (디버그용) 날짜가 잘 선택되었는지 'Toast' 메시지를 띄웁니다.
            val formattedDate = SimpleDateFormat("yyyy년 MM월 dd일", Locale.KOREA).format(selectedDate)
            Toast.makeText(context, "$formattedDate 기록을 로드합니다.", Toast.LENGTH_SHORT).show()

            // 3. (★핵심★) '핵심 두뇌'(ViewModel)에게 "이 '날짜'의 '기록'을 '가져와'!"라고 '요청'합니다.
            // (님의 '팀원 1 가이드라인' 원칙 1)
            // (이 기능을 위해서는 'RehabViewModel'에 'fetchHistoryForDate' 함수가 '필요'합니다.)
            // viewModel.fetchHistoryForDate(selectedDate)
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // 메모리 누수 방지
    }
}