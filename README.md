# StudentHub Android

> App Android ispirata al progetto StudentHub. Tieni traccia di esami, CFU e media con un sistema di gamification. Sviluppata con Kotlin, Clean Architecture (MVVM), Room, Coroutines e Flow. UniBO Sistemi Mobili 2025/26.

---

## Panoramica

**StudentHub** è un'applicazione Android nativa per la gestione della carriera universitaria. Gli studenti possono tracciare esami, CFU, media e progressi accademici; il sistema premia le attività completate (esami superati, obiettivi raggiunti) con un sistema di punti e ricompense.

Questo progetto è la versione mobile Android del progetto web StudentHub originale, sviluppato come progetto finale per il corso di **Sistemi Mobili** dell'Università di Bologna (2025/2026).

---

## Stack Tecnologico

| Layer | Tecnologia |
|-------|-----------|
| Linguaggio | Kotlin (100%) |
| Architettura | Clean Architecture — Multi-modulo |
| Pattern UI | MVVM (ViewModel + StateFlow/LiveData) |
| UI Toolkit | Views / XML + ViewBinding → Jetpack Compose (pianificato) |
| Async | Kotlin Coroutines + Flow |
| DB locale | Room (dati strutturati) |
| Preferenze | DataStore (Preferences) |
| Caricamento immagini | Glide |
| Min SDK | 31 (Android 12) |
| Target SDK | 36 |

---

## Architettura

Il progetto segue la **Clean Architecture** con separazione rigorosa dei moduli:

```
:app        → Entry point, Application class, DI manuale (RepositoryProvider)
:ui         → Activities, Fragments, ViewModels, Adapter, Custom Views
:domain     → Use Cases, interfacce Repository, Domain Models (Kotlin puro, no Android)
:data       → Implementazioni Repository, Room DAO/Entity, DataStore
```

**Regole di dipendenza tra moduli:**
```
:app  →  :ui, :domain, :data
:ui   →  :domain
:data →  :domain
:domain  →  (nessuna dipendenza interna)
```

**Flusso dei dati:**
```
View (Fragment/Activity)
  └── ViewModel  [:ui]
        └── UseCase  [:domain]
              └── interfaccia Repository  [:domain]
                    └── RepositoryImpl  [:data]
                          └── Room DAO / DataStore
```

---

## Funzionalità

- **Gestione esami** — aggiungi, modifica e visualizza i tuoi esami con voto e CFU
- **Dashboard accademica** — media, CFU totali, avanzamento verso la laurea
- **Gamification** — punti e ricompense per traguardi accademici completati
- **Persistenza locale** — tutti i dati salvati localmente con Room; preferenze tramite DataStore
- *(Pianificato)* Autenticazione e sincronizzazione con il backend StudentHub (Node.js / MySQL)

---

## Struttura del Progetto

```
studenthub-android/
├── app/
│   └── src/main/java/com/unibo/android/corsolp2526/
│       └── CustomApplication.kt
├── domain/
│   └── src/main/java/com/unibo/android/domain/
│       ├── models/
│       ├── repositories/
│       └── usecases/
├── data/
│   └── src/main/java/com/unibo/android/data/
│       ├── di/
│       └── repositories/
├── ui/
│   └── src/main/java/com/unibo/android/ui/
│       ├── adapters/
│       ├── customs/
│       ├── fragments/
│       ├── HomeActivity.kt
│       └── SplashActivity.kt
├── gradle/
│   └── libs.versions.toml
├── build.gradle.kts
└── settings.gradle.kts
```

---

## Avvio del Progetto

### Prerequisiti

- Android Studio Hedgehog o versione successiva
- JDK 11+
- Dispositivo Android o emulatore con API 31+

### Build e installazione

```bash
# Clona il repository
git clone https://github.com/diegoandruccioli/studenthub-android.git

# Apri in Android Studio e sincronizza Gradle, oppure compila da CLI:
./gradlew clean build

# Installa su dispositivo/emulatore connesso
./gradlew installDebug
```

### Esecuzione dei test

```bash
# Test unitari
./gradlew test

# Test strumentali (richiede dispositivo/emulatore connesso)
./gradlew connectedAndroidTest
```

---

## Connessione al Backend

L'app comunica con il [backend StudentHub](https://github.com/diegoandruccioli/StudentHub) (Node.js / MySQL). La configurazione cambia in base a dove gira l'app.

### Emulatore Android Studio

L'emulatore gira sulla stessa macchina del backend. Gli emulatori Android mappano il `localhost` dell'host sulla costante `10.0.2.2`.

1. Avvia il backend sul tuo computer (porta predefinita `3010`).
2. Avvia l'emulatore da Android Studio.
3. L'app è già configurata per raggiungere `http://10.0.2.2:3010/api/` — nessuna modifica necessaria.

### Dispositivo Fisico Android

Il telefono e il computer che esegue il backend devono essere sulla **stessa rete Wi-Fi**.

1. Trova l'indirizzo IP locale del tuo computer (es. `192.168.1.x`).
2. Avvia il backend sul tuo computer (porta `3010`).
3. Apri il file `data/src/main/java/com/unibo/android/data/remote/NetworkClient.kt` e modifica la costante `BASE_URL` alla riga 13:
   ```kotlin
   private const val BASE_URL = "http://<ip-del-tuo-computer>:3010/api/"
   ```
4. Connetti il telefono alla stessa rete Wi-Fi, poi avvia l'app.

> **Nota:** `localhost` e `10.0.2.2` non funzionano su dispositivo fisico — serve l'IP della rete locale.

---

## Conformità ai Requisiti del Corso

Questo progetto soddisfa tutti i requisiti obbligatori per l'esame UniBO Sistemi Mobili:

- [x] Clean Architecture multi-modulo (domain / data / ui / app)
- [x] MVVM — ViewModel come unico state holder, nessuna logica di business nella View
- [x] ViewModel + StateFlow/LiveData — sopravvive ai cambiamenti di configurazione
- [x] Use Cases nel layer domain — singola responsabilità
- [x] Repository Pattern — unico punto di accesso ai dati
- [x] Kotlin Coroutines — separazione rigorosa Main/Background thread
- [x] Room — persistenza locale dati strutturati
- [x] DataStore — preferenze chiave-valore
- [x] ViewBinding — nessun `findViewById`
- [x] RecyclerView con Adapter/ViewHolder
- [x] Gestione Runtime Permissions

---

## Licenza

Progetto accademico — Università di Bologna, 2025/2026.
