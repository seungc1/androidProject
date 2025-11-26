package com.example.androidproject.presentation.detail

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.androidproject.R
import com.example.androidproject.databinding.FragmentExerciseFeedbackBinding
import com.example.androidproject.presentation.viewmodel.RehabViewModel
import com.google.android.material.button.MaterialButton // MaterialButton import 추가
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ExerciseFeedbackFragment : Fragment() {

    private var _binding: FragmentExerciseFeedbackBinding? = null
    private val binding get() = _binding!!

    private val viewModel: RehabViewModel by activityViewModels()

    private lateinit var exerciseId: String
    private var isCompletedStatus: Boolean = true // 현재 선택된 완수 여부 상태 (기본값: true)
    private var selectedRating: Int = 3         // 현재 선택된 만족도 상태 (기본값: 3 - 중)

    // 성별 버튼처럼 색상 변경 로직에 사용할 색상 리소스 ID 정의
    private val primaryColorResId = R.color.primaryColor
    private val whiteColorResId = R.color.white
    private val dividerColorResId = R.color.divider

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
            setupCustomButtonListeners()
            setupSaveButton(exerciseId)
        }
    }

    /**
     * 성별 버튼과 동일한 방식으로 동작하도록 커스텀 로직 구현
     */
    private fun setupCustomButtonListeners() {
        // 1. 완수 여부 버튼 그룹 관리
        val completionButtons = listOf(binding.completedYesButton, binding.completedNoButton)

        // 초기 상태 설정
        updateButtonStyle(binding.completedYesButton, true) // 기본: 완수함 선택

        completionButtons.forEach { button ->
            button.setOnClickListener {
                val isYes = it.id == R.id.completedYesButton
                if (isYes != isCompletedStatus) { // 상태가 변경되었을 때만 업데이트
                    isCompletedStatus = isYes
                    // 선택된 버튼만 활성화하고 나머지는 비활성화 (토글 로직)
                    completionButtons.forEach { btn ->
                        updateButtonStyle(btn, btn.id == it.id)
                    }
                }
            }
        }

        // 2. 수행 만족도 버튼 그룹 관리
        val ratingButtons = listOf(binding.ratingHighButton, binding.ratingMediumButton, binding.ratingLowButton)

        // 초기 상태 설정
        updateButtonStyle(binding.ratingMediumButton, true) // 기본: 중(보통) 선택

        ratingButtons.forEach { button ->
            button.setOnClickListener {
                val newRating = when (it.id) {
                    R.id.ratingHighButton -> 5
                    R.id.ratingMediumButton -> 3
                    R.id.ratingLowButton -> 1
                    else -> 3
                }

                if (newRating != selectedRating) { // 상태가 변경되었을 때만 업데이트
                    selectedRating = newRating
                    // 선택된 버튼만 활성화하고 나머지는 비활성화
                    ratingButtons.forEach { btn ->
                        updateButtonStyle(btn, btn.id == it.id)
                    }
                }
            }
        }
    }

    /**
     * 성별 버튼의 동적 스타일 변경 로직을 재사용합니다.
     */
    private fun updateButtonStyle(button: MaterialButton, isSelected: Boolean) {
        val primaryColor = ContextCompat.getColor(requireContext(), primaryColorResId)
        val onPrimaryColor = ContextCompat.getColor(requireContext(), whiteColorResId)
        val outlineColor = ContextCompat.getColor(requireContext(), dividerColorResId)
        val defaultTextColor = ContextCompat.getColor(requireContext(), R.color.textSecondary)

        if (isSelected) {
            // 선택됨: 배경색 채우기, 텍스트 흰색, 테두리 제거
            button.backgroundTintList = ColorStateList.valueOf(primaryColor)
            button.setTextColor(onPrimaryColor)
            button.strokeWidth = 0
            button.strokeColor = ColorStateList.valueOf(primaryColor)
        } else {
            // 미선택: 배경 투명, 텍스트 회색, 테두리 유지
            button.backgroundTintList = ColorStateList.valueOf(Color.TRANSPARENT)
            button.setTextColor(defaultTextColor)
            button.strokeColor = ColorStateList.valueOf(outlineColor)
            button.strokeWidth = 3 // 3dp로 설정 (성별 버튼과 유사하게)
        }
    }


    private fun setupSaveButton(exerciseId: String) {
        binding.saveButton.setOnClickListener {
            // 1. 상태 변수에서 최종 값 가져오기
            val rating = selectedRating
            val isCompleted = isCompletedStatus
            val notes = binding.notesEditText.text.toString()

            // 2. ViewModel에 기록 저장을 요청합니다.
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