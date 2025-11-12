package com.example.androidproject.presentation.main // (중요) 님의 패키지 경로

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
import androidx.navigation.fragment.findNavController // (★필수★)
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.androidproject.databinding.FragmentHomeBinding
import com.example.androidproject.presentation.viewmodel.RehabViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * [파일 7/11] - '홈' 화면 '두뇌'
 * (★수정★) '식단' 아이템 '클릭' 시 '식단 상세' 화면으로 '이동'하도록 '수정'합니다.
 */
@AndroidEntryPoint
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    // (★필수★) '상세' 화면들과 ViewModel을 '공유'
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
     * (★수정★) 'DietAdapter'의 '클릭' 람다에 '내비게이션' 코드를 '추가'합니다.
     */
    private fun setupAdapters() {
        // 1. '운동' 어댑터 생성 (기존 코드)
        exerciseAdapter = ExerciseAdapter { exercise ->
            // 'nav_graph.xml'이 '자동 생성'한 'Safe Args' 'Action'을 '사용'합니다.
            val action = HomeFragmentDirections.actionNavigationHomeToExerciseDetailFragment(
                exerciseId = exercise.id
            )
            findNavController().navigate(action) // '운동 상세' 화면으로 '이동'
        }

        // 2. '식단' 어댑터 생성 (★수정★)
        dietAdapter = DietAdapter { diet ->
            // (★ 추가 ★) 'nav_graph.xml'이 '자동 생성'한 'Safe Args' 'Action'을 '사용'합니다.
            val action = HomeFragmentDirections.actionNavigationHomeToDietDetailFragment(
                dietId = diet.id // (★핵심★) '클릭'된 '식단'의 'ID'를 '전달'
            )
            findNavController().navigate(action) // '식단 상세' 화면으로 '이동'
        }
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

    private fun observeViewModel() {
        // ... (수정 없음) ...
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {

                viewModel.uiState.collectLatest { state ->
                    binding.loadingProgressBar.isVisible = state.isLoading
                    binding.exerciseCard.isVisible = !state.isLoading
                    binding.dietCard.isVisible = !state.isLoading

                    binding.exerciseTitleTextView.text = "오늘의 운동 (${state.currentInjuryName ?: "전신"})"
                    binding.dietTitleTextView.text = "AI 추천 식단 (${state.userName}님)"

                    exerciseAdapter.submitList(state.todayExercises)
                    dietAdapter.submitList(state.recommendedDiets)

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