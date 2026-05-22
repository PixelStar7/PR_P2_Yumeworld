package net.iesmila.dam.yumeworld.server.core.states;

import net.iesmila.dam.yumeworld.server.api.dto.CombatRewardsResult;
import net.iesmila.dam.yumeworld.server.api.dto.EnemyInfo;
import net.iesmila.dam.yumeworld.server.api.dto.PlayerCombatSnapshot;
import net.iesmila.dam.yumeworld.server.api.messages.JSONMessage;
import net.iesmila.dam.yumeworld.server.api.messages.combat.in.PlayerAction_IN;
import net.iesmila.dam.yumeworld.server.api.messages.combat.out.CombatInfo_OUT;
import net.iesmila.dam.yumeworld.server.api.messages.combat.out.CombatUpdate_OUT;
import net.iesmila.dam.yumeworld.server.api.messages.combat.out.EndCombat_OUT;
import net.iesmila.dam.yumeworld.server.api.messages.combat.out.EnemyAction_OUT;
import net.iesmila.dam.yumeworld.server.api.messages.combat.out.GameOver_OUT;
import net.iesmila.dam.yumeworld.server.api.messages.combat.out.TakeDamage_OUT;
import net.iesmila.dam.yumeworld.server.api.messages.generic.out.ActionResult_OUT;
import net.iesmila.dam.yumeworld.server.api.messages.mapexploration.out.CombatStart_OUT;
import net.iesmila.dam.yumeworld.server.api.models.EnemyCombatState;
import net.iesmila.dam.yumeworld.server.api.models.Player;
import net.iesmila.dam.yumeworld.server.api.models.PlayerCharacterState;
import net.iesmila.dam.yumeworld.server.core.GameInstance;
import net.iesmila.dam.yumeworld.server.core.GameMessage;
import net.iesmila.dam.yumeworld.server.core.enums.TargetValues;
import net.iesmila.dam.yumeworld.server.db.model.Ability;
import net.iesmila.dam.yumeworld.server.db.model.Act;
import net.iesmila.dam.yumeworld.server.db.model.ActionEffect;
import net.iesmila.dam.yumeworld.server.db.model.Enemy;
import net.iesmila.dam.yumeworld.server.db.model.EnemyBiome;
import net.iesmila.dam.yumeworld.server.db.model.ParameterValue;
import net.iesmila.dam.yumeworld.server.db.model.YuCharacter;
import net.iesmila.dam.yumeworld.server.db.repository.RepositoryProvider;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static net.iesmila.dam.yumeworld.server.core.enums.Codes.*;

/**
 * Estat de combat.
 *
 * Flux:
 *   1. Constructor:
 *      - Broadcast COMBAT_START (transició d'escena al client).
 *      - Llegir EnemyBiomes del bioma del pis actual i crear EnemyCombatState
 *        per cada enemic (multiplicat per enbi_quantity). Assigna combatId 0,1,2...
 *        Carrega els action IDs dels enemics via YUMEDANT_ACT (JOIN FETCH).
 *      - Carregar les accions vàlides + costos MP de cada jugador.
 *      - Decideix l'ordre dels jugadors aleatòriament.
 *      - Broadcast COMBAT_INFO amb enemics + ordre.
 *   2. tick():
 *      - Si toca a un jugador (currentTurnPlayerId != -1): espera PlayerAction_IN
 *        del jugador correcte. Si arriba, valida i processa.
 *      - Si toca als enemics (currentTurnPlayerId == -1): executa el torn
 *        complet dels enemics dins del mateix tick (sense bloquejar).
 *   3. Resolució d'efectes (resolveAndApplyEffects):
 *      - Carrega l'Act amb els seus ActionEffects i ParameterValues.
 *      - Interpreta el tipus d'efecte (Simple Damage, Cura percentual, Cura absoluta).
 *      - Aplica l'efecte als targets corresponents i fa broadcast dels missatges.
 *   4. Comprova fi del combat:
 *      - Tots els enemics morts → victòria.
 *      - Tots els jugadors morts → derrota → GameOver + StateEndGame(victory=false).
 *
 * Final del combat:
 *   - Victòria normal: EndCombat_OUT + transició a StateCombatRewards.
 *   - Victòria boss: EndCombat_OUT + transició directa a StateEndGame(victory=true).
 *   - Derrota: GameOver_OUT + transició a StateEndGame(victory=false).
 *
 * Sistema d'efectes (IDs fixats per les dades demo):
 *   ef_id=1 "Simple Damage":   damage = max(0, attackerStat * magnitude - defenderDef)
 *   ef_id=2 "Cura percentual": heal = (targetMaxHP * magnitude) / 100
 *   ef_id=3 "Cura absoluta":   heal = magnitude (HP fix)
 *   ef_id=4 "Estat alterat":   TODO (PlayerCharacterState no té alteredStates encara)
 *   ef_id=5 "Modificació atributs": TODO
 *
 * IDs de ParameterType (fixats per les dades demo):
 *   pt_id=4 Magnitude, pt_id=5 ValueType (A/P), pt_id=6 Target, pt_id=9 RefAttr
 *
 * Targets (valor del camp Target a PARAMETER_VALUE):
 *   E=un enemic (targetCombatId), T=tots els enemics,
 *   S=self, A=un aliat (targetCombatId com playerId),
 *   W=tots els aliats, SA=self + tots els aliats
 *
 * Limitacions actuals (TODOs):
 *   - No hi ha missatge específic per curacions (reflectides via CombatUpdate_OUT).
 *   - Altered states i modificació d'atributs no implementats.
 *   - El biomeId del pis ve directament de FloorInfo, que MapGenerationService
 *     omple amb el bi_id real del bioma a la BD.
 */
