package net.iesmila.dam.yumeworld.server.core.states;

import net.iesmila.dam.yumeworld.server.api.dto.CharacterModification;
import net.iesmila.dam.yumeworld.server.api.messages.JSONMessage;
import net.iesmila.dam.yumeworld.server.api.messages.generic.out.ActionResult_OUT;
import net.iesmila.dam.yumeworld.server.api.messages.generic.out.CharacterUpdate_OUT;
import net.iesmila.dam.yumeworld.server.api.messages.mapexploration.out.RestStart_OUT;
import net.iesmila.dam.yumeworld.server.api.messages.rest.in.LightCampfire_IN;
import net.iesmila.dam.yumeworld.server.api.messages.generic.in.ReadyToLeave_IN;
import net.iesmila.dam.yumeworld.server.api.messages.rest.out.CampfireLit_OUT;
import net.iesmila.dam.yumeworld.server.api.messages.generic.out.PlayersReadyToLeave_OUT;
import net.iesmila.dam.yumeworld.server.api.messages.generic.out.ReturnToMap_OUT;
import net.iesmila.dam.yumeworld.server.api.models.Player;
import net.iesmila.dam.yumeworld.server.api.models.PlayerCharacterState;
import net.iesmila.dam.yumeworld.server.core.GameInstance;
import net.iesmila.dam.yumeworld.server.core.GameMessage;
import net.iesmila.dam.yumeworld.server.db.repository.RepositoryProvider;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static net.iesmila.dam.yumeworld.server.core.enums.Codes.*;

/**
 * Estat de la sala de descans.
 *
 * Flux:
 *   1. Entrada → broadcast RestInfo_OUT (foguera apagada).
 *   2. El primer jugador clica la foguera → LightCampfire_IN.
 *   3. Servidor cura tothom (HP +HP_HEAL_RATIO, MP +MP_HEAL_RATIO sobre el màxim).
 *      Per cada jugador que canvia stats → CharacterUpdate_OUT (broadcast).
 *      Broadcast CampfireLit_OUT.
 *   4. Cada jugador envia ReadyToLeave_IN al seu ritme.
 *      Broadcast PlayersReadyToLeave_OUT amb la llista actualitzada.
 *   5. Quan tots han enviat ReadyToLeave_IN → broadcast ReturnToMap_OUT
 *      i transició a StateMapVisualization.
 *
 * Validacions:
 *   - LightCampfire_IN amb la foguera ja encesa → ActionResult_OUT amb error suau.
 *   - ReadyToLeave_IN duplicat del mateix jugador → ignorat sense error.
 *   - Jugadors al màxim de HP/MP → no es curen ni s'envia CharacterUpdate_OUT.
 *   - Missatges no esperats → log warning, sense respondre.
 */
public class StateRest extends State {

    // TODO: portar aquests valors a config.properties si volem balanceig sense recompilar.
    private static final double HP_HEAL_RATIO = 0.30;  // +30% del HP màxim
    private static final double MP_HEAL_RATIO = 0.50;  // +50% del MP màxim

    private boolean campfireLit = false;
    private final Set<Long> playersReadyToLeave = new HashSet<>();

    public StateRest(GameInstance game, ObjectMapper objectMapper, RepositoryProvider repoProvider) {
        super(game, objectMapper, repoProvider);

        System.out.println("[StateRest] [" + game.getId() + "] Inicialitzant estat de descans. Foguera apagada.");
        game.broadcast(new JSONMessage(game.getId(), new RestStart_OUT()));
    }

    @Override
    public void tick() {
        GameMessage message = game.pollMessage(5, TimeUnit.SECONDS);
        if (message == null) return;

        try {
            JSONMessage gm = objectMapper.readValue(message.payload(), JSONMessage.class);
            System.out.println("[StateRest] [" + game.getId() + "] TICK - Rebut missatge de tipus: " + gm.messageType + " del jugador " + message.user().getId());

            if (LightCampfire_IN.TYPE.equals(gm.messageType)) {
                handleLightCampfire(message.user(), gm);
            } else if (ReadyToLeave_IN.TYPE.equals(gm.messageType)) {
                handleReadyToLeave(message.user(), gm);
            } else {
                System.out.println("[StateRest] [" + game.getId() + "] Tipus de missatge desconegut o no esperat en aquest estat: " + gm.messageType);
            }
        } catch (Exception e) {
            System.err.println("[StateRest] [" + game.getId() + "] Error parsejant el missatge: " + e.getMessage());
        }
    }

