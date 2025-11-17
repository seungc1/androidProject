package com.example.androidproject.presentation.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible // (★ 필수 ★) 'isVisible' 확장 함수 import
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController // (★ 필수 ★) '내비게이션'을 위해 import
import com.example.androidproject.R // (★ 필수 ★) 'R.id' (내비게이션 'action')을 '참조'
import com.example.androidproject.databinding.FragmentProfileBinding
import com.example.androidproject.presentation.viewmodel.RehabViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * [수정 파일 3/3] - '개인정보' Fragment (두뇌)
 *
 * (★ 수정 ★) '데이터가 없을 때' '입력'을 '유도'하는
 * '빈 화면'(emptyProfileView)을 '표시'하는 '로직'을 '추가'합니다.
 * (★ 수정 ★) '__binding' '오타'를 '_binding'으로 '수정'합니다.
 */
@AndroidEntryPoint
class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    // (★ 수정 ★) '치명적인' '오타' '수정' (__binding -> _binding)
    private val binding get() = _binding!!

    // ViewModel은 '공유'
    private val viewModel: RehabViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. (★ 수정 ★) '수정' 버튼 '클릭' 시 '수정' 페이지로 '이동'
        binding.editButton.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_profile_to_profileEditFragment)
        }

        // 2. (★ 추가 ★) '환자 정보 입력하기' 버튼 '클릭' 시 '수정' 페이지로 '이동'
        binding.navigateToEditButton.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_profile_to_profileEditFragment)
        }

        // 3. (★ 수정 ★) 'UI 상태'를 '관찰'하여 '3가지' '화면'('로딩', '빈 화면', '데이터')을 '제어'
        observeUiState()
    }

    /**
     * '핵심 두뇌'(ViewModel)의 'uiState'를 '관찰'합니다.
     * (★ 수정 ★) '로딩', '빈 화면', '데이터 있음' '상태'를 '제어'합니다.
     */
    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {

                viewModel.uiState.collectLatest { state ->

                    // 1. '로딩' 상태 '제어'
                    binding.profileLoadingSpinner.isVisible = state.isLoading

                    // 2. (★ 핵심 ★) '빈 화면' '상태' '제어'
                    val isEmpty = state.userName.isEmpty() && !state.isLoading
                    binding.emptyProfileView.isVisible = isEmpty

                    // 3. (★ 핵심 ★) '데이터 있음' '상태' '제어'
                    val hasData = state.userName.isNotEmpty() && !state.isLoading
                    binding.profileDataView.isVisible = hasData

                    // '데이터가 있을' 경우에만 'TextView'에 '값'을 '설정'
                    if (hasData) {
                        val user = viewModel.dummyUser // (ViewModel의 '최신' 더미 데이터)

                        binding.nameTextView.text = user.name
                        binding.ageTextView.text = user.age.toString()
                        binding.genderTextView.text = user.gender
                        binding.heightTextView.text = "${user.heightCm} cm"
                        binding.weightTextView.text = "${user.weightKg} kg"
                        binding.allergyTextView.text =
                            user.allergyInfo.joinToString(", ").ifEmpty { "없음" }

                        binding.injuryAreaTextView.text = state.currentInjuryArea ?: "정보 없음"
                        binding.injuryNameTextView.text = state.currentInjuryName ?: "정보 없음"

                        binding.painLevelTextView.text = "${user.currentPainLevel} / 10"
                        binding.additionalNotesTextView.text = user.additionalNotes ?: "없음"
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // 메모리 누수 방지
    }
}