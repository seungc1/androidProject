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

    // (★추가★) 데이터가 실제로 로드되었는지 확인하는 플래그
    private var isDataLoaded = false

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

        // (★수정★) 초기에는 저장 버튼 비활성화 (데이터 로드 후 활성화)
        binding.saveButton.isEnabled = false

        observeProfileData()
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
     * (★수정★) 데이터 관찰 로직 강화
     * - null 체크를 더 엄격하게 하여, 빈 값으로 UI가 초기화되는 것을 방지합니다.
     * - 데이터가 모두 로드되었을 때만 '저장' 버튼을 활성화합니다.
     */
    private fun observeProfileData() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {

                launch {
                    viewModel.currentUser.collectLatest { user ->
                        if (user != null) {
                            // 이름이 "로딩 중..."이 아닐 때만 반영 (임시 객체 필터링)
                            if (user.name != "로딩 중...") {
                                binding.nameEditText.setText(user.name)
                                binding.ageEditText.setText(if(user.age > 0) user.age.toString() else "")

                                selectedGender = user.gender
                                updateGenderSelection(user.gender)

                                binding.heightEditText.setText(if(user.heightCm > 0) user.heightCm.toString() else "")
                                binding.weightEditText.setText(if(user.weightKg > 0.0) user.weightKg.toString() else "")
                                binding.allergyEditText.setText(user.allergyInfo.joinToString(", "))

                                val painLevel = user.currentPainLevel.toFloat().coerceIn(0f, 10f)
                                binding.painLevelSlider.value = painLevel
                                updatePainLevelText(painLevel.toInt())

                                binding.additionalNotesEditText.setText(user.additionalNotes ?: "")

                                // 사용자 데이터 로드 완료 표시
                                checkDataLoaded()
                            }
                        }
                    }
                }

                launch {
                    viewModel.currentInjury.collectLatest { injury ->
                        // injury가 null일 수도 있음 (부상 정보가 없는 경우)
                        // 하지만 "초기화"되는 문제를 막기 위해, null이 아닌 경우에만 확실하게 세팅
                        if (injury != null) {
                            binding.injuryAreaEditText.setText(injury.bodyPart)
                            binding.injuryNameEditText.setText(injury.name)
                        }
                        // 부상 정보 로드 시도 완료 (null이어도 로드는 된 것임)
                        checkDataLoaded()
                    }
                }
            }
        }
    }

    // (★추가★) 데이터 로드 완료 체크 함수
    private fun checkDataLoaded() {
        // 사용자 정보가 있으면 로드된 것으로 간주하고 버튼 활성화
        if (viewModel.currentUser.value != null) {
            isDataLoaded = true
            binding.saveButton.isEnabled = true
        }
    }

    private fun setupSaveButton() {
        binding.saveButton.setOnClickListener {
            // (★수정★) 데이터 로드 확인
            if (!isDataLoaded) {
                Toast.makeText(context, "데이터를 불러오는 중입니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // ViewModel의 현재 값 사용 (dummyUser 대신)
            val currentUser = viewModel.currentUser.value!!

            // (★수정★) 현재 입력된 환부/질환명/통증수준을 가져옵니다.
            val currentInjuryBodyPart = binding.injuryAreaEditText.text.toString()
            val currentInjuryName = binding.injuryNameEditText.text.toString()
            val finalPainLevel = binding.painLevelSlider.value.toInt()

            val inputName = binding.nameEditText.text.toString()
            val inputAge = binding.ageEditText.text.toString()
            val inputGender = if (selectedGender.isNotEmpty()) selectedGender else currentUser.gender
            val inputHeight = binding.heightEditText.text.toString()
            val inputWeight = binding.weightEditText.text.toString()
            val inputAllergy = binding.allergyEditText.text.toString()
            val inputNotes = binding.additionalNotesEditText.text.toString()

            // 유효성 검사
            if (inputName.isBlank()) {
                Toast.makeText(context, "이름을 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (inputAge.toIntOrNull() == null || inputAge.toInt() <= 0) {
                Toast.makeText(context, "나이를 올바르게 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (inputGender.isBlank() || inputGender == "미설정") {
                Toast.makeText(context, "성별을 선택해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (inputHeight.toIntOrNull() == null || inputHeight.toInt() <= 0) {
                Toast.makeText(context, "키를 올바르게 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (inputWeight.toDoubleOrNull() == null || inputWeight.toDouble() <= 0.0) {
                Toast.makeText(context, "몸무게를 올바르게 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            // 환부/질환명 필수 체크
            if (currentInjuryBodyPart.isBlank() || currentInjuryBodyPart == "없음") {
                Toast.makeText(context, "환부(부상 부위)를 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (currentInjuryName.isBlank() || currentInjuryName == "없음") {
                Toast.makeText(context, "질환명을 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 객체 생성
            val updatedUser = currentUser.copy(
                name = inputName,
                gender = inputGender,
                age = inputAge.toInt(),
                heightCm = inputHeight.toInt(),
                weightKg = inputWeight.toDouble(),
                allergyInfo = if (inputAllergy.isNotBlank()) inputAllergy.split(",").map { it.trim() } else emptyList(),
                currentPainLevel = finalPainLevel, // (★수정★) 최신 통증 레벨 반영
                additionalNotes = inputNotes
            )

            // ViewModel에 업데이트 요청 (환부, 질환명도 함께 전달)
            viewModel.updateUserProfile(updatedUser, currentInjuryName, currentInjuryBodyPart)

            Toast.makeText(context, "프로필이 저장되었습니다.", Toast.LENGTH_SHORT).show()
            findNavController().popBackStack()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}