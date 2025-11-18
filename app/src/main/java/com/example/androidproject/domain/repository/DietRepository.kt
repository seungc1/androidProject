package com.example.androidproject.domain.repository

import com.example.androidproject.domain.model.Diet
import kotlinx.coroutines.flow.Flow

interface DietRepository {
    suspend fun upsertDiets(diets: List<Diet>)
    fun getDietById(dietId: String): Flow<Diet?>
}