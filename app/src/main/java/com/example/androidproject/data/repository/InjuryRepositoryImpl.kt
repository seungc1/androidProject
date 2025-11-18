package com.example.androidproject.data.repository

import com.example.androidproject.data.local.datasource.LocalDataSource
import com.example.androidproject.data.remote.datasource.FirebaseDataSource // (★ 추가)
import com.example.androidproject.data.mapper.toDomain
import com.example.androidproject.data.mapper.toEntity
import com.example.androidproject.domain.model.Injury
import com.example.androidproject.domain.repository.InjuryRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

class InjuryRepositoryImpl @Inject constructor(
    private val localDataSource: LocalDataSource,
    private val firebaseDataSource: FirebaseDataSource // (★ 추가 주입)
) : InjuryRepository {

    override suspend fun upsertInjury(injury: Injury, userId: String) {
        try {
            // 1. 서버(Firebase)에 먼저 저장 (실패 시 예외 발생하여 중단됨)
            firebaseDataSource.upsertInjury(userId, injury)

            // 2. 성공 시 로컬(Room)에도 저장
            localDataSource.upsertInjury(injury.toEntity(userId))
        } catch (e: Exception) {
            e.printStackTrace()
            // (선택 사항) 인터넷이 없을 때 로컬에만이라도 저장하려면 여기서 예외 처리 후 로컬 저장 호출
            // localDataSource.upsertInjury(injury.toEntity(userId))
            throw e
        }
    }

    override fun getInjuryById(injuryId: String): Flow<Injury?> {
        // 로컬 데이터 우선 반환
        return localDataSource.getInjuryById(injuryId).map { it?.toDomain() }
    }

    override fun getInjuriesForUser(userId: String): Flow<List<Injury>> {
        // 1. 로컬 데이터를 먼저 구독 (화면에 즉시 표시됨)
        val localFlow = localDataSource.getInjuriesForUser(userId).map { list ->
            list.map { it.toDomain() }
        }

        // 2. 백그라운드에서 서버 데이터 가져와서 로컬 동기화 (Fire-and-Forget)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val remoteInjuries = firebaseDataSource.getInjuries(userId)
                if (remoteInjuries.isNotEmpty()) {
                    // 서버에서 받은 데이터를 로컬 DB에 덮어쓰기 -> localFlow가 자동으로 갱신됨
                    remoteInjuries.forEach { injury ->
                        localDataSource.upsertInjury(injury.toEntity(userId))
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace() // 네트워크 오류 등은 무시하고 로컬 데이터 유지
            }
        }

        return localFlow
    }
}