package com.example.androidproject.presentation.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
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
import com.google.android.material.card.MaterialCardView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import com.example.androidproject.domain.usecase.GetDailyHistoryUseCase
import com.example.androidproject.data.local.SessionManager
import com.example.androidproject.data.ExerciseCatalog
import com.example.androidproject.domain.usecase.GetWeeklyAnalysisUseCase // 👈 추가
import com.example.androidproject.domain.repository.UserRepository // 👈 추가
import java.util.Date
import javax.inject.Inject
import android.util.Log
import java.text.SimpleDateFormat
import java.util.Locale

@AndroidEntryPoint
class ProfileFragment : Fragment() {

    @Inject // 👈 오늘 기록 로드를 위해 UseCase 주입
    lateinit var getDailyHistoryUseCase: GetDailyHistoryUseCase
    @Inject // 👈 사용자 ID를 가져오기 위해 SessionManager 주입
    lateinit var sessionManager: SessionManager
    @Inject // 👈 주간 분석을 위한 UseCase 추가
    lateinit var getWeeklyAnalysisUseCase: GetWeeklyAnalysisUseCase
    @Inject // 👈 User 객체를 가져오기 위해 UserRepository 추가 (선택 사항이지만 ViewModel 외부에서 User가 필요할 때 유용)
    lateinit var userRepository: UserRepository


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

        /* // (★추가★) 테스트 데이터 생성 버튼 연결
         binding.generateTestDataButton.setOnClickListener {
             viewModel.createTestHistory()
             Toast.makeText(requireContext(), "지난 7일간의 운동/식단 기록이 생성되었습니다.", Toast.LENGTH_SHORT).show()
         }*/

        // ★★★ [추가] 모든 데이터 삭제 버튼 연결 ★★★
        binding.deleteAllDataButton.setOnClickListener {
            // 사용자에게 경고 메시지 표시 후 삭제 확인
            android.app.AlertDialog.Builder(requireContext())
                .setTitle("⚠️ 경고: 모든 데이터 삭제")
                .setMessage("계정의 모든 운동/식단 기록, AI 루틴, 캐시가 영구적으로 삭제되며, 로그아웃됩니다. 계속하시겠습니까?")
                .setPositiveButton("삭제 및 로그아웃") { _, _ ->
                    viewModel.deleteAllUserData()
                    // 로그아웃 후 로그인 화면으로 이동
                    val intent = android.content.Intent(requireActivity(), com.example.androidproject.presentation.auth.LoginActivity::class.java)
                    intent.flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    requireActivity().finish()
                }
                .setNegativeButton("취소", null)
                .show()
        }

        // --- [추가] 코드 검증용 임시 기록 카드 항상 보이게 설정 ---
        binding.profileDataView.findViewById<MaterialCardView>(R.id.testHistoryCard)?.isVisible = true
        binding.profileDataView.findViewById<MaterialCardView>(R.id.testAnalysisCard)?.isVisible = true
        // --- [추가] 끝 ---

        // --- [추가] 오늘 날짜의 운동 및 식단 기록 로드 (임시 영역에 표시) ---
        loadTodayHistory()
        // --- [추가] 끝 ---

        // --- [추가] 주간 AI 분석 리포트 로드 (임시 영역에 표시) ---
        loadWeeklyAnalysis()
        // --- [추가] 끝 ---

        observeData()
    }

    private fun loadTodayHistory() {
        val userId = sessionManager.getUserId()
        val textView = binding.profileDataView.findViewById<TextView>(R.id.testHistoryRecordsTextView)

        if (userId.isNullOrEmpty()) {
            textView?.text = "로그인된 사용자 정보가 없습니다."
            return
        }

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                // 오늘 날짜의 데이터 로드
                val (rehabSessions, dietSessions) = getDailyHistoryUseCase(userId, Date()).first()

                val output = StringBuilder()

                output.append("--- 운동 기록 (${rehabSessions.size}개) ---\n")
                if (rehabSessions.isEmpty()) {
                    output.append("오늘 완료한 운동 기록이 없습니다.\n")
                } else {
                    rehabSessions.sortedBy { it.dateTime }.forEach { session ->
                        // ExerciseCatalog에서 이름 찾기
                        val exerciseName = ExerciseCatalog.allExercises
                            .find { it.id == session.exerciseId }
                            ?.name ?: "알 수 없는 운동 (${session.exerciseId})"

                        val time = SimpleDateFormat("a h:mm", Locale.KOREA).format(session.dateTime)
                        output.append("• [운동] $time: $exerciseName (${session.sets}세트, ${session.reps}회)\n")
                    }
                }

                output.append("\n--- 식단 기록 (${dietSessions.size}개) ---\n")
                if (dietSessions.isEmpty()) {
                    output.append("오늘 먹은 음식 기록이 없습니다.\n")
                } else {
                    dietSessions.sortedBy { it.dateTime }.forEach { session ->
                        val foodName = session.foodName ?: "알 수 없는 식단"
                        val time = SimpleDateFormat("a h:mm", Locale.KOREA).format(session.dateTime)
                        output.append("• [식단] $time: $foodName (${session.actualQuantity}${session.actualUnit})\n")
                    }
                }

                textView?.text = output.toString()

            } catch (e: Exception) {
                Log.e("ProfileFragment", "기록 로드 실패: ${e.message}", e)
                textView?.text = "기록 로드 중 오류 발생: ${e.message}"
            }
        }
    }


    private fun loadWeeklyAnalysis() {
        val userId = sessionManager.getUserId()
        val summaryTextView = binding.profileDataView.findViewById<TextView>(R.id.testAnalysisSummaryTextView)
        val strengthsTextView = binding.profileDataView.findViewById<TextView>(R.id.testAnalysisStrengthsTextView)
        val improvementTextView = binding.profileDataView.findViewById<TextView>(R.id.testAnalysisImprovementTextView)
        val tipsTextView = binding.profileDataView.findViewById<TextView>(R.id.testAnalysisTipsTextView)
        val nextStepsTextView = binding.profileDataView.findViewById<TextView>(R.id.testAnalysisNextStepsTextView)

        if (userId.isNullOrEmpty()) {
            summaryTextView?.text = "사용자 정보가 없어 분석을 로드할 수 없습니다."
            return
        }

        summaryTextView?.text = "AI 분석 로드 중..."

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                // ViewModel에서 User 객체를 가져와 분석 UseCase에 전달
                val user = viewModel.currentUser.filterNotNull().first()

                getWeeklyAnalysisUseCase(user)
                    .collectLatest { result ->
                        summaryTextView?.text = "요약: ${result.summary}"

                        // 목록 데이터를 포맷팅하여 표시
                        strengthsTextView?.text = result.strengths.joinToString("\n") { "• $it" }.ifEmpty { "내용 없음" }
                        improvementTextView?.text = result.areasForImprovement.joinToString("\n") { "• $it" }.ifEmpty { "내용 없음" }
                        tipsTextView?.text = result.personalizedTips.joinToString("\n") { "• $it" }.ifEmpty { "내용 없음" }
                        nextStepsTextView?.text = "다음 단계 권장 사항: ${result.nextStepsRecommendation}"
                    }
            } catch (e: Exception) {
                Log.e("ProfileFragment", "주간 분석 로드 실패: ${e.message}", e)
                summaryTextView?.text = "AI 분석 로드 중 오류 발생: ${e.message}"
                strengthsTextView?.text = "오류로 인해 상세 분석을 불러올 수 없습니다."
                improvementTextView?.text = "-"
                tipsTextView?.text = "-"
                nextStepsTextView?.text = "다음 단계 권장 사항: -"
            }
        }
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