public class StateCombat extends State {

    // -----------------------------------------------------------------------
    // IDs de ParameterType (constants per no dependre de strings en calent)
    // -----------------------------------------------------------------------
    private static final int PT_MAGNITUDE  = 4;
    private static final int PT_VALUE_TYPE = 5;
    private static final int PT_TARGET     = 6;
    private static final int PT_REF_ATTR   = 9;

    // IDs d'efecte
    private static final int EF_SIMPLE_DAMAGE   = 1;
    private static final int EF_CURA_PERCENTUAL = 2;
    private static final int EF_CURA_ABSOLUTA   = 3;
    private static final int EF_ESTAT_ALTERAT   = 4;

    // ID de l'acció bàsica (fallback si l'enemic no té accions a YUMEDANT_ACT)
    private static final int BASIC_ATTACK_ID = 1;

    // -----------------------------------------------------------------------

    private final boolean isBoss;

    /** Estat runtime dels enemics indexat per combatId. LinkedHashMap manté ordre d'inserció. */
    private final Map<Integer, EnemyCombatState> enemyStates = new LinkedHashMap<>();

    /**
     * Cache d'entitats Enemy indexada per enemyId. S'omple a initializeEnemies()
     * per poder afegir nom/imatge als EnemyInfo dels broadcasts sense tornar a
     * consultar la BD a cada CombatUpdate_OUT.
     */
    private final Map<Integer, Enemy> enemyCache = new HashMap<>();

    /** Ordre dels jugadors al torn (decidit aleatòriament al constructor, immutable). */
    private final List<Long> playerTurnOrder = new ArrayList<>();

    /** Index dins de playerTurnOrder. Si == size, toca als enemics. */
    private int turnIndex = 0;

    /** ID del jugador que té el torn ACTUAL, o -1 si toca als enemics. */
    private long currentTurnPlayerId = -1;

    private int round = 1;
    private boolean combatEnded = false;

    public StateCombat(GameInstance game, ObjectMapper objectMapper, RepositoryProvider repoProvider, boolean isBoss) {
        super(game, objectMapper, repoProvider);
        this.isBoss = isBoss;

        System.out.println("[StateCombat] [" + game.getId() + "] Inicialitzant combat" + (isBoss ? " (BOSS)" : "") + " al pis " + game.getCurrentFloor() + ".");

        // 1. Notificar al client que canviï d'escena (ABANS del COMBAT_INFO)
        game.broadcast(new JSONMessage(game.getId(), new CombatStart_OUT()));

        // 2. Inicialitzar enemics i jugadors
        initializeEnemies();
        initializePlayerActions();
        initializePlayerTurnOrder();
        advanceTurnToFirstPlayer();

        // 3. Broadcast de l'estat inicial del combat
        broadcastCombatStart();
    }

    public boolean isBoss() {
        return isBoss;
    }

    // ===================================================================
    // INICIALITZACIÓ
    // ===================================================================

