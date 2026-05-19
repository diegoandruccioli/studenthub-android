# StudentHub Android

> App Android per la gestione della carriera universitaria con gamification. Kotlin · Clean Architecture · MVVM · Jetpack Compose · Room · WorkManager. UniBO Sistemi Mobili 2025/26.

---

## Panoramica

**StudentHub** è un'applicazione Android nativa che permette agli studenti di tracciare esami, CFU e media ponderata, monitorare obiettivi di gamification e sincronizzare i dati con il backend StudentHub (Node.js / MySQL).

Progetto finale per il corso di **Sistemi Mobili** — Università di Bologna, 2025/2026.

---

## Stack Tecnologico

| Layer | Tecnologia |
|-------|-----------|
| Linguaggio | Kotlin 2.2.10 (100%, no Java) |
| Architettura | Clean Architecture multi-modulo |
| Pattern UI | MVVM — ViewModel + StateFlow |
| UI Toolkit | Jetpack Compose + Material Design 3 |
| Navigazione | `NavigationSuiteScaffold` adattivo (phone / tablet / desktop) |
| Async | Kotlin Coroutines + Flow |
| DB locale | Room 2.7.2 |
| Preferenze | DataStore (Preferences) |
| Networking | Retrofit 2 + OkHttp 4 + TokenAuthenticator |
| Background | WorkManager (sync offline-first) |
| Test | JUnit 4 + Mockito-Kotlin + kotlinx-coroutines-test |
| Min SDK | 31 (Android 12) |
| Target SDK | 36 |

---

## Architettura

Il progetto segue la **Clean Architecture** con separazione rigorosa in 4 moduli Gradle:

```
:app        → Entry point, CustomApplication, DI manuale (RepositoryProvider)
:domain     → Use Cases, interfacce Repository, Domain Models (Kotlin puro, zero import Android)
:data       → RepositoryImpl, Room DAO/Entity, DataStore, Retrofit services, WorkManager Worker
:ui         → Composable screens, ViewModel, tema Material You
```

**Dipendenze tra moduli:**
```
:app  →  :ui, :domain, :data
:ui   →  :domain
:data →  :domain
:domain  →  (nessuna dipendenza interna)
```

**Flusso dei dati (reattivo):**
```
Composable
  └── ViewModel  [:ui]              ← collectAsStateWithLifecycle()
        └── UseCase  [:domain]      ← StateFlow / stateIn()
              └── Repository (interfaccia)  [:domain]
                    └── RepositoryImpl  [:data]
                          ├── Room DAO      → Flow<T> (Room emette aggiornamenti automatici)
                          └── Retrofit API  → suspend fun (Dispatchers.IO)
```

---

## Struttura del Progetto

```
studenthub-android/
├── app/
│   └── src/main/java/com/unibo/android/studenthub/
│       └── CustomApplication.kt          ← RepositoryProvider + WorkManager bootstrap
├── domain/
│   └── src/main/java/com/unibo/android/domain/
│       ├── model/                         ← Esame, Statistiche, Obiettivo, Settings, User
│       ├── repository/                    ← interfacce: EsameRepository, AuthRepository, …
│       └── usecase/                       ← GetEsamiUseCase, LoginUseCase, SyncWorker, …
├── data/
│   └── src/main/java/com/unibo/android/data/
│       ├── local/                         ← Room: StudentHubDatabase, DAO, Entity, Mapper
│       ├── remote/                        ← Retrofit: AuthApiService, ExamApiService, …
│       ├── repository/                    ← implementazioni repository
│       └── worker/                        ← SyncExamsWorker (WorkManager)
├── ui/
│   └── src/main/java/com/unibo/android/ui/
│       ├── screens/
│       │   ├── auth/                      ← LoginScreen, RegisterScreen, AuthViewModel
│       │   ├── libretto/                  ← LibrettoScreen, LibrettoViewModel, EsameCard
│       │   ├── statistiche/               ← StatisticheScreen, StatisticheViewModel
│       │   ├── obiettivi/                 ← ObiettiviScreen, ObiettiviViewModel
│       │   └── profilo/                   ← ProfiloScreen, ProfiloViewModel
│       ├── theme/                         ← StudentHubTheme, Color, Type
│       └── MainActivity.kt               ← NavigationSuiteScaffold, RootNavigation
├── gradle/
│   └── libs.versions.toml
└── settings.gradle.kts
```

