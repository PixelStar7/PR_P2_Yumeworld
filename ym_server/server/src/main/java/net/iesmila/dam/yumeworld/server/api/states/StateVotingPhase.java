package net.iesmila.dam.yumeworld.server.api.states;

import net.iesmila.dam.yumeworld.server.api.messages.JSONMessage;
import net.iesmila.dam.yumeworld.server.components.GameInstance;
import net.iesmila.dam.yumeworld.server.components.GameMessage;
import net.iesmila.dam.yumeworld.server.db.repository.RepositoryProvider;
import tools.jackson.databind.ObjectMapper;

import java.util.concurrent.TimeUnit;

public class StateVotingPhase extends State {

    public StateVotingPhase(GameInstance game, ObjectMapper objectMapper, RepositoryProvider repoProvider) {
        super(game, objectMapper, repoProvider);
    }

    @Override
    public void tick() {

        GameMessage message = game.pollMessage(5, TimeUnit.SECONDS);

        if (message != null) {
            // Ha entrat algo
            JSONMessage gm = objectMapper.readValue(message.payload(), JSONMessage.class);
            System.out.println("TICK: " + gm.messageType);
            //TODO: Completar les accions segons el que estigui entrant...
        }
    }
}
