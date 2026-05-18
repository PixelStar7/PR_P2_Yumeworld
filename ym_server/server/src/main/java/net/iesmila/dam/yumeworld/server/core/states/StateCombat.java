package net.iesmila.dam.yumeworld.server.core.states;

import net.iesmila.dam.yumeworld.server.api.dto.CombatRewardsResult;
import net.iesmila.dam.yumeworld.server.api.dto.EnemyInfo;
import net.iesmila.dam.yumeworld.server.api.dto.PlayerCombatSnapshot;
import net.iesmila.dam.yumeworld.server.api.messages.JSONMessage;
import net.iesmila.dam.yumeworld.server.api.messages.combat.in.PlayerAction_IN;
import net.iesmila.dam.yumeworld.server.api.messages.combat.out.CombatStart_OUT;
import net.iesmila.dam.yumeworld.server.api.messages.combat.out.CombatUpdate_OUT;
import net.iesmila.dam.yumeworld.server.api.messages.combat.out.EndCombat_OUT;
import net.iesmila.dam.yumeworld.server.api.messages.combat.out.EnemyAction_OUT;
import net.iesmila.dam.yumeworld.server.api.messages.combat.out.GameOver_OUT;
import net.iesmila.dam.yumeworld.server.api.messages.combat.out.TakeDamage_OUT;
import net.iesmila.dam.yumeworld.server.api.messages.generic.out.ActionResult_OUT;
import net.iesmila.dam.yumeworld.server.api.models.Player;
import net.iesmila.dam.yumeworld.server.api.models.PlayerCharacterState;
import net.iesmila.dam.yumeworld.server.api.models.EnemyCombatState;
import net.iesmila.dam.yumeworld.server.core.GameInstance;
import net.iesmila.dam.yumeworld.server.core.GameMessage;
import net.iesmila.dam.yumeworld.server.db.model.Enemy;
import net.iesmila.dam.yumeworld.server.db.model.EnemyFloor;
import net.iesmila.dam.yumeworld.server.db.repository.RepositoryProvider;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static net.iesmila.dam.yumeworld.server.core.enums.Codes.*;

/**
 * Estat de combat.
 *
 * Flux:
 *   1. Constructor:
 *      - Llegir EnemyFloors del pis actual i crear EnemyCombatState per cada
 *        enemic (multiplicat per enfl_quantity). Assigna combatId 0, 1, 2...
 *      - Decideix l'ordre dels jugadors aleatòriament.
 *      - Broadcast CombatStart_OUT amb enemics + ordre.
 *   2. tick():
 *      - Si toca a un jugador (currentTurnPlayerId != -1): espera PlayerAction_IN
 *        del jugador correcte. Si arriba, valida i processa.
 *      - Si toca als enemics (currentTurnPlayerId == -1): executa el torn
 *        complet dels enemics dins del mateix tick (sense bloquejar).
 *   3. Després de cada acció comprova fi del combat:
 *      - Tots els enemics morts → victòria.
 *      - Tots els jugadors morts → derrota → GameOver + StateEndGame(victory=false).
 *
 * Final del combat:
 *   - Victòria normal: EndCombat_OUT + transició a StateCombatRewards.
 *   - Victòria boss: EndCombat_OUT + transició directa a StateEndGame(victory=true).
 *   - Derrota: GameOver_OUT + transició a StateEndGame(victory=false).
 *
 * Limitacions actuals (TODOs):
 *   - Fórmula de dany: simple atk - def. Cal evolucionar al sistema d'efectes
 *     complet (ACTION_EFFECT + PARAMETER_VALUE) per a curacions, buffs, etc.
 *   - L'actionId del jugador no es valida contra YUMEDANT_ACT.
 *   - Els enemics fan un atac genèric (actionId=-1) que aplica dany simple.
 *   - El cost MP de les habilitats no es valida (ABILITY.ab_cost).
 *   - Mapping floorNum (runtime) → fl_id (BD): assumim fl_id = floorNum + 1.
 */


public class StateCombat extends State {

    private final boolean isBoss;

