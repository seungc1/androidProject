package com.example.androidproject.presentation.main

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.androidproject.databinding.ItemDietBinding // (★필수★) '한 줄 견본'의 ViewBinding import
import com.example.androidproject.domain.model.Diet

/**
 * [파일 4/11] - '식단' 목록 관리자
 * 'AI 추천 식단' ('RecyclerView')의 '목록 관리자'입니다.
 * 'Diet' 데이터('필드값')를 'item_diet.xml' ('한 줄 견본')에 '연결'합니다.
 */
class DietAdapter : ListAdapter<Diet, DietAdapter.DietViewHolder>(DietDiffCallback()) {

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
            binding.root.setOnClickListener {
                // 나중에 '두뇌'(HomeFragment)에게 '상세보기' 클릭을 '알려주는'
                // 람다(lambda) 코드를 여기에 추가할 수 있습니다.
                // (예: onItemClick?.invoke(getItem(adapterPosition)))
            }
        }

        /**
         * '데이터'('필드값')를 'UI'('한 줄 견본')에 '연결'합니다.
         *
         * (★원상 복구★)
         * '빌드'가 '성공'했으므로, '자동 생성'된 'ItemDietBinding'이
         * 'dietDetailTextView' 등을 '인식'할 수 있습니다.
         */
        fun bind(diet: Diet) {
            // (★원상 복구★) '진짜' 연결 코드를 '복구'합니다.
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