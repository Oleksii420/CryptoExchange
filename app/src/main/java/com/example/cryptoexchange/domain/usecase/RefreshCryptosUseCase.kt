package com.example.cryptoexchange.domain.usecase

import com.example.cryptoexchange.domain.model.RefreshResult
import com.example.cryptoexchange.domain.repository.CryptoRepository

class RefreshCryptosUseCase(private val repository: CryptoRepository) {
    suspend operator fun invoke(): RefreshResult = repository.refresh()
}