    /** Estat runtime dels enemics indexat per combatId. LinkedHashMap manté ordre d'inserció. */
    private final Map<Integer, EnemyCombatState> enemyStates = new LinkedHashMap<>();

    /** Ordre dels jugadors al torn (decidit aleatòriament al constructor, immutable). */
    private final List<Long> playerTurnOrder = new ArrayList<>();

    /** Index dins de playerTurnOrder. Si == size, toca als enemics. */
    private int turnIndex = 0;

    /** ID del jugador que té el torn ACTUAL, o -1 si toca als enemics. */
    private long currentTurnPlayerId = -1;

    private int round = 1;
    private boolean combatEnded = false;

    public StateCombat(GameInstance game, ObjectMapper objectMapper, RepositoryProvider repoProvider) {
        this(game, objectMapper, repoProvider, false);
    }

    public StateCombat(GameInstance game, ObjectMapper objectMapper, RepositoryProvider repoProvider, boolean isBoss) {
        super(game, objectMapper, repoProvider);
        this.isBoss = isBoss;

        System.out.println("[StateCombat] [" + game.getId() + "] Inicialitzant combat" + (isBoss ? " (BOSS)" : "") + " al pis " + game.getCurrentFloor() + ".");

        initializeEnemies();
        initializePlayerTurnOrder();
        advanceTurnToFirstPlayer();
        broadcastCombatStart();
    }

    public boolean isBoss() {
        return isBoss;
    }

    // ===================================================================
    // INICIALITZACIÓ
    // ===================================================================

    /**
     * Llegeix els EnemyFloors del pis actual i crea un EnemyCombatState per
     * cada còpia d'enemic indicada per enfl_quantity.
     * TODO: mapping floorNum → fl_id. Assumim fl_id = floorNum + 1.
     */
    private void initializeEnemies() {
        int floorId = game.getCurrentFloor() + 1;
        List<EnemyFloor> enemyFloors = repoProvider.RepoEnemyFloor.findByFloorId(floorId);

        if (enemyFloors.isEmpty()) {
            System.err.println("[StateCombat] [" + game.getId() + "] WARN - No hi ha cap entrada a ENEMY_FLOORS per al pis fl_id=" + floorId + ". Combat sense enemics!");
            return;
        }

        // Ordenem per enemyId per garantir un ordre estable als torns
        enemyFloors.sort(Comparator.comparingInt(ef -> ef.getEnemy().getId()));

        int combatId = 0;
        for (EnemyFloor ef : enemyFloors) {
            Enemy enemy = ef.getEnemy();
            int quantity = ef.getQuantity();
            for (int i = 0; i < quantity; i++) {
                EnemyCombatState state = new EnemyCombatState(
                        combatId++,
                        enemy.getId(),
                        enemy.getHp(),
                        enemy.getMp(),
                        enemy.getAtk(),
                        enemy.getDef(),
                        enemy.getLevel(),
                        new ArrayList<>()   // TODO: llegir YUMEDANT_ACT per omplir
                );
                enemyStates.put(state.getCombatId(), state);
            }
        }

        System.out.println("[StateCombat] [" + game.getId() + "] " + enemyStates.size() + " enemics creats.");
    }

    /**
     * Decideix aleatòriament l'ordre en què els jugadors actuen durant tot
     * el combat (es manté igual fins al final).
     */
    private void initializePlayerTurnOrder() {
        playerTurnOrder.addAll(game.getPlayerStates().keySet());
        Collections.shuffle(playerTurnOrder, game.getRandom());
        System.out.println("[StateCombat] [" + game.getId() + "] Ordre de torns dels jugadors: " + playerTurnOrder);
    }

    /**
     * Posa el torn al primer jugador viu de la llista. Si tots són morts
     * (no hauria de passar al començar), passa als enemics.
     */
    private void advanceTurnToFirstPlayer() {
        turnIndex = 0;
        while (turnIndex < playerTurnOrder.size()) {
            long pid = playerTurnOrder.get(turnIndex);
            PlayerCharacterState pcs = game.getPlayerState(pid);
            if (pcs != null && pcs.getCurrentHp() > 0) {
                currentTurnPlayerId = pid;
                return;
            }
            turnIndex++;
        }
        // Cap jugador viu (cas extrem)
        currentTurnPlayerId = -1;
    }

