package com.example.androidproject.presentation.main // (중요) 님의 패키지 경로

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible // (★필수★) 'isVisible' 확장 함수 import
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController // (★필수★) '내비게이션' import
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.androidproject.databinding.FragmentHomeBinding
import com.example.androidproject.presentation.viewmodel.RehabViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * [파일 7/11] - '홈' 화면 '두뇌'
 * (★ 수정 ★) '빈 화면'의 '개인정보 입력하기' '버튼'('goToProfile...')에
 * '클릭 리스너'를 '복구'하고, '수정' 페이지로 '이동'하도록 '수정'합니다.
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
        setupClickListeners() // (★ 복구 ★) '빈 화면'의 '버튼' 리스너 '연결'
        observeViewModel()
    }

    /**
     * '목록' '클릭' 시 '상세' 페이지로 '이동'하는 '어댑터' '설정'
     */
    private fun setupAdapters() {
        // 1. '운동' 어댑터 생성 (기존 '상세' 페이지 이동 코드)
        exerciseAdapter = ExerciseAdapter { exercise ->
            val action = HomeFragmentDirections.actionNavigationHomeToExerciseDetailFragment(
                exerciseId = exercise.id
            )
            findNavController().navigate(action)
        }

        // 2. '식단' 어댑터 생성 (기존 '상세' 페이지 이동 코드)
        dietAdapter = DietAdapter { diet ->
            val action = HomeFragmentDirections.actionNavigationHomeToDietDetailFragment(
                dietId = diet.id
            )
            findNavController().navigate(action)
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

    /**
     * (★ 복구 및 수정 ★)
     * '빈 화면'의 '개인정보 입력하기' 버튼 '클릭' '리스너' '설정'
     */
    private fun setupClickListeners() {

        // '운동' 카드의 '개인정보' 버튼
        binding.goToProfileFromExerciseButton.setOnClickListener {
            // 'nav_graph.xml'에 '새로 추가'한 'action' ID를 '사용'하여 '이동'
            val action = HomeFragmentDirections.actionNavigationHomeToProfileEditFragment()
            findNavController().navigate(action)
        }

        // '식단' 카드의 '개인정보' 버튼
        binding.goToProfileFromDietButton.setOnClickListener {
            // 'nav_graph.xml'에 '새로 추가'한 'action' ID를 '사용'하여 '이동'
            val action = HomeFragmentDirections.actionNavigationHomeToProfileEditFragment()
            findNavController().navigate(action)
        }
    }

    /**
     * (★ 수정 ★) 'UI 상태' '관찰' 로직 ('빈 화면' '제어' 로직은 '유지')
     */
    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {

                viewModel.uiState.collectLatest { state ->
                    // 1. '로딩 스피너' '표시' 여부
                    binding.loadingProgressBar.isVisible = state.isLoading

                    // 2. '카드' '표시' 여부 ('로딩'이 '끝나면' '무조건' '표시')
                    binding.exerciseCard.isVisible = !state.isLoading
                    binding.dietCard.isVisible = !state.isLoading

                    // 3. '운동' '데이터' '유무' '처리'
                    // (★ 수정 ★) '사용자 이름'이 '없으면' '무조건' '빈 화면' '표시'
                    val hasExercises = state.todayExercises.isNotEmpty()
                    val hasProfile = state.userName.isNotEmpty()

                    binding.exerciseRecyclerView.isVisible = hasExercises && hasProfile
                    binding.emptyViewExercise.isVisible = !hasExercises || !hasProfile

                    // 4. '식단' '데이터' '유무' '처리'
                    // (★ 수정 ★) '사용자 이름'이 '없으면' '무조건' '빈 화면' '표시'
                    val hasDiets = state.recommendedDiets.isNotEmpty()
                    binding.dietRecyclerView.isVisible = hasDiets && hasProfile
                    binding.emptyViewDiet.isVisible = !hasDiets || !hasProfile

                    // 5. '데이터' '업데이트'
                    binding.exerciseTitleTextView.text = "오늘의 운동 (${state.currentInjuryName ?: "전신"})"
                    binding.dietTitleTextView.text = "AI 추천 식단 (${state.userName.ifEmpty { "사용자" }}님)" // '이름'이 '없으면' "사용자님"

                    exerciseAdapter.submitList(state.todayExercises)
                    dietAdapter.submitList(state.recommendedDiets)

                    // 6. '오류' '처리'
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