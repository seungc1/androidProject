package com.example.androidproject.presentation.profile

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
import com.example.androidproject.R
import com.example.androidproject.databinding.FragmentProfileBinding
import com.example.androidproject.presentation.auth.LoginActivity
import com.example.androidproject.presentation.viewmodel.RehabViewModel
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

        // (★추가★) 테스트 데이터 생성 버튼 연결
        binding.generateTestDataButton.setOnClickListener {
            viewModel.createTestHistory()
            Toast.makeText(requireContext(), "지난 7일간의 운동/식단 기록이 생성되었습니다.", Toast.LENGTH_SHORT).show()
        }

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
                        binding.injuryAreaTextView.text = injury?.bodyPart ?: "정보 없음"
                        binding.injuryNameTextView.text = injury?.name ?: "정보 없음"
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