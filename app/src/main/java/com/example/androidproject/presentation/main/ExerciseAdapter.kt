package com.example.androidproject.presentation.main

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.androidproject.databinding.ItemExerciseBinding // (★필수★) '한 줄 견본'의 ViewBinding import
import com.example.androidproject.domain.model.Exercise

/**
 * [새 파일 2/5]
 * '운동 to-do 리스트' ('RecyclerView')의 '목록 관리자'입니다.
 * 'TodayExercise' 데이터 묶음을 'item_exercise.xml' ('한 줄 견본')에 '연결'합니다.
 */
class ExerciseAdapter(
    // (★핵심★) '두뇌'(HomeFragment)가 '체크박스' 클릭을 '알 수 있도록' 람다(lambda)를 전달받습니다.
    private val onToggleClick: (TodayExercise) -> Unit
) : ListAdapter<TodayExercise, ExerciseAdapter.ExerciseViewHolder>(ExerciseDiffCallback()) {

    /**
     * '한 줄 견본'(item_exercise.xml)을 '생성'합니다.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExerciseViewHolder {
        val binding = ItemExerciseBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ExerciseViewHolder(binding)
    }

    /**
     * '데이터'('TodayExercise')를 '한 줄 견본'(ViewHolder)에 '연결'합니다.
     */
    override fun onBindViewHolder(holder: ExerciseViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    /**
     * '한 줄 견본'(item_exercise.xml)의 UI 요소들을 '관리'하는 '뷰 홀더'입니다.
     */
    inner class ExerciseViewHolder(private val binding: ItemExerciseBinding) : RecyclerView.ViewHolder(binding.root) {

        init {
            // (★핵심★) '체크박스'를 클릭하면, '두뇌'(HomeFragment)에게 "클릭됐다!"고 '알려줍니다'.
            binding.exerciseStatusCheckBox.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    onToggleClick(getItem(adapterPosition))
                }
            }
        }

        fun bind(todayExercise: TodayExercise) {
            val exercise = todayExercise.exercise // '필드값' (Exercise)
            binding.exerciseNameTextView.text = exercise.name
            binding.exerciseDetailTextView.text = "부위: ${exercise.bodyPart} / ${exercise.difficulty}"

            // (★핵심★) '데이터'의 '완료 상태'를 '체크박스' UI에 '연결'합니다.
            // (setOnClickListener가 아닌 'setOnCheckedChangeListener'를 사용하지 않도록 주의)
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
        // 'isCompleted' 상태가 바뀌었을 때도 '새로고침'되도록 '==' (내용 비교)를 합니다.
        return oldItem == newItem
    }
}