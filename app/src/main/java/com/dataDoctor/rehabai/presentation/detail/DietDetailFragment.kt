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
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide // [추가]
import com.dataDoctor.rehabai.databinding.FragmentDietDetailBinding
import com.dataDoctor.rehabai.presentation.viewmodel.DietDetailViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class DietDetailFragment : Fragment() {

    private var _binding: FragmentDietDetailBinding? = null
    private val binding get() = _binding!!

    // (★수정★) 전용 ViewModel 사용
    private val viewModel: DietDetailViewModel by viewModels()

    private val args: DietDetailFragmentArgs by navArgs()
    private lateinit var alternativeFoodAdapter: AlternativeFoodAdapter // [복구]

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
        // [복구] 어댑터 설정
        alternativeFoodAdapter = AlternativeFoodAdapter()
        binding.alternativesRecyclerView.apply {
            adapter = alternativeFoodAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.dietDetailState.collectLatest { state ->
                    
                    state.diet?.let { diet ->
                        binding.detailDietNameTextView.text = diet.foodName
                        binding.detailDietInfoTextView.text =
                            "${diet.mealType} / ${diet.calorie} kcal (단 ${diet.protein}g, 탄 ${diet.carbs}g, 지 ${diet.fat}g)"
                        
                        // [복구] AI 추천 이유 텍스트 설정
                        binding.aiReasonTextView.text =
                            diet.aiRecommendationReason ?: "AI가 추천 이유를 제공하지 않았습니다."
                    }

                    // [수정] 모드에 따른 UI 가시성 제어
                    if (state.isAiRecommendation) {
                        // AI 모드: 사진 숨김, AI 정보 표시
                        binding.dietImageView.isVisible = false
                        binding.aiReasonTitleTextView.isVisible = true
                        binding.aiReasonTextView.isVisible = true
                        binding.alternativesTitleTextView.isVisible = true
                        binding.alternativesRecyclerView.isVisible = true
                        
                        // 대체 식품 리스트 업데이트
                        alternativeFoodAdapter.submitList(state.alternatives)
                    } else {
                        // 기록 모드: 사진 표시, AI 정보 숨김
                        binding.dietImageView.isVisible = true
                        binding.aiReasonTitleTextView.isVisible = false
                        binding.aiReasonTextView.isVisible = false
                        binding.alternativesTitleTextView.isVisible = false
                        binding.alternativesRecyclerView.isVisible = false

                        // 이미지 로드
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