---

## Flussi Utente

### Autenticazione

```mermaid
flowchart TD
    START([Avvio app]) --> DS{DataStore\nisLoggedIn}
    DS -->|null — lettura async| SPIN[Spinner]
    SPIN --> DS
    DS -->|false| LOGIN[LoginScreen]
    DS -->|true| COOKIE{Cookie JWT\nvalido?}

    COOKIE -->|Sì — PersistentCookieJar| APP[StudentHubApp]
    COOKIE -->|No — 401 → refresh| REFRESH{POST /auth/refresh}
    REFRESH -->|200| APP
    REFRESH -->|fallito| LOGIN

    LOGIN -->|Tap Registrati| REG[RegisterScreen]
    REG -->|Tap Ho già un account| LOGIN

    LOGIN -->|POST /auth/login ✓\ncookie salvato su disco| APP
    LOGIN -->|POST /auth/login ✗| ERR_L[Snackbar errore]
    ERR_L --> LOGIN

    REG -->|POST /auth/register ✓\ncookie salvato su disco| APP
    REG -->|POST /auth/register ✗| ERR_R[Snackbar errore]
    ERR_R --> REG
```

---

### Navigazione principale

```mermaid
flowchart TD
    APP[StudentHubApp] --> NAV{NavigationSuiteScaffold\nadattivo}
    NAV -->|Tab 1| LIB[Libretto]
    NAV -->|Tab 2| STAT[Statistiche]
    NAV -->|Tab 3| OBJ[Obiettivi]
    NAV -->|Tab 4| PROF[Profilo]
```

---

### Libretto Esami

```mermaid
flowchart TD
    LIB[Libretto] --> REFRESH_LIB[init: GET /exams\nsincronizza server → Room]
    REFRESH_LIB --> LIB_LIST[Lista esami reattiva\nRoom Flow → StateFlow → UI]

    LIB_LIST --> SORT[FilterChip: DATA · VOTO · CFU\nASC / DESC]
    SORT --> LIB_LIST

    LIB_LIST --> LIB_ADD[FAB + → dialog Aggiungi\nnome · voto 18-30 · CFU 1-48 · data · lode se 30]
    LIB_ADD --> INS{Inserimento}
    INS -->|Room insert immediato| LIB_LIST
    INS -->|POST /exams async| SYNC_OK{API ok?}
    SYNC_OK -->|Sì| MARK[markSynced\nremoteId + pendingSync=false]
    SYNC_OK -->|No — rete assente| PENDING[pendingSync=true\nSyncWorker riprende]

    LIB_LIST --> LIB_EDIT[Tap ✎ → dialog Modifica\npre-riempito con valori attuali]
    LIB_EDIT --> UPD{Aggiornamento}
    UPD -->|Room update immediato| LIB_LIST
    UPD -->|PUT /exams/:id async| UPD_OK{API ok?}
    UPD_OK -->|Sì| MARK
    UPD_OK -->|No| PENDING
    LIB_EDIT -->|checkObiettivi| OBJ_CHK[Valuta obiettivi]

    LIB_LIST --> LIB_DEL[Tap 🗑 → dialog conferma eliminazione]
    LIB_DEL --> DEL_API{DELETE /exams/:id}
    DEL_API -->|200| DEL_LOCAL[Room delete]
    DEL_API -->|fallito / offline| KEEP[Esame mantenuto\nutente può riprovare]
    DEL_LOCAL --> OBJ_CHK
```

---

### Statistiche

```mermaid
flowchart TD
    STAT[Statistiche] --> CHECK{Esami presenti?}
    CHECK -->|No| EMPTY[Stato Empty\nmessaggio placeholder]
    CHECK -->|Sì| CALC[GetStatisticheUseCase\nsu tutti gli esami Room]
    CALC --> DATA[Media ponderata\nCFU sostenuti · Base laurea /110]
    DATA --> CHART[Grafico andamento carriera\nanimate 1500ms all'ingresso\nvoti + media progressiva]
```

---

### Obiettivi

