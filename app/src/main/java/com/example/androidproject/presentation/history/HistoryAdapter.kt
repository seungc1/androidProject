package com.example.androidproject.presentation.history

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.example.androidproject.data.ExerciseCatalog // 👈 추가: 운동 카탈로그 import
import com.example.androidproject.databinding.ItemDietBinding
import com.example.androidproject.databinding.ItemExerciseBinding
import com.example.androidproject.domain.model.DietSession
import com.example.androidproject.domain.model.RehabSession
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.Date

/**
 * [새 파일] - '기록' 탭의 '목록 관리자'
 */

// 1. '데이터 묶음' 정의
sealed class HistoryItem {
    data class Exercise(val session: RehabSession) : HistoryItem() {
        override val id: String = session.id
        override val dateTime: Date = session.dateTime
    }
    data class Diet(val session: DietSession) : HistoryItem() {
        override val id: String = session.id
        override val dateTime: Date = session.dateTime
    }

    abstract val id: String
    abstract val dateTime: Date
}

// 2. '목록 관리자' (Adapter) 정의
class HistoryAdapter : ListAdapter<HistoryItem, HistoryAdapter.HistoryViewHolder>(HistoryDiffCallback()) {

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is HistoryItem.Exercise -> VIEW_TYPE_EXERCISE
            is HistoryItem.Diet -> VIEW_TYPE_DIET
            else -> throw IllegalArgumentException("Unknown item type")
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = when (viewType) {
            VIEW_TYPE_EXERCISE -> ItemExerciseBinding.inflate(inflater, parent, false)
            VIEW_TYPE_DIET -> ItemDietBinding.inflate(inflater, parent, false)
            else -> throw IllegalArgumentException("Invalid view type")
        }
        return HistoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class HistoryViewHolder(private val binding: ViewBinding) : RecyclerView.ViewHolder(binding.root) {

        private val timeFormatter = SimpleDateFormat("a h:mm", Locale.KOREA)

        fun bind(item: HistoryItem) {
            when (item) {
                // '운동' 데이터
                is HistoryItem.Exercise -> {
                    val exerciseBinding = binding as ItemExerciseBinding
                    val session = item.session

                    // 1. 운동 이름: 기록된 운동 ID 대신 실제 운동 이름을 표시하도록 수정합니다.
                    // --- 수정 시작 ---
                    val exerciseName = ExerciseCatalog.allExercises
                        .find { it.id == session.exerciseId } // ID로 카탈로그에서 운동을 찾습니다.
                        ?.name
                        ?: "알 수 없는 운동 (${session.exerciseId})" // 찾지 못하면 대체 텍스트를 사용합니다.

                    exerciseBinding.exerciseNameTextView.text = exerciseName
                    // --- 수정 종료 ---

                    // 2. 상세 정보: 세트, 횟수, 만족도 결합
                    val ratingText = when (session.userRating) {
                        5 -> "매우 좋음 (⭐)"
                        4 -> "좋음 (👍)"
                        3 -> "보통 (😐)"
                        2 -> "힘듦 (💦)"
                        1 -> "나쁨 (❌)"
                        else -> "평가 없음"
                    }

                    // 홈 탭과 유사하게 상세 정보 구성
                    exerciseBinding.exerciseDetailTextView.text =
                        "수행: ${session.sets} 세트 / ${session.reps} 회" +
                                " | 만족도: $ratingText"

                    // 3. 체크박스 영역: 기록 시간 표시 및 비활성화
                    exerciseBinding.exerciseStatusCheckBox.text = timeFormatter.format(session.dateTime)
                    exerciseBinding.exerciseStatusCheckBox.isClickable = false
                    exerciseBinding.exerciseStatusCheckBox.isChecked = false
                }

                // '식단' 데이터
                is HistoryItem.Diet -> {
                    val dietBinding = binding as ItemDietBinding
                    val session = item.session

                    // 1. 만족도 텍스트 생성
                    val satisfactionText = when (session.userSatisfaction) {
                        5 -> "매우 만족 (⭐)"
                        4 -> "만족 (👍)"
                        3 -> "보통 (😐)"
                        2 -> "불만족 (💦)"
                        1 -> "매우 불만족 (❌)"
                        else -> "평가 없음"
                    }

                    // 2. 음식 이름 표시 (foodName이 있으면 표시, 없으면 dietId 표시)
                    val displayName = session.foodName ?: "식단: ${session.dietId}"
                    dietBinding.dietNameTextView.text = displayName
                    dietBinding.dietDetailTextView.text =
                        "${session.actualQuantity} ${session.actualUnit} 섭취" +
                                " | 만족도: $satisfactionText"

                    // 3. 칼로리/시간: 우측에 시간 표시
                    // AI 추천 식단의 칼로리 필드가 없으므로, 우측에는 시간만 표시
                    dietBinding.dietCaloriesTextView.text = timeFormatter.format(session.dateTime)
                }
            }
        }
    }

    companion object {
        private const val VIEW_TYPE_EXERCISE = 1
        private const val VIEW_TYPE_DIET = 2
    }
}

/**
 * 'DiffUtil'
 */
class HistoryDiffCallback : DiffUtil.ItemCallback<HistoryItem>() {
    override fun areItemsTheSame(oldItem: HistoryItem, newItem: HistoryItem): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: HistoryItem, newItem: HistoryItem): Boolean {
        return oldItem == newItem
    }
}