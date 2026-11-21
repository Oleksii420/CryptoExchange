package com.example.cryptoexchange.sync

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.example.cryptoexchange.CryptoApp
import com.example.cryptoexchange.domain.model.RefreshResult

class CryptoSyncWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        Log.d(TAG, "Старт оновлення, спроба №${runAttemptCount + 1}")

        // Беремо use case з "комори". Worker не знає ні про Retrofit, ні про Room.
        val container = (applicationContext as CryptoApp).container
        val result = container.refreshCryptos()
        Log.d(TAG, "Результат: $result")

        return when (result) {
            is RefreshResult.Success ->
                Result.success(workDataOf(KEY_MESSAGE to "Оновлено: ${result.count} монет"))

            // Тимчасові проблеми: є сенс спробувати пізніше
            RefreshResult.NoInternet ->
                retryOrFail("Немає з'єднання з інтернетом")
            RefreshResult.RateLimited ->
                retryOrFail("Забагато запитів, спробуйте пізніше")
            is RefreshResult.ServerError ->
                if (result.code in 500..599) retryOrFail("Сервер тимчасово недоступний (${result.code})")
                else fail("Помилка сервера: код ${result.code}")

            // Постійні проблеми: повтор нічого не змінить
            RefreshResult.InvalidApiKey ->
                fail("Неправильний або відсутній API-ключ")
            RefreshResult.BadResponse ->
                fail("Некоректна відповідь сервера")
        }
    }

    // Повторюємо не більше MAX_ATTEMPTS разів, далі здаємося
    private fun retryOrFail(message: String): Result =
        if (runAttemptCount + 1 < MAX_ATTEMPTS) Result.retry() else fail(message)

    private fun fail(message: String): Result =
        Result.failure(workDataOf(KEY_MESSAGE to message))

    companion object {
        const val KEY_MESSAGE = "message"
        private const val MAX_ATTEMPTS = 3
        private const val TAG = "CryptoSync"
    }
}