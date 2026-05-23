# Canvis als missatges del servidor


## Índex

1. 

---


## Sistema rest

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

## Sistema de combat

### Decisions estructurals

- **Estandarditzem amb missatges genèrics de sala**: `ClaimReward_IN` i `AllRewardsClaimed_OUT` **eliminats**. `StateCombatRewards` reusa `ReadyToLeave_IN` + `PlayersReadyToLeave_OUT` + `ReturnToMap_OUT` (igual que `StateChest`/`StateRest`).
- **Loot del combat funciona com el cofre**: cada jugador agafa **1 ítem** dels disponibles via `PickItem_IN`/`ItemPicked_OUT`. Diferència amb `StateChest`: aquí l'XP s'aplica a TOTS els jugadors al constructor (igual per a tots).
- **`EndCombat_OUT`** queda com a FLAG buit ("combat acabat"). Les recompenses concretes (XP, items) arriben via **`CombatRewards_OUT`** des de `StateCombatRewards`.
- **`StateEndGame`** segueix patrons diferents segons victory:
  - Victòria: broadcast `GameSummary_OUT` + esperar `ReturnToLobby_IN` de tots → `LobbyReturn_OUT` + `game.stop()`.
  - Derrota: broadcast `GameSummary_OUT(victory=false)` + `LobbyReturn_OUT` immediat + `game.stop()` (auto-tancament).

### Flux complet d'un combat normal

```
StateCombat (victòria) →
  - broadcast EndCombat_OUT (flag, sense dades)
  - setCurrentState(new StateCombatRewards(rewards))
    ↓
StateCombatRewards constructor:
  - aplica XP a tots els PlayerCharacterState
  - broadcast CombatRewards_OUT (xpGained + loot)
    ↓
Cada jugador:
  - PickItem_IN  → ItemPicked_OUT, afegit a inventoryItemIds
  - ReadyToLeave_IN → PlayersReadyToLeave_OUT
    ↓
Tots ReadyToLeave →
  - broadcast ReturnToMap_OUT
  - setCurrentState(new StateMapVisualization(...))
```

### Flux complet del boss

```
StateCombat(isBoss=true) (victòria) →
  - broadcast EndCombat_OUT
  - aplica XP a tots els PlayerCharacterState
  - setCurrentState(new StateEndGame(victory=true))
    ↓
StateEndGame constructor (victory=true):
  - broadcast GameSummary_OUT
    ↓
Cada jugador:
  - ReturnToLobby_IN
    ↓
Tots ReturnToLobby →
  - broadcast LobbyReturn_OUT
  - game.stop()
```

### Flux complet de derrota

```
StateCombat (tots morts) →
  - broadcast GameOver_OUT
  - setCurrentState(new StateEndGame(victory=false))
    ↓
StateEndGame constructor (victory=false):
  - broadcast GameSummary_OUT(victory=false)
  - broadcast LobbyReturn_OUT (sense esperar confirmació)
  - game.stop()
```

## Cicle final

| # | Decisió |
|---|---|
| Q1 | `EndCombat_OUT` és flag pur; `CombatRewards_OUT` porta dades. |
| Q2a | XP igual per a tots els jugadors. |
| Q2b | XP s'aplica al constructor de `StateCombatRewards` (immediat). |
| Q2c | Loot funciona com el cofre (Y): 1 ítem per jugador, competitiu. |
| Q3 | Camí I: reusar `ReadyToLeave_IN`/`PlayersReadyToLeave_OUT`/`ReturnToMap_OUT`. |
| Q4 | Rewards passats per constructor. |
| Q5a | `GameSummary_OUT` amb playerSummaries (HP/level/exp/items). |
| Q5b | `totalCoins` tret (comentari documentat per al futur). |
| Q6 | `floorsCleared` derivat de `currentFloor + 1`. |
| Q7 | (c): broadcast + `game.stop()` sense tancar WebSockets. |
| Q8 | `PlayerCharacterState` no es persisteix (partides independents). |
| Q9 | Derrota: auto-tancament sense esperar confirmació. |
| Q10 | `LobbyReturn_OUT` nou per a transició de UI. |

---

## Sistema de combat / `StateCombat` (NOU)

### Flux complet

1. **Constructor**: llegeix `ENEMY_FLOORS` del pis actual i crea un `EnemyCombatState` per cada còpia (`enfl_quantity`). Decideix l'ordre dels jugadors aleatòriament (es manté tot el combat). Broadcast `CombatStart_OUT`.
2. **`tick()`**:
   - Si toca a un jugador (`currentTurnPlayerId != -1`): espera `PlayerAction_IN`.
   - Si toca als enemics (`currentTurnPlayerId == -1`): executa el torn complet sense bloquejar.
3. **Acció jugador**:
   - Valida identitat, torn, jugador viu, objectiu vàlid.
   - Aplica dany simple (`max(0, atk - def)`) → broadcast `TakeDamage_OUT`.
   - Comprova fi del combat → si no, avança torn → broadcast `CombatUpdate_OUT`.
4. **Torn enemics**: per cada enemic viu (per `enemyId`), tria jugador objectiu aleatori i ataca → `EnemyAction_OUT` + `TakeDamage_OUT`. Round++.
5. **Final**:
   - Victòria normal → `EndCombat_OUT` + `StateCombatRewards`.
   - Victòria boss → `EndCombat_OUT` + `StateEndGame(victory=true)` directament.
   - Derrota (tots morts) → `GameOver_OUT` + `StateEndGame(victory=false)`.

### Decisions documentades del sistema combat

| # | Decisió |
|---|---|
| Q1 | Torn alternat per equips: tots jugadors, llavors tots enemics. |
| Q2 | Ordre dels jugadors aleatori a l'inici, mantingut tot el combat. |
| Q3 | Ordre dels enemics per `enemyId` (estable). |
| Q4 | `ENEMY_FLOORS.enfl_quantity` interpretat com a quantitat fixa per combat. |
| Q5 | `BOSS_COMBAT`: mateixa lògica, dades diferents al pis final. TODO afegir `is_boss` a `ENEMY` en el futur. |
| Q6 | Estat enemics local a `StateCombat` (`Map<Integer, EnemyCombatState>`). |
| Q7 | `EnemyCombatState` amb `combatId`/`enemyId`/HP/MP/atk/def/level/actionIds. |
| Q8 | `PlayerAction_IN` amb playerId/actionId/targetCombatId/targetType. `combatId` és identificador d'instància. |
| Q9 | Totes les validacions: torn, propietat acció, MP, objectiu viu. (Algunes són TODO ara.) |
| Q10 | Fórmula simple atk - def. **TODO**: evolucionar al sistema d'efectes complet (ACTION_EFFECT + PARAMETER_VALUE). |
| Q11 | Curacions/buffs: TODO documentat. Per ara totes les accions són atacs. |
| Q12 | Victòria normal → `StateCombatRewards`. |
| Q13 | Victòria boss → `StateEndGame(victory=true)` directament. |
| Q14 | Derrota → `GameOver_OUT` + `StateEndGame(victory=false)`. |
| Q15 | `CombatStart_OUT` creat. |
| Q16 | `CombatUpdate_OUT` estructurat com a "estat agregat actual". |
| Q17 | `TakeDamage_OUT` amb actor/target/damage/newHp. |
| Q18 | `EnemyInfo` amb combatId/enemyId/name/imageUrl/HP/MP. |


## `StateMapVisualization` 

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
