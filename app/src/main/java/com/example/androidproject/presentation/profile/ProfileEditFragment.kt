package com.example.androidproject.presentation.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.androidproject.databinding.FragmentProfileEditBinding
import com.example.androidproject.domain.model.User
import com.example.androidproject.presentation.viewmodel.RehabViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ProfileEditFragment : Fragment() {

    private var _binding: FragmentProfileEditBinding? = null
    private val binding get() = _binding!!

    private val viewModel: RehabViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileEditBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupSaveButton()
        setupPainLevelSlider()
        loadCurrentProfileData()
        handleBackPress()
    }

    private fun handleBackPress() {
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            val isComplete = viewModel.uiState.value.isProfileComplete
            if (!isComplete) {
                Toast.makeText(context, "정보 입력을 완료해야 서비스를 이용할 수 있습니다.", Toast.LENGTH_SHORT).show()
                requireActivity().finish()
            } else {
                if (isEnabled) {
                    isEnabled = false
                    requireActivity().onBackPressed()
                }
            }
        }
    }

    private fun setupPainLevelSlider() {
        // (★수정★) 슬라이더 값이 바뀔 때마다 텍스트+설명 업데이트
        binding.painLevelSlider.addOnChangeListener { _, value, _ ->
            updatePainLevelText(value.toInt())
        }
    }

    /**
     * (★추가★) 통증 점수에 따른 설명을 텍스트뷰에 표시하는 함수
     */
    private fun updatePainLevelText(value: Int) {
        val description = when (value) {
            0 -> "통증 없음"
            in 1..3 -> "경미함 (약간 불편)"
            in 4..6 -> "중등도 (일상생활 불편)"
            in 7..9 -> "심함 (매우 고통스러움)"
            10 -> "극심함 (응급 상황)"
            else -> ""
        }
        binding.painLevelValueTextView.text = "$value - $description"
    }

    private fun loadCurrentProfileData() {
        viewLifecycleOwner.lifecycleScope.launch {
            val user = viewModel.dummyUser
            val injury = viewModel.dummyInjury

            binding.nameEditText.hint = user.name
            binding.nameEditText.setText("")

            binding.ageEditText.hint = user.age.toString()
            binding.ageEditText.setText("")

            binding.genderEditText.hint = user.gender
            binding.genderEditText.setText("")

            binding.heightEditText.hint = user.heightCm.toString()
            binding.heightEditText.setText("")

            binding.weightEditText.hint = user.weightKg.toString()
            binding.weightEditText.setText("")

            binding.allergyEditText.hint = user.allergyInfo.joinToString(", ")
            binding.allergyEditText.setText("")

            binding.injuryAreaEditText.hint = injury.bodyPart
            binding.injuryAreaEditText.setText("")

            binding.injuryNameEditText.hint = injury.name
            binding.injuryNameEditText.setText("")

            // (★수정★) 초기값 설정 시에도 설명 텍스트 업데이트
            val painLevel = user.currentPainLevel.toFloat().coerceIn(0f, 10f)
            binding.painLevelSlider.value = painLevel
            updatePainLevelText(painLevel.toInt())

            binding.additionalNotesEditText.hint = user.additionalNotes ?: "추가 사항 없음"
            binding.additionalNotesEditText.setText("")
        }
    }

    private fun setupSaveButton() {
        binding.saveButton.setOnClickListener {
            val currentUser = viewModel.dummyUser
            val currentInjury = viewModel.dummyInjury

            val inputName = binding.nameEditText.text.toString()
            val inputAge = binding.ageEditText.text.toString()
            val inputGender = binding.genderEditText.text.toString()
            val inputHeight = binding.heightEditText.text.toString()
            val inputWeight = binding.weightEditText.text.toString()
            val inputAllergy = binding.allergyEditText.text.toString()

            val inputInjuryArea = binding.injuryAreaEditText.text.toString()
            val inputInjuryName = binding.injuryNameEditText.text.toString()
            val inputNotes = binding.additionalNotesEditText.text.toString()

            val finalName = inputName.ifBlank { currentUser.name }
            val finalAge = inputAge.toIntOrNull() ?: currentUser.age
            val finalGender = inputGender.ifBlank { currentUser.gender }
            val finalHeight = inputHeight.toIntOrNull() ?: currentUser.heightCm
            val finalWeight = inputWeight.toDoubleOrNull() ?: currentUser.weightKg
            val finalInjuryArea = inputInjuryArea.ifBlank { currentInjury.bodyPart }
            val finalInjuryName = inputInjuryName.ifBlank { currentInjury.name }

            if (finalName.isBlank() || finalName == "신규 사용자") {
                Toast.makeText(context, "이름을 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (finalAge <= 0) {
                Toast.makeText(context, "나이를 올바르게 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (finalGender.isBlank() || finalGender == "미설정") {
                Toast.makeText(context, "성별을 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (finalHeight <= 0) {
                Toast.makeText(context, "키를 올바르게 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (finalWeight <= 0.0) {
                Toast.makeText(context, "몸무게를 올바르게 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (finalInjuryArea.isBlank() || finalInjuryArea == "없음") {
                Toast.makeText(context, "환부(부상 부위)를 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (finalInjuryName.isBlank() || finalInjuryName == "없음") {
                Toast.makeText(context, "질환명을 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val updatedUser = User(
                id = currentUser.id,
                password = currentUser.password,
                name = finalName,
                gender = finalGender,
                age = finalAge,
                heightCm = finalHeight,
                weightKg = finalWeight,

                activityLevel = currentUser.activityLevel,
                fitnessGoal = currentUser.fitnessGoal,

                allergyInfo = if (inputAllergy.isNotBlank()) {
                    inputAllergy.split(",").map { it.trim() }
                } else {
                    currentUser.allergyInfo
                },

                preferredDietType = currentUser.preferredDietType,
                preferredDietaryTypes = currentUser.preferredDietaryTypes,
                equipmentAvailable = currentUser.equipmentAvailable,

                currentPainLevel = binding.painLevelSlider.value.toInt(),

                additionalNotes = inputNotes.ifBlank { currentUser.additionalNotes },
                targetCalories = currentUser.targetCalories,
                currentInjuryId = currentUser.currentInjuryId
            )

            viewModel.updateUserProfile(updatedUser, finalInjuryName, finalInjuryArea)

            Toast.makeText(context, "프로필이 저장되었습니다.", Toast.LENGTH_SHORT).show()
            findNavController().popBackStack()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}