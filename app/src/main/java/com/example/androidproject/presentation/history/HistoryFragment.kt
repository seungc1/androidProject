package com.example.androidproject.presentation.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.androidproject.databinding.FragmentHistoryBinding // (★수정★) '실제 UI'의 ViewBinding import
import dagger.hilt.android.AndroidEntryPoint

/**
 * [파일 9/11] - '기록' 화면 '두뇌'
 * (★수정★) '임시' 텍스트 대신 '실제 UI'(fragment_history.xml)를
 * 사용하도록 'ViewBinding' 코드로 '수정'합니다.
 */
@AndroidEntryPoint
class HistoryFragment : Fragment() {

    // (★수정★) ViewBinding 설정
    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // (★수정★) '실제 UI' (fragment_history.xml)를 연결
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // (성준민)
        // 나중에 이 곳에서 ViewModel의 '기록' 데이터를 가져와서
        // binding.historyRecyclerView 에 '연결'하는 코드를
        // 작성하게 됩니다. (팀원 1의 로드맵 Phase 4)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // 메모리 누수 방지
    }
}