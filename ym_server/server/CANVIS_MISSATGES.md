# Canvis als missatges del servidor

> **Savepoint** del refactor de missatges fet per la Sara.
> Llegir abans de fer pull per saber què espera el servidor i què ha canviat.

## Índex

1. [Resum executiu](#resum-executiu)
2. [Missatges nous](#missatges-nous)
3. [Missatges modificats](#missatges-modificats)
4. [Missatges eliminats](#missatges-eliminats)
5. [Canvis a estats i `GameInstance`](#canvis-a-estats-i-gameinstance)
6. [Sistema de loot (NOU)](#sistema-de-loot-nou)
7. [Convencions adoptades](#convencions-adoptades)
8. [Pendents](#pendents)

---

## Resum executiu

- **Sistema "Ready" eliminat:** ja no hi ha missatge `SetReady_IN`. Quan tots els jugadors tenen un personatge assignat, la partida comença automàticament.
- **Voting phase eliminada:** tots els missatges de votació i l'estat `StateVotingPhase` desapareixen. La tria del següent node la decideix el primer jugador que clica.
- **Sistema de menús eliminat:** els menús són responsabilitat del client (settings, deck, inventari...). Si el servidor necessita enviar dades fresques per a un menú concret, es crearà un missatge específic (`Request*_IN` / `*Snapshot_OUT`).
- **Bioma per pis (no per sala):** cada `FloorInfo` porta un sol `biomeName`. El `RoomInfo` ja no té bioma; el dedueix del seu pis.
- **`playerId` és sempre `long`** als missatges (acord amb el Xuban).
- **Missatges nous** per cobrir els estats que tenim però encara no estan implementats: `combatrewards/`, `endgame/`, `OpenChest_IN`, `ChestOpened_OUT`, `PickItem_IN`, `ItemPicked_OUT`, `PlayerFinishedRest_OUT`, `EnemyAction_OUT`, `PlayerDisconnected_OUT`.

---

## Missatges nous

### `combat/out/EnemyAction_OUT.java`

Acció executada per un enemic durant el combat. Camps: `enemyId` (int), `actionId` (int, FK a taula `ACT`), `targetPlayerId` (int).

### Carpeta nova `combatrewards/`

Repartiment de recompenses després d'un combat (estat `StateCombatRewards`).

- `combatrewards/out/CombatRewards_OUT.java` — XP, monedes i loot per a cada jugador.
- `combatrewards/in/ClaimReward_IN.java` — confirmació individual del jugador.
- `combatrewards/out/AllRewardsClaimed_OUT.java` — broadcast quan tothom ha confirmat → transició al següent estat.

### Carpeta nova `endgame/`

Final de partida (estat `StateEndGame`).

- `endgame/out/GameSummary_OUT.java` — resum de la partida (`victory`, `floorsCleared`, `totalDamageDealt`, `totalCoins`).
- `endgame/in/ReturnToLobby_IN.java` — confirmació del jugador per tornar al lobby.

### Sales (`rooms/`)

- `rooms/in/OpenChest_IN.java` — petició d'obrir un cofre.
- `rooms/out/ChestOpened_OUT.java` — broadcast amb el contingut disponible (`openedByPlayerId`, `availableItemIds`).
- `rooms/in/PickItem_IN.java` — petició d'agafar un objecte concret.
- `rooms/out/ItemPicked_OUT.java` — broadcast confirmant qui s'ha quedat què.
- `rooms/out/PlayerFinishedRest_OUT.java` — un jugador ha acabat el descans (per a `StateRest`).

### `generic/out/PlayerDisconnected_OUT.java`

Broadcast quan un jugador es desconnecta. Per ara s'utilitzarà per acabar la partida (no hi ha reconnexió).

---

## Missatges modificats

### `mapexploration/out/MapUpdate_OUT.java`

Abans estava buit. **Ara porta `public List<FloorInfo> floors`** i un constructor `MapUpdate_OUT(List<FloorInfo> floors)`. S'envia en entrar a `StateMapVisualization` amb el mapa generat.

### `mapexploration/minimodels/FloorInfo.java`

- Tots els camps a `public` (perquè Jackson els serialitzi).
- Camp i paràmetre del constructor renomenats: `biome_name` / `biome_path` → **`biomeName`**.
- Constructor: `FloorInfo(int num, String name, String biomeName, List<RoomInfo> rooms)`.

### `mapexploration/minimodels/RoomInfo.java`

- **No** porta `biomeName` (el bioma és per pis, no per sala).
- Constructor de 4 paràmetres assigna sempre `is_current = false` (els jugadors tenen múltiples punts d'entrada al mapa, ningú comença "actiu").

### `characterselection/in/PickCharacter_IN.java`

- Renomenat des de `PickCharacterMessage_IN.java` (treta la paraula "Message" perquè quadri amb la convenció de la resta).
- Camp `playerId` ara és **`long`** (era `int`).
- Camp `characterId` queda com a `int`.

### `characterselection/minimodels/PlayerInfo.java`

- **Eliminat el camp `isReady`** (ja no hi ha sistema Ready).
- `id` ja era `long` — sense canvis.

### `characterselection/minimodels/CharacterInfo.java`

- `selectedPlayerId` ara és **`long`** (era `int`) i té **valor per defecte `-1`** ("cap jugador no l'ha agafat"). Abans era `0`, que xocava amb `playerId == 0`.

### `mapexploration/in/MoveToNode_IN.java`

Abans estava buit. **Ara té camps:**
- `playerId` (long) — qui fa la petició.
- `floorNum` (int) — pis on vol moure's el grup.
- `roomId` (int) — node concret triat dins del pis.

### `generic/out/PlayerDisconnected_OUT.java`

`playerId` és `long` (era `int` quan es va crear).

### `generic/minimodels/CharacterModification.java`

Camps tots `public` (abans eren package-private i Jackson no els serialitzava). Constructor sense arguments afegit.

---

## Missatges eliminats

### Carpeta `votingphase/` sencera

S'ha eliminat tot el sistema de votació:
- `votingphase/in/SubmitVote_IN.java`
- `votingphase/out/VoteUpdate_OUT.java`
- `votingphase/out/VotingResult_OUT.java`
- `votingphase/out/StartVoting_OUT.java`

També l'estat `api/states/StateVotingPhase.java`.

**Motiu:** el primer jugador en clicar un node decideix on va el grup. No cal votació.

### Sistema "Ready" (`SetReady_IN`)

- `characterselection/in/SetReady_IN.java` — eliminat.
- Camp `PlayerInfo.isReady` — eliminat.
- Mètode `handleSetReady` de `StateCharacterSelection` — eliminat.

**Motiu:** "ready" és redundant amb "tinc un personatge". Quan tots tenen personatge, comença la partida.

### Sistema de menú genèric (Opció A)

S'han eliminat tots els missatges genèrics de menú:
- `generic/in/OpenMenu_IN.java`
- `generic/in/CloseMenu_IN.java`
- `generic/out/Open_Menu_OUT.java`
- `generic/out/OpenedMenu_OUT.java`
- `generic/out/ClosedMenu_OUT.java`

També els mètodes orfes `open_menu(JsonNode)` i `close_menu(JsonNode)` del `GameInstance`.

**Motiu:** no els fèiem servir, i la majoria de menús són purament client (settings, deck preview, inventari...). Si en algun moment cal una pantalla amb dades fresques del servidor, crearem un missatge `Request*_IN` / `*Snapshot_OUT` específic per a aquell cas.

### `HostStartGame_IN.java`

Estava comentat des de fa temps amb la nota "NO ES FARÀ SERVIR AQUESTA CLASSE". Eliminat per netedat.

---

## Canvis a estats i `GameInstance`

### `StateCharacterSelection`

- Eliminat `import SetReady_IN`.
- `tick()` només processa `PickCharacter_IN`.
- `handlePickCharacter` ja no té el bloc `isPlayerReady` que bloquejava canviar de personatge.
- `checkAllReadyAndStart()` renomenat a **`checkAllPickedAndStart()`** amb nova lògica: per a cada jugador, comprova que existeix algun `CharacterInfo` amb `isSelected=true && selectedPlayerId == player.id`.
- Es crida `checkAllPickedAndStart()` al final de cada `handlePickCharacter` exitós.
- Quan tots tenen personatge: broadcast de `GameStart_OUT` i transició a **`StateMapCreation`** (no a `StateMapVisualization` directament).

### `StateMapCreation` (nou flux)

- Estat de transició curt. El constructor genera el mapa i fa la transició immediata a `StateMapVisualization`.
- Crida al servei: `mapGenerationService.generateFloor(i, numFloors, columnsAquestPis)` — sense paràmetre de bioma (el calcula el servei).
- L'últim pis té `numColumns = 1` (només el boss); la resta tenen `numColumnes = 4`.
- Persisteix les `floors` al `GameInstance` amb `game.setFloors(floors)`.

### `StateMapVisualization`

- Al constructor: llegeix `game.getFloors()` i fa broadcast de `MapUpdate_OUT(floors)`.
- `tick()` preparat amb try/catch però sense lògica encara (TODO `MoveToNode_IN`).

### `MapGenerationService` / `IMapGenerationService`

- Signatures noves: `generateFloor(floorNum, totalFloors, numColumns)` i `generateTypeFloor(floorNum, totalFloors, numColumns, type)`.
- Eliminat `generateRandomFloor` (era duplicat).
- Cada `generateFloor` crea **una sola fila** de `numColumns` `RoomInfo` (un pis = una fila a Slay-the-Spire).
- `determineRoomType(floorNum, totalFloors)`: pis 0 → tot `COMBAT`; últim → tot `BOSS_COMBAT`; intermedis → mixt amb probabilitats.
- `determineBiome(floorNum, totalFloors)`: distribueix els 5 biomes (`numbness`, `lightSleep`, `deepSleep`, `remSleep`, `nightmare`) proporcionalment.

### `GameInstance`

Camps nous:
- `private FloorInfo[] floors` — mapa generat per `StateMapCreation`.
- `private volatile int currentFloor = -1` — pis on és el grup.
- `private volatile int currentRoomId = -1` — sala dins del pis.

Getters/setters per als tres camps.

Mètodes orfes eliminats:
- Bloc `MÈTODES DE FASE DE VOTACIÓ` (`submit_vote`, `enviarVoteUpdate`, `enviarVotingResult`).
- `open_menu(JsonNode)` i `close_menu(JsonNode)`.

---

## Sistema de descans / `StateRest` (NOU)

### Flux complet

1. Entrada a l'estat → broadcast **`RestInfo_OUT(campfireLit=false)`**.
2. Algun jugador clica la foguera → IN **`LightCampfire_IN { playerId }`**.
3. Servidor:
   - Valida que la foguera no estigui ja encesa.
   - Aplica curació: HP +30%, MP +50% (capats al màxim).
   - Per cada jugador que realment canvia stats → broadcast **`CharacterUpdate_OUT(CharacterModification)`**.
   - Si un jugador ja estava al màxim → no s'envia missatge per a ell.
   - Marca la foguera com encesa.
   - Broadcast **`CampfireLit_OUT { ignitedByPlayerId }`** (els clients mostren botó "sortir").
4. Cada jugador clica "sortir" → IN **`ReadyToLeave_IN { playerId }`**.
   - Broadcast **`PlayersReadyToLeave_OUT { readyPlayerIds }`** amb la llista actualitzada.
5. Quan tots han enviat `ReadyToLeave_IN` → broadcast **`ReturnToMap_OUT`** i transició a `StateMapVisualization`.

### DTO i model nous

- **`api/models/PlayerCharacterState.java`** — estat de joc d'un personatge durant la partida. Camps: `playerId` (long), `characterId` (int), `currentHp/maxHp`, `currentMp/maxMp`, `level`, `exp`, `atk`, `def`. Mètodes `healHp(int)` i `healMp(int)` que capen al màxim.

### Missatges nous

- **`rooms/in/LightCampfire_IN`** — `playerId` (long).
- **`rooms/in/ReadyToLeave_IN`** — `playerId` (long). Substitueix l'antic `LeaveRoom_IN`.
- **`rooms/out/CampfireLit_OUT`** — `ignitedByPlayerId` (long).
- **`rooms/out/PlayersReadyToLeave_OUT`** — `readyPlayerIds` (List<Long>). Substitueix l'antic `PlayerFinishedRest_OUT`.
- **`rooms/out/ReturnToMap_OUT`** — sense camps. Broadcast just abans de transicionar al mapa.
- **`generic/out/CharacterUpdate_OUT`** — embolica un `CharacterModification`. Broadcast genèric per a canvis de stats (descans, combat, level up...).

### Missatges modificats

- **`mapexploration/out/RestInfo_OUT`** — afegit camp `boolean campfireLit` i constructor.

### Missatges eliminats

- **`rooms/in/LeaveRoom_IN.java`** — substituït per `ReadyToLeave_IN` (nom més precís: "estic llest per sortir" enlloc de "vull sortir ja").
- **`rooms/out/PlayerFinishedRest_OUT.java`** — substituït per `PlayersReadyToLeave_OUT` (un broadcast amb tota la llista, no un per jugador).

### Models modificats

- **`db/model/Yumedant.java`** — afegits els getters/setters que faltaven: `getDescription/getLevel/getHp/getMp/getAtk/getDef/getImage` (abans només `getId`, `getName`, `getActs`).

### Canvis a `GameInstance`

- Afegit camp `Map<Long, PlayerCharacterState> playerStates` (ConcurrentHashMap) amb getters `getPlayerStates()`, `getPlayerState(long)` i setter `putPlayerState(long, PlayerCharacterState)`.
- Eliminat el mètode orfe `leave_room(JsonNode)`.

### Canvis a `StateCharacterSelection`

- Nou mètode `initializePlayerStates()` que es crida des de `checkAllPickedAndStart()` just abans de la transició a `StateMapCreation`. Per cada jugador llegeix el `YuCharacter` triat (via `RepoYuCharacter`) i crea el seu `PlayerCharacterState` inicial (HP/MP al màxim, exp a 0). Es desa al `GameInstance.playerStates`.

### `StateRest` implementat

Implementació completa segons el flux descrit. Punts clau:
- Constants `HP_HEAL_RATIO = 0.30` i `MP_HEAL_RATIO = 0.50` com a `private static final`. TODO documentat: portar a `config.properties` si volem balanceig sense recompilar.
- Lògica de curació directa a l'estat (mètode privat `handleLightCampfire`). Si en algun moment la reutilitzem (combat amb pocions, etc.), és fàcil extreure-la a un `HealingService`.
- Validacions: el jugador és qui diu ser; la foguera no està ja encesa; cada `ReadyToLeave_IN` només compta un cop per jugador; missatges no esperats simplement es logguen.

### Decisions documentades del sistema rest

| # | Decisió |
|---|---|
| Q1 | Curació per acció col·lectiva: el primer que clica la foguera cura tot el grup. |
| Q2 | +30% HP, variable per paràmetre (`HP_HEAL_RATIO`). |
| Q3 | També MP (+50%, percentatge diferent). |
| Q4 | `Map<Long, PlayerCharacterState>` al `GameInstance`. |
| Q5 | `CharacterUpdate_OUT` genèric que embolica un `CharacterModification`. |
| Q6 | Transició quan tots envien `ReadyToLeave_IN` (cada un al seu ritme). |
| Q7 | `PlayersReadyToLeave_OUT` per veure qui ja ha confirmat. |
| Q8 | Jugador al màxim de HP/MP: no es cura, no s'envia update. |
| Q9 | Missatges no esperats: log warning, sense respondre. |

---

## `StateMapVisualization` — implementació de `MoveToNode_IN` (NOU)

### Flux complet

1. **Entrada a l'estat**:
   - Si tornem d'una sub-state (`currentRoomId >= 0`), marquem la sala visitada com `is_cleared=true`.
   - Broadcast `MapUpdate_OUT { floors, currentFloor, currentRoomId }`.
2. **`tick()` espera `MoveToNode_IN`**:
   - Valida identitat del jugador.
   - Valida pis destí (vegeu adjacència).
   - Valida room destí (vegeu adjacència).
3. **Si vàlid**:
   - Actualitza `game.setCurrentFloor()` i `game.setCurrentRoomId()`.
   - Envia `ActionResult_OUT(true, OK)` individual al jugador.
   - Broadcast `MapUpdate_OUT` amb la nova posició (**feedback immediat** abans de la transició).
   - Transició a la sub-state segons el `RoomType`.

### Regla d'adjacència

```
isValidFloor:
  - Primera entrada (curFloor == -1): targetFloor == 0
  - Després: targetFloor == curFloor + 1, dins de rang

isValidRoom (assumeix isValidFloor OK):
  - targetRoomId dins de [0, numColumnsDest - 1]
  - Primera entrada: qualsevol roomId
  - Pis destí amb 1 node (boss): qualsevol previ hi pot anar
  - Adjacència estàndard: |targetRoomId - curRoom| <= 1
```

### Mapping RoomType → estat

| RoomType | Sub-state |
|---|---|
| `COMBAT` | `new StateCombat(..., isBoss=false)` |
| `BOSS_COMBAT` | `new StateCombat(..., isBoss=true)` |
| `REST_EVENT` | `new StateRest(...)` |
| `CHEST_EVENT` | `new StateChest(...)` |

### Race condition

Si arriben múltiples `MoveToNode_IN` quasi alhora, el primer guanya i fa la transició. Els següents arriben quan ja estem en una altra `state`, on no es processen — **log warning + ignorar**.

### Canvis al model i missatges

- **`api/dto/RoomInfo.java`** — eliminat el camp `is_current` (la posició actual viu només al `GameInstance`). Constructors actualitzats.
- **`mapexploration/out/MapUpdate_OUT.java`** — afegits camps `int currentFloor` i `int currentRoomId`. Constructor actualitzat a `(floors, currentFloor, currentRoomId)`.
- **`services/impl/MapGenerationService.java`** — `new RoomInfo(col, type, false, false)` → `new RoomInfo(col, type, false)`.
- **`core/states/StateCombat.java`** — afegit constructor `(game, om, rp, boolean isBoss)` i getter `isBoss()`. El constructor original delega a aquest amb `isBoss=false`. La implementació interna segueix essent un esqueleton (TODO).

### Decisions documentades

| # | Decisió |
|---|---|
| M1 | Primera entrada al mapa: qualsevol node del pis 0. |
| M2 | Adjacència `(i-1, i, i+1)` clampada; pis amb 1 sol node accepta qualsevol previ. |
| M3 | `is_current` eliminat de `RoomInfo`; la posició actual viu al `GameInstance` i el client la rep via `MapUpdate_OUT`. |
| M4 | Re-broadcast `MapUpdate_OUT` després d'un `MoveToNode_IN` vàlid (feedback immediat). |
| M5 | `BOSS_COMBAT` → `StateCombat(isBoss=true)`. Mateix estat, paràmetre diferent. |
| M6 | Race condition: log + ignorar (el segon `MoveToNode_IN` ja no es processarà perquè estem en una altra state). |

---

## Sistema del cofre / `StateChest` (NOU)

### Flux complet

1. Entrada a l'estat → broadcast **`ChestInfo_OUT`** (cofre tancat).
2. Algun jugador clica el cofre → IN **`OpenChest_IN { playerId }`**.
3. Servidor:
   - Valida que el cofre no s'hagi obert ja.
   - Genera loot via **`lootService.generateChestLoot(numPlayers, game.getRandom())`**. Es generen tants ítems com jugadors hi ha (`game.getPlayers().size()`), amb repetició permesa.
   - Marca el cofre com obert.
   - Broadcast **`ChestOpened_OUT { openedByPlayerId, availableItems }`** amb la llista de `LootInfo` completa (no només IDs).
4. Cada jugador envia **`PickItem_IN { playerId, itemId }`** per agafar UN sol ítem.
   - Validacions: cofre obert; jugador no ha agafat res abans; ítem disponible al cofre i no agafat ja per ningú.
   - Broadcast **`ItemPicked_OUT { playerId, itemId }`**.
5. Cada jugador envia **`ReadyToLeave_IN`** al seu ritme.
   - Broadcast **`PlayersReadyToLeave_OUT`** amb la llista actualitzada.
6. Quan tots han enviat `ReadyToLeave_IN` → broadcast **`ReturnToMap_OUT`** i transició a `StateMapVisualization`.

### Regla clau

**Cada jugador pot agafar com a molt 1 ítem.** Si no envia `PickItem_IN` abans del seu `ReadyToLeave_IN`, perd l'oportunitat. Si un ítem el demanen dos jugadors, el primer que arriba se'l queda.

### Missatges modificats

- **`rooms/in/OpenChest_IN`** — `playerId` ara és `long`.
- **`rooms/in/PickItem_IN`** — `playerId` ara és `long`.
- **`rooms/out/ItemPicked_OUT`** — `playerId` ara és `long`. Afegits constructors.
- **`rooms/out/ChestOpened_OUT`** — **canvi gros**: el camp `availableItemIds: List<Integer>` ha passat a ser `availableItems: List<LootInfo>` per portar tota la info del DTO (nom, descripció, imatge, efectes). `openedByPlayerId` també ara és `long`. Afegits constructors.

### Missatges eliminats

- **`rooms/out/ChestLoot_OUT.java`** — redundant amb `ChestOpened_OUT` (la decisió ja s'havia documentat).

### Canvis a `RepositoryProvider`

- Afegit `@Autowired LootService LootService` perquè els estats hi puguin accedir via `repoProvider.LootService` sense haver d'instanciar manualment el servei (i perdre els `@Autowired` interns dels repositoris).
- Documentat com a "Provider de beans" en general; nota futura per dividir en `RepositoryProvider` + `ServiceProvider` quan tinguem més serveis.

### `StateChest` implementat

Implementació completa segons el flux. Punts clau:

- **Estat intern** del cofre (variables d'instància):
  - `chestOpened` (bool).
  - `availableItems` (`List<LootInfo>`) — el contingut generat.
  - `pickedItemIds` (`Set<Integer>`) — ítems ja agafats.
  - `playersWhoPicked` (`Set<Long>`) — jugadors que ja han agafat un ítem.
  - `playersReadyToLeave` (`Set<Long>`).
- Tot l'estat **és local a l'estat** (no es persisteix al `GameInstance`); quan acaba l'estat, es perd.
- **Validacions exhaustives** a cada `handle*`:
  - Identitat del jugador.
  - Estat del cofre (obert/tancat).
  - Un ítem per jugador.
  - Ítem existeix al cofre.
  - Ítem no ja agafat per un altre.
- Codis d'error pendents (apareixen com a TODO al codi): `CHEST_ALREADY_OPENED`, `CHEST_NOT_OPEN`, `ALREADY_PICKED`, `INVALID_ITEM`, `ITEM_ALREADY_TAKEN`.

### Decisions documentades del sistema chest

| # | Decisió |
|---|---|
| C1 | Generació de contingut: lazy (en obrir-se). |
| C2 | Nombre d'ítems: 1 per jugador (`game.getPlayers().size()`). |
| C3 | Repartiment: 1 ítem màxim per jugador, free-for-all amb límit. |
| C4 | No es pot reobrir un cofre. |
| C5 | Transició quan tots envien `ReadyToLeave_IN`. |
| C6 | `ChestLoot_OUT` eliminat (redundant amb `ChestOpened_OUT`). |

---

## Sistema de loot (NOU)

### Canvis a la BD

Cal afegir la columna **`en_given_coins int NOT NULL DEFAULT 0`** a la taula `ENEMY` (per a botiga futura). De moment el servidor NO la llegeix; queda gravada per quan implementem la botiga.

```sql
ALTER TABLE ENEMY ADD COLUMN en_given_coins int NOT NULL DEFAULT 0;
```

### DTOs nous

- **`api/dto/EffectInfo.java`** — `effectId` (int), `name` (String), factory `fromEffect(Effect)`. Reflecteix la taula `EFFECT`. Pendent ampliar amb paràmetres (magnitude, duration) extrets de `PARAMETER_VALUE` quan calgui.
- **`api/dto/CombatRewardsResult.java`** — resultat agregat d'un combat: `xpGained` (int) i `loot` (List<LootInfo>). Sense `coinsGained` mentre no hi hagi botiga.

### DTOs modificats

- **`api/dto/LootInfo.java`** — abans buit, ara: `itemId` (int), `name`, `description`, `imageUrl`, `effects` (List<EffectInfo>). Factory `fromItem(Item)` que també omple els efectes (cal que els `actionEffects` estiguin precarregats al query per evitar `LazyInitializationException`).

### Models modificats

- **`db/model/Act.java`** — afegida la relació `@OneToMany(mappedBy = "action") List<ActionEffect> actionEffects`. Heretat per `Item` i `Ability`. Permet `item.getActionEffects()` per construir tooltips i `LootInfo`.

### Repositoris

- **`db/repository/ItemRepository.java`** — reemplaçat `findPossibleDropsByEnemyId` per dos mètodes nous:
  - `findDropsByEnemyId(int)` → `List<EnemyDrop>` amb `JOIN FETCH` sobre `item`, `item.actionEffects` i `actionEffects.effect`.
  - `findAllWithEffects()` → `List<Item>` per al pool global del cofre, amb `JOIN FETCH` complet.
- **`db/repository/EnemyRepository.java`** — nou, simple `JpaRepository<Enemy, Integer>`. Necessari per llegir `en_given_exp` al càlcul d'XP.
- **`db/repository/RepositoryProvider.java`** — registrats `RepoItem` i `RepoEnemy`.

### Servei

- **`services/LootService.java`** — reescriptura completa. Tres mètodes públics que accepten un `Random` per partida:
  - `generateChestLoot(int numItems, Random random)` — repetició permesa (TODO: passar a "sense repetició" quan el pool sigui prou gran).
  - `generateEnemyDropAggregated(List<Integer> enemyIds, Random random)` — roll agregat amb **interpretació A (màxima probabilitat)**: per cada item únic dels drops, es queda la chance més alta entre tots els enemics i fa UN sol roll.
  - `generateCombatRewards(List<Integer> enemyIds, Random random)` — retorna `CombatRewardsResult` amb XP total (suma de `en_given_exp`) i loot agregat.

Warnings via `System.err` si:
- El pool d'items està buit.
- Un enemic no té entrades a `ENEMY_DROPS`.
- Un enemic no existeix a la BD.

### Core

- **`core/GameInstance.java`** — afegit camp `private final Random random = new Random()` amb getter `getRandom()`. Els estats que necessitin loot agafaran `game.getRandom()` i el passaran als mètodes de `LootService`.

### Decisions documentades del sistema de loot

| # | Decisió |
|---|---|
| Q1 | Cofre: el `numItems` el passa qui crida (típicament 1 per jugador, `game.getPlayers().size()`). |
| Q2 | Pool petit: es permet repetició mentre tinguem pocs items. TODO: passar a no-repetició. |
| Q3 | Sense filtre per pis (no hi ha `rarity` ni `min_floor` a la BD). |
| Q4 | Només cofres normals; cofres especials documentats per al futur. |
| Q5 | Roll agregat per combat amb **màxima probabilitat** (interpretació A). |
| Q6 | Enemics sense drops → warning, no excepció. |
| Q7 | `LootService` calcula XP + loot. Monedes quan hi hagi botiga. |
| Q8 | `Random` per partida (camp de `GameInstance`). |
| Q9 | Efectes inclosos al `LootInfo` via factory + `JOIN FETCH`. |

---

## Convencions adoptades

- **Tots els camps públics** als minimodels i missatges, perquè Jackson els serialitzi sense getters. Si veus un camp package-private, fes-lo públic.
- **`playerId` sempre `long`** (acord amb el Xuban). `characterId` segueix sent `int`.
- **Imatges (backgrounds, sprites, etc.) viuen al client.** El servidor només envia identificadors (`biomeName`, `characterId`...). El client mapeja a assets locals.
- **Convencions de nom de fitxer:**
  - Missatges entrants: `*_IN.java` amb camelCase (`PickCharacter_IN`, `MoveToNode_IN`, `ClaimReward_IN`).
  - Missatges sortints: `*_OUT.java` amb camelCase (`LobbyUpdate_OUT`, `MapUpdate_OUT`, `GameSummary_OUT`).
  - Constants `TYPE`: `UPPER_SNAKE_CASE` (`"PICK_CHARACTER"`, `"MAP_UPDATE"`...).

---

## Pendents

Coses identificades a la revisió que **encara no s'han tocat**:

- `ActionResult_OUT.TYPE = "RESULT"` — més descriptiu seria `"ACTION_RESULT"`. Cosmètic.
- `Login_OUT.TYPE = "LOGIN"` — podria ser `"LOGIN_RESULT"` per coherència. Cosmètic.
- Subclasses de `NodeInfo_OUT` (`BossInfo_OUT`, `ChestInfo_OUT`, `CombatInfo_OUT`, `RestInfo_OUT`) són esquelets sense camps. Cal omplir-les quan implementem la transició de sales.
- Implementació real de `StateMapVisualization.tick()` per processar `MoveToNode_IN`:
  - Validació d'adjacència `(i-1, i, i+1)` clampada.
  - Actualització de `currentFloor` / `currentRoomId` al `GameInstance`.
  - Broadcast del nou `MapUpdate_OUT` o un missatge més lleuger.
  - Transició a l'estat de la sala (`StateCombat` / `StateChest` / `StateRest` / boss).

---

*Document generat com a savepoint. Quan facis més canvis als missatges, demana un resum nou perquè aquesta llista quedi al dia.*
