package com.example.androidproject.presentation.history

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
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

                    exerciseBinding.exerciseNameTextView.text = "운동: ${session.exerciseId}" // (임시)
                    exerciseBinding.exerciseDetailTextView.text =
                        "${session.sets} 세트 / ${session.reps} 회 (평점: ${session.userRating ?: "없음"})"

                    exerciseBinding.exerciseStatusCheckBox.text = timeFormatter.format(session.dateTime)
                    exerciseBinding.exerciseStatusCheckBox.isClickable = false
                    exerciseBinding.exerciseStatusCheckBox.isChecked = false
                }

                // '식단' 데이터
                is HistoryItem.Diet -> {
                    val dietBinding = binding as ItemDietBinding
                    val session = item.session

                    // [수정] foodName이 있으면 표시, 없으면 dietId 표시
                    val displayName = session.foodName ?: "식단: ${session.dietId}"
                    dietBinding.dietNameTextView.text = displayName
                    dietBinding.dietDetailTextView.text =
                        "섭취량: ${session.actualQuantity} ${session.actualUnit} (만족도: ${session.userSatisfaction ?: "없음"})"

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