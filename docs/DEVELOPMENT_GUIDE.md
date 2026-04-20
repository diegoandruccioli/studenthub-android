# StudentHub Android — Guida allo sviluppo

> Documento condiviso per il team. Aggiorna lo stato di ogni sezione non appena la feature è completata.

---

## Stato avanzamento

| # | Feature | Responsabile | Stato |
|---|---|---|---|
| 1 | Login + Registrazione | — | ⬜ Da fare |
| 2 | Lista esami (RecyclerView + Room) | — | ⬜ Da fare |
| 3 | Aggiungi / Modifica esame | — | ⬜ Da fare |
| 4 | Statistiche | — | ⬜ Da fare |
| 5 | Gamification (livello, badge, leaderboard) | — | ⬜ Da fare |
| 6 | Impostazioni | — | ⬜ Da fare |
| 7 | Network layer (Retrofit + CookieJar) | — | ⬜ Da fare |
| 8 | Modifica CORS backend | — | ⬜ Da fare |

**Legenda**: ⬜ Da fare · 🔄 In corso · ✅ Completato

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

## Problema critico: CORS backend

### Situazione attuale

Il backend ha il CORS configurato in modo fisso per il frontend web:

```typescript
// backend/server.ts — STATO ATTUALE (da modificare)
app.use(cors({
  origin: 'http://localhost:5173',
  credentials: true
}));
```

Questo blocca qualsiasi richiesta che non provenga esattamente da `http://localhost:5173`. Un'app Android non invia un `Origin` uguale a quello, quindi in ambiente di sviluppo locale (emulatore o dispositivo fisico sulla stessa rete) le richieste vengono rifiutate.

### Modifica necessaria nel backend

Chi lavora sul backend deve modificare `backend/server.ts` prima che l'integrazione Android possa funzionare:

```typescript
// backend/server.ts — DA APPLICARE
const allowedOrigins = [
  'http://localhost:5173',   // frontend web
  'http://10.0.2.2:3000',   // emulatore Android (mappa a localhost della macchina host)
];

app.use(cors({
  origin: (origin, callback) => {
    // origin è undefined per richieste native (app mobile) — le lasciamo passare
    if (!origin || allowedOrigins.includes(origin)) {
      callback(null, true);
    } else {
      callback(new Error('CORS non permesso'));
    }
  },
  credentials: true
}));
```

> **Nota**: le app native Android non inviano sempre un header `Origin`. OkHttp non lo aggiunge di default. Per questo il controllo `!origin` è necessario.

### Indirizzi IP da sapere

| Contesto | Indirizzo da usare nell'app Android |
|---|---|
| Emulatore Android Studio | `http://10.0.2.2:3000` |
| Dispositivo fisico (stessa rete Wi-Fi) | `http://<IP_LOCALE_PC>:3000` (es. `192.168.1.x`) |
| Produzione | TBD — URL pubblico del server |

Configura l'URL base in un file `local.properties` o in una costante di build (`BuildConfig`) — mai hardcoded nel codice.

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

**7. Network layer**
- Aggiungere Retrofit 2 + OkHttp al modulo `:data`
- Implementare `CookieJar` custom + `SessionDataStore`
- Creare `AuthApiService`, `EsameApiService`, ecc. (interfacce Retrofit)
- Il `EsameRepositoryImpl` diventa: prova remote → fallback locale (o viceversa)

**8. Modifica CORS backend** (prerequisito della Fase 3)
- Vedi sezione sopra

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

---

## API Backend — riferimento rapido

Base URL: `http://10.0.2.2:3000/api` (emulatore) · tutte le risposte sono JSON in italiano

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
