package com.example.androidproject.presentation.detail

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
import com.example.androidproject.databinding.FragmentDietDetailBinding
import com.example.androidproject.presentation.viewmodel.DietDetailViewModel
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
    }

    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.dietDetailState.collectLatest { state ->
                    binding.alternativesLoadingProgressBar.isVisible = state.isLoading

                    state.diet?.let { diet ->
                        binding.detailDietNameTextView.text = diet.foodName
                        binding.detailDietInfoTextView.text =
                            "${diet.mealType} / ${diet.calorie} kcal (단 ${diet.protein}g, 탄 ${diet.carbs}g, 지 ${diet.fat}g)"
                        binding.aiReasonTextView.text =
                            diet.aiRecommendationReason ?: "AI가 추천 이유를 제공하지 않았습니다."
                    }

                    alternativeFoodAdapter.submitList(state.alternatives)

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