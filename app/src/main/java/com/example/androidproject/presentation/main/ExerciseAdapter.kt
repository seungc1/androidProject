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
            binding.exerciseDetailTextView.text = "부위: ${exercise.bodyPart} / ${exercise.difficulty}"

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