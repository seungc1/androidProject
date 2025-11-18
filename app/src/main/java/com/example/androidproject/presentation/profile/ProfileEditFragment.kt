package com.example.androidproject.presentation.profile

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.addCallback
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.androidproject.R
import com.example.androidproject.databinding.FragmentProfileEditBinding
import com.example.androidproject.domain.model.User
import com.example.androidproject.presentation.viewmodel.RehabViewModel
import com.google.android.material.button.MaterialButton
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ProfileEditFragment : Fragment() {

    private var _binding: FragmentProfileEditBinding? = null
    private val binding get() = _binding!!

    private val viewModel: RehabViewModel by activityViewModels()

    // (★추가★) 현재 선택된 성별을 추적하는 변수
    private var selectedGender: String = ""

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
        setupGenderButtons() // (★추가★) 성별 버튼 리스너 설정
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
        binding.painLevelSlider.addOnChangeListener { _, value, _ ->
            updatePainLevelText(value.toInt())
        }
    }

    private fun updatePainLevelText(value: Int) {
        val description = when (value) {
            0 -> "통증 없음"
            1 -> "아주 경미함 (거의 느껴지지 않음)"
            2 -> "경미함 (약간 거슬리는 정도)"
            3 -> "약한 통증 (참을 수 있음)"
            4 -> "불편함 (치통 정도의 통증)"
            5 -> "통증 있음 (계속 신경 쓰임)"
            6 -> "꽤 아픔 (활동에 지장이 생김)"
            7 -> "심함 (진통제가 필요한 수준)"
            8 -> "매우 심함 (일상생활 불가)"
            9 -> "극심함 (참기 힘든 고통)"
            10 -> "최악의 통증 (응급 상황)"
            else -> ""
        }
        binding.painLevelValueTextView.text = "$value - $description"
    }

    // (★추가★) 성별 버튼 클릭 리스너
    private fun setupGenderButtons() {
        binding.genderMaleButton.setOnClickListener {
            updateGenderSelection("남성")
        }
        binding.genderFemaleButton.setOnClickListener {
            updateGenderSelection("여성")
        }
    }

    // (★추가★) 선택된 성별에 따라 버튼 스타일(색상, 테두리) 변경
    private fun updateGenderSelection(gender: String) {
        selectedGender = gender

        val primaryColor = ContextCompat.getColor(requireContext(), com.google.android.material.R.color.design_default_color_primary)
        val onPrimaryColor = ContextCompat.getColor(requireContext(), com.google.android.material.R.color.design_default_color_on_primary)
        val outlineColor = Color.GRAY // 또는 테마의 outline color
        val surfaceColor = Color.TRANSPARENT

        // 남성 버튼 스타일 업데이트
        if (gender == "남성") {
            setButtonStyle(binding.genderMaleButton, true, primaryColor, onPrimaryColor, outlineColor)
            setButtonStyle(binding.genderFemaleButton, false, primaryColor, onPrimaryColor, outlineColor)
        } else if (gender == "여성") {
            setButtonStyle(binding.genderMaleButton, false, primaryColor, onPrimaryColor, outlineColor)
            setButtonStyle(binding.genderFemaleButton, true, primaryColor, onPrimaryColor, outlineColor)
        }
    }

    private fun setButtonStyle(button: MaterialButton, isSelected: Boolean, primary: Int, onPrimary: Int, outline: Int) {
        if (isSelected) {
            // 선택됨: 배경색(Primary), 글자색(White), 테두리 없음
            button.backgroundTintList = ColorStateList.valueOf(primary)
            button.setTextColor(onPrimary)
            button.strokeWidth = 0
        } else {
            // 선택 안됨: 배경색(투명), 글자색(Primary or Gray), 테두리 있음
            button.backgroundTintList = ColorStateList.valueOf(Color.TRANSPARENT)
            button.setTextColor(primary) // 또는 Color.GRAY
            button.strokeColor = ColorStateList.valueOf(outline)
            button.strokeWidth = 3 // 1dp 정도 (px 단위이므로 3 정도 줌)
        }
    }

    private fun loadCurrentProfileData() {
        viewLifecycleOwner.lifecycleScope.launch {
            val user = viewModel.dummyUser
            val injury = viewModel.dummyInjury

            binding.nameEditText.hint = user.name
            binding.nameEditText.setText("")

            binding.ageEditText.hint = user.age.toString()
            binding.ageEditText.setText("")

            // (★수정★) 초기 성별 설정
            updateGenderSelection(user.gender)

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

            // (★수정★) 변수에 저장된 성별 사용
            // 초기값이 빈 문자열일 수 있으므로, 선택 안했으면 기존 값 유지
            val finalGender = if (selectedGender.isNotEmpty()) selectedGender else currentUser.gender

            val inputHeight = binding.heightEditText.text.toString()
            val inputWeight = binding.weightEditText.text.toString()
            val inputAllergy = binding.allergyEditText.text.toString()

            val inputInjuryArea = binding.injuryAreaEditText.text.toString()
            val inputInjuryName = binding.injuryNameEditText.text.toString()
            val inputNotes = binding.additionalNotesEditText.text.toString()

            val finalName = inputName.ifBlank { currentUser.name }
            val finalAge = inputAge.toIntOrNull() ?: currentUser.age
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
            // (★수정★) 성별 유효성 검사
            if (finalGender.isBlank() || finalGender == "미설정") {
                Toast.makeText(context, "성별을 선택해주세요.", Toast.LENGTH_SHORT).show()
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