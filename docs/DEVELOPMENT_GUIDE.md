# StudentHub Android — Guida allo sviluppo

> Documento condiviso per il team. Aggiorna lo stato di ogni sezione non appena la feature è completata.

---

## Stato avanzamento

### Backend (prerequisiti — da completare prima dello sviluppo mobile)

| # | Task backend | Responsabile | Stato |
|---|---|---|---|
| B1 | Migration `updated_at` su tabella `esami` | — | ✅ Completato |
| B2 | ~~Fix CORS per richieste native Android~~ | — | N/A — CORS non si applica a Retrofit/OkHttp |

### Android

| # | Feature | Responsabile | Stato |
|---|---|---|---|
| 1 | Login + Registrazione | — | ⬜ Da fare |
| 2 | Lista esami (RecyclerView + Room) | — | ⬜ Da fare |
| 3 | Aggiungi / Modifica esame | — | ⬜ Da fare |
| 4 | Statistiche | — | ⬜ Da fare |
| 5 | Gamification (livello, badge, leaderboard) | — | ⬜ Da fare |
| 6 | Impostazioni | — | ⬜ Da fare |
| 7 | Network layer (Retrofit + CookieJar) | — | ⬜ Da fare |
| 8 | Offline-first sync (WorkManager `SyncExamsWorker`) | — | ⬜ Da fare |
| 9 | Notifiche rank + permesso runtime (WorkManager `LeaderboardCheckWorker`) | — | ⬜ Da fare |

**Legenda**: ⬜ Da fare · 🔄 In corso · ✅ Completato

---

## Fase 0 — Prerequisiti backend (da completare prima di iniziare lo sviluppo Android)

Queste due modifiche devono essere applicate al repo `github.com/diegoandruccioli/StudentHub`
prima di procedere con l'integrazione network sull'app mobile.

### B1 — Migration: aggiunta colonna `updated_at` su `esami`

**Perché**: senza un timestamp di ultima modifica non è possibile applicare la strategia
last-write-wins per la risoluzione dei conflitti offline/online (es. stesso esame modificato
dal telefono offline e dalla piattaforma web prima della sincronizzazione).

**Impatto sulla piattaforma web**: nessuno — tutte le query esistenti usano colonne esplicite;
`updated_at` si popola e si aggiorna automaticamente via MySQL senza modifiche al codice.

```sql
ALTER TABLE esami
ADD COLUMN updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
AFTER created_at;
```

Aggiornare anche `backend/sql/init.sql` per mantenere lo script di inizializzazione allineato:

```sql
-- nella CREATE TABLE esami, aggiungere dopo created_at:
updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
```

---

### B2 — Fix CORS ~~per richieste native Android~~ — NON necessario

**CORS è un meccanismo browser-only.** Retrofit/OkHttp non invia l'header `Origin` e non controlla
gli header `Access-Control-*` nella risposta. Il backend risponde normalmente alle richieste Android
senza alcuna modifica. Il `CORS_ORIGIN` nel `.env` serve solo al frontend web.

---

## Problema critico: autenticazione JWT via cookie

### Come funziona il backend

Il backend (`github.com/diegoandruccioli/StudentHub`) **non restituisce il JWT nel body** della risposta. Lo invia tramite un **cookie HTTP-only**:

```
Set-Cookie: token=<jwt_value>; HttpOnly; Secure; SameSite=Strict; Max-Age=2592000
```

Un browser gestisce questo in modo trasparente. Un'app Android **non lo fa automaticamente**: se non configurata, ogni richiesta dopo il login arriva senza il cookie e il server risponde `401 Non autenticato`.

### Perché è un problema per Android

- Android non ha un "browser cookie store" nativo disponibile per Retrofit/OkHttp
- Il cookie deve essere estratto dall'header `Set-Cookie` dopo il login e reinviato come `Cookie: token=<valore>` su ogni richiesta successiva
- Il cookie dura **30 giorni** — va persistito su disco (DataStore) per sopravvivere alla chiusura dell'app
- **Non esiste un endpoint `/refresh`** nel backend: alla scadenza l'utente deve rifare il login

### Soluzione: OkHttp CookieJar + DataStore

La soluzione corretta è implementare un `CookieJar` custom in OkHttp che:

1. **Salva** il cookie `token` in DataStore (Preferences) quando arriva da login/register
2. **Reinvia** il cookie `token` come header su ogni richiesta autenticata
3. **Cancella** il cookie da DataStore al logout

```
POST /api/auth/login
    ↓
Response header: Set-Cookie: token=abc123...
    ↓
CookieJar salva "abc123..." in DataStore
    ↓
GET /api/exams (richiesta successiva)
    ↑
CookieJar inietta: Cookie: token=abc123...
```

### Struttura dei layer coinvolti

