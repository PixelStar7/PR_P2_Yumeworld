# Decisions documentades — Yumeworld Server

Referència de disseny per al servidor i el client Unity. Cada secció cobreix un estat del joc:
el flux del servidor (lògica interna + missatges enviats), el flux esperat del client (missatges
rebuts i enviats) i la taula de decisions de disseny preses.

---

## Índex

1. [Visió global del flux de partida](#1-visió-global-del-flux-de-partida)
2. [StateCharacterSelection — Selecció de personatge](#2-statecharacterselection--selecció-de-personatge)
3. [StateMapCreation — Generació del mapa](#3-statemapcreation--generació-del-mapa)
4. [StateMapVisualization — Exploració del mapa](#4-statemapvisualization--exploració-del-mapa)
5. [StateCombat — Combat](#5-statecombat--combat)
6. [StateCombatRewards — Recompenses de combat](#6-statecombatrewards--recompenses-de-combat)
7. [StateRest — Descans](#7-staterest--descans)
8. [StateChest — Cofre](#8-statechest--cofre)
9. [StateEndGame — Fi de partida](#9-stateendgame--fi-de-partida)
10. [Decisions transversals](#10-decisions-transversals)

---

## 1. Visió global del flux de partida

```
Jugadors connecten
        │
        ▼
StateCharacterSelection   ← tots trien personatge
        │ GAME_START
        ▼
StateMapCreation          ← genera mapes (sense espera, transició immediata)
        │
        ▼
StateMapVisualization ◄───────────────────────────────────┐
        │ (primer jugador que clica un node)               │
        ├─ COMBAT      ──► StateCombat ──► StateCombatRewards ─┤
        ├─ BOSS_COMBAT ──► StateCombat(boss) ──► StateEndGame  │
        ├─ REST_EVENT  ──► StateRest ─────────────────────────┤
        └─ CHEST_EVENT ──► StateChest ────────────────────────┘
                                  │
                          tots morts
                                  │
                                  ▼
                            StateEndGame
```

**Convencions generals:**
- `playerId` és sempre `long`. `characterId` és `int`.
- Missatges entrants: `*_IN`. Missatges sortints: `*_OUT`. Constants `TYPE`: `UPPER_SNAKE_CASE`.
- Camps públics a tots els DTOs i missatges (Jackson no serialitza camps privats sense getters explícits).
- Les imatges viuen al client; el servidor envia noms d'asset (`imageUrl`, `biomeName`...).

---

## 2. StateCharacterSelection — Selecció de personatge

### Flux del servidor

```
Constructor:
  broadcast  LOBBY_START { numPlayers }
  broadcast  LOBBY_UPDATE { players[], characters[] }   ← estat inicial

tick() espera PICK_CHARACTER { playerId, characterId }:
  si playerId no coincideix amb la sessió → send ACTION_RESULT(false, INVALID_PLAYER)
  si characterId no existeix a la llista  → send ACTION_RESULT(false, INVALID_CHARACTER)
  si characterId ja agafat per un altre   → send ACTION_RESULT(false, CHARACTER_ALREADY_SELECTED)
  si OK:
    des-assigna qualsevol personatge previ d'aquest jugador
    assigna characterId a playerId
    send    ACTION_RESULT(true, OK)           ← confirmació individual
    broadcast LOBBY_UPDATE { players[], characters[] }  ← estat actualitzat

  si tots els jugadors tenen personatge:
    initializePlayerStates()                  ← crea PlayerCharacterState per a cada jugador
    broadcast GAME_START
    → StateMapCreation
```

### Flux del client

```
Rep LOBBY_START    → mostra pantalla de selecció, reserva espais per a N jugadors
Rep LOBBY_UPDATE   → renderitza llista de personatges amb imageUrl; marca els ocupats
                     (pot rebre-la múltiples vegades, cada cop que algú fa un canvi)

Jugador clica un personatge:
  send PICK_CHARACTER { playerId, characterId }
  Rep ACTION_RESULT:
    false → mostra error segons codi (INVALID_CHARACTER, CHARACTER_ALREADY_SELECTED...)
    true  → feedback visual de "personatge assignat"
  Rep LOBBY_UPDATE (broadcast) → actualitza la UI amb l'estat global

Rep GAME_START → amaga pantalla de selecció, mostra loading de mapa
```

### Decisions

| # | Decisió |
|---|---|
| CS1 | No hi ha sistema "Ready". Quan tots tenen personatge, la partida comença automàticament. |
| CS2 | Un jugador pot canviar de personatge fins que la partida comença (el primer queda lliure). |
| CS3 | `selectedPlayerId` per defecte és `-1` ("cap jugador"). Evita col·lisió amb `playerId == 0`. |
| CS4 | `CharacterInfo.imageUrl` s'omple amb `yu_image` de la BD (nom de l'asset Unity). |
| CS5 | `initializePlayerStates()` carrega el `YuCharacter` amb `findByIdWithRoleAbilities` (JOIN FETCH) per tenir el rol i els seus bonus de level-up disponibles sense LazyInit. |

---

## 3. StateMapCreation — Generació del mapa

### Flux del servidor

```
Constructor (sense espera de missatges):
  genera N pisos via MapGenerationService.generateFloor(i, totalFloors, numColumns)
    - Pis 0          → numColumns = 4, sales majoritàriament COMBAT
    - Pisos intermedis → numColumns = 4, mix COMBAT/REST_EVENT/CHEST_EVENT
    - Últim pis      → numColumns = 1 (només BOSS_COMBAT)
  determinaBiome per pis (distribueix els 5 biomes proporcionalment)
  game.setFloors(floors)
  → StateMapVisualization (immediat, sense broadcast)
```

### Flux del client

```
Rep GAME_START (de StateCharacterSelection) → mostra pantalla de càrrega
No rep cap missatge de StateMapCreation (és una transició interna)
Rep MAP_UPDATE (de StateMapVisualization) → ja té el mapa llest per renderitzar
```

### Decisions

| # | Decisió                                                                                        |
|---|------------------------------------------------------------------------------------------------|
| MC1 | `StateMapCreation` és un estat de transició pur: genera i desa, sense esperar cap missatge.    |
| MC2 | L'últim pis té una sola columna (el boss); la resta en tenen N.                                |
| MC3 | `determineBiome` distribueix els 5 biomes (`numbness → nightmare`) proporcionalment als pisos. |
| MC4 | El mapa es genera un sol cop i es desa a `GameInstance.floors`.                                |

---

## 4. StateMapVisualization — Exploració del mapa

### Flux del servidor

```
Constructor:
  si tornem d'una sub-state (currentRoomId >= 0):
    marca la sala anterior com is_cleared = true
  broadcast MAP_UPDATE { floors[], currentFloor, currentRoomId }

tick() espera MOVE_TO_NODE { playerId, floorNum, roomId }:
  valida identitat del jugador
  valida adjacència de pis:
    primera entrada (currentFloor == -1) → targetFloor ha de ser 0
    després                              → targetFloor == currentFloor + 1
  valida adjacència de sala:
    primera entrada o pis amb 1 node (boss) → qualsevol roomId
    estàndard                               → |targetRoomId - currentRoomId| <= 1
  si error → send ACTION_RESULT(false, codi)
  si OK:
    actualitza game.currentFloor, game.currentRoomId
    send    ACTION_RESULT(true, OK)
    broadcast MAP_UPDATE { floors[], currentFloor, currentRoomId }   ← feedback immediat
    → sub-estat segons RoomType:
        COMBAT      → StateCombat(isBoss=false)
        BOSS_COMBAT → StateCombat(isBoss=true)
        REST_EVENT  → StateRest
        CHEST_EVENT → StateChest
```

### Flux del client

```
Rep MAP_UPDATE → renderitza el mapa complet:
  - mostra tots els pisos i les seves sales amb tipus i estat (cleared/available)
  - ressalta la sala actual (currentFloor, currentRoomId)
  - habilita els nodes adjacents com a clicables

Jugador clica un node adjacent:
  send MOVE_TO_NODE { playerId, floorNum, roomId }
  Rep ACTION_RESULT:
    false → mostra error (node no accessible)
    true  → res (el MAP_UPDATE ja arribarà)
  Rep MAP_UPDATE → actualitza la posició al mapa

Transició:
  Quan el servidor canvia d'estat, el client rebrà el missatge d'inici del nou estat
  (COMBAT_START, REST_INFO, CHEST_INFO...) i canviarà de pantalla.
```

### Decisions

| # | Decisió |
|---|---|
| M1 | El primer jugador que clica un node vàlid decideix on va tot el grup (sense votació). |
| M2 | Adjacència `(i-1, i, i+1)` clampada; pis amb 1 sol node accepta qualsevol previ. |
| M3 | `is_current` eliminat de `RoomInfo`; la posició actual viu al `GameInstance` i s'envia via `MAP_UPDATE`. |
| M4 | Re-broadcast `MAP_UPDATE` just abans de la transició (feedback immediat al client). |
| M5 | Race condition: el segon `MoveToNode_IN` s'ignora (log + warning) perquè el primer ja ha canviat d'estat. |

---

## 5. StateCombat — Combat

### Flux del servidor

```
Constructor:
  broadcast COMBAT_START
  initializeEnemies()          ← crea EnemyCombatState per cada enemic del pis
  initializePlayerActions()    ← carrega YUMEDANT_ACT + PLAYERROLE_ABILITY per a cada jugador
  initializePlayerTurnOrder()  ← ordre aleatori, es manté tot el combat
  advanceTurnToFirstPlayer()
  broadcast COMBAT_INFO { isBoss, enemies[], playerTurnOrder[], currentTurnPlayerId }
  send (individual) PLAYER_ACTIONS { actions[] }   ← un per jugador, amb les seves accions

tick():
  si currentTurnPlayerId != -1 (torn d'un jugador):
    espera PLAYER_ACTION { playerId, actionId, targetCombatId, targetType }
      valida identitat     → ACTION_RESULT(false, INVALID_PLAYER)
      valida torn          → ACTION_RESULT(false, NOT_YOUR_TURN)
      valida jugador viu   → ACTION_RESULT(false, INVALID_PLAYER)
      valida actionId      → ACTION_RESULT(false, INVALID_ACTION)
      valida MP suficient  → ACTION_RESULT(false, NOT_ENOUGH_MP)
      si OK:
        consumeix MP
        resolveAndApplyEffects(actionId, ...)
          carrega Act amb efectes des de BD (findByIdWithEffects)
          per cada ActionEffect:
            Efecte 1 (Simple Damage):   damage = max(0, attackerStat * magnitude - defenderDef)
            Efecte 2 (Cura percentual): cura magnitude% del maxHp/maxMp
            Efecte 3 (Cura absoluta):   cura magnitude punts
            Efecte 4/5: TODO
          broadcast TAKE_DAMAGE o actualitza HP/MP
        send ACTION_RESULT(true, OK)
        comprova fi combat
        avança torn → broadcast COMBAT_UPDATE { playerStates[], enemyStates[], currentTurnPlayerId, round }

  si currentTurnPlayerId == -1 (torn dels enemics):
    per cada enemic viu (ordenats per enemyId):
      tria jugador objectiu aleatori entre els vius
      tria acció aleatòria de enemy.actionIds (fallback a BASIC_ATTACK si buida)
      broadcast ENEMY_ACTION { enemyCombatId, enemyId, actionId, targetPlayerId }
      resolveAndApplyEffects(...)
    round++
    avança torn al primer jugador viu
    broadcast COMBAT_UPDATE

Final:
  tots enemics morts → broadcast END_COMBAT → StateCombatRewards (normal) / applyXP + StateEndGame (boss)
  tots jugadors morts → broadcast GAME_OVER → StateEndGame(victory=false)
```

### Flux del client

```
Rep COMBAT_START  → mostra pantalla de combat (loading)
Rep COMBAT_INFO   → inicialitza la UI:
  - mostra enemics amb HP/MP i imatge
  - desa l'ordre de torns
  - ressalta qui té el torn
Rep PLAYER_ACTIONS (individual) → carrega el menú d'accions:
  - mostra cada acció amb nom, descripció, cost MP i icona
  - deshabilita les accions sense prou MP

Quan és el teu torn (currentTurnPlayerId == el teu playerId):
  Jugador selecciona acció i objectiu:
    send PLAYER_ACTION { playerId, actionId, targetCombatId, targetType }
  Rep ACTION_RESULT:
    false → mostra error (NOT_YOUR_TURN, INVALID_ACTION, NOT_ENOUGH_MP...)
    true  → feedback visual d'acció executada

Rep TAKE_DAMAGE   → reprodueix animació de dany / curació al target
Rep ENEMY_ACTION  → reprodueix animació de l'enemic atacant
Rep COMBAT_UPDATE → actualitza HP/MP de tots els participants, ressalta el nou torn

Rep END_COMBAT → tanca la pantalla de combat
Rep GAME_OVER  → mostra pantalla de derrota
```

### Decisions

| # | Decisió |
|---|---|
| C1 | Torn alternat per equips: primer tots els jugadors (en ordre decidit a l'inici), llavors tots els enemics. |
| C2 | Ordre dels jugadors aleatori al constructor, mantingut fins al final del combat. |
| C3 | Ordre dels enemics per `enemyId` (estable i reproducible). |
| C4 | `ENEMY_BIOMES.enbi_quantity` és la quantitat d'instàncies d'aquell enemic al combat. |
| C5 | `isBoss=true` → mateixa lògica que combat normal, diferent camí de sortida (→ StateEndGame). |
| C6 | Estat dels enemics local a `StateCombat` (`Map<Long, EnemyCombatState>`); no es persisteix. |
| C7 | `PLAYER_ACTIONS` s'envia individualment (no broadcast): cada jugador veu només les seves accions. |
| C8 | Fórmula de dany: `max(0, attackerStat * magnitude - defenderDef)`. |
| C9 | `BASIC_ATTACK_ID = 1` com a fallback si un enemic o personatge no té accions a la BD. |
| C10 | Efectes carregats via `ActRepository.findByIdWithEffects` (JOIN FETCH) per evitar LazyInit fora de transacció. |
| C11 | `validActionMpCosts` (Map) per a validació interna + `availableActions` (List<ActionInfo>) per enviar al client. |

---

## 6. StateCombatRewards — Recompenses de combat

### Flux del servidor

```
Constructor:
  aplica XP a tots els jugadors:
    PlayerCharacterState.addXpAndCheckLevelUp(xp, rng)
    per cada nivell pujat → broadcast CHARACTER_UPDATE { old_level, new_level, old/new HP/MP... }
  broadcast COMBAT_REWARDS { xpGained, loot[] }

tick() accepta dos tipus de missatge per jugador:

  PICK_ITEM { playerId, itemId }:
    valida identitat
    valida que el jugador no hagi agafat res encara
    valida que l'ítem existeixi al loot
    valida que l'ítem no l'hagi agafat ja un altre
    si OK:
      afegeix itemId a PlayerCharacterState.inventoryItemIds
      send    ACTION_RESULT(true, OK)
      broadcast ITEM_PICKED { playerId, itemId }

  READY_TO_LEAVE { playerId }:
    valida identitat
    registra el jugador com a llest (idempotent)
    broadcast PLAYERS_READY_TO_LEAVE { readyPlayerIds[] }
    si tots llestos:
      broadcast RETURN_TO_MAP
      → StateMapVisualization
```

### Flux del client

```
Rep END_COMBAT (de StateCombat) → tanca la UI de combat
Rep CHARACTER_UPDATE (0 o més) → mostra animació / notificació de level-up si new_level > old_level
Rep COMBAT_REWARDS { xpGained, loot[] } → mostra pantalla de recompenses:
  - mostra XP guanyada
  - mostra llista d'ítems disponibles (cada jugador pot agafar-ne 1)

Jugador clica un ítem:
  send PICK_ITEM { playerId, itemId }
  Rep ACTION_RESULT:
    false → mostra error (ítem ja agafat, ja tens un ítem...)
    true  → marca l'ítem com agafat a la UI
  Rep ITEM_PICKED (broadcast) → actualitza la UI per a tots (qui s'ha quedat què)

Jugador clica "sortir":
  send READY_TO_LEAVE { playerId }
  Rep PLAYERS_READY_TO_LEAVE → mostra quants ja han confirmat (N/total)

Rep RETURN_TO_MAP → torna a la pantalla del mapa
```

### Decisions

| # | Decisió |
|---|---|
| CR1 | XP igual per a tots els jugadors (no es divideix). |
| CR2 | XP s'aplica al constructor (immediat), no s'espera que el jugador confirmi. |
| CR3 | Loot funciona igual que el cofre: 1 ítem màxim per jugador, free-for-all. |
| CR4 | Si un jugador no agafa cap ítem i envia `READY_TO_LEAVE`, perd l'oportunitat. |
| CR5 | Reusa `ReadyToLeave_IN` / `PlayersReadyToLeave_OUT` / `ReturnToMap_OUT` (mateixos que StateRest i StateChest). |
| CR6 | Si el jugador puja de nivell, `CHARACTER_UPDATE` s'emet per a cada nivell pujat (pot ser > 1). |
| CR7 | Fórmula de level-up: `xpToNextLevel = BASE_XP * level²` (BASE_XP = 100). XP sobrant es trasllada al nou nivell. |
| CR8 | En pujar de nivell: `+1 o +2` aleatori a cada stat base + `+1 o +2` addicional si `role.pr_stat > 0`. |

---

## 7. StateRest — Descans

### Flux del servidor

```
Constructor:
  broadcast REST_INFO { campfireLit: false }

tick() accepta dos tipus de missatge:

  LIGHT_CAMPFIRE { playerId }:
    valida identitat
    valida que la foguera no estigui ja encesa → ACTION_RESULT(false, CAMPFIRE_ALREADY_LIT)
    si OK:
      per cada jugador:
        cura HP: min(maxHp, currentHp + maxHp * HP_HEAL_RATIO)   (HP_HEAL_RATIO = 0.30)
        cura MP: min(maxMp, currentMp + maxMp * MP_HEAL_RATIO)   (MP_HEAL_RATIO = 0.50)
        si algun stat ha canviat → broadcast CHARACTER_UPDATE { playerId, old/new HP/MP }
      marca foguera com encesa
      broadcast CAMPFIRE_LIT { ignitedByPlayerId }

  READY_TO_LEAVE { playerId }:
    valida identitat
    registra el jugador com a llest (idempotent)
    broadcast PLAYERS_READY_TO_LEAVE { readyPlayerIds[] }
    si tots llestos:
      broadcast RETURN_TO_MAP
      → StateMapVisualization
```

### Flux del client

```
Rep REST_INFO { campfireLit: false } → mostra pantalla de descans:
  - botó "encendre foguera" actiu
  - botó "sortir" actiu (es pot sortir sense encendre)

Jugador clica "encendre foguera":
  send LIGHT_CAMPFIRE { playerId }
  Rep CHARACTER_UPDATE (broadcast, un per jugador curat) → actualitza barres HP/MP de tots
  Rep CAMPFIRE_LIT { ignitedByPlayerId } → mostra animació de foguera; deshabilita el botó

Jugador clica "sortir":
  send READY_TO_LEAVE { playerId }
  Rep PLAYERS_READY_TO_LEAVE → mostra "N/total llestos"

Rep RETURN_TO_MAP → torna a la pantalla del mapa
```

### Decisions

| # | Decisió |
|---|---|
| R1 | Curació col·lectiva: el primer que encén la foguera cura tot el grup. |
| R2 | Ràtios de curació: HP +30%, MP +50% (constants `HP_HEAL_RATIO` / `MP_HEAL_RATIO`). |
| R3 | Si un jugador ja estava al màxim d'HP i MP, no s'envia `CHARACTER_UPDATE` per a ell. |
| R4 | Es pot sortir sense encendre la foguera (útil si tots estan ja a màxim). |
| R5 | `CHARACTER_UPDATE` és el missatge genèric per a qualsevol canvi de stats (descans, combat, level-up). |
| R6 | Missatges no esperats en aquest estat: log warning, sense respondre. |

---

## 8. StateChest — Cofre

### Flux del servidor

```
Constructor:
  broadcast CHEST_INFO { chestOpened: false }

tick() accepta tres tipus de missatge:

  OPEN_CHEST { playerId }:
    valida identitat
    valida que el cofre no estigui ja obert → ACTION_RESULT(false, CHEST_ALREADY_OPENED)
    si OK:
      genera loot: lootService.generateChestLoot(numPlayers, game.getRandom())
      marca cofre com obert
      broadcast CHEST_OPENED { openedByPlayerId, availableItems: LootInfo[] }

  PICK_ITEM { playerId, itemId }:
    valida identitat
    valida que el cofre estigui obert → ACTION_RESULT(false, CHEST_NOT_OPEN)
    valida que el jugador no hagi agafat res encara
    valida que l'ítem existeixi al loot
    valida que l'ítem no l'hagi agafat ja un altre
    si OK:
      afegeix itemId a PlayerCharacterState.inventoryItemIds
      send    ACTION_RESULT(true, OK)
      broadcast ITEM_PICKED { playerId, itemId }

  READY_TO_LEAVE { playerId }:
    registra el jugador com a llest
    broadcast PLAYERS_READY_TO_LEAVE { readyPlayerIds[] }
    si tots llestos:
      broadcast RETURN_TO_MAP
      → StateMapVisualization
```

### Flux del client

```
Rep CHEST_INFO { chestOpened: false } → mostra pantalla de cofre:
  - botó "obrir cofre" actiu

Jugador clica "obrir cofre":
  send OPEN_CHEST { playerId }
  Rep CHEST_OPENED { openedByPlayerId, availableItems[] } → mostra els ítems disponibles
    (LootInfo inclou: itemId, name, description, imageUrl, effects[])

Jugador clica un ítem:
  send PICK_ITEM { playerId, itemId }
  Rep ACTION_RESULT:
    false → error (ítem ja agafat, ja tens un ítem...)
    true  → marca l'ítem com agafat
  Rep ITEM_PICKED (broadcast) → actualitza qui s'ha quedat què

Jugador clica "sortir":
  send READY_TO_LEAVE { playerId }
  Rep PLAYERS_READY_TO_LEAVE → mostra "N/total llestos"

Rep RETURN_TO_MAP → torna al mapa
```

### Decisions

| # | Decisió |
|---|---|
| CH1 | Loot generat de forma lazy (en obrir-se el cofre, no al constructor). |
| CH2 | Nombre d'ítems generats: 1 per jugador (`game.getPlayers().size()`). Repetició permesa (pool petit). |
| CH3 | Repartiment competitiu: 1 ítem màxim per jugador; el primer que demana un ítem se'l queda. |
| CH4 | Un cofre no es pot reobrir. |
| CH5 | Tot l'estat intern (items, picks, readies) és local a l'estat i es perd en sortir. |
| CH6 | `LootInfo` porta la informació completa (nom, descripció, imageUrl, efectes) — no només l'ID. |

---

## 9. StateEndGame — Fi de partida

### Flux del servidor

```
Constructor (victory = true):
  broadcast GAME_SUMMARY { victory: true, floorsCleared, playerSummaries[] }

Constructor (victory = false):
  broadcast GAME_SUMMARY { victory: false, floorsCleared, playerSummaries[] }
  broadcast LOBBY_RETURN   ← auto-tancament sense esperar confirmació
  game.stop()

tick() [només si victory = true]:
  RETURN_TO_LOBBY { playerId }:
    registra confirmació
    si tots han confirmat:
      broadcast LOBBY_RETURN
      game.stop()
```

### Flux del client

```
Rep END_COMBAT o GAME_OVER (de StateCombat) → transició a pantalla final

Rep GAME_SUMMARY { victory, floorsCleared, playerSummaries[] } → mostra:
  - victòria o derrota
  - pisos completats
  - resum de cada jugador (HP final, nivell, exp, ítems recollits)

Si victory = true:
  Jugador clica "tornar al lobby":
    send RETURN_TO_LOBBY { playerId }
    Rep PLAYERS_READY_TO_LEAVE (opcional, si s'implementa) → mostra qui ha confirmat

Rep LOBBY_RETURN → torna a la pantalla principal / lobby
  (si victory = false, arriba automàticament sense interacció del jugador)
```

### Decisions

| # | Decisió |
|---|---|
| EG1 | Derrota: auto-tancament sense esperar confirmació dels jugadors (`LOBBY_RETURN` immediat). |
| EG2 | Victòria: espera que tots enviïn `RETURN_TO_LOBBY` (cadascun al seu ritme). |
| EG3 | `game.stop()` no tanca els WebSockets; envia `LOBBY_RETURN` i el client tanca la connexió. |
| EG4 | `floorsCleared` es deriva de `game.currentFloor + 1`. |
| EG5 | `totalCoins` tret de `GameSummary_OUT` (pendent implementar botiga). |
| EG6 | `PlayerCharacterState` no es persisteix a la BD; les partides són independents. |

---

## 10. Decisions transversals

### Arquitectura del servidor

| # | Decisió |
|---|---|
| T1 | **Màquina d'estats**: cada estat és una classe `State` que implementa `tick()`. El `GameInstance` guarda l'estat actual i crida `tick()` en bucle. |
| T2 | **`RepositoryProvider`**: bean Spring que agrupa repositoris i serveis injectats. Es passa als estats per constructor perquè no siguin Spring beans. |
| T3 | **`PlayerCharacterState`** viu al `GameInstance` des que el jugador tria personatge fins al final. No es persisteix a la BD entre partides. |
| T4 | **LazyInit**: totes les relacions JPA necessàries durant el combat es carreguen amb `JOIN FETCH` explícit als repositoris (fora de `@Transactional`). |
| T5 | **`Random` per partida**: `GameInstance` té un `Random` compartit per a totes les decisions aleatòries de la partida (loot, ordre torns, etc.). |
| T6 | **`BASIC_ATTACK_ID = 1`**: fallback universal per a enemics o personatges sense accions a la BD. |

### Sistema de level-up

| # | Decisió |
|---|---|
| LU1 | Fórmula quadràtica: `xpToNextLevel = BASE_XP * level²` (BASE_XP = 100). Modificable canviant la constant o el mètode `xpToNextLevel()`. |
| LU2 | XP sobrant d'una pujada es trasllada al nivell nou (no es perd). |
| LU3 | Boost per nivell: `+1 o +2` aleatori a cada stat (HP, MP, ATK, DEF) + `+1 o +2` addicional si `PLAYERROLE.pr_stat > 0`. |
| LU4 | XP no persisteix entre partides (viu només al `PlayerCharacterState` runtime). |
| LU5 | `CHARACTER_UPDATE` és el missatge de notificació; es poden enviar múltiples seguits si es puja més d'un nivell. |

### Sistema de loot

| # | Decisió |
|---|---|
| L1 | `LootService` genera loot de cofres (pool global) i de combats (drops per enemic). |
| L2 | Drop de combat: roll agregat amb la **màxima probabilitat** entre tots els enemics del combat per a cada ítem únic. |
| L3 | Cofre: repetició permesa mentre el pool sigui petit. TODO: passar a no-repetició. |
| L4 | Enemics sense drops definits a la BD → warning al log, no excepció. |
| L5 | `LootInfo` porta la informació completa de l'ítem (nom, descripció, imageUrl, efectes). |
