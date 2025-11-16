package com.example.androidproject.presentation.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.androidproject.databinding.FragmentProfileEditBinding // (★ 주의 ★) 'FragmentProfileEditBinding' import
import com.example.androidproject.domain.model.User
import com.example.androidproject.presentation.viewmodel.RehabViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * [새 파일 2/2] - '개인정보 수정' Fragment (두뇌)
 * '개인정보' 탭의 '수정' 로직을 '전담'하는 '새로운' 프래그먼트입니다.
 */
@AndroidEntryPoint
class ProfileEditFragment : Fragment() {

    // (★ 주의 ★) 'FragmentProfileEditBinding' 사용
    private var _binding: FragmentProfileEditBinding? = null
    private val binding get() = _binding!!

    // ViewModel은 '공유'
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

        // 1. '저장' 버튼 '클릭 리스너' 설정
        setupSaveButton()

        // 2. 'ViewModel'의 '현재' 데이터를 'EditText'에 '표시' (1회성 로드)
        loadCurrentProfileData()
    }

    /**
     * 'ViewModel'의 '현재' 데이터를 'EditText'에 '채워' 넣습니다.
     */
    private fun loadCurrentProfileData() {
        // 'uiState'는 '공유' ViewModel에 '이미' '로드'되어 있습니다.
        viewLifecycleOwner.lifecycleScope.launch {
            val state = viewModel.uiState.value // (collectLatest 대신 '현재 값'을 '즉시' 가져옴)
            val user = viewModel.dummyUser
            val injury = viewModel.dummyInjury

            // 'UI'에 '데이터'를 '설정'합니다.
            binding.nameEditText.setText(user.name)
            binding.ageEditText.setText(user.age.toString())
            binding.genderEditText.setText(user.gender)
            binding.heightEditText.setText(user.heightCm.toString())
            binding.weightEditText.setText(user.weightKg.toString())
            binding.allergyEditText.setText(user.allergyInfo.joinToString(", "))

            // (★ 수정 ★) '환부'와 '질환명'을 '분리'하여 '표시'
            binding.injuryAreaEditText.setText(injury.bodyPart)
            binding.injuryNameEditText.setText(injury.name)

            binding.painLevelEditText.setText(user.currentPainLevel.toString())
            binding.additionalNotesEditText.setText(user.additionalNotes)
        }
    }

    /**
     * '저장' 버튼 '클릭' 시 '동작'을 '정의'합니다.
     */
    private fun setupSaveButton() {
        binding.saveButton.setOnClickListener {
            // (1) 'EditText'에서 '현재' 입력된 '값'들을 '가져옵니다'.
            val updatedUser = User(
                id = viewModel.dummyUser.id,
                name = binding.nameEditText.text.toString(),
                gender = binding.genderEditText.text.toString(),
                age = binding.ageEditText.text.toString().toIntOrNull() ?: 0,
                heightCm = binding.heightEditText.text.toString().toIntOrNull() ?: 0,
                weightKg = binding.weightEditText.text.toString().toDoubleOrNull() ?: 0.0,
                activityLevel = viewModel.dummyUser.activityLevel,

                // (★ 삭제 ★) 'fitnessGoal'은 'User' 모델에는 있지만 'UI'에서 '제거'됨
                fitnessGoal = viewModel.dummyUser.fitnessGoal, // (기존 값 '유지')

                allergyInfo = binding.allergyEditText.text.toString().split(",").map { it.trim() },
                preferredDietType = viewModel.dummyUser.preferredDietType,
                preferredDietaryTypes = viewModel.dummyUser.preferredDietaryTypes,
                equipmentAvailable = viewModel.dummyUser.equipmentAvailable,
                currentPainLevel = binding.painLevelEditText.text.toString().toIntOrNull() ?: 0,
                additionalNotes = binding.additionalNotesEditText.text.toString(),
                targetCalories = viewModel.dummyUser.targetCalories,
                currentInjuryId = viewModel.dummyUser.currentInjuryId
            )

            // (★ 수정 ★) '환부'와 '질환명'을 '별도'로 '가져옵니다'.
            val updatedInjuryArea = binding.injuryAreaEditText.text.toString()
            val updatedInjuryName = binding.injuryNameEditText.text.toString()

            // (2) 'ViewModel'의 '사용자 업데이트' 함수를 '호출'합니다.
            viewModel.updateUserProfile(updatedUser, updatedInjuryName, updatedInjuryArea)

            // (3) '저장'이 '완료'되었음을 '알리고' '이전' 화면('개인정보' 탭)으로 '복귀'합니다.
            Toast.makeText(context, "프로필이 저장되었습니다.", Toast.LENGTH_SHORT).show()
            findNavController().popBackStack()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // 메모리 누수 방지
    }
}