    /**
     * Llegeix els EnemyBiomes del bioma del pis actual i crea un EnemyCombatState
     * per cada còpia d'enemic indicada per enbi_quantity.
     * Els action IDs es carreguen des de YUMEDANT_ACT via el JOIN FETCH de la query.
     *
     * El biomeId del pis ja ve resolt a FloorInfo (omplert per MapGenerationService
     * a partir del bi_id real de la BD), així que aquí simplement el consultem.
     */
    private void initializeEnemies() {
        int curFloor = game.getCurrentFloor();
        var floors = game.getFloors();

        if (floors == null || curFloor < 0 || curFloor >= floors.length) {
            System.err.println("[StateCombat] [" + game.getId() + "] ERROR - currentFloor=" + curFloor
                    + " fora de rang o mapa no inicialitzat. Combat sense enemics!");
            return;
        }

        int biomeId = floors[curFloor].biomeId;
        if (biomeId <= 0) {
            System.err.println("[StateCombat] [" + game.getId() + "] WARN - El pis " + curFloor
                    + " no té biomeId vàlid (=" + biomeId + "). Combat sense enemics!");
            return;
        }

        List<EnemyBiome> enemyBiomes = repoProvider.RepoEnemyBiome.findByBiomeId(biomeId);

        if (enemyBiomes.isEmpty()) {
            System.err.println("[StateCombat] [" + game.getId() + "] WARN - No hi ha cap entrada a ENEMY_BIOMES per al bioma bi_id="
                    + biomeId + " (pis " + curFloor + "). Combat sense enemics!");
            return;
        }

        // Ordenem per enemyId per garantir un ordre estable als torns
        enemyBiomes.sort(Comparator.comparingInt(eb -> eb.getEnemy().getId()));

        int combatId = 0;
        for (EnemyBiome eb : enemyBiomes) {
            Enemy enemy = eb.getEnemy();
            int quantity = eb.getQuantity();

            // Guardem l'entitat al cache per poder reutilitzar nom/imatge als broadcasts
            enemyCache.put(enemy.getId(), enemy);

            // Carregar els action IDs de l'enemic (carregats via JOIN FETCH a findByBiomeId)
            List<Integer> actionIds = new ArrayList<>();
            if (enemy.getActs() != null) {
                for (Act act : enemy.getActs()) {
                    actionIds.add(act.getId());
                }
            }
            if (actionIds.isEmpty()) {
                System.out.println("[StateCombat] [" + game.getId() + "] WARN - Enemic " + enemy.getId()
                        + " (" + enemy.getName() + ") no té accions a YUMEDANT_ACT. Usarà BASIC_ATTACK_ID=" + BASIC_ATTACK_ID + " com a fallback.");
                actionIds.add(BASIC_ATTACK_ID);
            }

            for (int i = 0; i < quantity; i++) {
                EnemyCombatState state = new EnemyCombatState(
                        combatId++,
                        enemy.getId(),
                        enemy.getHp(),
                        enemy.getMp(),
                        enemy.getAtk(),
                        enemy.getDef(),
                        enemy.getLevel(),
                        new ArrayList<>(actionIds)
                );
                enemyStates.put(state.getCombatId(), state);
            }
        }

        System.out.println("[StateCombat] [" + game.getId() + "] " + enemyStates.size()
                + " enemics creats per al bioma bi_id=" + biomeId + ".");
    }

    /**
     * Per a cada jugador, carrega les accions vàlides del seu personatge
     * (YUMEDANT_ACT) i el cost MP de cada una (ABILITY.ab_cost).
     * El resultat es guarda a PlayerCharacterState.validActionMpCosts per
     * poder fer validació ràpida quan arriba un PlayerAction_IN.
     */
    private void initializePlayerActions() {
        for (Map.Entry<Long, PlayerCharacterState> entry : game.getPlayerStates().entrySet()) {
            long playerId = entry.getKey();
            PlayerCharacterState pcs = entry.getValue();

            Optional<YuCharacter> opt = repoProvider.RepoYuCharacter.findByIdWithActs(pcs.getCharacterId());
            if (opt.isEmpty()) {
                System.err.println("[StateCombat] [" + game.getId() + "] WARN - No s'ha trobat el personatge "
                        + pcs.getCharacterId() + " per al jugador " + playerId + ". Registrant BASIC_ATTACK_ID com a fallback.");
                pcs.registerAction(BASIC_ATTACK_ID, 0);
                continue;
            }

            YuCharacter character = opt.get();
            if (character.getActs() == null || character.getActs().isEmpty()) {
                System.err.println("[StateCombat] [" + game.getId() + "] WARN - El personatge " + pcs.getCharacterId()
                        + " no té accions a YUMEDANT_ACT. Registrant BASIC_ATTACK_ID com a fallback.");
                pcs.registerAction(BASIC_ATTACK_ID, 0);
                continue;
            }

            for (Act act : character.getActs()) {
                int mpCost = 0;
                if (act instanceof Ability) {
                    Integer cost = ((Ability) act).getCost();
                    mpCost = (cost != null) ? cost : 0;
                }
                pcs.registerAction(act.getId(), mpCost);
            }

            System.out.println("[StateCombat] [" + game.getId() + "] Jugador " + playerId
                    + " (char " + pcs.getCharacterId() + "): " + pcs.getValidActionMpCosts().size() + " accions registrades: "
                    + pcs.getValidActionMpCosts());
        }
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
            sendInvalid(player, NOT_YOUR_TURN.value);
            return;
        }

