package com.example.cryptoexchange

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.lifecycleScope
import com.example.cryptoexchange.data.remote.CoinGeckoNetwork
import com.example.cryptoexchange.ui.theme.CryptoExchangeTheme
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleScope.launch {
            try {
                val response = CoinGeckoNetwork.api.getTopCoins()
                if (response.isSuccessful) {
                    val coins = response.body().orEmpty()
                    val first = coins.firstOrNull()
                    Log.d("CoinGecko", "Отримано ${coins.size} монет, перша: ${first?.name} = ${first?.currentPrice} USD")
                } else {
                    Log.w("CoinGecko", "Помилка сервера: код ${response.code()}")
                }
            } catch (e: CancellationException) {
                throw e                       // скасування не ковтаємо (пам'ятаєш з корутин?)
            } catch (e: Exception) {
                Log.w("CoinGecko", "Не вдалося: ${e.message}")
            }
        }
        enableEdgeToEdge()
        setContent {
            CryptoExchangeTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "Android",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    CryptoExchangeTheme {
        Greeting("Android")
    }
}