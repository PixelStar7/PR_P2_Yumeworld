# Protocol de missatges — Yumeworld Client

Referència per al client Unity. Descriu **tots els missatges** que el servidor envia, quan els envia, quin payload porten, i quin comportament s'espera del client a cada transició de pantalla.

---

## Índex

1. [Format general dels missatges](#1-format-general-dels-missatges)
2. [Connexió i login](#2-connexió-i-login)
3. [Selecció de personatge](#3-selecció-de-personatge)
4. [Mapa](#4-mapa)
5. [Combat](#5-combat)
6. [Recompenses de combat](#6-recompenses-de-combat)
7. [Descans](#7-descans)
8. [Cofre](#8-cofre)
9. [Fi de partida](#9-fi-de-partida)
10. [Missatges genèrics](#10-missatges-genèrics)
11. [Missatges que envia el client](#11-missatges-que-envia-el-client)

---

## 1. Format general dels missatges

Tots els missatges viatgen com a **JSON** sobre WebSocket amb aquest embolcall:

```json
{
  "gameId": "abc-123",
  "type": "NOM_DEL_MISSATGE",
  "body": { ...camps específics... }
}
```

- `gameId`: identificador de la partida (string). S'obté en fer login.
- `type`: constant `UPPER_SNAKE_CASE` que identifica el missatge.
- `body`: objecte amb els camps del missatge (pot ser `{}` si el missatge no porta dades).

**Convencions:**
- `playerId` és sempre `long`.
- `characterId`, `itemId`, `actionId` són `int`.
- `combatId` és l'identificador d'instància d'un enemic *dins d'un combat concret* (0, 1, 2…), no és l'id de la BD.
- `imageUrl` és el nom de l'asset Unity (string), no una URL real.
- Els missatges marcats com **[broadcast]** arriben a tots els jugadors de la partida.
- Els marcats com **[individual]** arriben només al jugador destinatari.

**Patró estàndard de transició de pantalla:**

Cada pantalla nova segueix sempre el mateix patró de dos missatges:

```
*_START  →  transició (sense dades, carrega la nova escena)
*_INFO   →  dades inicials de l'escena (es rep un cop la pantalla és activa)
```

| Pantalla          | Senyal de transició | Dades inicials        |
|-------------------|---------------------|-----------------------|
| Combat            | `COMBAT_START`      | `COMBAT_INFO`         |
| Recompenses       | `END_COMBAT`        | `COMBAT_REWARDS`      |
| Descans           | `REST_START`        | `REST_INFO`           |
| Cofre             | `CHEST_START`       | `CHEST_INFO`          |
| Mapa              | `RETURN_TO_MAP`     | `MAP_UPDATE`          |
| Selecció          | —                   | `LOBBY_START`         |
| Fi de partida     | `GAME_OVER` / `END_COMBAT` (boss) | `GAME_SUMMARY` |

---

## 2. Connexió i login

### `LOGIN` [individual]

Enviat automàticament en connectar-se. Dona al client el seu identificador de jugador.

```json
{
  "type": "LOGIN",
  "body": {
    "playerId": 42
  }
}
```

> ⚠️ Desa aquest `playerId` — és obligatori en tots els missatges que el client envia al servidor.

---

### `PLAYER_DISCONNECTED` [broadcast]

Un jugador s'ha desconnectat de la partida.

```json
{
  "type": "PLAYER_DISCONNECTED",
  "body": {
    "playerId": 42
  }
}
```

---

## 3. Selecció de personatge

### `LOBBY_START` [broadcast]

**→ Transició a pantalla de selecció de personatge.**

```json
{
  "type": "LOBBY_START",
  "body": {
    "numPlayers": 4
  }
}
```

| Camp | Tipus | Descripció |
|------|-------|------------|
| `numPlayers` | `int` | Nombre total de jugadors de la partida |

---

### `LOBBY_UPDATE` [broadcast]

Estat actualitzat de la selecció. S'envia en entrar i cada vegada que un jugador tria o canvia de personatge.

```json
{
  "type": "LOBBY_UPDATE",
  "body": {
    "players": [
      { "id": 1, "name": "Jugador1" },
      { "id": 2, "name": "Jugador2" }
    ],
    "characters": [
      {
        "characterId": 1,
        "name": "Pingu",
        "imageUrl": "pingu",
        "selectedPlayerId": 1,
        "isSelected": true
      },
      {
        "characterId": 2,
        "name": "Michi",
        "imageUrl": "michi",
        "selectedPlayerId": -1,
        "isSelected": false
      }
    ]
  }
}
```

| Camp | Tipus | Descripció |
|------|-------|------------|
| `players[].id` | `long` | `playerId` del jugador |
| `players[].name` | `string` | Nom del jugador |
| `characters[].characterId` | `int` | Id del personatge |
| `characters[].imageUrl` | `string` | Nom de l'asset Unity |
| `characters[].selectedPlayerId` | `long` | Qui l'ha triat (`-1` = lliure) |
| `characters[].isSelected` | `bool` | `true` si algú el té triat |

---

### `GAME_START` [broadcast]

**→ Transició a pantalla de mapa** (loading). El servidor passa a generar el mapa internament; el client rebrà `MAP_UPDATE` tot seguit.

```json
{ "type": "GAME_START", "body": {} }
```

---

## 4. Mapa

### `MAP_UPDATE` [broadcast]

**Dades de la pantalla de mapa.** S'envia en entrar al mapa per primera vegada i cada vegada que el grup es mou a un nou node (o torna d'una sala).

```json
{
  "type": "MAP_UPDATE",
  "body": {
    "currentFloor": 0,
    "currentRoomId": 2,
    "floors": [
      {
        "num": 0,
        "name": "Pis 0",
        "biomeId": 1,
        "biomeName": "numbness",
        "biomeBackground": "bg1",
        "rooms": [
          { "id": 0, "type": "COMBAT",      "is_cleared": true  },
          { "id": 1, "type": "REST_EVENT",  "is_cleared": false },
          { "id": 2, "type": "CHEST_EVENT", "is_cleared": false }
        ]
      }
    ]
  }
}
```

| Camp | Tipus | Descripció |
|------|-------|------------|
| `currentFloor` | `int` | Pis actual (`-1` = no s'ha mogut encara) |
| `currentRoomId` | `int` | Sala actual dins del pis (`-1` = inicial) |
| `floors[].rooms[].type` | `string` | `COMBAT`, `BOSS_COMBAT`, `REST_EVENT`, `CHEST_EVENT` |
| `floors[].rooms[].is_cleared` | `bool` | `true` si la sala ja s'ha completat |

**Nodes clicables:** el client ha d'habilitar les sales del pis `currentFloor + 1` amb `id` dins de `[currentRoomId-1, currentRoomId+1]`. Si `currentFloor == -1`, habilitar totes les sales del pis 0.

**Seqüència completa al tornar d'una sala:**
```
RETURN_TO_MAP  →  carrega escena de mapa
MAP_UPDATE     →  rep les dades (sala anterior marcada is_cleared=true)
```

---

### Transicions de node a sub-estat

Quan el grup entra a una sala, el servidor envia **primer** `MAP_UPDATE` (confirmació visual) i immediatament després el senyal de transició de l'estat nou:

| Tipus de sala | Senyal de transició |
|---------------|---------------------|
| `COMBAT`      | `COMBAT_START`      |
| `BOSS_COMBAT` | `COMBAT_START`      |
| `REST_EVENT`  | `REST_START`        |
| `CHEST_EVENT` | `CHEST_START`       |

---

## 5. Combat

### `COMBAT_START` [broadcast]

**→ Transició a pantalla de combat.** No porta dades; serveix per carregar l'escena.

```json
{ "type": "COMBAT_START", "body": {} }
```

---

### `COMBAT_INFO` [broadcast]

**Dades inicials del combat.** Arriba just després de `COMBAT_START`.

```json
{
  "type": "COMBAT_INFO",
  "body": {
    "isBoss": false,
    "enemies": [
      {
        "combatId": 0,
        "enemyId": 6,
        "name": "Doguh",
        "imageUrl": "scareddog",
        "currentHp": 25,
        "maxHp": 25,
        "currentMp": 0,
        "maxMp": 0
      }
    ],
    "playerTurnOrder": [2, 1, 3],
    "currentTurnPlayerId": 2
  }
}
```

| Camp | Tipus | Descripció |
|------|-------|------------|
| `isBoss` | `bool` | `true` si és combat de boss |
| `enemies[].combatId` | `int` | Id d'instància dins d'aquest combat (0, 1, 2…) |
| `enemies[].enemyId` | `int` | Id de la BD |
| `playerTurnOrder` | `long[]` | Ordre de torn dels jugadors (fix durant tot el combat) |
| `currentTurnPlayerId` | `long` | Qui té el primer torn (`-1` = torns dels enemics) |

---

### `PLAYER_ACTIONS` [individual]

Llista d'accions disponibles per al jugador receptor. Arriba just després de `COMBAT_INFO`, **una sola vegada per combat**.

```json
{
  "type": "PLAYER_ACTIONS",
  "body": {
    "actions": [
      {
        "actionId": 1,
        "name": "Basic atack",
        "description": "Cop simple amb l'arma.",
        "mpCost": 0,
        "imageUrl": "icon_basic_atack"
      },
      {
        "actionId": 5,
        "name": "Cop Demolidor",
        "description": "Atac físic pesat cap a un sol objectiu.",
        "mpCost": 12,
        "imageUrl": "icon_cop_demolidor"
      }
    ]
  }
}
```

> Usa `mpCost` per deshabilitar les accions que el jugador no pot pagar. El `actionId` és el que has d'enviar de tornada en `PLAYER_ACTION`.

---

### `TAKE_DAMAGE` [broadcast]

Un participant ha rebut dany o curació (valor negatiu = curació).

```json
{
  "type": "TAKE_DAMAGE",
  "body": {
    "actorId": 2,
    "actorType": "PLAYER",
    "targetId": 0,
    "targetType": "ENEMY",
    "damageAmount": 14,
    "newHp": 11
  }
}
```

| Camp | Tipus | Descripció |
|------|-------|------------|
| `actorType` / `targetType` | `string` | `"PLAYER"` o `"ENEMY"` |
| `actorId` | `long` | Si `PLAYER`: `playerId`. Si `ENEMY`: `combatId` |
| `targetId` | `long` | Igual que `actorId` |
| `newHp` | `int` | HP resultant del target |

---

### `ENEMY_ACTION` [broadcast]

Un enemic ha executat una acció. Usa'l per reproduir l'animació de l'enemic.

```json
{
  "type": "ENEMY_ACTION",
  "body": {
    "enemyCombatId": 0,
    "enemyId": 6,
    "actionId": 1,
    "targetPlayerId": 2
  }
}
```

> `targetPlayerId` és `-1` si l'acció no té un jugador concret com a objectiu (AoE).

---

### `COMBAT_UPDATE` [broadcast]

Snapshot complet de l'estat del combat. Arriba **després de cada acció** (jugador o enemic). Usa'l per refrescar tota la UI de stats d'un sol cop.

```json
{
  "type": "COMBAT_UPDATE",
  "body": {
    "round": 1,
    "currentTurnPlayerId": 1,
    "playerStates": [
      {
        "playerId": 1,
        "characterId": 3,
        "currentHp": 35,
        "maxHp": 40,
        "currentMp": 10,
        "maxMp": 16
      }
    ],
    "enemyStates": [
      {
        "combatId": 0,
        "enemyId": 6,
        "name": "Doguh",
        "imageUrl": "scareddog",
        "currentHp": 11,
        "maxHp": 25,
        "currentMp": 0,
        "maxMp": 0
      }
    ]
  }
}
```

| Camp | Descripció |
|------|------------|
| `currentTurnPlayerId` | `playerId` del jugador que ha d'actuar. `-1` si toca als enemics. |
| `round` | Número de ronda (comença a 1) |

---

### `END_COMBAT` [broadcast]

**→ Transició a pantalla de recompenses.** Tots els enemics han mort. No porta dades.

```json
{ "type": "END_COMBAT", "body": {} }
```

> ⚠️ **Aclariment important:** `END_COMBAT` significa *"surt de la pantalla de combat i entra a la de recompenses"*. **No** és el senyal per tornar al mapa. El senyal per tornar al mapa és `RETURN_TO_MAP`, que arriba al final de la pantalla de recompenses un cop tots els jugadors han enviat `READY_TO_LEAVE`.

---

### `GAME_OVER` [broadcast]

**→ Transició a fi de partida (derrota).** Tots els jugadors han mort. No porta dades.

```json
{ "type": "GAME_OVER", "body": {} }
```

> Després d'aquest missatge arribarà `GAME_SUMMARY { victory: false }`.

---

## 6. Recompenses de combat

Flux complet de la pantalla de recompenses:

```
[StateCombat envia END_COMBAT]          ← entrar a pantalla de recompenses
  CHARACTER_UPDATE × n  (si hi ha level-up)
  COMBAT_REWARDS                        ← dades: XP + llista d'ítems disponibles

  [cada jugador, opcionalment]
    → PICK_ITEM           (client → servidor)
    ← ITEM_PICKED         (broadcast)

  [cada jugador, quan acaba]
    → READY_TO_LEAVE      (client → servidor)
    ← PLAYERS_READY_TO_LEAVE  (broadcast, comptador actualitzat)

  [quan TOTS han enviat READY_TO_LEAVE]
  RETURN_TO_MAP                         ← sortir de la pantalla de recompenses
  MAP_UPDATE                            ← dades del mapa
```

---

### `CHARACTER_UPDATE` [broadcast] *(0 o més)*

Si algun jugador ha pujat de nivell durant el combat, arriben **abans** de `COMBAT_REWARDS`, un per cada nivell pujat.

```json
{
  "type": "CHARACTER_UPDATE",
  "body": {
    "modification": {
      "id": 3,
      "old_level": 1, "new_level": 2,
      "old_hp": 40,   "new_hp": 43,
      "old_mp": 16,   "new_mp": 18,
      "old_exp": 95,  "new_exp": 5
    }
  }
}
```

| Camp | Descripció |
|------|------------|
| `id` | `characterId` del personatge afectat |
| `old_*` / `new_*` | Valors abans i després del canvi |
| `new_altered_state` | Estat alterat nou (null si no n'hi ha) |

---

### `COMBAT_REWARDS` [broadcast]

**Dades de la pantalla de recompenses.**

```json
{
  "type": "COMBAT_REWARDS",
  "body": {
    "xpGained": 50,
    "loot": [
      {
        "itemId": 3,
        "name": "Poció curativa (Petita)",
        "description": "Recupera 15 punts de vida.",
        "imageUrl": "item_pociocurativa",
        "effects": [{ "effectId": 3, "name": "Restauració absoluta" }]
      }
    ]
  }
}
```

> Cada jugador pot agafar **com a màxim 1 ítem** del loot. Envia `PICK_ITEM` per reclamar-lo.

---

### `ITEM_PICKED` [broadcast]

Un jugador ha agafat un ítem. Marca'l com a no disponible a la UI.

```json
{
  "type": "ITEM_PICKED",
  "body": {
    "playerId": 2,
    "itemId": 3
  }
}
```

---

### `PLAYERS_READY_TO_LEAVE` [broadcast]

Actualització de quants jugadors han confirmat sortir. Mostra un comptador tipus "esperant 2/4".

```json
{
  "type": "PLAYERS_READY_TO_LEAVE",
  "body": {
    "readyPlayerIds": [1, 3]
  }
}
```

---

### `RETURN_TO_MAP` [broadcast]

**→ Transició de tornada al mapa.** Arriba quan tots han confirmat. Tot seguit arriba `MAP_UPDATE`.

```json
{ "type": "RETURN_TO_MAP", "body": {} }
```

---

## 7. Descans

### `REST_START` [broadcast]

**→ Transició a pantalla de descans.** No porta dades.

```json
{ "type": "REST_START", "body": {} }
```

---

### `REST_INFO` [broadcast]

**Dades inicials de la sala de descans.** Arriba just després de `REST_START`.

```json
{
  "type": "REST_INFO",
  "body": {
    "campfireLit": false
  }
}
```

---

### `CAMPFIRE_LIT` [broadcast]

La foguera s'ha encès i la curació s'ha aplicat. Deshabilita el botó "Encendre foguera".

```json
{
  "type": "CAMPFIRE_LIT",
  "body": {
    "ignitedByPlayerId": 2
  }
}
```

> Immediatament abans d'aquest missatge arriben 0 o més `CHARACTER_UPDATE` amb les cures aplicades a cada jugador.

---

*(Sortir de la sala: `PLAYERS_READY_TO_LEAVE` → `RETURN_TO_MAP` → `MAP_UPDATE`, igual que a Recompenses)*

---

## 8. Cofre

### `CHEST_START` [broadcast]

**→ Transició a pantalla de cofre.** No porta dades.

```json
{ "type": "CHEST_START", "body": {} }
```

---

### `CHEST_INFO` [broadcast]

**Dades inicials de la sala del cofre.** Arriba just després de `CHEST_START`.

```json
{
  "type": "CHEST_INFO",
  "body": {
    "chestOpened": false
  }
}
```

---

### `CHEST_OPENED` [broadcast]

El cofre s'ha obert. Mostra els ítems disponibles.

```json
{
  "type": "CHEST_OPENED",
  "body": {
    "openedByPlayerId": 1,
    "availableItems": [
      {
        "itemId": 15,
        "name": "Poció curativa (Gran)",
        "description": "Recupera 50 punts de vida.",
        "imageUrl": "item_pociocurativa_gran",
        "effects": [{ "effectId": 3, "name": "Restauració absoluta" }]
      }
    ]
  }
}
```

*(Recollida d'ítems i sortida: igual que a Recompenses — `PICK_ITEM` → `ITEM_PICKED` → `READY_TO_LEAVE` → `PLAYERS_READY_TO_LEAVE` → `RETURN_TO_MAP` → `MAP_UPDATE`)*

---

## 9. Fi de partida

### `GAME_SUMMARY` [broadcast]

**Dades de la pantalla final.** Arriba després de `GAME_OVER` (derrota) o `END_COMBAT` del boss (victòria).

```json
{
  "type": "GAME_SUMMARY",
  "body": {
    "victory": true,
    "floorsCleared": 5,
    "playerSummaries": [
      {
        "playerId": 1,
        "characterId": 3,
        "currentHp": 22,
        "maxHp": 40,
        "level": 3,
        "exp": 45,
        "itemsCollected": 2
      }
    ]
  }
}
```

---

### `LOBBY_RETURN` [broadcast]

**→ Tanca la sessió i torna al menú principal.** En derrota arriba automàticament. En victòria arriba quan tots han enviat `RETURN_TO_LOBBY`.

```json
{ "type": "LOBBY_RETURN", "body": {} }
```

---

## 10. Missatges genèrics

### `ACTION_RESULT` [individual]

Resposta a qualsevol acció del client. Sempre és **individual**.

```json
{
  "type": "ACTION_RESULT",
  "body": {
    "success": false,
    "errorCode": 4,
    "message": "Not your turn"
  }
}
```

| `success` | Descripció |
|-----------|------------|
| `true` | Acció acceptada (errorCode = 0) |
| `false` | Acció rebutjada. Consulta `message` per mostrar l'error |

Codis d'error rellevants:

| Codi | Significat |
|------|------------|
| 0  | OK |
| 1  | Invalid player |
| 2  | Invalid character |
| 3  | Character already selected |
| 4  | Not your turn |
| 5  | Invalid action |
| 6  | Not enough MP |
| 7  | Chest already opened |
| 8  | Campfire already lit |
| 9  | Item already picked |

---

### `CHARACTER_UPDATE` [broadcast]

Canvi d'stats d'un personatge (curació, dany, level-up). Reutilitzat a totes les pantalles. Consulta la secció 6 per al format complet.

---

## 11. Missatges que envia el client

Resum dels missatges **d'entrada** que el servidor espera. En tots cal incloure `playerId`.

| Missatge | Pantalla | Camps extra |
|----------|----------|-------------|
| `PICK_CHARACTER` | Selecció | `characterId: int` |
| `MOVE_TO_NODE` | Mapa | `floorNum: int`, `roomId: int` |
| `PLAYER_ACTION` | Combat | `actionId: int`, `targetCombatId: long`, `targetType: string` |
| `LIGHT_CAMPFIRE` | Descans | — |
| `OPEN_CHEST` | Cofre | — |
| `PICK_ITEM` | Recompenses / Cofre | `itemId: int` |
| `READY_TO_LEAVE` | Recompenses / Descans / Cofre | — |
| `RETURN_TO_LOBBY` | Fi de partida | — |

**`targetType` a `PLAYER_ACTION`:**

| Valor | Significat |
|-------|------------|
| `"E"` o `"ENEMY"` | Un enemic concret (`targetCombatId` = `combatId`) |
| `"T"` o `"ALL_ENEMIES"` | Tots els enemics (ignora `targetCombatId`) |
| `"S"` o `"SELF"` | El propi jugador (ignora `targetCombatId`) |
| `"A"` o `"ALLY"` | Un aliat concret (`targetCombatId` = `playerId` de l'aliat) |
| `"W"` o `"TEAM"` | Tot l'equip (ignora `targetCombatId`) |

> El servidor accepta tant el codi d'un sol caràcter (`"E"`) com el nom complet de l'enum (`"ENEMY"`). Per consistència, usa els codis curts.

---

## Apèndix — Inconsistències i canvis pendents