        // Validació: el jugador està viu?
        PlayerCharacterState attacker = game.getPlayerState(msg.playerId);
        if (attacker == null || attacker.getCurrentHp() <= 0) {
            sendInvalid(player, INVALID_PLAYER.value);
            return;
        }

        // Validació: l'actionId pertany al personatge (YUMEDANT_ACT)?
        if (!attacker.isActionValid(msg.actionId)) {
            System.out.println("[StateCombat] [" + game.getId() + "] FAIL - actionId " + msg.actionId + " no vàlid per al jugador " + msg.playerId + ".");
            sendInvalid(player, INVALID_ACTION.value);
            return;
        }

        // Validació: MP suficient?
        int mpCost = attacker.getActionMpCost(msg.actionId);
        if (attacker.getCurrentMp() < mpCost) {
            System.out.println("[StateCombat] [" + game.getId() + "] FAIL - Jugador " + msg.playerId + " no té prou MP (té "
                    + attacker.getCurrentMp() + ", necessita " + mpCost + ").");
            sendInvalid(player, NOT_ENOUGH_MP.value);
            return;
        }

        // Consumir MP
        if (mpCost > 0) {
            attacker.setCurrentMp(attacker.getCurrentMp() - mpCost);
        }

        // Carregar l'acció amb els seus efectes i aplicar-los
        String tt = msg.targetType != null ? msg.targetType : "E";
        resolveAndApplyEffects(msg.actionId, msg.playerId, "PLAYER", msg.targetCombatId, tt);

        // Confirmació individual al jugador
        game.send(player.getSession(), new JSONMessage(game.getId(),
                new ActionResult_OUT(true, OK.value)));

        // Comprovar fi de combat
        if (checkCombatEnd()) return;

