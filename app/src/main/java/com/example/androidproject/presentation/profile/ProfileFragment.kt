package com.example.androidproject.presentation.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController // (★ 추가 ★) '내비게이션'을 위해 import
import com.example.androidproject.R // (★ 추가 ★) 'R.id' (내비게이션 'action')을 '참조'
import com.example.androidproject.databinding.FragmentProfileBinding // (★ 수정 ★) 'FragmentProfileBinding' import
import com.example.androidproject.presentation.viewmodel.RehabViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * [수정 파일 6/7] - '개인정보' Fragment (두뇌)
 *
 * (★ 수정 ★) '읽기 전용' UI에 '데이터'를 '표시'하고,
 * '수정' 버튼 '클릭' 시 'ProfileEditFragment'로 '이동'하도록 '변경'합니다.
 */
@AndroidEntryPoint
class ProfileFragment : Fragment() {

    // (★ 수정 ★) '읽기 전용' UI (fragment_profile.xml)의 'Binding'
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    // ViewModel은 '공유'
    private val viewModel: RehabViewModel by activityViewModels()

    // (★ 삭제 ★) 'isEditMode' 변수 '제거' ('ProfileEditFragment'로 '이동')

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // (★ 수정 ★) '수정' 버튼 '클릭' 시 '수정' 페이지로 '이동'
        binding.editButton.setOnClickListener {
            // 'nav_graph.xml'에 '새로 추가'한 'action' ID를 '사용'하여 '이동'
            findNavController().navigate(R.id.action_navigation_profile_to_profileEditFragment)
        }

        // (★ 수정 ★) 'UI 상태'를 '관찰'하여 'TextView'에 '표시'
        observeUiState()
    }

    // (★ 삭제 ★) 'setupEditSaveButton', 'toggleEditMode', 'saveProfileChanges' 함수 '모두' '제거'
    // (이 로직들은 'ProfileEditFragment'로 '이동'되었습니다.)


    /**
     * '핵심 두뇌'(ViewModel)의 'uiState'를 '관찰'합니다.
     * (★ 수정 ★) 'EditText' 대신 'TextView'에 '데이터'를 '설정'합니다.
     */
    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {

                viewModel.uiState.collectLatest { state ->
                    if (!state.isLoading) {
                        val user = viewModel.dummyUser // (ViewModel의 '최신' 더미 데이터)

                        // (★ 수정 ★) '읽기 전용' TextView에 '데이터' '표시'
                        binding.nameTextView.text = user.name
                        binding.ageTextView.text = user.age.toString()
                        binding.genderTextView.text = user.gender
                        binding.heightTextView.text = "${user.heightCm} cm"
                        binding.weightTextView.text = "${user.weightKg} kg"
                        binding.allergyTextView.text =
                            user.allergyInfo.joinToString(", ").ifEmpty { "없음" }

                        // (★ 수정 ★) '환부'와 '질환명'을 '분리'하여 '표시'
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