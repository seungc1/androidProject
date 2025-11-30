package com.dataDoctor.rehabai.domain.repository

import com.dataDoctor.rehabai.domain.model.Diet
import kotlinx.coroutines.flow.Flow

interface DietRepository {
    suspend fun upsertDiets(diets: List<Diet>)
    fun getDietById(dietId: String): Flow<Diet?>
}