package com.example.androidproject.presentation.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
// import com.example.androidproject.databinding.FragmentMainBinding // ViewBinding 사용 가정
// import com.example.androidproject.presentation.main.adapter.ExerciseAdapter
// import com.example.androidproject.presentation.main.adapter.DietAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainFragment : Fragment() {

    // private var _binding: FragmentMainBinding? = null
    // private val binding get() = _binding!!

    // Hilt를 통해 ViewModel 주입
    private val viewModel: MainViewModel by viewModels()

    // RecyclerView 어댑터 (별도 구현 필요)
    // private lateinit var exerciseAdapter: ExerciseAdapter
    // private lateinit var dietAdapter: DietAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // _binding = FragmentMainBinding.inflate(inflater, container, false)
        // return binding.root
        return super.onCreateView(inflater, container, savedInstanceState) // 임시 반환
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerViews()
        observeUiState()
    }

    private fun setupRecyclerViews() {
        // exerciseAdapter = ExerciseAdapter(
        //     onItemClick = { exercise ->
        //         // TODO: 운동 상세 화면으로 이동 (Navigation Component)
        //     },
        //     onCheckClick = { exerciseId ->
        //         viewModel.toggleExerciseCompletion(exerciseId)
        //     }
        // )
        // binding.exerciseRecyclerView.adapter = exerciseAdapter

        // dietAdapter = DietAdapter(
        //     onItemClick = { diet ->
        //         // TODO: 식단 상세 바텀시트 표시
        //     }
        // )
        // binding.dietRecyclerView.adapter = dietAdapter
    }

    /**
     * ViewModel의 UI State를 구독하고 UI를 갱신합니다.
     */
    private fun observeUiState() {
        // Fragment의 View Lifecycle에 맞춰 안전하게 Flow를 구독
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(androidx.lifecycle.Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->

                    // (시뮬레이션) 바인딩된 View에 상태 적용
                    // binding.progressBar.isVisible = state.isLoading

                    // // 환영 메시지 및 부상 정보
                    // binding.welcomeTextView.text = "안녕하세요, ${state.userName}님!"
                    // if (state.currentInjuryName != null) {
                    //     binding.injuryInfoTextView.text = "현재 재활 부위: ${state.currentInjuryName}"
                    //     binding.injuryInfoTextView.isVisible = true
                    // } else {
                    //     binding.injuryInfoTextView.isVisible = false
                    // }

                    // // RecyclerView 어댑터에 데이터 제출
                    // exerciseAdapter.submitList(state.todayExercises)
                    // dietAdapter.submitList(state.recommendedDiets)

                    // // 에러 메시지 처리 (1회성)
                    // state.errorMessage?.let {
                    //     Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
                    //     // TODO: ViewModel에서 에러 메시지 처리 완료 알림 (1회성 이벤트)
                    // }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // _binding = null // ViewBinding 메모리 누수 방지
    }
}