    // ===================================================================
    // TICK
    // ===================================================================

    @Override
    public void tick() {
        if (combatEnded) return;

        if (currentTurnPlayerId == -1) {
            // Toca als enemics: execució immediata, sense esperar missatges
            executeEnemiesTurn();
            return;
        }

        // Toca a un jugador concret: esperem el seu PlayerAction_IN
        GameMessage message = game.pollMessage(5, TimeUnit.SECONDS);
        if (message == null) return;

        try {
            JSONMessage gm = objectMapper.readValue(message.payload(), JSONMessage.class);
            System.out.println("[StateCombat] [" + game.getId() + "] TICK - Rebut " + gm.messageType + " de jugador " + message.user().getId());

            if (PlayerAction_IN.TYPE.equals(gm.messageType)) {
                handlePlayerAction(message.user(), gm);
            } else {
                System.out.println("[StateCombat] [" + game.getId() + "] Missatge no esperat: " + gm.messageType);
            }
        } catch (Exception e) {
            System.err.println("[StateCombat] [" + game.getId() + "] Error parsejant: " + e.getMessage());
        }
    }

    // ===================================================================
    // ACCIONS DELS JUGADORS
    // ===================================================================

    private void handlePlayerAction(Player player, JSONMessage gm) {
        PlayerAction_IN msg = objectMapper.treeToValue(gm.data, PlayerAction_IN.class);

        // Validació: identitat
        if (player.getId() != msg.playerId) {
            sendInvalid(player, INVALID_PLAYER.value);
            return;
        }

        // Validació: és el torn d'aquest jugador?
        if (msg.playerId != currentTurnPlayerId) {
            System.out.println("[StateCombat] [" + game.getId() + "] FAIL - No és el torn de " + msg.playerId + " (toca a " + currentTurnPlayerId + ").");
            sendInvalid(player, INVALID_PLAYER.value);  // TODO: codi NOT_YOUR_TURN
            return;
        }

        // Validació: el jugador està viu?
        PlayerCharacterState attacker = game.getPlayerState(msg.playerId);
        if (attacker == null || attacker.getCurrentHp() <= 0) {
            sendInvalid(player, INVALID_PLAYER.value);
            return;
        }

        // TODO: validar que msg.actionId pertany al personatge (YUMEDANT_ACT)
        // TODO: validar cost MP si és Ability
        // TODO: sistema d'efectes complet (ACTION_EFFECT + PARAMETER_VALUE)
        // Per ara, totes les accions són un atac genèric: damage = max(0, atk - def)

        // Validació: objectiu segons targetType
        String tt = msg.targetType != null ? msg.targetType : "ENEMY";
        switch (tt) {
            case "ENEMY": {
                EnemyCombatState target = enemyStates.get(msg.targetCombatId);
                if (target == null || !target.isAlive()) {
                    sendInvalid(player, INVALID_CHARACTER.value);
                    return;
                }
                applyDamage(attacker.getAtk(), target.getDef(),
                        msg.playerId, "PLAYER",
                        target.getCombatId(), "ENEMY");
                break;
            }
            case "ALL_ENEMIES": {
                for (EnemyCombatState target : enemyStates.values()) {
                    if (!target.isAlive()) continue;
                    applyDamage(attacker.getAtk(), target.getDef(),
                            msg.playerId, "PLAYER",
                            target.getCombatId(), "ENEMY");
                }
                break;
            }
            case "SELF":
            case "PLAYER":
            case "TEAM":
                // TODO: requereix sistema d'efectes (curació, buff, etc.). Per ara, noop.
                System.out.println("[StateCombat] [" + game.getId() + "] TODO - targetType " + tt + " no implementat amb fórmula simple.");
                break;
            default:
                sendInvalid(player, INVALID_CHARACTER.value);
                return;
        }

        // Confirmació individual al jugador
        game.send(player.getSession(), new JSONMessage(game.getId(),
                new ActionResult_OUT(true, OK.value)));

        // Comprovar fi de combat
        if (checkCombatEnd()) return;

        // Avançar el torn
        advanceTurn();
        broadcastCombatUpdate();
    }

