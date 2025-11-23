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
import com.example.androidproject.data.ExerciseCatalog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText

@AndroidEntryPoint
class ProfileEditFragment : Fragment() {

    private var _binding: FragmentProfileEditBinding? = null
    private val binding get() = _binding!!

    private val viewModel: RehabViewModel by activityViewModels()

    private var selectedGender: String = ""

    // (★추가★) 데이터가 실제로 로드되었는지 확인하는 플래그
    private var isDataLoaded = false

    // ★★★ [수정] 선택된 환부 및 질환명 목록을 저장할 상태 변수 ★★★
    private var selectedInjuryAreas = mutableListOf<String>()
    private var selectedInjuryNames = mutableListOf<String>()

    // ★★★ [수정] 환부 옵션 정의: 정렬 후 "기타 (직접 입력)"을 마지막에 추가 ★★★
    private val INJURY_AREAS_OPTIONS by lazy {
        (ExerciseCatalog.allExercises
            .map { it.bodyPart }
            .distinct()
            .sorted()
                + listOf(MANUAL_INPUT_OPTION) // 마지막 옵션으로 추가
                ).toList()
    }

    // ★★★ [수정] 질환명 옵션 정의: 정렬 후 "기타 (직접 입력)"을 마지막에 추가 ★★★
    private val INJURY_NAMES_OPTIONS = listOf(
        "염좌 (삠)", "근육통", "염증 (관절염/건염)", "수술 후 재활",
        "골절 (회복기)", "디스크 (추간판 탈출증)", "만성 통증"
    ).sorted() + listOf(MANUAL_INPUT_OPTION)

