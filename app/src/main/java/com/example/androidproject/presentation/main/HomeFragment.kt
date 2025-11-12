package com.example.androidproject.presentation.main // (중요) 님의 패키지 경로

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible // (★추가★) 'isVisible' 확장 함수 import
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels // (★수정★) 'viewModels' -> 'activityViewModels'
import androidx.lifecycle.Lifecycle // (★추가★)
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle // (★추가★)
import androidx.navigation.fragment.findNavController // (★추가★) '내비게이션'을 위해 import
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.androidproject.databinding.FragmentHomeBinding
import com.example.androidproject.presentation.viewmodel.RehabViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * [파일 7/11] - '홈' 화면 '두뇌'
 * (★수정★) '운동' 아이템 '클릭' 시 '상세' 화면으로 '이동'하도록 '수정'합니다.
 * (★수정★) '상세' 화면과 'ViewModel'을 '공유'하기 위해 'by viewModels()' -> 'by activityViewModels()'로 '수정'합니다.
 */
@AndroidEntryPoint
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    // (★수정★) 'by viewModels()' -> 'by activityViewModels()'
    // '상세' 화면(ExerciseDetailFragment)과 '동일한' ViewModel 인스턴스를 '공유'합니다.
    private val viewModel: RehabViewModel by activityViewModels()

    private lateinit var exerciseAdapter: ExerciseAdapter
    private lateinit var dietAdapter: DietAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupAdapters()
        setupRecyclerViews()
        observeViewModel()
    }

    /**
     * (★수정★) 'ExerciseAdapter'의 '클릭' 람다를 '내비게이션' 코드로 '변경'합니다.
     */
    private fun setupAdapters() {
        // 1. '운동' 어댑터 생성
        exerciseAdapter = ExerciseAdapter { exercise ->
            // (★수정★) 'toggle' 람다 대신 'onItemClick' 람다 구현
            // 'nav_graph.xml'이 '자동 생성'한 'Safe Args' 'Action'을 '사용'합니다.
            val action = HomeFragmentDirections.actionNavigationHomeToExerciseDetailFragment(
                exerciseId = exercise.id // (★핵심★) '클릭'된 '운동'의 'ID'를 '전달'
            )
            findNavController().navigate(action) // '상세' 화면으로 '이동'
        }

        // 2. '식단' 어댑터 생성
        dietAdapter = DietAdapter()
    }

    private fun setupRecyclerViews() {
        // ... (수정 없음) ...
        binding.exerciseRecyclerView.apply {
            adapter = exerciseAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
        binding.dietRecyclerView.apply {
            adapter = dietAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    /**
     * (★수정★) 'UI 상태' '관찰' 코드를 '표준'에 맞게 '수정'합니다.
     * (isVisible, repeatOnLifecycle 등)
     */
    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            // (★수정★) 'Fragment'의 'View' 생명주기에 '안전하게' '연결'
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {

                viewModel.uiState.collectLatest { state ->
                    // (★수정★) 'isVisible' 확장 함수를 '사용'하여 '간결'하게 '수정'
                    binding.loadingProgressBar.isVisible = state.isLoading
                    binding.exerciseCard.isVisible = !state.isLoading
                    binding.dietCard.isVisible = !state.isLoading

                    // (참고) '환영' 메시지를 '추가'하면 좋습니다.
                    // binding.welcomeTextView.text = "${state.userName}님, 안녕하세요!"

                    // (★수정★) '텍스트' '업데이트' (기존과 동일)
                    binding.exerciseTitleTextView.text = "오늘의 운동 (${state.currentInjuryName ?: "전신"})"
                    binding.dietTitleTextView.text = "AI 추천 식단 (${state.userName}님)"

                    // (★수정★) '목록' '업데이트' (기존과 동일)
                    exerciseAdapter.submitList(state.todayExercises)
                    dietAdapter.submitList(state.recommendedDiets)

                    // (★수정★) '오류' '처리' (기존과 동일)
                    state.errorMessage?.let {
                        Toast.makeText(context, "오류: $it", Toast.LENGTH_LONG).show()
                        viewModel.clearErrorMessage()
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