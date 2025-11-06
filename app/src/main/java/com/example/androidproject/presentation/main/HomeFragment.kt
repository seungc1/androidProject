package com.example.androidproject.presentation.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.androidproject.R
import com.example.androidproject.databinding.FragmentHomeBinding // (성준민 추가) ViewBinding import
import dagger.hilt.android.AndroidEntryPoint

/**
 * [연결성 2/4] - '홈' Fragment (두뇌)
 * fragment_home.xml의 실제 로직을 담당합니다.
 * nav_graph.xml이 이 파일을 참조합니다.
 */
@AndroidEntryPoint // Hilt를 사용하기 위한 어노테이션
class HomeFragment : Fragment() {

    // ViewBinding 설정
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // fragment_home.xml 레이아웃과 '두뇌'를 연결합니다.
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // (나중에 ViewModel과 RecyclerView 어댑터를 여기서 연결합니다)
        // 예: binding.exerciseRecyclerView.adapter = ...
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // 메모리 누수 방지
    }
}