```mermaid
flowchart TD
    OBJ[Obiettivi] --> OBJ_FLOW[StateFlow sempre attivo\nRoom Flow → obiettivi in tempo reale]
    OBJ_FLOW --> OBJ_LIST[Lista 4 obiettivi con XP]

    OBJ_LIST --> PP[Primo Passo\n≥ 1 esame superato · 150 XP]
    OBJ_LIST --> SEC[Secchione\n≥ 1 esame con lode · 300 XP]
    OBJ_LIST --> MAR[Maratoneta\n3+ esami nello stesso mese · 500 XP]
    OBJ_LIST --> GDB[Giro di Boa\n≥ 90 CFU totali · 800 XP]

    ADD_DEL[add / update / delete esame] -->|checkObiettiviUseCase| EVAL[GoalEvaluator\nvaluta ogni obiettivo]
    EVAL -->|non ancora completato + criterio soddisfatto| UNLOCK[updateGoalCompletion = true]
    UNLOCK --> OBJ_FLOW
```

---

### Profilo e Impostazioni

```mermaid
flowchart TD
    PROF[Profilo] --> LOAD{GET /settings}
    LOAD -->|200| CACHE_SAVE[Salva in SettingsDataStore]
    CACHE_SAVE --> FORM[Form: tema voti · soglie RGB]
    LOAD -->|fallito| CACHE_READ{Cache DataStore\ndisponibile?}
    CACHE_READ -->|Sì| FORM
    CACHE_READ -->|No — mai connesso| ERR_PROF[Error state\ndefault STANDARD/18/30]

    FORM --> SELECT[Dropdown: STANDARD · LODE · RGB]
    SELECT -->|RGB selezionato| SOGLIE[Campi soglia bassa/alta\nvalidazione 18-30]

    FORM --> SAVE[Tap Salva → isSaving=true\nform disabilitato]
    SAVE --> PUT{PUT /settings}
    PUT -->|200| CACHE_UPD[Aggiorna SettingsDataStore\nSuccess state]
    PUT -->|fallito| ERR_SAVE[Error state\nSnackbar + mantiene valori inseriti]

    PROF --> LOGOUT[Tap Esci]
    LOGOUT -->|POST /auth/logout| CLR[clearCookies\nDataStore = false]
    CLR --> LOGIN[LoginScreen]
```

---

### Sincronizzazione Background

```mermaid
flowchart TD
    WM[WorkManager\nSyncExamsWorker\nogni ora · NetworkType.CONNECTED] --> FETCH[getUnsyncedEsami\nWHERE pending_sync = 1]
    FETCH -->|lista vuota| SUCCESS[Result.success]
    FETCH -->|lista non vuota| LOOP[per ogni esame]

    LOOP --> HAS_REMOTE{remoteId\nnon null?}

    HAS_REMOTE -->|No — nuovo esame| POST[POST /exams]
    POST -->|200| MARK_NEW[markSynced\nremoteId assegnato]
    POST -->|fallito| ERR_W[hasError = true]

    HAS_REMOTE -->|Sì — esame modificato| PUT_W[PUT /exams/:remoteId]
    PUT_W -->|200| MARK_UPD[markSynced\npendingSync = false]
    PUT_W -->|404 — rimosso dal server| RESET[remoteId = null\nricreato al prossimo run]
    PUT_W -->|altro errore| ERR_W

    ERR_W --> RETRY[Result.retry]
    MARK_NEW & MARK_UPD & RESET --> NEXT[esame successivo]

    subgraph AUTH [TokenAuthenticator — OkHttp]
        T401[401 ricevuto] --> RFRSH[POST /auth/refresh]
        RFRSH -->|200| RETRY_REQ[retry richiesta originale\ntrasparente per l'app]
        RFRSH -->|fallito| SETOUT[setLoggedIn false\nUI → LoginScreen]
    end
```

---

## Schermate

