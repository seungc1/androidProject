package com.example.androidproject.presentation.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.androidproject.R
import com.example.androidproject.databinding.FragmentExerciseFeedbackBinding
import com.example.androidproject.presentation.viewmodel.RehabViewModel
import dagger.hilt.android.AndroidEntryPoint

/**
 * [수정 완료] - 알약 모양 버튼 (MaterialButtonToggleGroup)에 맞춰 코드 수정 및 초기 선택 설정
 */
@AndroidEntryPoint
class ExerciseFeedbackFragment : Fragment() {

    private var _binding: FragmentExerciseFeedbackBinding? = null
    private val binding get() = _binding!!

    private val viewModel: RehabViewModel by activityViewModels()

    private lateinit var exerciseId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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
        _binding = FragmentExerciseFeedbackBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (exerciseId.isNotEmpty()) {
            // ★★★ [수정] MaterialButtonToggleGroup의 초기 선택 상태를 코드에서 설정 ★★★
            binding.completionToggleGroup.check(R.id.completedYesButton) // "완수함" 초기 선택
            binding.ratingToggleGroup.check(R.id.ratingMediumButton)    // "중 (보통)" 초기 선택

            setupSaveButton(exerciseId)
        }
    }

    private fun setupSaveButton(exerciseId: String) {
        binding.saveButton.setOnClickListener {
            // 1. 만족도 (ratingToggleGroup) 값을 1/3/5로 변환
            val rating = when (binding.ratingToggleGroup.checkedButtonId) {
                R.id.ratingHighButton -> 5
                R.id.ratingMediumButton -> 3
                R.id.ratingLowButton -> 1
                else -> 3 // 기본값 '중'
            }

            // 2. 운동 완수 여부 (completionToggleGroup)
            val isCompleted = binding.completionToggleGroup.checkedButtonId == R.id.completedYesButton

            // 3. 후기 텍스트
            val notes = binding.notesEditText.text.toString()

            // 4. ViewModel에 기록 저장을 요청합니다.
            viewModel.saveRehabSessionDetails(
                exerciseId = exerciseId,
                rating = rating,
                notes = notes,
                isCompleted = isCompleted
            )

            Toast.makeText(context, "운동 기록이 저장되었습니다.", Toast.LENGTH_SHORT).show()

            findNavController().popBackStack()
            findNavController().popBackStack()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}