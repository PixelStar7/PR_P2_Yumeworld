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
