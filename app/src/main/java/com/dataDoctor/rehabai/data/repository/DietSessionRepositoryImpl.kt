package com.dataDoctor.rehabai.data.repository

import com.dataDoctor.rehabai.data.local.datasource.LocalDataSource
import com.dataDoctor.rehabai.data.remote.datasource.FirebaseDataSource // (★ 추가)
import com.dataDoctor.rehabai.data.mapper.toDomain
import com.dataDoctor.rehabai.data.mapper.toEntity
import com.dataDoctor.rehabai.domain.model.DietSession
import com.dataDoctor.rehabai.domain.repository.DietSessionRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first // [추가]
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DietSessionRepositoryImpl @Inject constructor(
    private val localDataSource: LocalDataSource,
    private val firebaseDataSource: FirebaseDataSource // (★ 추가 주입)
) : DietSessionRepository {

    // [추가] 최근에 저장/수정한 ID를 추적하여 동기화 시 삭제 방지 (메모리 캐시)
    private val recentlySavedIds = java.util.concurrent.ConcurrentHashMap<String, Long>()

    override suspend fun addDietSession(session: DietSession): Flow<Unit> {
        android.util.Log.d("DIET_REPO", "addDietSession called: ${session.id}, foodName=${session.foodName}")
        
        // [추가] 저장 시도 시 ID를 '최근 저장됨'으로 마킹
        recentlySavedIds[session.id] = System.currentTimeMillis()

        try {
            // 1. Firebase 저장
            android.util.Log.d("DIET_REPO", "Saving to Firebase...")

            // [추가] 로컬 이미지인 경우 Firebase Storage에 업로드하고 URL 교체
            var sessionToSave = session
            val photoUrl = session.photoUrl
            if (photoUrl != null && photoUrl.startsWith("file://")) {
                try {
                    val downloadUrl = firebaseDataSource.uploadImage(session.userId, android.net.Uri.parse(photoUrl))
                    sessionToSave = session.copy(photoUrl = downloadUrl)
                    android.util.Log.d("DIET_REPO", "Image uploaded to Firebase Storage: $downloadUrl")
                } catch (e: Exception) {
                    android.util.Log.e("DIET_REPO", "Image upload failed: ${e.message}")
                    // 업로드 실패 시 로컬 URI 유지 (오프라인 등)
                }
            }

            firebaseDataSource.addDietSession(sessionToSave)
            android.util.Log.d("DIET_REPO", "Firebase save complete")
            
            // 2. Local 저장
            android.util.Log.d("DIET_REPO", "Saving to local DB...")
            localDataSource.addDietSession(session.toEntity())
            android.util.Log.d("DIET_REPO", "Local DB save complete")
        } catch (e: Exception) {
            android.util.Log.e("DIET_REPO", "Error in addDietSession: ${e.message}", e)
            throw e
        }
        
        return flowOf(Unit)
    }

    override suspend fun getDietHistory(userId: String): Flow<List<DietSession>> {
        // 1. 로컬 데이터 구독
        val localData = localDataSource.getDietHistory(userId).map { entityList ->
            entityList.map { it.toDomain() }
        }

        // 2. 서버 데이터 동기화
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val remoteSessions = firebaseDataSource.getDietHistory(userId)
                
                // [수정] 삭제 동기화 로직 제거 (업데이트 시 레이스 컨디션으로 인한 데이터 삭제 방지)
                // 로컬에만 있고 리모트에는 없는 데이터(삭제된 데이터)를 찾아서 로컬에서도 삭제하는 로직은
                // 방금 추가한 데이터가 아직 리모트 쿼리에 반영되지 않았을 때 로컬 데이터를 삭제해버리는 부작용이 있음.
                // 따라서 일단 주석 처리하고, 삭제는 명시적인 삭제 액션에서 처리하도록 함.
                val localSessions = localDataSource.getDietHistory(userId).first() // 현재 로컬 데이터 스냅샷
                val remoteIds = remoteSessions.map { it.id }.toSet()
                val now = System.currentTimeMillis()
                val syncLagBuffer = 10 * 1000L // 10초 버퍼
                
                localSessions.forEach { localEntity ->
                    if (!remoteIds.contains(localEntity.id)) {
                        // [안전한 동기화 개선]
                        // "최근에 앱에서 저장한 기록"인지 확인
                        val lastSavedTime = recentlySavedIds[localEntity.id] ?: 0L
                        val isRecentlySaved = (now - lastSavedTime) < syncLagBuffer
                        
                        if (!isRecentlySaved) {
                            android.util.Log.d("DIET_SYNC", "Deleting local session not found in remote: ${localEntity.id}")
                            localDataSource.deleteDietSessionById(localEntity.id)
                        } else {
                            android.util.Log.w("DIET_SYNC", "Skipping deletion of recently saved session: ${localEntity.id}")
                        }
                    }
                }

                // 리모트 데이터 로컬에 추가/업데이트
                remoteSessions.forEach { session ->
                    localDataSource.addDietSession(session.toEntity())
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return localData
    }

    override suspend fun getDietSessionsBetween(userId: String, startDate: Date, endDate: Date): Flow<List<DietSession>> {
        return localDataSource.getDietSessionsBetween(userId, startDate, endDate).map { entityList ->
            entityList.map { it.toDomain() }
        }
    }

    override suspend fun getDietSessionById(id: String): Flow<DietSession?> {
        return localDataSource.getDietSessionById(id).map { it?.toDomain() }
    }
}