    // ===================================================================
    // ACCIONS DELS ENEMICS
    // ===================================================================

    /**
     * Executa el torn complet dels enemics: cada enemic viu, en ordre per
     * combatId, ataca un jugador viu aleatori. Comprova fi del combat a cada pas.
     */
    private void executeEnemiesTurn() {
        System.out.println("[StateCombat] [" + game.getId() + "] >>> Torn dels enemics (round " + round + ") <<<");

        Random rng = game.getRandom();
        for (EnemyCombatState enemy : enemyStates.values()) {
            if (!enemy.isAlive()) continue;

            // Triar jugador objectiu aleatori entre els vius
            List<Long> alive = playerTurnOrder.stream()
                    .filter(pid -> {
                        PlayerCharacterState p = game.getPlayerState(pid);
                        return p != null && p.getCurrentHp() > 0;
                    })
                    .collect(Collectors.toList());

            if (alive.isEmpty()) {
                // Tots els jugadors morts ja
                break;
            }

            long targetPlayerId = alive.get(rng.nextInt(alive.size()));
            PlayerCharacterState target = game.getPlayerState(targetPlayerId);

            // Broadcast l'acció de l'enemic
            // TODO: actionId real quan implementem accions enemigues. Per ara -1 = atac bàsic.
            game.broadcast(new JSONMessage(game.getId(),
                    new EnemyAction_OUT(enemy.getCombatId(), enemy.getEnemyId(), -1, targetPlayerId)));

            // Aplicar dany simple
            applyDamage(enemy.getAtk(), target.getDef(),
                    enemy.getCombatId(), "ENEMY",
                    targetPlayerId, "PLAYER");

            // Comprovar fi del combat després de cada atac enemic
            if (checkCombatEnd()) return;
        }

        // Final del round
        round++;
        advanceTurnToFirstPlayer();
        broadcastCombatUpdate();
    }

    // ===================================================================
    // DANY I VALIDACIONS
    // ===================================================================

    /**
     * Aplica un atac amb fórmula simple atk - def (capat a 0). Modifica
     * directament l'estat del target i broadcasteja TakeDamage_OUT.
     */
    private void applyDamage(int atk, int def,
                             long actorId, String actorType,
                             long targetId, String targetType) {
        int damage = Math.max(0, atk - def);
        int newHp;

        if ("ENEMY".equals(targetType)) {
            EnemyCombatState target = enemyStates.get((int) targetId);
            target.takeDamage(damage);
            newHp = target.getCurrentHp();
            System.out.println("[StateCombat] [" + game.getId() + "] " + actorType + " " + actorId + " → ENEMY " + targetId + " (" + damage + " dany, HP " + newHp + "/" + target.getMaxHp() + ").");
        } else {
            PlayerCharacterState target = game.getPlayerState(targetId);
            int oldHp = target.getCurrentHp();
            target.setCurrentHp(Math.max(0, oldHp - damage));
            newHp = target.getCurrentHp();
            System.out.println("[StateCombat] [" + game.getId() + "] " + actorType + " " + actorId + " → PLAYER " + targetId + " (" + damage + " dany, HP " + newHp + "/" + target.getMaxHp() + ").");
        }

        game.broadcast(new JSONMessage(game.getId(),
                new TakeDamage_OUT(actorId, actorType, targetId, targetType, damage, newHp)));
    }

    private void sendInvalid(Player player, int code) {
        game.send(player.getSession(), new JSONMessage(game.getId(),
                new ActionResult_OUT(false, code)));
    }

    // ===================================================================
    // GESTIÓ DE TORNS
    // ===================================================================

    /**
     * Passa al següent jugador viu de la llista. Si arribem al final, posa
     * el flag "toca als enemics" (currentTurnPlayerId = -1).
     */
    private void advanceTurn() {
        turnIndex++;
        while (turnIndex < playerTurnOrder.size()) {
            long pid = playerTurnOrder.get(turnIndex);
            PlayerCharacterState pcs = game.getPlayerState(pid);
            if (pcs != null && pcs.getCurrentHp() > 0) {
                currentTurnPlayerId = pid;
                return;
            }
            turnIndex++;
        }
        // Hem acabat tots els jugadors → torn dels enemics
        currentTurnPlayerId = -1;
    }

