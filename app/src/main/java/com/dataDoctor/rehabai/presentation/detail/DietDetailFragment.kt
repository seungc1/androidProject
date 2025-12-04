package com.dataDoctor.rehabai.presentation.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.dataDoctor.rehabai.databinding.FragmentDietDetailBinding
import com.dataDoctor.rehabai.presentation.viewmodel.DietDetailViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class DietDetailFragment : Fragment() {

    private var _binding: FragmentDietDetailBinding? = null
    private val binding get() = _binding!!

    private val viewModel: DietDetailViewModel by viewModels()

    private val args: DietDetailFragmentArgs by navArgs()
    private lateinit var alternativeFoodAdapter: AlternativeFoodAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDietDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeUiState()
        viewModel.loadDietDetails(args.dietId)
    }

    private fun setupRecyclerView() {
        alternativeFoodAdapter = AlternativeFoodAdapter()
        binding.alternativesRecyclerView.apply {
            adapter = alternativeFoodAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }

        // [추가] 수정 버튼 리스너
        binding.editButton.setOnClickListener {
            val state = viewModel.dietDetailState.value
            state.diet?.let { diet ->
                // Diet 객체에는 timestamp 등의 정보가 부족할 수 있으므로, 
                // DietSession에서 가져오거나 Diet 객체에 필드를 추가해야 함.
                // 현재 Diet 객체는 toDomain()으로 변환된 것이라 원본 데이터가 일부 없을 수 있음.
                // 하지만 기본적인 수정은 가능함.
                
                // 주의: Diet 객체의 id는 DietSession의 id여야 함.
                val dialog = com.dataDoctor.rehabai.presentation.main.DietRecordDialog.newInstance(
                    id = diet.id,
                    foodName = diet.foodName,
                    mealType = diet.mealType,
                    quantity = diet.quantity,
                    unit = diet.unit,
                    satisfaction = 5, // Diet 객체에 만족도가 없다면 기본값
                    timestamp = state.timestamp ?: System.currentTimeMillis(), // [수정] 상태의 타임스탬프 사용
                    photoUrl = state.photoUrl
                )
                dialog.show(parentFragmentManager, com.dataDoctor.rehabai.presentation.main.DietRecordDialog.TAG)
            }
        }

        // 돌아가기 버튼 리스너
        binding.backButton.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.dietDetailState.collectLatest { state ->
                    
                    state.diet?.let { diet ->
                        binding.detailDietNameTextView.text = diet.foodName
                        
                        // [수정] 기록된 식단(AI 추천 아님)일 경우 섭취량과 단위 표시
                        if (state.isAiRecommendation) {
                             binding.detailDietInfoTextView.text =
                                "${diet.mealType} / ${diet.calorie} kcal (단 ${diet.protein}g, 탄 ${diet.carbs}g, 지 ${diet.fat}g)"
                        } else {
                            // 1.0 -> 1 로 표시하기 위한 로직
                            val quantityString = if (diet.quantity % 1.0 == 0.0) {
                                diet.quantity.toInt().toString()
                            } else {
                                diet.quantity.toString()
                            }
                            binding.detailDietInfoTextView.text = "${diet.mealType} / $quantityString${diet.unit}"
                        }
                        
                        binding.aiReasonTextView.text =
                            diet.aiRecommendationReason ?: "AI가 추천 이유를 제공하지 않았습니다."
                    }

                    if (state.isAiRecommendation) {
                        // AI 모드
                        binding.dietImageView.isVisible = false
                        binding.aiReasonTitleTextView.isVisible = true
                        binding.aiReasonTextView.isVisible = true
                        binding.alternativesTitleTextView.isVisible = true
                        binding.alternativesRecyclerView.isVisible = true
                        binding.editButton.isVisible = false // [추가] AI 추천은 수정 불가
                        
                        alternativeFoodAdapter.submitList(state.alternatives)
                    } else {
                        // 기록 모드
                        binding.dietImageView.isVisible = true
                        binding.aiReasonTitleTextView.isVisible = false
                        binding.aiReasonTextView.isVisible = false
                        binding.alternativesTitleTextView.isVisible = false
                        binding.alternativesRecyclerView.isVisible = false
                        binding.editButton.isVisible = true // [추가] 기록은 수정 가능

                        state.photoUrl?.let { url ->
                            Glide.with(this@DietDetailFragment)
                                .load(url)
                                .placeholder(android.R.drawable.ic_menu_gallery)
                                .error(android.R.drawable.ic_menu_report_image)
                                .into(binding.dietImageView)
                        }
                    }

                    state.errorMessage?.let { message ->
                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                        viewModel.clearErrorMessage()
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