        // Avançar el torn i actualitzar estat
        advanceTurn();
        broadcastCombatUpdate();
    }

    // ===================================================================
    // ACCIONS DELS ENEMICS
    // ===================================================================

    /**
     * Executa el torn complet dels enemics: cada enemic viu, en ordre per
     * combatId, tria una acció aleatòria de la seva llista i l'aplica
     * contra un jugador viu aleatori. Comprova fi del combat a cada pas.
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

            if (alive.isEmpty()) break;

            long targetPlayerId = alive.get(rng.nextInt(alive.size()));

            // Triar acció aleatòria de la llista de l'enemic
            List<Integer> actionIds = enemy.getActionIds();
            int chosenActionId = actionIds.get(rng.nextInt(actionIds.size()));

            // Broadcast de l'acció de l'enemic (per animació al client)
            game.broadcast(new JSONMessage(game.getId(),
                    new EnemyAction_OUT(enemy.getCombatId(), enemy.getEnemyId(), chosenActionId, targetPlayerId)));

            // Aplicar els efectes de l'acció
            // Per a un enemic, el target "E" del seu efecte apunta a un jugador concret
            resolveAndApplyEffects(chosenActionId, enemy.getCombatId(), "ENEMY", (int) targetPlayerId, "E");

            // Comprovar fi del combat després de cada atac enemic
            if (checkCombatEnd()) return;
        }

        // Final del round
        round++;
        advanceTurnToFirstPlayer();
        broadcastCombatUpdate();
    }

    // ===================================================================
    // SISTEMA D'EFECTES
    // ===================================================================

    /**
     * Carrega un Act des de la BD (amb ActionEffects i ParameterValues precarregats)
     * i aplica cada ActionEffect als targets corresponents.
     *
     * Si l'Act no existeix o no té efectes definits, fa fallback a atac simple (atk - def).
     *
     * @param actionId            ID de l'acció (ac_id)
     * @param actorId             ID de l'actor (playerId o combatId de l'enemic)
     * @param actorType           "PLAYER" o "ENEMY"
     * @param requestedTargetId   Target hint del client (combatId enemic o playerId)
     * @param requestedTargetType String del client o intern ("E", "T", "S", "A", "W", "SA",
     *                            "ENEMY", "ALL_ENEMIES", "SELF", "PLAYER", "TEAM")
     */
    private void resolveAndApplyEffects(int actionId, long actorId, String actorType,
                                        long requestedTargetId, String requestedTargetType) {

        // Normalitzar el targetType del client al format intern de la BD
        String normTarget = normalizeTargetType(requestedTargetType);

        Optional<Act> actOpt = repoProvider.RepoAct.findByIdWithEffects(actionId);

        if (actOpt.isEmpty()) {
            System.err.println("[StateCombat] [" + game.getId() + "] WARN - Act " + actionId + " no trobat a BD. Fallback a atac simple.");
            applyFallbackDamage(actorId, actorType, requestedTargetId, normTarget);
            return;
        }

        Act act = actOpt.get();
        List<ActionEffect> effects = act.getActionEffects();

        if (effects == null || effects.isEmpty()) {
            System.err.println("[StateCombat] [" + game.getId() + "] WARN - Act " + actionId
                    + " (" + act.getName() + ") sense efectes definits. Fallback a atac simple.");
            applyFallbackDamage(actorId, actorType, requestedTargetId, normTarget);
            return;
        }

        for (ActionEffect ae : effects) {
            applyOneEffect(ae, actorId, actorType, requestedTargetId, normTarget);
        }
    }

    /**
     * Normalitza el targetType que ve del client al codi de un sol caràcter
     * usat a la BD (PARAMETER_VALUE.pv_string_value per al Target).
     * Primer intenta fer match contra el TargetValues enum; si no, usa el
     * valor directament (per als codis compostos com "SA").
     */
    private String normalizeTargetType(String clientTargetType) {
        if (clientTargetType == null) return String.valueOf(TargetValues.ENEMY.value);
        // Intentar match directe per nom de l'enum (ENEMY, ALL_ENEMIES, SELF, ALLY, TEAM)
        try {
            TargetValues tv = TargetValues.valueOf(clientTargetType.toUpperCase());
            return String.valueOf(tv.value);
        } catch (IllegalArgumentException ignored) {
            // No és un nom d'enum: pot ser ja el codi curt (E, T, S, A, W, SA)
            return clientTargetType;
        }
    }

    /**
     * Aplica un sol ActionEffect: llegeix els ParameterValues, determina targets
     * i executa la lògica de l'efecte.
     */
    private void applyOneEffect(ActionEffect ae, long actorId, String actorType,
                                long requestedTargetId, String requestedTargetType) {

        // Construir mapa pt_id → ParameterValue per accés ràpid
        Map<Integer, ParameterValue> pvMap = new HashMap<>();
        if (ae.getParameterValues() != null) {
            for (ParameterValue pv : ae.getParameterValues()) {
                pvMap.put(pv.getId().getParameterTypeId(), pv);
            }
        }

        int magnitude  = safeNum(pvMap.get(PT_MAGNITUDE),  1);
        String target  = safeStr(pvMap.get(PT_TARGET),     requestedTargetType);
        String refAttr = safeStr(pvMap.get(PT_REF_ATTR),   "ATK");

        int effectId     = ae.getEffect() != null ? ae.getEffect().getId()   : -1;
        String effectName = ae.getEffect() != null ? ae.getEffect().getName() : "?";

        System.out.println("[StateCombat] [" + game.getId() + "] Efecte ef_id=" + effectId + " (" + effectName
                + ") | magnitude=" + magnitude + " target=" + target + " refAttr=" + refAttr);

        switch (effectId) {
            case EF_SIMPLE_DAMAGE:
                applySimpleDamageEffect(actorId, actorType, requestedTargetId, target, magnitude, refAttr);
                break;
            case EF_CURA_PERCENTUAL:
                applyCuraPercentualEffect(actorId, actorType, requestedTargetId, target, magnitude);
                break;
            case EF_CURA_ABSOLUTA:
                applyCuraAbsolutaEffect(actorId, actorType, requestedTargetId, target, magnitude);
                break;
            case EF_ESTAT_ALTERAT:
                System.out.println("[StateCombat] [" + game.getId() + "] TODO - Estat alterat no implementat (PlayerCharacterState sense alteredStates).");
                break;
            default:
                System.err.println("[StateCombat] [" + game.getId() + "] WARN - Efecte ef_id=" + effectId + " (" + effectName + ") desconegut. Ignorant.");
        }
    }

    // -----------------------------------------------------------------------
    // Efecte 1: Simple Damage   damage = max(0, attackerStat * magnitude - defDef)
    // -----------------------------------------------------------------------

    private void applySimpleDamageEffect(long actorId, String actorType,
                                         long requestedTargetId, String effectTarget,
                                         int magnitude, String refAttr) {
        int attackerStat = getActorStat(actorId, actorType, refAttr);
        List<long[]> targets = resolveTargets(effectTarget, requestedTargetId, actorId, actorType);

        for (long[] t : targets) {
            long targetId   = t[0];
            String targetType = (t[1] == 0) ? "PLAYER" : "ENEMY";
            int defenderDef = getTargetDef(targetId, targetType);
            int damage = Math.max(0, attackerStat * magnitude - defenderDef);
            applyDamage(damage, actorId, actorType, targetId, targetType);
        }
    }

    // -----------------------------------------------------------------------
    // Efecte 2: Cura percentual   heal = (maxHP * magnitude) / 100
    // -----------------------------------------------------------------------

    private void applyCuraPercentualEffect(long actorId, String actorType,
                                           long requestedTargetId, String effectTarget,
                                           int magnitude) {
        List<long[]> targets = resolveTargets(effectTarget, requestedTargetId, actorId, actorType);
        for (long[] t : targets) {
            long targetId   = t[0];
            String targetType = (t[1] == 0) ? "PLAYER" : "ENEMY";
            if (!"PLAYER".equals(targetType)) {
                System.out.println("[StateCombat] [" + game.getId() + "] INFO - Cura percentual sobre enemic ignorada (combatId=" + targetId + ").");
                continue;
            }
            PlayerCharacterState pcs = game.getPlayerState(targetId);
            if (pcs == null || pcs.getCurrentHp() <= 0) continue;
            int healAmount = (pcs.getMaxHp() * magnitude) / 100;
            int oldHp = pcs.healHp(healAmount);
            System.out.println("[StateCombat] [" + game.getId() + "] PLAYER " + targetId
                    + " curat (%) " + oldHp + " → " + pcs.getCurrentHp() + " (+" + healAmount + " HP).");
        }
    }

    // -----------------------------------------------------------------------
    // Efecte 3: Cura absoluta   heal = magnitude (HP fix)
    // -----------------------------------------------------------------------

    private void applyCuraAbsolutaEffect(long actorId, String actorType,
                                         long requestedTargetId, String effectTarget,
                                         int magnitude) {
        List<long[]> targets = resolveTargets(effectTarget, requestedTargetId, actorId, actorType);
        for (long[] t : targets) {
            long targetId   = t[0];
            String targetType = (t[1] == 0) ? "PLAYER" : "ENEMY";
            if (!"PLAYER".equals(targetType)) {
                System.out.println("[StateCombat] [" + game.getId() + "] INFO - Cura absoluta sobre enemic ignorada (combatId=" + targetId + ").");
                continue;
            }
            PlayerCharacterState pcs = game.getPlayerState(targetId);
            if (pcs == null || pcs.getCurrentHp() <= 0) continue;
            int oldHp = pcs.healHp(magnitude);
            System.out.println("[StateCombat] [" + game.getId() + "] PLAYER " + targetId
                    + " curat (abs) " + oldHp + " → " + pcs.getCurrentHp() + " (+" + magnitude + " HP).");
        }
    }

    // -----------------------------------------------------------------------
    // Resolució de targets
    // -----------------------------------------------------------------------

    /**
     * Converteix el codi "Target" de l'efecte (E, T, S, A, W, SA) en una llista
     * de parells [id, type] on type=0→PLAYER, type=1→ENEMY.
     *
     * Quan l'efecte permet apuntar a un target concret (E o A), s'usa requestedTargetId
     * com a hint. Per a AoE (T, W, SA) s'ignora.
     *
     * Nota sobre "E" quan l'actor és un ENEMY: el camp "E" de l'efecte apunta a
     * "l'enemic de l'actor", que en un combat P vs E significa un jugador.
     */
    private List<long[]> resolveTargets(String effectTarget, long requestedTargetId,
                                        long actorId, String actorType) {
        List<long[]> result = new ArrayList<>();

        // Comparar contra les constants de TargetValues per llegibilitat
        String eCode  = String.valueOf(TargetValues.ENEMY.value);       // "E"
        String tCode  = String.valueOf(TargetValues.ALL_ENEMIES.value); // "T"
        String sCode  = String.valueOf(TargetValues.SELF.value);        // "S"
        String aCode  = String.valueOf(TargetValues.ALLY.value);        // "A"
        String wCode  = String.valueOf(TargetValues.TEAM.value);        // "W"

        if (effectTarget.equals(eCode)) {
            // Un sol objectiu contrari
            if ("ENEMY".equals(actorType)) {
                // Enemic atacant → target és un jugador concret (requestedTargetId = playerId)
                result.add(new long[]{requestedTargetId, 0}); // 0 = PLAYER
            } else {
                // Jugador atacant → target és un enemic concret (requestedTargetId = combatId)
                result.add(new long[]{requestedTargetId, 1}); // 1 = ENEMY
            }
        } else if (effectTarget.equals(tCode)) {
            // Tots els objectius contraris
            if ("PLAYER".equals(actorType)) {
                for (EnemyCombatState e : enemyStates.values()) {
                    if (e.isAlive()) result.add(new long[]{e.getCombatId(), 1});
                }
            } else {
                for (Map.Entry<Long, PlayerCharacterState> entry : game.getPlayerStates().entrySet()) {
                    if (entry.getValue().getCurrentHp() > 0) result.add(new long[]{entry.getKey(), 0});
                }
            }
        } else if (effectTarget.equals(sCode)) {
            // Self
            if ("PLAYER".equals(actorType)) {
                result.add(new long[]{actorId, 0});
            } else {
                result.add(new long[]{actorId, 1});
            }
        } else if (effectTarget.equals(aCode)) {
            // Un aliat concret (requestedTargetId = playerId del company)
            result.add(new long[]{requestedTargetId, 0});
        } else if (effectTarget.equals(wCode)) {
            // Tots els aliats vius
            for (Map.Entry<Long, PlayerCharacterState> entry : game.getPlayerStates().entrySet()) {
                if (entry.getValue().getCurrentHp() > 0) result.add(new long[]{entry.getKey(), 0});
            }
        } else if ("SA".equals(effectTarget)) {
            // Self + tots els aliats vius (Basic Heal i similars; "SA" no és al TargetValues enum)
            for (Map.Entry<Long, PlayerCharacterState> entry : game.getPlayerStates().entrySet()) {
                if (entry.getValue().getCurrentHp() > 0) result.add(new long[]{entry.getKey(), 0});
            }
        } else {
            System.err.println("[StateCombat] [" + game.getId() + "] WARN - Target desconegut: '" + effectTarget + "'. Ignorant efecte.");
        }

        return result;
    }

    // -----------------------------------------------------------------------
    // Dany i helpers
    // -----------------------------------------------------------------------

    /**
     * Aplica un valor de dany a un target i fa broadcast de TakeDamage_OUT.
     */
    private void applyDamage(int damage, long actorId, String actorType,
                             long targetId, String targetType) {
        int newHp;
        if ("ENEMY".equals(targetType)) {
            EnemyCombatState target = enemyStates.get((int) targetId);
            if (target == null || !target.isAlive()) return;
            target.takeDamage(damage);
            newHp = target.getCurrentHp();
            System.out.println("[StateCombat] [" + game.getId() + "] " + actorType + " " + actorId
                    + " → ENEMY " + targetId + " (" + damage + " dany, HP " + newHp + "/" + target.getMaxHp() + ").");
        } else {
            PlayerCharacterState target = game.getPlayerState(targetId);
            if (target == null || target.getCurrentHp() <= 0) return;
            int oldHp = target.getCurrentHp();
            target.setCurrentHp(Math.max(0, oldHp - damage));
            newHp = target.getCurrentHp();
            System.out.println("[StateCombat] [" + game.getId() + "] " + actorType + " " + actorId
                    + " → PLAYER " + targetId + " (" + damage + " dany, HP " + newHp + "/" + target.getMaxHp() + ").");
        }
        game.broadcast(new JSONMessage(game.getId(),
                new TakeDamage_OUT(actorId, actorType, targetId, targetType, damage, newHp)));
    }

    /**
     * Fallback quan una acció no té efectes definits a la BD: atac simple atk - def.
     */
    private void applyFallbackDamage(long actorId, String actorType,
                                     long targetId, String targetType) {
        int atk = getActorStat(actorId, actorType, "ATK");
        int def = getTargetDef(targetId, targetType);
        int damage = Math.max(0, atk - def);
        applyDamage(damage, actorId, actorType, targetId, targetType);
    }

    /**
     * Retorna l'stat indicat per refAttr de l'actor (jugador o enemic).
     * Suporta "ATK", "DEF", "HP", "MP". Per defecte retorna ATK.
     */
    private int getActorStat(long actorId, String actorType, String refAttr) {
        if ("PLAYER".equals(actorType)) {
            PlayerCharacterState pcs = game.getPlayerState(actorId);
            if (pcs == null) return 0;
            switch (refAttr) {
                case "ATK": return pcs.getAtk();
                case "DEF": return pcs.getDef();
                case "HP":  return pcs.getCurrentHp();
                case "MP":  return pcs.getCurrentMp();
                default:    return pcs.getAtk();
            }
        } else {
            EnemyCombatState ecs = enemyStates.get((int) actorId);
            if (ecs == null) return 0;
            switch (refAttr) {
                case "ATK": return ecs.getAtk();
                case "DEF": return ecs.getDef();
                case "HP":  return ecs.getCurrentHp();
                case "MP":  return ecs.getCurrentMp();
                default:    return ecs.getAtk();
            }
        }
    }

    /** Retorna la DEF del target (per a la fórmula de dany). */
    private int getTargetDef(long targetId, String targetType) {
        if ("PLAYER".equals(targetType)) {
            PlayerCharacterState pcs = game.getPlayerState(targetId);
            return (pcs != null) ? pcs.getDef() : 0;
        } else {
            EnemyCombatState ecs = enemyStates.get((int) targetId);
            return (ecs != null) ? ecs.getDef() : 0;
        }
    }

    /** Llegeix el valor numèric d'un ParameterValue, amb fallback si és null. */
    private int safeNum(ParameterValue pv, int fallback) {
        return (pv != null && pv.getNumValue() != null) ? pv.getNumValue() : fallback;
    }

    /** Llegeix el valor string d'un ParameterValue, amb fallback si és null. */
    private String safeStr(ParameterValue pv, String fallback) {
        return (pv != null && pv.getStringValue() != null) ? pv.getStringValue() : fallback;
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
        boolean anyEnemyAlive  = enemyStates.values().stream().anyMatch(EnemyCombatState::isAlive);
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

        List<Integer> enemyIds = enemyStates.values().stream()
                .map(EnemyCombatState::getEnemyId)
                .distinct()
                .collect(Collectors.toList());
        CombatRewardsResult rewards = repoProvider.LootService.generateCombatRewards(enemyIds, game.getRandom());

        game.broadcast(new JSONMessage(game.getId(), new EndCombat_OUT()));

        if (isBoss) {
            System.out.println("[StateCombat] [" + game.getId() + "] Boss derrotat → StateEndGame(victory=true).");
            applyXpToAllPlayers(rewards.xpGained);
            game.setCurrentState(new StateEndGame(game, objectMapper, repoProvider, true));
        } else {
            System.out.println("[StateCombat] [" + game.getId() + "] Combat normal acabat → StateCombatRewards.");
            game.setCurrentState(new StateCombatRewards(game, objectMapper, repoProvider, rewards));
        }
    }

    private void applyXpToAllPlayers(int xp) {
        for (PlayerCharacterState pcs : game.getPlayerStates().values()) {
            pcs.setExp(pcs.getExp() + xp);
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
                .map(s -> EnemyInfo.from(s, enemyCache.get(s.getEnemyId())))
                .collect(Collectors.toList());

        game.broadcast(new JSONMessage(game.getId(),
                new CombatInfo_OUT(isBoss, enemies, new ArrayList<>(playerTurnOrder), currentTurnPlayerId)));
        System.out.println("[StateCombat] [" + game.getId() + "] COMBAT_INFO broadcastejat. Primer torn: " + currentTurnPlayerId + ".");
    }

    private void broadcastCombatUpdate() {
        List<PlayerCombatSnapshot> playerSnapshots = game.getPlayerStates().values().stream()
                .map(PlayerCombatSnapshot::from)
                .collect(Collectors.toList());

        List<EnemyInfo> enemySnapshots = enemyStates.values().stream()
                .map(s -> EnemyInfo.from(s, enemyCache.get(s.getEnemyId())))
                .collect(Collectors.toList());

        game.broadcast(new JSONMessage(game.getId(),
                new CombatUpdate_OUT(playerSnapshots, enemySnapshots, currentTurnPlayerId, round)));
    }
}
