package com.example.androidproject.presentation.main

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.androidproject.databinding.ItemExerciseBinding
import com.example.androidproject.domain.model.Exercise

/**
 * [새 파일 2/5]
 * '운동 to-do 리스트' ('RecyclerView')의 '목록 관리자'입니다.
 *
 * (★수정★) '체크박스' 클릭('onToggleClick') 대신,
 * '아이템 전체' 클릭('onItemClick')을 '감지'하여
 * '상세' 화면으로 '이동'하도록 '수정'합니다.
 */
class ExerciseAdapter(
    // (★수정★) 'onToggleClick' -> 'onItemClick'
    // 'TodayExercise' 대신 'Exercise' '필드값' 자체를 '전달'합니다.
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
            // (★수정★) '체크박스' 클릭 리스너를 '제거'하고,
            // '아이템 전체'(binding.root) 클릭 리스너로 '변경'합니다.
            binding.root.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    // (★수정★) 'onItemClick' 람다를 '호출'하고 'Exercise' '객체'를 '전달'합니다.
                    onItemClick(getItem(adapterPosition).exercise)
                }
            }
        }

        fun bind(todayExercise: TodayExercise) {
            val exercise = todayExercise.exercise
            binding.exerciseNameTextView.text = exercise.name
            binding.exerciseDetailTextView.text = "부위: ${exercise.bodyPart} / ${exercise.difficulty}"

            // '데이터'의 '완료 상태'를 '체크박스' UI에 '연결'합니다.
            // (item_exercise.xml에서 'clickable=false'로 '설정'했기 때문에 '표시'만 됩니다.)
            binding.exerciseStatusCheckBox.isChecked = todayExercise.isCompleted
        }
    }
}

/**
 * '목록'이 '효율적으로' '새로고침'될 수 있도록 돕는 'DiffUtil'입니다.
 */
class ExerciseDiffCallback : DiffUtil.ItemCallback<TodayExercise>() {
    override fun areItemsTheSame(oldItem: TodayExercise, newItem: TodayExercise): Boolean {
        return oldItem.exercise.id == newItem.exercise.id
    }

    override fun areContentsTheSame(oldItem: TodayExercise, newItem: TodayExercise): Boolean {
        return oldItem == newItem
    }
}