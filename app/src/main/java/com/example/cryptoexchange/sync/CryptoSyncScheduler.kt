package com.example.cryptoexchange.sync

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit
import androidx.work.WorkInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

object CryptoSyncScheduler {

    const val PERIODIC_WORK = "crypto_sync_periodic"
    const val MANUAL_WORK = "crypto_sync_manual"

    // Запускати тільки коли є мережа
    private val networkConstraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()

    // Щогодини. KEEP: якщо вже заплановано, нічого не міняємо.
    fun schedulePeriodic(context: Context) {
        val request = PeriodicWorkRequestBuilder<CryptoSyncWorker>(1, TimeUnit.HOURS)
            .setConstraints(networkConstraints)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            PERIODIC_WORK,
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }

    // Кнопка "Оновити". KEEP: якщо оновлення вже йде, друге не запускаємо.
    fun refreshNow(context: Context) {
        val request = OneTimeWorkRequestBuilder<CryptoSyncWorker>()
            .setConstraints(networkConstraints)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            MANUAL_WORK,
            ExistingWorkPolicy.KEEP,
            request
        )
    }

    // Стежимо за роботою "Оновити" і перекладаємо мову WorkManager на нашу (SyncStatus)
    fun observeManualStatus(context: Context): Flow<SyncStatus> =
        WorkManager.getInstance(context)
            .getWorkInfosForUniqueWorkFlow(MANUAL_WORK)
            .map { infos ->
                // Якщо є незавершена робота, показуємо її. Інакше показуємо останню.
                val info = infos.firstOrNull { !it.state.isFinished } ?: infos.lastOrNull()
                info.toSyncStatus()
            }

    private fun WorkInfo?.toSyncStatus(): SyncStatus {
        if (this == null) return SyncStatus.Idle             // кнопку ще не натискали

        return when (state) {
            WorkInfo.State.ENQUEUED, WorkInfo.State.BLOCKED ->
                if (runAttemptCount > 0) SyncStatus.Retrying else SyncStatus.WaitingForNetwork
            WorkInfo.State.RUNNING -> SyncStatus.Running
            WorkInfo.State.SUCCEEDED -> SyncStatus.Succeeded(
                outputData.getString(CryptoSyncWorker.KEY_MESSAGE) ?: "Дані оновлено"
            )
            WorkInfo.State.FAILED -> SyncStatus.Failed(
                outputData.getString(CryptoSyncWorker.KEY_MESSAGE) ?: "Не вдалося оновити дані"
            )
            WorkInfo.State.CANCELLED -> SyncStatus.Idle
        }
    }
}