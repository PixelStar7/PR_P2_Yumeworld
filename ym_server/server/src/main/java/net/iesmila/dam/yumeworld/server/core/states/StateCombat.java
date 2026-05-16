package net.iesmila.dam.yumeworld.server.core.states;

import net.iesmila.dam.yumeworld.server.api.messages.JSONMessage;
import net.iesmila.dam.yumeworld.server.core.GameInstance;
import net.iesmila.dam.yumeworld.server.core.GameMessage;
import net.iesmila.dam.yumeworld.server.db.repository.RepositoryProvider;
import tools.jackson.databind.ObjectMapper;

import java.util.concurrent.TimeUnit;

/**
 * Estat de combat. ESQUELETON — pendent d'implementar completament.
 *
 * El flag isBoss diferencia un combat normal d'un combat contra el boss
 * (últim pis). La lògica de combat és la mateixa; al final el boss combat
 * hauria de portar a StateEndGame en cas de victòria.
 */
public class StateCombat extends State {

    private final boolean isBoss;

    /** Combat normal. */
    public StateCombat(GameInstance game, ObjectMapper objectMapper, RepositoryProvider repoProvider) {
        this(game, objectMapper, repoProvider, false);
    }

    /** Combat amb opció de boss (last floor). */
    public StateCombat(GameInstance game, ObjectMapper objectMapper, RepositoryProvider repoProvider, boolean isBoss) {
        super(game, objectMapper, repoProvider);
        this.isBoss = isBoss;
        System.out.println("[StateCombat] [" + game.getId() + "] Inicialitzant combat" + (isBoss ? " (BOSS)" : "") + ". TODO: implementació pendent.");
    }

    public boolean isBoss() {
        return isBoss;
    }

    @Override
    public void tick() {
        GameMessage message = game.pollMessage(5, TimeUnit.SECONDS);
        if (message == null) return;

        try {
            JSONMessage gm = objectMapper.readValue(message.payload(), JSONMessage.class);
            System.out.println("[StateCombat] [" + game.getId() + "] TICK - Rebut missatge de tipus: " + gm.messageType);
            // TODO: gestionar PlayerAction_IN, calcular dany, turnos, etc.
        } catch (Exception e) {
            System.err.println("[StateCombat] [" + game.getId() + "] Error parsejant el missatge: " + e.getMessage());
        }
    }
}
