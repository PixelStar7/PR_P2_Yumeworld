package net.iesmila.dam.yumeworld.server.api.states;

import net.iesmila.dam.yumeworld.server.api.messages.JSONMessage;
import net.iesmila.dam.yumeworld.server.components.GameInstance;
import net.iesmila.dam.yumeworld.server.components.GameMessage;
import net.iesmila.dam.yumeworld.server.db.repository.RepositoryProvider;
import tools.jackson.databind.ObjectMapper;

import java.util.concurrent.TimeUnit;

public class StateMapVisualization extends State{
    //TODO: Refactor to StateMapExploration

    public StateMapVisualization(GameInstance game, ObjectMapper om, RepositoryProvider rp){
        super(game, om, rp); // Passem la instància única cap amunt
    }

    @Override
    public void tick() {
        GameMessage message = game.pollMessage(5, TimeUnit.SECONDS);
        if (message != null) {
            // Ha entrat algo
            JSONMessage gm = objectMapper.readValue(message.payload(), JSONMessage.class);
            System.out.println("TICK: " + gm.messageType);

            
        }


    }


}
