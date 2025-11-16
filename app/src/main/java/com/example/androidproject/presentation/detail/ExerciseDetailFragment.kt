package com.example.androidproject.presentation.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels // (★중요★) '홈'과 '공유'하기 위해 'activityViewModels' 사용
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.androidproject.R
import com.example.androidproject.databinding.FragmentExerciseDetailBinding
import com.example.androidproject.domain.model.Exercise
import com.example.androidproject.presentation.viewmodel.RehabViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * [새 파일 2/2] - '운동 상세' 두뇌
 * 경로: presentation/detail/ExerciseDetailFragment.kt
 *
 * (Goal 1) '홈'에서 'ID'를 '전달'받습니다. (Safe Args)
 * (Goal 2) '만족도'와 '후기'를 '입력'받아 ViewModel에 '저장'을 '요청'합니다.
 */
@AndroidEntryPoint
class ExerciseDetailFragment : Fragment() {

    private var _binding: FragmentExerciseDetailBinding? = null
    private val binding get() = _binding!!

    // (★핵심★) '홈'(HomeFragment)과 '동일한' ViewModel 인스턴스를 '공유'합니다.
    // (그래야 '저장' 시 '홈' 화면의 '체크' 상태를 '업데이트'할 수 있습니다.)
    private val viewModel: RehabViewModel by activityViewModels()

    // (★핵심★) 'nav_graph.xml'이 '자동 생성'한 'Args' 클래스를 '주입'받습니다.
    private val args: ExerciseDetailFragmentArgs by navArgs()

    private var selectedExercise: Exercise? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentExerciseDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. '홈'에서 '전달'받은 'exerciseId'를 '사용'합니다.
        val exerciseId = args.exerciseId

        // 2. 'ViewModel'의 '현재' 상태(uiState)에서 'ID'와 '일치'하는 '운동'을 '찾아' UI에 '표시'합니다.
        loadExerciseDetails(exerciseId)

        // 3. '저장' 버튼 '클릭' 리스너를 '설정'합니다.
        setupSaveButton(exerciseId)
    }

    private fun loadExerciseDetails(exerciseId: String) {
        // ViewModel은 '공유'되므로 'uiState'에 '이미' 데이터가 있습니다.
        viewLifecycleOwner.lifecycleScope.launch {
            // (first()를 사용해 '현재' 스냅샷을 '한 번만' 가져옵니다.)
            val state = viewModel.uiState.first()
            selectedExercise = state.todayExercises.find { it.exercise.id == exerciseId }?.exercise

            if (selectedExercise != null) {
                binding.detailExerciseNameTextView.text = selectedExercise!!.name
                binding.detailExerciseDescriptionTextView.text = selectedExercise!!.description
            } else {
                // (오류 처리)
                Toast.makeText(context, "운동 정보를 불러오는 데 실패했습니다.", Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()
            }
        }
    }

    private fun setupSaveButton(exerciseId: String) {
        binding.saveButton.setOnClickListener {
            // (Goal 2) '입력'된 '만족도'와 '후기'를 '가져옵니다'.

            // 1. '만족도' (상/중/하)를 '숫자' (5/3/1)로 '변환'합니다.
            val rating = when (binding.ratingRadioGroup.checkedRadioButtonId) {
                R.id.ratingHigh -> 5
                R.id.ratingMedium -> 3
                R.id.ratingLow -> 1
                else -> 3 // (기본값 '중')
            }

            // 2. '후기' (EditText)
            val notes = binding.notesEditText.text.toString()

            // 3. (★핵심★) 'ViewModel'에 '저장'을 '요청'합니다. (Goal 2)
            viewModel.saveRehabSessionDetails(exerciseId, rating, notes)

            // 4. '저장'이 '완료'되었음을 '알리고' '홈' 화면으로 '복귀'합니다.
            Toast.makeText(context, "기록이 저장되었습니다.", Toast.LENGTH_SHORT).show()
            findNavController().popBackStack() // '뒤로가기'
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}