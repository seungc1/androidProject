package com.example.androidproject.presentation.main

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
import com.example.androidproject.R
import com.example.androidproject.databinding.FragmentHomeBinding
import com.example.androidproject.presentation.viewmodel.RehabViewModel
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
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {

                viewModel.uiState.collectLatest { state ->
                    // 1. '로딩 스피너'
                    binding.loadingProgressBar.isVisible = state.isLoading
                    binding.exerciseCard.isVisible = !state.isLoading
                    binding.dietCard.isVisible = !state.isLoading

                    val hasExercises = state.todayExercises.isNotEmpty()
                    val hasProfile = state.userName.isNotEmpty()

                    // 2. 운동 '빈 화면' 로직
                    binding.exerciseRecyclerView.isVisible = hasExercises
                    binding.emptyViewExercise.isVisible = !hasExercises

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

                    // 3. 식단 '빈 화면' 로직
                    val hasDiets = state.recommendedDiets.isNotEmpty()
                    binding.dietRecyclerView.isVisible = hasDiets
                    binding.emptyViewDiet.isVisible = !hasDiets

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