    /**
     * Encén la foguera (si no està ja encesa), cura tothom i broadcasteja els canvis.
     */
    private void handleLightCampfire(Player player, JSONMessage gm) {
        LightCampfire_IN msg = objectMapper.treeToValue(gm.data, LightCampfire_IN.class);

        // Validació: el jugador és qui diu ser?
        if (player.getId() != msg.playerId) {
            System.out.println("[StateRest] [" + game.getId() + "] FAIL - Sessió " + player.getId() + " diu ser jugador " + msg.playerId);
            game.send(player.getSession(), new JSONMessage(game.getId(),
                    new ActionResult_OUT(false, INVALID_PLAYER.value)));
            return;
        }

        // Validació: la foguera ja està encesa?
        if (campfireLit) {
            System.out.println("[StateRest] [" + game.getId() + "] FAIL - La foguera ja està encesa, el jugador " + msg.playerId + " hi arriba tard.");
            // Resposta individual suau: el client mostra "ja s'ha encès".
            game.send(player.getSession(), new JSONMessage(game.getId(),
                    new ActionResult_OUT(false, CAMPFIRE_ALREADY_LIT.value)));
            return;
        }

        // Cures: per a cada jugador, calcular delta HP/MP i broadcastejar si hi ha canvis
        System.out.println("[StateRest] [" + game.getId() + "] Foguera encesa pel jugador " + msg.playerId + ". Aplicant curació al grup.");
        for (PlayerCharacterState state : game.getPlayerStates().values()) {
            int hpIncrement = (int) Math.round(state.getMaxHp() * HP_HEAL_RATIO);
            int mpIncrement = (int) Math.round(state.getMaxMp() * MP_HEAL_RATIO);

            int oldHp = state.getCurrentHp();
            int oldMp = state.getCurrentMp();

            state.healHp(hpIncrement);
            state.healMp(mpIncrement);

            int newHp = state.getCurrentHp();
            int newMp = state.getCurrentMp();

            // Q8: si el jugador ja estava al màxim de tot, no enviem missatge
            boolean hpChanged = (newHp != oldHp);
            boolean mpChanged = (newMp != oldMp);

            if (hpChanged || mpChanged) {
                CharacterModification mod = new CharacterModification();
                mod.id = state.getCharacterId();
                mod.old_hp = oldHp;
                mod.new_hp = newHp;
                mod.old_mp = oldMp;
                mod.new_mp = newMp;
                game.broadcast(new JSONMessage(game.getId(), new CharacterUpdate_OUT(mod)));
                System.out.println("[StateRest] [" + game.getId() + "] Jugador " + state.getPlayerId() + ": HP " + oldHp + "→" + newHp + ", MP " + oldMp + "→" + newMp);
            } else {
                System.out.println("[StateRest] [" + game.getId() + "] Jugador " + state.getPlayerId() + " ja estava al màxim, no es cura.");
            }
        }

        campfireLit = true;
        game.broadcast(new JSONMessage(game.getId(), new CampfireLit_OUT(msg.playerId)));
        System.out.println("[StateRest] [" + game.getId() + "] Curació completada. CampfireLit_OUT broadcastejat.");
    }

    /**
     * Registra que un jugador ja està llest per sortir. Quan tots ho estiguin,
     * fa broadcast de ReturnToMap_OUT i transició a StateMapVisualization.
     */
    private void handleReadyToLeave(Player player, JSONMessage gm) {
        ReadyToLeave_IN msg = objectMapper.treeToValue(gm.data, ReadyToLeave_IN.class);

        // Validació: el jugador és qui diu ser?
        if (player.getId() != msg.playerId) {
            System.out.println("[StateRest] [" + game.getId() + "] FAIL - Sessió " + player.getId() + " diu ser jugador " + msg.playerId);
            game.send(player.getSession(), new JSONMessage(game.getId(),
                    new ActionResult_OUT(false, INVALID_PLAYER.value)));
            return;
        }

        // Si ja l'havia enviat, ignorem silenciosament
        if (!playersReadyToLeave.add(msg.playerId)) {
            System.out.println("[StateRest] [" + game.getId() + "] Jugador " + msg.playerId + " ja havia confirmat sortir. Ignorat.");
            return;
        }

        System.out.println("[StateRest] [" + game.getId() + "] Jugador " + msg.playerId + " llest per sortir (" + playersReadyToLeave.size() + "/" + game.getPlayers().size() + ").");

        // Broadcast amb la llista actualitzada
        List<Long> readyList = new ArrayList<>(playersReadyToLeave);
        game.broadcast(new JSONMessage(game.getId(), new PlayersReadyToLeave_OUT(readyList)));

        // Si tots estan llests, transició
        if (playersReadyToLeave.size() >= game.getPlayers().size()) {
            System.out.println("[StateRest] [" + game.getId() + "] >>> TOTS ELS JUGADORS ESTAN LLESTOS PER SORTIR <<<");
            game.broadcast(new JSONMessage(game.getId(), new ReturnToMap_OUT()));
            game.setCurrentState(new StateMapVisualization(game, objectMapper, repoProvider));
        }
    }
}
