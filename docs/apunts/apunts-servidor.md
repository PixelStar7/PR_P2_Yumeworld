# Apunts servidor
Hello :3 Aquest document farà una mica a mode d'apunts per tenir clar com funciona el nostre servidor i consolidar els coneixements. Aquí podem apuntar dubtes, explicacions, etc.


## Capes del servidor en aquest projecte

1. **db/model** → Entitats de la base de dades (JPA/Hibernate). Representen taules de la BD.
2. **api/models** → Models de domini / estat intern del joc (en memòria). Objectes que el servidor usa durant la partida (p. ex. `Player`, `EnemyCombatState`).
3. **api/dto** → DTOs (Data Transfer Objects): objectes JSON que defineixen el contracte extern de l'API (p. ex. `PlayerInfo`, `CharacterInfo`).
4. **api/messages** → Missatges WebSocket (in/out per funcionalitat). Contracte de comunicació en temps real amb el client (p. ex. `PickCharacter_IN`, `GameStart_OUT`).

## Diferencia entre DTO, Messages i Models
Els tres són "objectes amb dades" però serveixen per a coses diferents i viuen en capes diferents.

### Models (db/model i api/models)
- **db/model**: Entitats de base de dades. Tenen camps interns i relacions JPA. No s'exposen directament al client.
- **api/models**: Models de domini / estat del joc en memòria. Representen la partida en execució (jugador connectat, estat de combat, etc.). No són per sí mateix el que es envia al client, són la "memòria" del servidor.

### DTO (api/dto)
Contracte **JSON** que entra/surt de l'API.

- **Propòsit**: Definir el contracte extern. Utilitzant DTO evitem exposar els models interns del servidor. Son plantilles de comunicació.

### Messages (api/messages)
Missatges **WebSocket** (in/out per funcionalitat). Contracte de comunicació en temps real amb el client.

- **Propòsit**: Notificar un event, una acció, etc. al client a través de WebSocket sense esperar resposta immediata en el sentit HTTP.


## ⚠️ Ús no habitual del servidor

En un servidor web **habitual** (p. ex. una API REST típica):

1. El client fa una petició HTTP (GET/POST...) a un endpoint.
2. Un **Controlador** rep la petició.
3. Es valida un **DTO** d'entrada.
4. Un **Servei** llegeix/escriu a la base de dades a través d'entitats (JPA).
5. Es converteix l'entitat a un **DTO** de resposta i s'envia al client.
6. La connexió és sense estat (stateless): cada petició és independent.

En el nostre projecte (servidor de joc en temps real) l'ús és **diferent**:

| Aspecte | Servidor habitual (REST) | El nostre servidor (Yumeworld) |
|---------|--------------------------|--------------------------------|
| **Protocol principal** | HTTP / REST | WebSocket (missatgeria bidireccional) |
| **Cicl de vida** | Stateless (cada petició és independent) | Estat connectat (el servidor manté la partida en memòria) |
| **Centre de la capa de negoci** | Servei + Entitats de BD directament | **`api/models`** (estat del joc en memòria); la BD (`db/model`) és per persistir, no per cada acció en temps real |
| **Comunicació al client** | Resposta HTTP amb DTO | Missatge WebSocket **_OUT** (`api/messages`) |
| **Entrada del client** | Petició HTTP amb DTO | Missatge WebSocket **_IN** (`api/messages`) |
| **Rol dels DTOs** | Capa principal de contracte API | Dades auxiliars o perfils; el contracte en temps real són els Messages |

En resum: no utilitzem el servidor com una API REST tradicional on la BD és el centre de cada petició. Aquí el servidor és un **motor de joc** que manté estat en memòria (`api/models`), s comunica amb el client via WebSocket (`api/messages`), i només toca la base de dades (`db/model`) per carregar o guardar progrés.

