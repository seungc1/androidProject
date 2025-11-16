package com.example.androidproject.presentation.main

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.androidproject.databinding.ItemDietBinding
import com.example.androidproject.domain.model.Diet

/**
 * [파일 4/11] - '식단' 목록 관리자
 * (★수정★) '아이템 클릭' 시 '상세' 화면으로 '이동'하기 위해
 * 생성자에서 'onItemClick' 람다를 '전달'받습니다.
 */
class DietAdapter(
    // (★ 추가 ★) '아이템 클릭' 람다
    private val onItemClick: (Diet) -> Unit
) : ListAdapter<Diet, DietAdapter.DietViewHolder>(DietDiffCallback()) {

    /**
     * '한 줄 견본'(item_diet.xml)을 '생성'합니다.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DietViewHolder {
        val binding = ItemDietBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DietViewHolder(binding)
    }

    /**
     * '데이터'('Diet')를 '한 줄 견본'(ViewHolder)에 '연결'합니다.
     */
    override fun onBindViewHolder(holder: DietViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    /**
     * '한 줄 견본'(item_diet.xml)의 UI 요소들을 '관리'하는 '뷰 홀더'입니다.
     */
    inner class DietViewHolder(private val binding: ItemDietBinding) : RecyclerView.ViewHolder(binding.root) {

        // (★핵심★) '카드 터치' 기능 (팀원 1의 로드맵 Phase 2)
        init {
            // (★ 수정 ★) '아이템 전체' (binding.root) 클릭 리스너
            binding.root.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    // (★ 수정 ★) 'onItemClick' 람다를 '호출'하고 'Diet' '객체'를 '전달'합니다.
                    onItemClick(getItem(adapterPosition))
                }
            }
        }

        /**
         * '데이터'('필드값')를 'UI'('한 줄 견본')에 '연결'합니다.
         */
        fun bind(diet: Diet) {
            binding.dietNameTextView.text = diet.foodName
            binding.dietDetailTextView.text = "${diet.mealType} / ${diet.protein}g 단백질"
            binding.dietCaloriesTextView.text = "${diet.calorie} kcal"
        }
    }
}

/**
 * '목록'이 '효율적으로' '새로고침'될 수 있도록 돕는 'DiffUtil'입니다.
 */
class DietDiffCallback : DiffUtil.ItemCallback<Diet>() {
    override fun areItemsTheSame(oldItem: Diet, newItem: Diet): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Diet, newItem: Diet): Boolean {
        return oldItem == newItem
    }
}