```
:data
  └── datasource/
        └── remote/
              ├── AuthRemoteDataSource.kt     ← chiama login/register, delega il cookie
              └── CookieManager.kt            ← CookieJar custom + DataStore persistence
  └── datasource/
        └── local/
              └── SessionDataStore.kt         ← salva/legge il token da DataStore
```

---

## Indirizzi IP backend — riferimento emulatore

Il backend Docker è esposto sulla porta **3010** dell'host. L'emulatore Android raggiunge la
macchina host all'indirizzo `10.0.2.2`.

| Contesto | URL base da usare nell'app Android |
|---|---|
| Emulatore Android Studio | `http://10.0.2.2:3010` |
| Dispositivo fisico (stessa rete Wi-Fi) | `http://<IP_LOCALE_PC>:3010` (es. `192.168.1.x`) |
| Produzione | TBD — URL pubblico del server |

Android 9+ blocca HTTP in chiaro per default. Il file `app/src/main/res/xml/network_security_config.xml`
già presente nel progetto consente il traffico HTTP verso `10.0.2.2`.

Configura l'URL base in `local.properties` o tramite `BuildConfig` — mai hardcoded nel codice.

---

## Ordine di sviluppo consigliato

### Fase 1 — Core locale (senza backend)

Implementa tutto con Room. L'app funziona offline; il backend si aggiunge dopo.

**1. Login / Registrazione**
- Schermate XML + ViewBinding
- ViewModel + StateFlow
- DataStore per persistere lo stato di sessione (isLoggedIn, userId)
- Use Case: `LoginUseCase`, `RegisterUseCase`
- Requisiti corso coperti: DataStore, ViewModel, Coroutines

**2. Lista esami**
- RecyclerView + DiffUtil
- Room Entity `EsameEntity`, DAO `EsameDao`
- Repository: `EsameRepository` (interfaccia domain) + `EsameRepositoryImpl` (data)
- ViewModel + StateFlow che osserva `Flow<List<Esame>>`
- Requisiti corso coperti: RecyclerView, Room, Flow reattivo, ViewModel

**3. Aggiungi / Modifica esame**
- Form con validazione (voto 18–30, CFU > 0, data obbligatoria)
- Use Case: `AddEsameUseCase`, `UpdateEsameUseCase`, `DeleteEsameUseCase`
- Calcolo XP locale: `xp = voto * cfu + (lode ? 50 : 0)`
- Requisiti corso coperti: Use Cases a responsabilità singola, SOLID

### Fase 2 — Schermate derivate

**4. Statistiche**
- Use Case: `GetStatisticheUseCase` — calcola media aritmetica, ponderata, base laurea, CFU totali
- Grafico andamento voti (MPAndroidChart o Canvas custom)
- UI reattiva: si aggiorna automaticamente quando cambiano i dati Room

**5. Gamification**
- Livelli XP hardcoded (stessa logica del backend)
- Badge: valutati localmente con le stesse 4 regole del backend
- Leaderboard: solo quando il network layer è attivo (Fase 3)

**6. Impostazioni**
- DataStore: `tema_voti` (DEFAULT / RGB), `rgb_soglia_bassa`, `rgb_soglia_alta`
- Sincronizzazione con backend al login (GET /api/settings)

### Fase 3 — Integrazione backend

Questa fase si aggiunge **sopra** la Fase 1 senza romperla, grazie al pattern Repository.
**Prerequisiti backend B1 e B2 devono essere completati prima di iniziare questa fase.**

**7. Network layer**
- Aggiungere Retrofit 2 + OkHttp al modulo `:data`
- Implementare `CookieJar` custom + `SessionDataStore`
- Creare `AuthApiService`, `EsameApiService`, `GamificationApiService` (interfacce Retrofit)
- `EsameRepositoryImpl`: strategia offline-first (vedi sotto)

**8. Offline-first sync con WorkManager**

Strategia: Room è sempre la fonte di verità locale. Il backend è la fonte di verità remota.

*Inserimento / modifica esame:*
```
Utente salva esame
    ↓
Salva in Room con flag pending_sync = true
    ↓
Rete disponibile?  ──Sì──→  POST/PUT /api/exams → ricevi risposta con xp aggiornato
    │                            ↓
    │                       Aggiorna Room con dati server (xp_guadagnati, updated_at)
    │                       Imposta pending_sync = false
    │
    └──No──→  Enqueue SyncExamsWorker con constraint NetworkType.CONNECTED
                    ↓ (appena torna la rete)
              Per ogni record con pending_sync = true:
                  Confronta updated_at locale vs remoto (last-write-wins)
                  Invia la versione più recente al server
                  Aggiorna Room con la risposta
```

*Campi aggiuntivi necessari su `EsameEntity` (Room):*
- `pendingSync: Boolean = false` — segna i record da sincronizzare
- `updatedAt: Long` — epoch ms, usato per last-write-wins contro `updated_at` del server

