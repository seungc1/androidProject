package com.dataDoctor.rehabai.presentation.detail

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.dataDoctor.rehabai.databinding.ItemAlternativeFoodBinding

/**
 * [새 파일 3/4] - '대체 식품' 목록 관리자
 * 경로: presentation/detail/AlternativeFoodAdapter.kt
 * 'String' (문자열) 목록을 'item_alternative_food.xml'에 '연결'합니다.
 */
class AlternativeFoodAdapter : ListAdapter<String, AlternativeFoodAdapter.AlternativeFoodViewHolder>(StringDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlternativeFoodViewHolder {
        val binding = ItemAlternativeFoodBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AlternativeFoodViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AlternativeFoodViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class AlternativeFoodViewHolder(private val binding: ItemAlternativeFoodBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(foodName: String) {
            binding.alternativeFoodNameTextView.text = foodName
        }
    }
}

class StringDiffCallback : DiffUtil.ItemCallback<String>() {
    override fun areItemsTheSame(oldItem: String, newItem: String): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: String, newItem: String): Boolean {
        return oldItem == newItem
    }
}