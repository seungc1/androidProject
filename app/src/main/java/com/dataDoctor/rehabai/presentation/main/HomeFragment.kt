package com.dataDoctor.rehabai.presentation.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.dataDoctor.rehabai.R
import com.dataDoctor.rehabai.databinding.FragmentHomeBinding
import com.dataDoctor.rehabai.presentation.viewmodel.RehabViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

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
        setupClickListeners()
        observeViewModel()
    }

    private fun setupAdapters() {
        exerciseAdapter = ExerciseAdapter { exercise ->
            val action = HomeFragmentDirections.actionNavigationHomeToExerciseDetailFragment(
                exerciseId = exercise.id
            )
            findNavController().navigate(action)
        }
        dietAdapter = DietAdapter { diet ->
            val action = HomeFragmentDirections.actionNavigationHomeToDietDetailFragment(
                dietId = diet.id
            )
            findNavController().navigate(action)
        }
    }

    private fun setupRecyclerViews() {
        binding.exerciseRecyclerView.apply {
            adapter = exerciseAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
        binding.dietRecyclerView.apply {
            adapter = dietAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun setupClickListeners() {
        binding.goToProfileFromExerciseButton.setOnClickListener {
            val action = HomeFragmentDirections.actionNavigationHomeToProfileEditFragment()
            findNavController().navigate(action)
        }
        binding.goToProfileFromDietButton.setOnClickListener {
            val action = HomeFragmentDirections.actionNavigationHomeToProfileEditFragment()
            findNavController().navigate(action)
        }

        // [추가] 식단 기록 FAB 클릭 리스너
        binding.recordDietFab.setOnClickListener {
            DietRecordDialog().show(childFragmentManager, DietRecordDialog.TAG)
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {

                viewModel.uiState.collectLatest { state ->

                    // 1. 전체 로딩 (스플래시 화면 종료 후 최초 로딩용)
                    // state.isLoading이 true일 때는 MainActivity에서 navHostFragment가 가려지므로, 여기서는 루틴 로딩만 신경씁니다.

                    // A. 기본 화면 로드 완료 (Profile 로드 완료)
                    val isContentReady = !state.isRoutineLoading // 루틴 로딩이 끝나야 컨텐츠가 표시됨
                    val hasProfile = state.userName.isNotEmpty()
                    val hasExercises = state.todayExercises.isNotEmpty()
                    val hasDiets = state.recommendedDiets.isNotEmpty()

                    // 컨텐츠 카드 전체의 가시성 (프로필 로딩이 끝나면 보여주기 시작)
                    binding.exerciseCard.isVisible = !state.isLoading
                    binding.dietCard.isVisible = !state.isLoading

                    // 2. 운동 컨텐츠 로딩 상태 제어 (isRoutineLoading에 따라 UI 전환)

                    // 운동 목록/빈 화면 표시 여부: 컨텐츠 로딩이 완료되었을 때만 표시
                    binding.exerciseRecyclerView.isVisible = isContentReady && hasExercises
                    binding.emptyViewExercise.isVisible = isContentReady && !hasExercises

                    if (state.isRoutineLoading) {
                        // 루틴 로딩 중일 때: 리사이클러뷰와 빈 화면은 숨깁니다. (컨텐츠 영역에 스피너가 있다고 가정)
                        binding.exerciseRecyclerView.isVisible = false
                        binding.emptyViewExercise.isVisible = false
                    } else {
                        // 로딩 완료 후:
                        if (!hasExercises) { // 운동이 없을 때
                            if (hasProfile) {
                                // 프로필O, 운동X -> "오늘은 운동 없음" (API 실패 또는 휴식일)
                                binding.emptyViewExercise.findViewById<TextView>(R.id.emptyExerciseTextView).text = getString(R.string.home_no_exercises_today)
                                binding.goToProfileFromExerciseButton.isVisible = false
                            } else {
                                // 프로필X, 운동X -> "개인정보 입력"
                                binding.emptyViewExercise.findViewById<TextView>(R.id.emptyExerciseTextView).text = getString(R.string.home_empty_message)
                                binding.goToProfileFromExerciseButton.isVisible = true
                            }
                        }
                    }

                    // 3. 식단 컨텐츠 로딩 상태 제어
                    binding.dietRecyclerView.isVisible = isContentReady && hasDiets
                    binding.emptyViewDiet.isVisible = isContentReady && !hasDiets

                    if (state.isRoutineLoading) {
                        // 로딩 중일 때 숨김
                        binding.dietRecyclerView.isVisible = false
                        binding.emptyViewDiet.isVisible = false
                    } else {
                        // 로딩 완료 후:
                        if (!hasDiets) { // 식단이 없을 때
                            if (hasProfile) {
                                // 프로필O, 식단X -> "오늘은 식단 없음" (임시 텍스트)
                                binding.emptyViewDiet.findViewById<TextView>(R.id.emptyDietTextView).text = "오늘은 추천된 식단이 없습니다."
                                binding.goToProfileFromDietButton.isVisible = false
                            } else {
                                // 프로필X, 식단X -> "개인정보 입력"
                                binding.emptyViewDiet.findViewById<TextView>(R.id.emptyDietTextView).text = getString(R.string.home_empty_message)
                                binding.goToProfileFromDietButton.isVisible = true
                            }
                        }
                    }

                    // 4. '데이터' '업데이트'
                    binding.exerciseTitleTextView.text = "오늘의 운동 (${state.currentInjuryName ?: "전신"})"
                    binding.dietTitleTextView.text = "AI 추천 식단 (${state.userName.ifEmpty { "사용자" }}님)"

                    exerciseAdapter.submitList(state.todayExercises)
                    dietAdapter.submitList(state.recommendedDiets)

                    // 5. '오류' '처리'
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