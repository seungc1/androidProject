package com.example.androidproject.presentation.main // (중요) 님의 패키지 경로

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.androidproject.databinding.FragmentHomeBinding
import com.example.androidproject.presentation.viewmodel.RehabViewModel // (★원상 복구★)
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * [파일 7/11] - '홈' 화면 '두뇌'
 * (★원상 복구★)
 * '빌드'에 성공했으므로, '임시'로 주석 처리했던
 * '모든' 연결 코드를 '원상 복구'합니다.
 */
@AndroidEntryPoint
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    // (★원상 복구★) Hilt가 'RehabViewModel'을 '주입'합니다.
    private val viewModel: RehabViewModel by viewModels()

    // (★원상 복구★) '목록 관리자' 2개 '선언'
    private lateinit var exerciseAdapter: ExerciseAdapter
    private lateinit var dietAdapter: DietAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    /**
     * (★원상 복구★)
     * '빌드'가 성공했으므로, '자동 생성'된 'FragmentHomeBinding'이
     * 'exerciseCard'와 'dietCard'를 '인식'할 수 있습니다.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // (★원상 복구★) '연결' 코드들을 '원상 복구'합니다.
        setupAdapters()
        setupRecyclerViews()
        observeViewModel()
    }

    /**
     * (★원상 복구★) '목록 관리자(Adapter)' 2개를 '생성'합니다.
     */
    private fun setupAdapters() {
        // 1. '운동' 어댑터 생성
        exerciseAdapter = ExerciseAdapter { todayExercise ->
            viewModel.toggleExerciseCompletion(todayExercise.exercise.id)
        }

        // 2. '식단' 어댑터 생성
        dietAdapter = DietAdapter()
    }

    /**
     * (★원상 복구★) '목록'(RecyclerView) 2개를 '설정'합니다.
     */
    private fun setupRecyclerViews() {
        // 1. '운동 목록'에 '운동 어댑터' '연결'
        binding.exerciseRecyclerView.apply {
            adapter = exerciseAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }

        // 2. '식단 목록'에 '식단 어댑터' '연결'
        binding.dietRecyclerView.apply {
            adapter = dietAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    /**
     * (★원상 복구★) '핵심 두뇌'(ViewModel)를 '관찰'합니다.
     */
    private fun observeViewModel() {
        // viewLifecycleOwner.lifecycleScope.launch를 사용하여 코루틴을 시작합니다.
        viewLifecycleOwner.lifecycleScope.launch {
            // 코루틴 스코프 안에서 collectLatest를 호출합니다.
            viewModel.uiState.collectLatest { state ->
                binding.loadingProgressBar.visibility = if (state.isLoading) View.VISIBLE else View.GONE
                binding.exerciseCard.visibility = if (state.isLoading) View.GONE else View.VISIBLE
                binding.dietCard.visibility = if (state.isLoading) View.GONE else View.VISIBLE
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // 메모리 누수 방지
    }
}