package com.example.androidproject.presentation.detail

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
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.androidproject.databinding.FragmentDietDetailBinding
import com.example.androidproject.presentation.viewmodel.RehabViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * [새 파일 4/4] - '식단 상세' 두뇌
 * 경로: presentation/detail/DietDetailFragment.kt
 *
 * '홈'에서 'ID'를 '전달'받아 (Safe Args),
 * 'ViewModel'에게 '상세 정보'와 '대체 식품'을 '요청'하고
 * UI에 '표시'합니다.
 */
@AndroidEntryPoint
class DietDetailFragment : Fragment() {

    private var _binding: FragmentDietDetailBinding? = null
    private val binding get() = _binding!!

    // '홈' 화면과 '동일한' ViewModel 인스턴스를 '공유'
    private val viewModel: RehabViewModel by activityViewModels()

    // 'nav_graph.xml'이 '자동 생성'한 'Args' 클래스를 '주입'
    private val args: DietDetailFragmentArgs by navArgs()

    private lateinit var alternativeFoodAdapter: AlternativeFoodAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentDietDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. '대체 식품' 목록 '관리자'와 'RecyclerView'를 '연결'
        setupRecyclerView()

        // 2. 'ViewModel'의 '상세 정보' 상태를 '관찰'
        observeUiState()

        // 3. '홈'에서 '전달'받은 'dietId'를 '사용'하여 'ViewModel'에 '데이터'를 '요청'
        viewModel.loadDietDetails(args.dietId)
    }

    private fun setupRecyclerView() {
        alternativeFoodAdapter = AlternativeFoodAdapter()
        binding.alternativesRecyclerView.apply {
            adapter = alternativeFoodAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    /**
     * 'ViewModel'의 'dietDetailState'를 '관찰'합니다.
     */
    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {

                viewModel.dietDetailState.collectLatest { state ->
                    // 1. 로딩 상태 '표시'
                    binding.alternativesLoadingProgressBar.isVisible = state.isLoading

                    // 2. '식단' 정보 '표시' (AI 추천 이유 등)
                    state.diet?.let { diet ->
                        binding.detailDietNameTextView.text = diet.foodName
                        binding.detailDietInfoTextView.text =
                            "${diet.mealType} / ${diet.calorie} kcal (단 ${diet.protein}g, 탄 ${diet.carbs}g, 지 ${diet.fat}g)"
                        binding.aiReasonTextView.text =
                            diet.aiRecommendationReason ?: "AI가 추천 이유를 제공하지 않았습니다."
                    }

                    // 3. '대체 식품' 목록 '표시'
                    alternativeFoodAdapter.submitList(state.alternatives)

                    // 4. '오류' '처리'
                    state.errorMessage?.let { message ->
                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                        viewModel.clearDietDetailErrorMessage()
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