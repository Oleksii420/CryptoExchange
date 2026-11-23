# Біржа криптовалют (CryptoExchange)

Android-застосунок, який показує **топ-30 криптовалют за ринковою капіталізацією** з цінами в USD, зберігає дані на телефоні (Room) і щогодини оновлює їх у фоні (WorkManager). Після першого завантаження список доступний без інтернету.

## Виконавець

**[Дідик Олексій]**. Проєкт виконано самостійно


## Можливості

- список топ-30: місце в рейтингу, логотип, назва, символ, ціна в USD, час оновлення;
- кнопка «Оновити» (одноразове завдання WorkManager);
- автоматичне оновлення щогодини, навіть коли застосунок закритий;
- офлайн-перегляд збережених даних, зокрема після перезапуску;
- стан оновлення: час останнього збереження, очікування мережі, оновлення, помилки;
- «Немає даних» замість відсутньої ціни; малі ціни без округлення до `0.00`;
- екран деталей з мінімумом і максимумом за 24 години;
- альбомний режим: список і деталі поруч;
- світла й темна тема; стан, прокрутка й вибрана монета зберігаються після повороту.

## Архітектура

Чиста архітектура + MVVM, шари виділені пакетами в одному модулі:

| Пакет | Що містить |
|---|---|
| `domain` | модель `Crypto`, `RefreshResult`, інтерфейс `CryptoRepository`, сценарії `ObserveCryptosUseCase`, `ObserveLastSyncTimeUseCase`, `RefreshCryptosUseCase`. Не залежить від Android, Room, Retrofit і WorkManager |
| `data` | `remote` (Retrofit, `CoinMarketDto`), `local` (Room: `CryptoEntity`, `CryptoDao`, `CryptoDatabase`), `mapper`, `repository` (`CryptoRepositoryImpl`) |
| `presentation` | `CryptoListViewModel`, `CryptoListUiState`, Compose-екрани списку й деталей |
| `sync` | `CryptoSyncWorker`, `CryptoSyncScheduler` (постановка завдань WorkManager) |
| `di` | `AppContainer`: ручне створення залежностей, живе в `CryptoApp` |

Шлях даних:

```text
WorkManager → CryptoSyncWorker → RefreshCryptosUseCase → CryptoRepositoryImpl → CoinGecko API → Room
Room → Flow → CryptoRepositoryImpl → CryptoListViewModel → StateFlow → Compose
```

Єдине джерело даних для екрана: **Room**. Відповідь сервера потрапляє на екран лише через базу. DTO, Entity і модель `domain` є окремими типами.

## Джерело даних і запит

У завданні вказано CoinDesk Data API, але 21 травня 2026 року його безкоштовний тариф закрито, тому використано **CoinGecko Demo API** (безкоштовно; 10 000 запитів на місяць, 100 на хвилину).

```text
GET https://api.coingecko.com/api/v3/coins/markets
    ?vs_currency=usd
    &order=market_cap_desc
    &per_page=30
    &page=1
    &precision=full
Заголовок: x-cg-demo-api-key: <ключ>
```

Використані поля: `id` (первинний ключ у Room), `name`, `symbol`, `image` (логотип), `current_price`, `market_cap_rank`, `high_24h`, `low_24h`, `last_updated`. Параметр `precision=full` повертає ціну без обрізання знаків.

## Налаштування API-ключа

1. Отримати безкоштовний Demo-ключ: https://www.coingecko.com/en/api/pricing
2. У корені проєкту відкрити `local.properties` і додати рядок (після `=` вставити свій ключ, без лапок):

   ```properties
   COINGECKO_API_KEY=
   ```

3. **File → Sync Project with Gradle Files.**

Gradle читає ключ з `local.properties` і передає його в `BuildConfig.COINGECKO_API_KEY`, а OkHttp-інтерцептор додає його заголовком до кожного запиту. Файл `local.properties` виключено з Git: ключ не потрапляє в репозиторій, логи чи відео.


## Фонова синхронізація

| | Періодичне оновлення | Кнопка «Оновити» |
|---|---|---|
| Тип | `PeriodicWorkRequest`, **кожну 1 годину** | `OneTimeWorkRequest` |
| Унікальне ім'я | `crypto_sync_periodic` | `crypto_sync_manual` |
| Політика | `ExistingPeriodicWorkPolicy.KEEP` | `ExistingWorkPolicy.KEEP` |
| Умова | `NetworkType.CONNECTED` | `NetworkType.CONNECTED` |
| Повтори | експоненційний backoff від 30 с, до 3 спроб | те саме |

Періодичне завдання реєструється в `CryptoApp.onCreate()`, тому поворот екрана й повторне відкриття не створюють дублікатів. Інтервал 1 година обрано з урахуванням ліміту Demo-плану (≈720 запитів на місяць). WorkManager не гарантує точну хвилину, тому в застосунку показано фактичний час збереження даних.

| Відповідь | Результат Worker |
|---|---|
| успіх | `success`, дані замінено в одній транзакції |
| немає мережі, 429 (ліміт), 5xx | `retry` з backoff, не більше 3 спроб |
| 401 / 403 (ключ) | `failure`: «Неправильний або відсутній API-ключ» (без повторів) |
| некоректна або порожня відповідь | `failure`, попередні дані залишаються |

## Зберігання даних (Room)

- таблиця `cryptos`, первинний ключ: `id` з API, тому повторне оновлення не створює дублікатів;
- DAO повертає `Flow`, відсортований за місцем у рейтингу;
- новий список записується функцією `replaceAll()` **в одній транзакції** (видалення + запис), тому монети, що вибули з топ-30, не накопичуються;
- база змінюється лише після успішного запиту й перевірки відповіді; при помилці попередні дані залишаються.

## Тести

Папка `app/src/test`:

- `CryptoMappersTest`: DTO без ціни, зображення чи id; малі ціни не округлюються до `0.00`; розбір часу;
- `CryptoRepositoryTest`: помилка мережі чи API зберігає базу; повторне оновлення без дублікатів; новий рейтинг повністю замінює старий.

Запуск: `./gradlew testDebugUnitTest`.

## Технології

Kotlin, Jetpack Compose (Material 3), MVVM, Coroutines і Flow, Retrofit 3.0.0 + Gson, Room 3.0.3 (KSP), WorkManager 2.9.0, Coil 3.3.0.

> Coil 3.6.x зібрано на Kotlin 2.4, який несумісний з версією Kotlin у проєкті, тому використано Coil 3.3.0 (той самий `AsyncImage`).

---

Data provided by [CoinGecko](https://www.coingecko.com/en/api).