    // ===================================================================
    // FI DEL COMBAT
    // ===================================================================

    /**
     * Comprova si el combat ha acabat (per victòria o derrota) i fa la
     * transició corresponent. Retorna true si el combat ha acabat.
     */
    private boolean checkCombatEnd() {
        boolean anyEnemyAlive = enemyStates.values().stream().anyMatch(EnemyCombatState::isAlive);
        boolean anyPlayerAlive = game.getPlayerStates().values().stream().anyMatch(p -> p.getCurrentHp() > 0);

        if (!anyEnemyAlive) {
            combatEnded = true;
            handleVictory();
            return true;
        }
        if (!anyPlayerAlive) {
            combatEnded = true;
            handleDefeat();
            return true;
        }
        return false;
    }

    private void handleVictory() {
        System.out.println("[StateCombat] [" + game.getId() + "] >>> VICTÒRIA <<<");

        // Generar recompenses
        List<Integer> enemyIds = enemyStates.values().stream()
                .map(EnemyCombatState::getEnemyId)
                .distinct()
                .collect(Collectors.toList());
        CombatRewardsResult rewards = repoProvider.LootService.generateCombatRewards(enemyIds, game.getRandom());

        game.broadcast(new JSONMessage(game.getId(), new EndCombat_OUT(rewards)));

        if (isBoss) {
            // Boss derrotat: directament a StateEndGame amb victory=true
            System.out.println("[StateCombat] [" + game.getId() + "] Boss derrotat → StateEndGame(victory=true).");
            game.setCurrentState(new StateEndGame(game, objectMapper, repoProvider, true));
        } else {
            // Combat normal: anem a StateCombatRewards
            System.out.println("[StateCombat] [" + game.getId() + "] Combat normal acabat → StateCombatRewards.");
            game.setCurrentState(new StateCombatRewards(game, objectMapper, repoProvider));
        }
    }

    private void handleDefeat() {
        System.out.println("[StateCombat] [" + game.getId() + "] >>> DERROTA <<<");
        game.broadcast(new JSONMessage(game.getId(), new GameOver_OUT()));
        game.setCurrentState(new StateEndGame(game, objectMapper, repoProvider, false));
    }

    // ===================================================================
    // BROADCASTS
    // ===================================================================

    private void broadcastCombatStart() {
        List<EnemyInfo> enemies = enemyStates.values().stream()
                .map(s -> new EnemyInfo(
                        s.getCombatId(),
                        s.getEnemyId(),
                        null,   // TODO: nom de l'enemic; cal repository call (LazyInit)
                        null,   // TODO: imatge
                        s.getCurrentHp(),
                        s.getMaxHp(),
                        s.getCurrentMp(),
                        s.getMaxMp()))
                .collect(Collectors.toList());

        game.broadcast(new JSONMessage(game.getId(),
                new CombatStart_OUT(isBoss, enemies, new ArrayList<>(playerTurnOrder), currentTurnPlayerId)));
        System.out.println("[StateCombat] [" + game.getId() + "] CombatStart_OUT broadcastejat. Primer torn: " + currentTurnPlayerId + ".");
    }

    private void broadcastCombatUpdate() {
        List<PlayerCombatSnapshot> playerSnapshots = game.getPlayerStates().values().stream()
                .map(PlayerCombatSnapshot::from)
                .collect(Collectors.toList());

        List<EnemyInfo> enemySnapshots = enemyStates.values().stream()
                .map(s -> new EnemyInfo(
                        s.getCombatId(),
                        s.getEnemyId(),
                        null,   // TODO: nom (cal repository call o cache)
                        null,
                        s.getCurrentHp(),
                        s.getMaxHp(),
                        s.getCurrentMp(),
                        s.getMaxMp()))
                .collect(Collectors.toList());

        game.broadcast(new JSONMessage(game.getId(),
                new CombatUpdate_OUT(playerSnapshots, enemySnapshots, currentTurnPlayerId, round)));
    }
}