| Tab | Schermata | Funzionalità principali |
|-----|-----------|------------------------|
| — | **Login** | Email + password, toggle visibilità, Snackbar errore server, link a Registrazione |
| — | **Registrazione** | Nome, cognome, email, password, Snackbar errore, link a Login |
| 1 | **Libretto** | Lista reattiva, sort DATA/VOTO/CFU × ASC/DESC, aggiunta/modifica/eliminazione esame con dialog, sync offline-first |
| 2 | **Statistiche** | Media ponderata, CFU totali, base laurea, grafico animato andamento voti + media progressiva |
| 3 | **Obiettivi** | 4 achievement con XP, stato completato/non completato aggiornato in tempo reale |
| 4 | **Profilo** | Tema voti (STANDARD/LODE/RGB), soglie RGB, salvataggio con cache offline, logout |

---

## Avvio del Progetto

### Prerequisiti

- Android Studio Meerkat o successivo
- JDK 11+
- Dispositivo o emulatore API 31+

### Build

```bash
git clone https://github.com/diegoandruccioli/studenthub-android.git

# Build completo + test
./gradlew clean build

# Solo test unitari
./gradlew test

# Installa su dispositivo/emulatore connesso
./gradlew installDebug
```

---

## Connessione al Backend

L'app punta a `http://10.0.2.2:3010/api/` (configurata in `NetworkClient.kt`).

### Emulatore Android Studio
1. Avvia il [backend StudentHub](https://github.com/diegoandruccioli/StudentHub) sulla porta `3010`
2. Avvia l'emulatore — nessuna modifica necessaria (`10.0.2.2` è il `localhost` dell'host)

### Dispositivo fisico
1. Computer e telefono sulla **stessa rete Wi-Fi**
2. Trova l'IP locale del computer (es. `192.168.1.x`)
3. In `data/src/main/java/com/unibo/android/data/remote/NetworkClient.kt` modifica:
   ```kotlin
   private const val BASE_URL = "http://<ip-del-tuo-computer>:3010/api/"
   ```

---

## Strategie di Persistenza

| Dato | Tecnologia | Comportamento offline |
|------|------------|----------------------|
| Esami | Room (SQLite) | Fonte di verità locale; sync via `pendingSync` flag + SyncWorker |
| Sessione utente | DataStore Preferences | `is_logged_in` flag persistito; cookie JWT persistito su SharedPreferences |
| Settings profilo | DataStore Preferences | Cache post GET/PUT; fallback automatico se server non raggiungibile |
| Cookie JWT | `PersistentCookieJar` (SharedPreferences) | Sopravvive ai riavvii; `TokenAuthenticator` gestisce il rinnovo su 401 |

---

## Conformità ai Requisiti del Corso

| Requisito | Stato |
|-----------|-------|
| Clean Architecture multi-modulo (domain / data / ui / app) | ✅ |
| MVVM — ViewModel unico state holder, no logica di business in View | ✅ |
| ViewModel sopravvive ai cambi di configurazione | ✅ |
| Use Cases nel layer domain — singola responsabilità | ✅ |
| Repository Pattern — unico punto di accesso ai dati | ✅ |
| Kotlin Coroutines — Main Thread mai bloccato | ✅ |
| Room — persistenza dati strutturati | ✅ |
| DataStore — preferenze chiave-valore (sessione + settings) | ✅ |
| Jetpack Compose con LazyColumn + `key` stabile | ✅ |
| Almeno 2 chiamate API remote | ✅ (10 endpoint implementati) |
| WorkManager — sync background offline-first | ✅ (opzionale 3) |
| Runtime Permissions | 🔲 (dichiarata, da implementare) |
| Relazione scritta | ✅ (`docs/relazione/relazione.tex`) |

---

## Test

38 test unitari distribuiti su `:domain` e `:ui`, zero failures.

```bash
./gradlew test
```

| Suite | Modulo | Test |
|-------|--------|------|
| `AuthUseCaseTest` | :domain | Login/register successo e fallimento |
| `GoalEvaluatorTest` | :domain | Tutti e 4 i GoalEvaluator con casi limite |
| `GetStatisticheUseCaseTest` | :domain | Media ponderata, CFU, baseLaurea, andamento |
| `LibrettoViewModelTest` | :ui | CRUD esami, sort, trigger obiettivi |
| `ProfiloViewModelTest` | :ui | Caricamento/salvataggio settings, logout |
| `StatisticheViewModelTest` | :ui | Empty state, Success, Error |

---

## Licenza

Progetto accademico — Università di Bologna, 2025/2026.
