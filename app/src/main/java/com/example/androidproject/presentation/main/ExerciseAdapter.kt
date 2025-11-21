package com.example.androidproject.presentation.main

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.androidproject.databinding.ItemExerciseBinding
import com.example.androidproject.domain.model.Exercise

class ExerciseAdapter(
    private val onItemClick: (Exercise) -> Unit
) : ListAdapter<TodayExercise, ExerciseAdapter.ExerciseViewHolder>(ExerciseDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExerciseViewHolder {
        val binding = ItemExerciseBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ExerciseViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ExerciseViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ExerciseViewHolder(private val binding: ItemExerciseBinding) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick(getItem(position).exercise)
                }
            }
        }

        fun bind(todayExercise: TodayExercise) {
            val exercise = todayExercise.exercise
            binding.exerciseNameTextView.text = exercise.name

            // 세트와 횟수를 상세 텍스트에 포함
            val setsText = exercise.sets?.let { "${it}세트" } ?: ""
            val repsText = exercise.reps?.let { "${it}회" } ?: ""

            // 세트나 횟수 중 하나라도 있을 때만 구분자를 사용합니다.
            val detailSeparator = if (setsText.isNotEmpty() && repsText.isNotEmpty()) " / " else ""

            // ★★★ [수정] 부위 및 난이도 정보를 모두 제거하고 세트/횟수 정보만 표시 ★★★
            var detailContent = "$setsText$detailSeparator$repsText"

            // 세트/횟수 정보가 모두 없을 경우를 대비해 기본 메시지를 설정할 수도 있습니다.
            if (detailContent.trim().isEmpty()) {
                detailContent = "세트/횟수 정보 없음"
            }

            binding.exerciseDetailTextView.text = detailContent

            binding.exerciseStatusCheckBox.isChecked = todayExercise.isCompleted
        }
    }
}

class ExerciseDiffCallback : DiffUtil.ItemCallback<TodayExercise>() {
    override fun areItemsTheSame(oldItem: TodayExercise, newItem: TodayExercise): Boolean {
        return oldItem.exercise.id == newItem.exercise.id
    }

    override fun areContentsTheSame(oldItem: TodayExercise, newItem: TodayExercise): Boolean {
        return oldItem == newItem
    }
}