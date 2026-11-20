package com.example.cryptoexchange.domain.model

// Усі можливі результати оновлення. sealed = список закритий, інших варіантів не буде.
sealed interface RefreshResult {
    data class Success(val count: Int) : RefreshResult    // збережено N монет
    data object NoInternet : RefreshResult                // немає мережі → спробувати пізніше
    data object InvalidApiKey : RefreshResult             // 401/403 → повтор не допоможе
    data object RateLimited : RefreshResult               // 429 → забагато запитів, почекати
    data class ServerError(val code: Int) : RefreshResult // 5xx та інші коди
    data object BadResponse : RefreshResult               // відповідь зламана або порожня
}