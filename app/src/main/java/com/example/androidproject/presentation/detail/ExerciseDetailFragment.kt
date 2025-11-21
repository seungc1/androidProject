package com.example.androidproject.presentation.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.androidproject.R
import com.example.androidproject.databinding.FragmentExerciseDetailBinding
import com.example.androidproject.domain.model.Exercise
import com.example.androidproject.presentation.viewmodel.RehabViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * [수정 완료] - Safe Args 오류 우회를 위해 Bundle을 사용하여 인자를 전달합니다.
 */
@AndroidEntryPoint
class ExerciseDetailFragment : Fragment() {

    private var _binding: FragmentExerciseDetailBinding? = null
    private val binding get() = _binding!!

    private val viewModel: RehabViewModel by activityViewModels()

    // Safe Args 대신 멤버 변수와 onCreate()에서 Bundle을 사용하여 인자를 받습니다.
    private var selectedExercise: Exercise? = null
    private lateinit var exerciseId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ★★★ [수정] arguments?.getString("인자 이름")을 사용하여 ID를 직접 가져옵니다. ★★★
        exerciseId = arguments?.getString("exerciseId") ?: run {
            Toast.makeText(context, "운동 ID를 불러올 수 없습니다.", Toast.LENGTH_SHORT).show()
            findNavController().popBackStack()
            ""
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentExerciseDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (exerciseId.isNotEmpty()) {
            loadExerciseDetails(exerciseId)
            setupCompleteButton(exerciseId)
        }
    }

    private fun loadExerciseDetails(exerciseId: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            val state = viewModel.uiState.first()
            selectedExercise = state.todayExercises.find { it.exercise.id == exerciseId }?.exercise

            if (selectedExercise != null) {
                val exercise = selectedExercise!!

                // 1. 운동 이름 및 상세 설명 표시
                binding.detailExerciseNameTextView.text = exercise.name

                // 세트/렙수 및 난이도 표시
                val setsText = exercise.sets?.let { "${it}세트" } ?: ""
                val repsText = exercise.reps?.let { "${it}회" } ?: ""
                val detailSeparator = if (setsText.isNotEmpty() && repsText.isNotEmpty()) " / " else ""

                binding.detailExerciseSetsRepsTextView.text =
                    "부위: ${exercise.bodyPart} / ${exercise.difficulty} / ${setsText}${detailSeparator}${repsText}"

                binding.detailExerciseDescriptionTextView.text = exercise.description

                // 2. 이미지 로드 로직 (imageName 사용)
                exercise.imageName?.let { imageName ->
                    val imageResId = resources.getIdentifier(
                        imageName,
                        "drawable",
                        requireContext().packageName
                    )

                    if (imageResId != 0) {
                        // binding.exerciseImageView.setImageResource(imageResId) // XML에 ImageView가 있다면 활성화
                        android.util.Log.d("ImageLoad", "Image found for: $imageName")
                    }
                }
            } else {
                Toast.makeText(context, "운동 정보를 불러오는 데 실패했습니다.", Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()
            }
        }
    }

    /**
     * [수정] '완료 버튼' 클릭 시 Bundle을 사용하여 'ExerciseFeedbackFragment'로 이동
     */
    private fun setupCompleteButton(exerciseId: String) {
        binding.completeButton.setOnClickListener {
            val bundle = Bundle().apply {
                putString("exerciseId", exerciseId)
            }

            // Bundle과 Fragment ID를 사용하여 이동합니다.
            findNavController().navigate(R.id.exerciseFeedbackFragment, bundle)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}