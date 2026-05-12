package net.iesmila.dam.yumeworld.server.core.states;

import net.iesmila.dam.yumeworld.server.api.messages.JSONMessage;
import net.iesmila.dam.yumeworld.server.api.dto.FloorInfo;
import net.iesmila.dam.yumeworld.server.api.messages.mapexploration.out.MapUpdate_OUT;
import net.iesmila.dam.yumeworld.server.core.GameInstance;
import net.iesmila.dam.yumeworld.server.core.GameMessage;
import net.iesmila.dam.yumeworld.server.db.repository.RepositoryProvider;
import tools.jackson.databind.ObjectMapper;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * Estat on els jugadors veuen el mapa generat i trien quina sala visiten
 * a cada pis. En entrar a aquest estat es fa un broadcast inicial amb tot
 * el mapa (MapUpdate_OUT) llegit del GameInstance.
 *
 * Aquest estat pot ser visitat múltiples vegades durant la partida (cada cop
 * que el grup torna al mapa després d'una sala). El mapa es genera un sol cop
 * a StateMapCreation i persisteix a GameInstance.
 */
public class StateMapVisualization extends State {

    public StateMapVisualization(GameInstance game, ObjectMapper om, RepositoryProvider rp) {
        super(game, om, rp);

        System.out.println("[StateMapVisualization] [" + game.getId() + "] Inicialitzant estat de visualització del mapa.");

        // Llegim el mapa generat per StateMapCreation i el difonem.
        FloorInfo[] floors = game.getFloors();
        if (floors == null || floors.length == 0) {
            System.err.println("[StateMapVisualization] [" + game.getId() + "] ERROR - No hi ha mapa al GameInstance. StateMapCreation no s'ha executat correctament?");
            return;
        }

        MapUpdate_OUT mapUpdate = new MapUpdate_OUT(Arrays.asList(floors));
        game.broadcast(new JSONMessage(game.getId(), mapUpdate));
        System.out.println("[StateMapVisualization] [" + game.getId() + "] MapUpdate_OUT broadcasted (" + floors.length + " pisos).");
    }

    @Override
    public void tick() {
        GameMessage message = game.pollMessage(5, TimeUnit.SECONDS);

        if (message != null) {
            try {
                JSONMessage gm = objectMapper.readValue(message.payload(), JSONMessage.class);
                System.out.println("[StateMapVisualization] [" + game.getId() + "] TICK - Rebut missatge de tipus: " + gm.messageType + " del jugador " + message.user().getId());

                // TODO: gestionar MoveToNode_IN, vot per la sala següent, etc.
                System.out.println("[StateMapVisualization] [" + game.getId() + "] Tipus de missatge encara no implementat: " + gm.messageType);
            } catch (Exception e) {
                System.err.println("[StateMapVisualization] [" + game.getId() + "] Error parsejant el missatge: " + e.getMessage());
            }
        }
    }
}
