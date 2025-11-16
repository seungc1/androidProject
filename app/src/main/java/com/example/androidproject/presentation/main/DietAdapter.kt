package com.example.androidproject.presentation.main

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.androidproject.databinding.ItemDietBinding
import com.example.androidproject.domain.model.Diet

class DietAdapter(
    private val onItemClick: (Diet) -> Unit
) : ListAdapter<Diet, DietAdapter.DietViewHolder>(DietDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DietViewHolder {
        val binding = ItemDietBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DietViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DietViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class DietViewHolder(private val binding: ItemDietBinding) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick(getItem(position))
                }
            }
        }

        fun bind(diet: Diet) {
            binding.dietNameTextView.text = diet.foodName
            binding.dietDetailTextView.text = "${diet.mealType} / ${diet.protein}g 단백질"
            binding.dietCaloriesTextView.text = "${diet.calorie} kcal"
        }
    }
}

class DietDiffCallback : DiffUtil.ItemCallback<Diet>() {
    override fun areItemsTheSame(oldItem: Diet, newItem: Diet): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Diet, newItem: Diet): Boolean {
        return oldItem == newItem
    }
}