    // ★★★ [추가] 상수 정의 ★★★
    companion object {
        const val MANUAL_INPUT_OPTION = "기타 (직접 입력)"
        const val DEFAULT_SELECTION_TEXT = "선택" // ★ 수정: "선택"으로 변경
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileEditBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupInjurySelectors()
        setupSaveButton()
        setupPainLevelSlider()
        setupGenderButtons()

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

        val primaryColor = ContextCompat.getColor(requireContext(), com.example.androidproject.R.color.primaryColor)
        val onPrimaryColor = ContextCompat.getColor(requireContext(), com.example.androidproject.R.color.white)
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

    private fun setupInjurySelectors() {
        // 1. 환부 선택 버튼 (selectInjuryAreaButton)에 리스너 연결
        binding.selectInjuryAreaButton.setOnClickListener {
            showMultiSelectDialog(
                isArea = true,
                title = "환부 (여러 개 선택 가능)",
                options = INJURY_AREAS_OPTIONS,
                currentSelection = selectedInjuryAreas,
                allOptions = INJURY_AREAS_OPTIONS,
                onUpdate = { newSelection ->
                    selectedInjuryAreas = newSelection.toMutableList()
                    val displayText = if (selectedInjuryAreas.isEmpty()) "" else selectedInjuryAreas.joinToString(", ") // 회색 영역 텍스트
                    binding.injuryAreaEditText.setText(displayText) // 디스플레이 필드 업데이트
                    binding.selectInjuryAreaButton.text = DEFAULT_SELECTION_TEXT // 버튼 텍스트 고정
                }
            )
        }
        // 2. 질환명 선택 버튼 (selectInjuryNameButton)에 리스너 연결
        binding.selectInjuryNameButton.setOnClickListener {
            showMultiSelectDialog(
                isArea = false,
                title = "질환명 (여러 개 선택 가능)",
                options = INJURY_NAMES_OPTIONS,
                currentSelection = selectedInjuryNames,
                allOptions = INJURY_NAMES_OPTIONS,
                onUpdate = { newSelection ->
                    selectedInjuryNames = newSelection.toMutableList()
                    val displayText = if (selectedInjuryNames.isEmpty()) "" else selectedInjuryNames.joinToString(", ") // 회색 영역 텍스트
                    binding.injuryNameEditText.setText(displayText) // 디스플레이 필드 업데이트
                    binding.selectInjuryNameButton.text = DEFAULT_SELECTION_TEXT // 버튼 텍스트 고정
                }
            )
        }
        // 초기 버튼 텍스트 설정 (선택이 아닌 경우)
        binding.selectInjuryAreaButton.text = DEFAULT_SELECTION_TEXT
        binding.selectInjuryNameButton.text = DEFAULT_SELECTION_TEXT
    }

    // ★★★ [수정] 다중 선택 다이얼로그 Helper 함수 (직접 입력 처리 통합) ★★★
    private fun showMultiSelectDialog(
        isArea: Boolean,
        title: String,
        options: List<String>,
        currentSelection: List<String>,
        allOptions: List<String>,
        onUpdate: (List<String>) -> Unit
    ) {
        val checkedItems = BooleanArray(options.size) { i ->
            val item = options[i]
            if (item == MANUAL_INPUT_OPTION) {
                currentSelection.any { it !in allOptions.filter { it != MANUAL_INPUT_OPTION } }
            } else {
                currentSelection.contains(item)
            }
        }

        val tempSelectionInDialog = currentSelection.toMutableList()

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(title)
            .setMultiChoiceItems(options.toTypedArray(), checkedItems) { _, which, isChecked ->
                val item = options[which]

                if (item == MANUAL_INPUT_OPTION) {
                    if (isChecked) {
                        tempSelectionInDialog.add(MANUAL_INPUT_OPTION)
                    } else {
                        tempSelectionInDialog.remove(MANUAL_INPUT_OPTION)
                    }
                } else {
                    if (isChecked) {
                        if (!tempSelectionInDialog.contains(item)) tempSelectionInDialog.add(item)
                    } else {
                        tempSelectionInDialog.remove(item)
                    }
                }
            }
            .setPositiveButton("선택 완료") { _, _ ->
                val needsManualInput = tempSelectionInDialog.contains(MANUAL_INPUT_OPTION)

                val fixedOptions = allOptions.filter { it != MANUAL_INPUT_OPTION }
                // 최종 기본 선택 항목 (정렬)
                val finalBaseSelection = tempSelectionInDialog
                    .filter { it != MANUAL_INPUT_OPTION && it in fixedOptions }
                    .sorted()

                if (needsManualInput) {
                    val initialCustomText = currentSelection.filter { it !in fixedOptions }.joinToString(", ")

                    showManualInputDialog(
                        dialogTitle = "$title - 직접 입력",
                        initialText = initialCustomText
                    ) { manualText ->

                        val manualEntries = manualText.split(",")
                            .map { it.trim() }
                            .filter { it.isNotBlank() }

                        val finalSelectionList = (finalBaseSelection + manualEntries).distinct()

                        onUpdate(finalSelectionList)

                        if (manualText.isBlank()) {
                            Toast.makeText(context, "'기타 (직접 입력)' 항목이 선택 해제되었습니다.", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "직접 입력 항목이 저장되었습니다.", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    onUpdate(finalBaseSelection)
                }
            }
            .setNegativeButton("취소", null)
            .show()
    }

    // ★★★ [추가] 텍스트 입력 다이얼로그 Helper 함수 ★★★
    private fun showManualInputDialog(
        dialogTitle: String,
        initialText: String,
        onInputConfirmed: (String) -> Unit
    ) {
        val context = requireContext()
        val editText = TextInputEditText(context).apply {
            setText(initialText.ifEmpty { "" })
            hint = "쉼표(,)로 구분하여 입력하세요."
        }

        val container = android.widget.FrameLayout(context).apply {
            val params = android.widget.FrameLayout.LayoutParams(
                android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                leftMargin = 50
                rightMargin = 50
            }
            editText.layoutParams = params
            addView(editText)
        }

        MaterialAlertDialogBuilder(context)
            .setTitle(dialogTitle)
            .setView(container)
            .setPositiveButton("확인") { _, _ ->
                onInputConfirmed(editText.text.toString().trim())
            }
            .setNegativeButton("취소") { _, _ ->
                onInputConfirmed("")
            }
            .show()
    }


    /**
     * (★수정★) 데이터 관찰 로직: 저장된 문자열을 파싱하여 버튼 및 디스플레이 필드를 초기화합니다.
     */
    private fun observeProfileData() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {

                launch {
                    viewModel.currentUser.collectLatest { user ->
                        if (user != null) {
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

                                checkDataLoaded()
                            }
                        }
                    }
                }

                launch {
                    viewModel.currentInjury.collectLatest { injury ->
                        if (injury != null) {
                            // 1. 환부 파싱 및 디스플레이 필드 업데이트
                            val areaString = injury.bodyPart.ifEmpty { "" }
                            // [수정] .toMutableList() 추가
                            selectedInjuryAreas = areaString.split(",")
                                .map { it.trim() }
                                .filter { it.isNotEmpty() && it != "없음" }
                                .toMutableList()
                            val areaDisplayText = if (selectedInjuryAreas.isEmpty()) "" else selectedInjuryAreas.joinToString(", ")
                            binding.injuryAreaEditText.setText(areaDisplayText)
                            binding.selectInjuryAreaButton.text = DEFAULT_SELECTION_TEXT

                            // 2. 질환명 파싱 및 디스플레이 필드 업데이트
                            val nameString = injury.name.ifEmpty { "" }
                            // [수정] .toMutableList() 추가
                            selectedInjuryNames = nameString.split(",")
                                .map { it.trim() }
                                .filter { it.isNotEmpty() && it != "없음" }
                                .toMutableList()

                            val nameDisplayText = if (selectedInjuryNames.isEmpty()) "" else selectedInjuryNames.joinToString(", ")
                            binding.injuryNameEditText.setText(nameDisplayText)
                            binding.selectInjuryNameButton.text = DEFAULT_SELECTION_TEXT
                        }
                        checkDataLoaded()
                    }
                }
            }
        }
    }

    private fun checkDataLoaded() {
        if (viewModel.currentUser.value != null) {
            isDataLoaded = true
            binding.saveButton.isEnabled = true
        }
    }

    private fun setupSaveButton() {
        binding.saveButton.setOnClickListener {
            if (!isDataLoaded) {
                Toast.makeText(context, "데이터를 불러오는 중입니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val currentUser = viewModel.currentUser.value!!

            // ★★★ 수정: 최종 환부/질환명은 디스플레이 필드의 텍스트를 사용 ★★★
            val finalInjuryBodyPartString = binding.injuryAreaEditText.text.toString().trim()
            val finalInjuryNameString = binding.injuryNameEditText.text.toString().trim()
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
            // 환부/질환명 필수 체크 (최종 텍스트가 비어있지 않은지 확인)
            if (finalInjuryBodyPartString.isBlank() || finalInjuryBodyPartString == "없음") {
                Toast.makeText(context, "환부(부상 부위)를 1개 이상 선택/입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (finalInjuryNameString.isBlank() || finalInjuryNameString == "없음") {
                Toast.makeText(context, "질환명을 1개 이상 선택/입력해주세요.", Toast.LENGTH_SHORT).show()
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
                currentPainLevel = finalPainLevel,
                additionalNotes = inputNotes
            )

            // ViewModel에 업데이트 요청
            viewModel.updateUserProfile(updatedUser, finalInjuryNameString, finalInjuryBodyPartString)

            Toast.makeText(context, "프로필이 저장되었습니다.", Toast.LENGTH_SHORT).show()
            findNavController().popBackStack()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}