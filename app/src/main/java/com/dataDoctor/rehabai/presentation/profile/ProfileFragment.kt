package com.dataDoctor.rehabai.presentation.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.dataDoctor.rehabai.R
import com.dataDoctor.rehabai.databinding.FragmentProfileBinding
import com.dataDoctor.rehabai.presentation.auth.LoginActivity
import com.dataDoctor.rehabai.presentation.viewmodel.RehabViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

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

        // 1. 기존 버튼들 연결
        binding.editButton.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_profile_to_profileEditFragment)
        }

        binding.navigateToEditButton.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_profile_to_profileEditFragment)
        }

        binding.accountChangeButton.setOnClickListener {
            viewModel.logout()
            val intent = Intent(requireActivity(), LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            requireActivity().finish()
        }

        // ====================================================================
        // [수정] 테스트 데이터 생성 버튼 활성화 (11/18 ~ 12/3 기록 생성)
        // ====================================================================

        binding.generateTestDataButton.apply {
            visibility = View.VISIBLE // 👈 버튼 보이게 설정 (기존 GONE -> VISIBLE)
            text = "11/18 ~ 12/3 운동 기록 생성 (테스트)" // 버튼 텍스트 변경
            setOnClickListener {
                viewLifecycleOwner.lifecycleScope.launch {
                    // ViewModel의 새로운 함수 호출 (createSpecificTestHistory)
                    viewModel.createSpecificTestHistory()
                    Toast.makeText(context, "✅ 11/18 ~ 12/3 기간의 운동 기록이 생성되었습니다.", Toast.LENGTH_LONG).show()
                }
            }
        }

        // [위험!] 계정의 모든 데이터 삭제 버튼 (실수 방지를 위해 숨김 유지)
        binding.deleteAllDataButton.setOnClickListener {
            // 사용자에게 경고 메시지 표시 후 삭제 확인
            android.app.AlertDialog.Builder(requireContext())
                .setTitle("⚠️ 경고: 모든 데이터 삭제")
                .setMessage("계정의 모든 운동/식단 기록, AI 루틴, 캐시가 영구적으로 삭제되며, 로그아웃됩니다. 계속하시겠습니까?")
                .setPositiveButton("삭제 및 로그아웃") { _, _ ->
                    viewModel.deleteAllUserData()
                    val intent = android.content.Intent(requireActivity(), com.dataDoctor.rehabai.presentation.auth.LoginActivity::class.java)
                    intent.flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    requireActivity().finish()
                }
                .setNegativeButton("취소", null)
                .show()
        }
        binding.deleteAllDataButton.visibility = View.GONE // 👈 숨김 처리 유지

        // ====================================================================

        observeData()
    }

    private fun observeData() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {

                launch {
                    viewModel.uiState.collectLatest { state ->
                        binding.profileLoadingSpinner.isVisible = state.isLoading

                        val isEmpty = state.userName.isEmpty() && !state.isLoading
                        binding.emptyProfileView.isVisible = isEmpty
                        binding.profileDataView.isVisible = !isEmpty && !state.isLoading
                    }
                }

                launch {
                    viewModel.currentUser.collectLatest { user ->
                        user?.let {
                            binding.nameTextView.text = it.name
                            binding.ageTextView.text = it.age.toString()
                            binding.genderTextView.text = it.gender
                            binding.heightTextView.text = "${it.heightCm} cm"
                            binding.weightTextView.text = "${it.weightKg} kg"
                            binding.allergyTextView.text = it.allergyInfo.joinToString(", ").ifEmpty { "없음" }
                            binding.painLevelTextView.text = "${it.currentPainLevel} / 10"
                            binding.additionalNotesTextView.text = it.additionalNotes ?: "없음"
                        }
                    }
                }

                launch {
                    viewModel.currentInjury.collectLatest { injury ->
                        binding.injuryAreaTextView?.text = injury?.bodyPart ?: "정보 없음"
                        binding.injuryNameTextView?.text = injury?.name ?: "정보 없음"
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}