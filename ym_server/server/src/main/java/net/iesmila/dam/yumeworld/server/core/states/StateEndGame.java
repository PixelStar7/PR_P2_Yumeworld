package net.iesmila.dam.yumeworld.server.core.states;

import net.iesmila.dam.yumeworld.server.api.messages.JSONMessage;
import net.iesmila.dam.yumeworld.server.core.GameInstance;
import net.iesmila.dam.yumeworld.server.core.GameMessage;
import net.iesmila.dam.yumeworld.server.db.repository.RepositoryProvider;
import tools.jackson.databind.ObjectMapper;

import java.util.concurrent.TimeUnit;

/**
 * Estat final de la partida. Es pot arribar des de:
 *   - StateCombat (BOSS_COMBAT) amb victory=true.
 *   - StateCombat (qualsevol) amb victory=false si tots els jugadors moren.
 *
 * ESQUELETON — pendent d'implementació completa amb GameSummary_OUT,
 * ReturnToLobby_IN, etc.
 */
public class StateEndGame extends State {

    private final boolean victory;

    public StateEndGame(GameInstance game, ObjectMapper objectMapper, RepositoryProvider repositoryProvider) {
        this(game, objectMapper, repositoryProvider, false);
    }

    public StateEndGame(GameInstance game, ObjectMapper objectMapper, RepositoryProvider repositoryProvider, boolean victory) {
        super(game, objectMapper, repositoryProvider);
        this.victory = victory;
        System.out.println("[StateEndGame] [" + game.getId() + "] Final de partida (victory=" + victory + "). TODO: implementació pendent.");
        // TODO: broadcast GameSummary_OUT amb estadístiques finals.
    }

    public boolean isVictory() {
        return victory;
    }

    @Override
    public void tick() {
        GameMessage message = game.pollMessage(5, TimeUnit.SECONDS);
        if (message == null) return;

        try {
            JSONMessage gm = objectMapper.readValue(message.payload(), JSONMessage.class);
            System.out.println("[StateEndGame] [" + game.getId() + "] TICK - Rebut missatge: " + gm.messageType);
            // TODO: gestionar ReturnToLobby_IN
        } catch (Exception e) {
            System.err.println("[StateEndGame] [" + game.getId() + "] Error: " + e.getMessage());
        }
    }
}
