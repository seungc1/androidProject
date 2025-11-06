package com.example.androidproject.presentation.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

/**
 * [연결성 3/4] - '기록' Fragment (임시 두뇌)
 * (nav_graph.xml이 참조할 수 있도록 임시로 생성)
 */
class HistoryFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        // (임시) fragment_history.xml 대신 간단한 텍스트 뷰를 반환
        return TextView(requireContext()).apply {
            text = "기록 화면 (HistoryFragment)"
            textSize = 24f
            textAlignment = View.TEXT_ALIGNMENT_CENTER
            // 시스템 바(상태표시줄 등)와 겹치지 않도록 패딩 추가
            ViewCompat.setOnApplyWindowInsetsListener(this) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }
        }
    }
}