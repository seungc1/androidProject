package com.dataDoctor.rehabai.data.repository

import com.dataDoctor.rehabai.data.local.datasource.LocalDataSource
import com.dataDoctor.rehabai.data.mapper.toDomain
import com.dataDoctor.rehabai.data.mapper.toEntity
import com.dataDoctor.rehabai.domain.model.Diet
import com.dataDoctor.rehabai.domain.repository.DietRepository // 👈 (Interface import)
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

// 🚨 [해결책] ': DietRepository' 부분이 빠졌는지 확인하세요.
class DietRepositoryImpl @Inject constructor(
    private val localDataSource: LocalDataSource
) : DietRepository {
    override suspend fun upsertDiets(diets: List<Diet>) {
        localDataSource.upsertDiets(diets.map { it.toEntity() })
    }

    override fun getDietById(dietId: String): Flow<Diet?> {
        return localDataSource.getDietById(dietId).map { it?.toDomain() }
    }
}