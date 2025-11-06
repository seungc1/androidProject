package com.example.androidproject.presentation.main // (중요) 님의 패키지 경로

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.androidproject.databinding.FragmentHomeBinding // (중요) ViewBinding import
import dagger.hilt.android.AndroidEntryPoint

/**
 * [파일 7/11] - '홈' 화면 '두뇌'
 * 'fragment_home.xml' UI와 짝을 이룹니다.
 *
 * (수정) '빌드'에 성공했으므로,
 * onViewCreated의 '진짜' 코드를 '원상 복구'합니다.
 */
@AndroidEntryPoint
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    // 이 'binding' 변수는 onCreateView가 호출된 후부터 onDestroyView가 호출되기 전까지만 유효합니다.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // ViewBinding을 사용하여 UI를 연결합니다.
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    /**
     * (수정) '빌드'에 성공했으므로, '임시'로 비워뒀던
     * '둥근 칸'을 화면에 보여주는 '진짜' 코드를 '원상 복구'합니다.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // (성준민)
        // 님이 원하신 '필드값'과 '로드맵'에 따라,
        // 나중에 이 곳에서 ViewModel의 '운동/식단' 데이터를 가져와서
        // binding.exerciseRecyclerView 와 binding.dietRecyclerView 에
        // '연결'하는 코드를 작성하게 됩니다.

        // (★원상 복구★)
        // '빌드'가 성공했기 때문에, 이제 이 코드는
        // 'Unresolved reference' (참조 없음) 오류를
        // 일으키지 않습니다!
        binding.loadingProgressBar.visibility = View.GONE
        binding.exerciseCard.visibility = View.VISIBLE
        binding.dietCard.visibility = View.VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // 메모리 누수 방지
    }
}