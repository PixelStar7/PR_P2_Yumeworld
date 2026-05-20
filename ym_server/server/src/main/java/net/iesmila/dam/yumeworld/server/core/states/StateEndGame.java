package net.iesmila.dam.yumeworld.server.core.states;

import net.iesmila.dam.yumeworld.server.api.dto.PlayerSummary;
import net.iesmila.dam.yumeworld.server.api.messages.JSONMessage;
import net.iesmila.dam.yumeworld.server.api.messages.endgame.in.ReturnToLobby_IN;
import net.iesmila.dam.yumeworld.server.api.messages.endgame.out.GameSummary_OUT;
import net.iesmila.dam.yumeworld.server.api.messages.endgame.out.LobbyReturn_OUT;
import net.iesmila.dam.yumeworld.server.api.messages.generic.out.ActionResult_OUT;
import net.iesmila.dam.yumeworld.server.api.models.Player;
import net.iesmila.dam.yumeworld.server.core.GameInstance;
import net.iesmila.dam.yumeworld.server.core.GameMessage;
import net.iesmila.dam.yumeworld.server.db.repository.RepositoryProvider;
import tools.jackson.databind.ObjectMapper;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static net.iesmila.dam.yumeworld.server.core.enums.Codes.*;

/**
 * Estat final de la partida. Es pot arribar des de:
 *   - StateCombat (BOSS_COMBAT) amb victory=true.
 *   - StateCombat (qualsevol) amb victory=false si tots els jugadors moren.
 *
 * Flux per victòria:
 *   1. Broadcast GameSummary_OUT(victory=true, ...).
 *   2. tick() espera ReturnToLobby_IN de cada jugador.
 *   3. Quan tots confirmen → broadcast LobbyReturn_OUT + game.stop().
 *      Les WebSockets queden obertes; el control passa al "lobby" extern.
 *
 * Flux per derrota (Q9-b: auto-tancament):
 *   1. Broadcast GameSummary_OUT(victory=false, ...).
 *   2. Broadcast LobbyReturn_OUT immediatament.
 *   3. game.stop() — no s'esperen confirmacions.
 *
 * TODO: PlayerCharacterState es perd quan el GameInstance s'atura. Si en
 * algun moment volem persistir el progrés (XP, etc.), cal escriure a la BD
 * abans de game.stop().
 */
public class StateEndGame extends State {

    private final boolean victory;
    private final Set<Long> playersWhoReturnToLobby = new HashSet<>();
    private boolean gameStopped = false;

    public StateEndGame(GameInstance game, ObjectMapper objectMapper, RepositoryProvider repositoryProvider) {
        this(game, objectMapper, repositoryProvider, false);
    }

    public StateEndGame(GameInstance game, ObjectMapper objectMapper, RepositoryProvider repositoryProvider, boolean victory) {
        super(game, objectMapper, repositoryProvider);
        this.victory = victory;

        System.out.println("[StateEndGame] [" + game.getId() + "] Fi de partida (victory=" + victory + ").");

        // Broadcast el resum final
        broadcastGameSummary();

        // Q9-b: derrota → auto-tancament sense esperar confirmacions
        if (!victory) {
            System.out.println("[StateEndGame] [" + game.getId() + "] Derrota: auto-tancament.");
            game.broadcast(new JSONMessage(game.getId(), new LobbyReturn_OUT()));
            game.stop();
            gameStopped = true;
        }
    }

    public boolean isVictory() {
        return victory;
    }

    @Override
    public void tick() {
        if (gameStopped) return;

        GameMessage message = game.pollMessage(5, TimeUnit.SECONDS);
        if (message == null) return;

        try {
            JSONMessage gm = objectMapper.readValue(message.payload(), JSONMessage.class);
            System.out.println("[StateEndGame] [" + game.getId() + "] TICK - " + gm.messageType);

            if (ReturnToLobby_IN.TYPE.equals(gm.messageType)) {
                handleReturnToLobby(message.user(), gm);
            } else {
                System.out.println("[StateEndGame] [" + game.getId() + "] Tipus de missatge no esperat: " + gm.messageType);
            }
        } catch (Exception e) {
            System.err.println("[StateEndGame] [" + game.getId() + "] Error: " + e.getMessage());
        }
    }

    /**
     * Registra que un jugador vol tornar al lobby. Quan tots ho fan, broadcast
     * LobbyReturn_OUT i game.stop() (Q7-c).
     */
    private void handleReturnToLobby(Player player, JSONMessage gm) {
        ReturnToLobby_IN msg = objectMapper.treeToValue(gm.data, ReturnToLobby_IN.class);

        if (player.getId() != msg.playerId) {
            game.send(player.getSession(), new JSONMessage(game.getId(),
                    new ActionResult_OUT(false, INVALID_PLAYER.value)));
            return;
        }

        if (!playersWhoReturnToLobby.add((long)msg.playerId)) {
            System.out.println("[StateEndGame] [" + game.getId() + "] Jugador " + msg.playerId + " ja havia confirmat tornar al lobby.");
            return;
        }

        System.out.println("[StateEndGame] [" + game.getId() + "] Jugador " + msg.playerId + " vol tornar al lobby (" + playersWhoReturnToLobby.size() + "/" + game.getPlayers().size() + ").");

        // Confirmació individual
        game.send(player.getSession(), new JSONMessage(game.getId(),
                new ActionResult_OUT(true, OK.value)));

        // Si tots han confirmat, tanquem la partida
        if (playersWhoReturnToLobby.size() >= game.getPlayers().size()) {
            System.out.println("[StateEndGame] [" + game.getId() + "] >>> TOTS HAN CONFIRMAT, TANCANT PARTIDA <<<");
            game.broadcast(new JSONMessage(game.getId(), new LobbyReturn_OUT()));
            game.stop();
            gameStopped = true;
        }
    }

    /**
     * Construeix i envia GameSummary_OUT amb el resum final de la partida.
     * - floorsCleared: deriva de currentFloor + 1 (Q6-a).
     * - playerSummaries: snapshot final de cada PlayerCharacterState.
     */
    private void broadcastGameSummary() {
        int floorsCleared = game.getCurrentFloor() + 1;  // currentFloor és 0-indexed

        List<PlayerSummary> summaries = game.getPlayerStates().values().stream()
                .map(PlayerSummary::from)
                .collect(Collectors.toList());

        game.broadcast(new JSONMessage(game.getId(),
                new GameSummary_OUT(victory, floorsCleared, summaries)));
        System.out.println("[StateEndGame] [" + game.getId() + "] GameSummary_OUT broadcastejat (floorsCleared=" + floorsCleared + ", " + summaries.size() + " jugadors).");
    }
}