*Worker:*
```kotlin
// :data/worker/SyncExamsWorker.kt
class SyncExamsWorker(ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params) {
    // Legge da Room tutti i record con pendingSync = true
    // Per ognuno: POST o PUT verso /api/exams con gestione last-write-wins
    // In caso di successo: aggiorna il record in Room e imposta pendingSync = false
}
```

**9. Notifiche rank gamification con WorkManager**

*Flusso:*
```
LeaderboardCheckWorker (periodico, solo con rete)
    ↓
GET /api/users/leaderboard
    ↓
Confronta rank ricevuto con rank salvato in DataStore
    ↓
Rank cambiato? → Emetti notifica locale ("Sei salito al posto N°X!")
               → Aggiorna rank in DataStore
```

*Permesso runtime (Android 13+):* `POST_NOTIFICATIONS` — va richiesto al primo avvio
dell'app, spiegando all'utente il motivo (aggiornamenti sulla classifica gamification).

*Worker:*
```kotlin
// :data/worker/LeaderboardCheckWorker.kt
class LeaderboardCheckWorker(ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params) {
    // Constraints: NetworkType.CONNECTED
    // Periodicità: ogni 15 minuti (minimo di WorkManager)
    // Confronta rank con DataStore, emette NotificationCompat se cambiato
}
```

*Requisiti esame coperti da questa fase:*
- **Opzionale 1** (persistenza locale Room) ✅ — già dalla Fase 1
- **Opzionale 2** (runtime permission) ✅ — permesso `POST_NOTIFICATIONS`
- **Opzionale 3** (servizi in background) ✅ — `SyncExamsWorker` + `LeaderboardCheckWorker`
- **Requisito obbligatorio networking** ✅ — almeno 2 chiamate API remote

---

## Architettura di riferimento

```
:app        → CustomApplication, RepositoryProvider (DI manuale)
:ui         → Fragment/Activity, ViewModel, Adapter, ViewHolder
:domain     → Use Cases, Repository interfaces, Domain Models
:data       → RepositoryImpl, Room (DAO + Entity), DataStore, Retrofit services
```

### Flusso dati

```
Fragment → ViewModel.stateFlow.collect()
               ↑
           ViewModel → UseCase.invoke()
                            ↑
                       Repository (interface, domain)
                            ↑
                       RepositoryImpl (data)
                         ├── RoomDAO        (locale)
                         └── RetrofitService (remoto, Fase 3)
```

---

## Regole tecniche da rispettare (requisiti esame)

- **Nessun Java** — solo Kotlin
- **Nessun `findViewById`** — solo ViewBinding
- **Main thread mai bloccato** — `viewModelScope + Dispatchers.IO` per Room/network
- **`MutableStateFlow` mai esposto** — il ViewModel espone solo `StateFlow` (val, non var)
- **RecyclerView sempre con `DiffUtil`**
- **Room Entity mai oltre il layer `:data`** — usa domain model (`Esame`, non `EsameEntity`)
- **Use Case = 1 classe, 1 responsabilità** — costruttore riceve solo il repository necessario
- **MVVM rigoroso** — il ViewModel non contiene logica di business; la View (Fragment/Activity) non contiene logica di stato; tutto il binding UI passa da `StateFlow.collect()`
- **Repository pulito** — Use Case e ViewModel non toccano mai direttamente Room, Retrofit o DataStore; accedono solo all'interfaccia Repository definita nel modulo `:domain`
- **Material You (M3)** — usare componenti Material Design 3; applicare Dynamic Color in modo che i colori dell'app si adattino al tema del dispositivo
- **Unit test obbligatori** — JUnit 4 + Mockito per ogni Use Case e ViewModel; i test vanno nel modulo corretto (`:domain/test`, `:ui/test`); senza test il codice non è considerato completo

---

## API Backend — riferimento rapido

Base URL: `http://10.0.2.2:3010/api` (emulatore) · tutte le risposte sono JSON in italiano

| Endpoint | Metodo | Auth | Note |
|---|---|---|---|
| `/auth/login` | POST | No | `{email, password}` |
| `/auth/register` | POST | No | `{nome, cognome, email, password}` |
| `/auth/logout` | POST | No | cancella cookie |
| `/exams` | GET | Cookie | query: sortBy, order, year |
| `/exams` | POST | Cookie | **body = array** `[{nome,voto,cfu,lode,data}]` |
| `/exams/:id` | PUT | Cookie | aggiorna + ricalcola XP |
| `/exams/:id` | DELETE | Cookie | ricalcola XP e badge |
| `/stats` | GET | Cookie | medie, base laurea, CFU |
| `/gamification/status` | GET | Cookie | livello corrente, xp, % progresso |
| `/gamification/my-badges` | GET | Cookie | badge sbloccati |
| `/gamification/badges` | GET | Cookie | catalogo completo |
| `/settings` | GET | Cookie | tema, soglie RGB |
| `/settings` | PUT | Cookie | aggiorna impostazioni |
| `/users/leaderboard` | GET | Cookie | top 50 + rank utente corrente |
