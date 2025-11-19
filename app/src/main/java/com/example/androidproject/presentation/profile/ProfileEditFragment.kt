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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.androidproject.R
import com.example.androidproject.databinding.FragmentProfileEditBinding
import com.example.androidproject.domain.model.User
import com.example.androidproject.presentation.viewmodel.RehabViewModel
import com.google.android.material.button.MaterialButton
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ProfileEditFragment : Fragment() {

    private var _binding: FragmentProfileEditBinding? = null
    private val binding get() = _binding!!

    private val viewModel: RehabViewModel by activityViewModels()

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
        setupGenderButtons()

        // (★수정★) 안전한 데이터 로드 함수 호출
        observeProfileData()

        handleBackPress()
    }

    private fun handleBackPress() {
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            // uiState의 값을 안전하게 확인
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

    private fun setupGenderButtons() {
        binding.genderMaleButton.setOnClickListener {
            updateGenderSelection("남성")
        }
        binding.genderFemaleButton.setOnClickListener {
            updateGenderSelection("여성")
        }
    }

    private fun updateGenderSelection(gender: String) {
        selectedGender = gender

        val primaryColor = ContextCompat.getColor(requireContext(), com.google.android.material.R.color.design_default_color_primary)
        val onPrimaryColor = ContextCompat.getColor(requireContext(), com.google.android.material.R.color.design_default_color_on_primary)
        val outlineColor = Color.LTGRAY

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
            button.backgroundTintList = ColorStateList.valueOf(primary)
            button.setTextColor(onPrimary)
            button.strokeWidth = 0
        } else {
            button.backgroundTintList = ColorStateList.valueOf(Color.TRANSPARENT)
            button.setTextColor(Color.GRAY)
            button.strokeColor = ColorStateList.valueOf(outline)
            button.strokeWidth = 3
        }
    }

    /**
     * (★수정★) ViewModel의 StateFlow를 구독하여 데이터가 준비되면 UI를 업데이트합니다.
     * 기존의 'dummyUser' 직접 접근 방식을 제거하여 크래시를 방지합니다.
     */
    private fun observeProfileData() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // 1. 사용자 정보 관찰
                launch {
                    viewModel.currentUser.collectLatest { user ->
                        user?.let {
                            binding.nameEditText.hint = it.name
                            binding.ageEditText.hint = it.age.toString()

                            // 성별 초기값 설정
                            updateGenderSelection(it.gender)

                            binding.heightEditText.hint = it.heightCm.toString()
                            binding.weightEditText.hint = it.weightKg.toString()
                            binding.allergyEditText.hint = it.allergyInfo.joinToString(", ")

                            val painLevel = it.currentPainLevel.toFloat().coerceIn(0f, 10f)
                            binding.painLevelSlider.value = painLevel
                            updatePainLevelText(painLevel.toInt())

                            binding.additionalNotesEditText.hint = it.additionalNotes ?: "추가 사항 없음"
                        }
                    }
                }

                // 2. 부상 정보 관찰
                launch {
                    viewModel.currentInjury.collectLatest { injury ->
                        injury?.let {
                            binding.injuryAreaEditText.hint = it.bodyPart
                            binding.injuryNameEditText.hint = it.name
                        }
                    }
                }
            }
        }
    }

    private fun setupSaveButton() {
        binding.saveButton.setOnClickListener {
            // (★수정★) 현재 로드된 데이터를 안전하게 가져옴 (dummyUser 사용 X)
            val currentUser = viewModel.currentUser.value
            val currentInjury = viewModel.currentInjury.value

            // 데이터가 아직 로드되지 않았다면 저장 막기 (안전 장치)
            if (currentUser == null) {
                Toast.makeText(context, "데이터를 불러오는 중입니다. 잠시 후 다시 시도해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val inputName = binding.nameEditText.text.toString()
            val inputAge = binding.ageEditText.text.toString()
            val inputGender = if (selectedGender.isNotEmpty()) selectedGender else currentUser.gender
            val inputHeight = binding.heightEditText.text.toString()
            val inputWeight = binding.weightEditText.text.toString()
            val inputAllergy = binding.allergyEditText.text.toString()
            val inputInjuryArea = binding.injuryAreaEditText.text.toString()
            val inputInjuryName = binding.injuryNameEditText.text.toString()
            val inputNotes = binding.additionalNotesEditText.text.toString()

            val finalName = inputName.ifBlank { currentUser.name }
            val finalAge = inputAge.toIntOrNull() ?: currentUser.age
            val finalGender = inputGender
            val finalHeight = inputHeight.toIntOrNull() ?: currentUser.heightCm
            val finalWeight = inputWeight.toDoubleOrNull() ?: currentUser.weightKg
            val finalInjuryArea = inputInjuryArea.ifBlank { currentInjury?.bodyPart ?: "" }
            val finalInjuryName = inputInjuryName.ifBlank { currentInjury?.name ?: "" }

            // 유효성 검사
            if (finalName.isBlank() || finalName == "신규 사용자") {
                Toast.makeText(context, "이름을 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (finalAge <= 0) {
                Toast.makeText(context, "나이를 올바르게 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
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