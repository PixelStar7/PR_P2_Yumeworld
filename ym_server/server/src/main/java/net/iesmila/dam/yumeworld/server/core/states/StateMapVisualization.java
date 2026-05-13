package net.iesmila.dam.yumeworld.server.core.states;

import net.iesmila.dam.yumeworld.server.api.messages.JSONMessage;
import net.iesmila.dam.yumeworld.server.api.dto.FloorInfo;
import net.iesmila.dam.yumeworld.server.api.messages.characterselection.in.PickCharacter_IN;
import net.iesmila.dam.yumeworld.server.api.messages.generic.out.ActionResult_OUT;
import net.iesmila.dam.yumeworld.server.api.messages.mapexploration.in.MoveToNode_IN;
import net.iesmila.dam.yumeworld.server.api.messages.mapexploration.out.MapUpdate_OUT;
import net.iesmila.dam.yumeworld.server.api.models.Player;
import net.iesmila.dam.yumeworld.server.core.GameInstance;
import net.iesmila.dam.yumeworld.server.core.GameMessage;
import net.iesmila.dam.yumeworld.server.db.repository.RepositoryProvider;
import tools.jackson.databind.ObjectMapper;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import static net.iesmila.dam.yumeworld.server.core.enums.Codes.*;

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

                if (MoveToNode_IN.TYPE.equals(gm.messageType)) {
                    handleMoveToNode(message.user(), gm);
                }else{
                    System.out.println("[StateMapVisualization] [" + game.getId() + "] Tipus de missatge encara no implementat: " + gm.messageType);
                }

            } catch (Exception e) {
                System.err.println("[StateMapVisualization] [" + game.getId() + "] Error parsejant el missatge: " + e.getMessage());
            }
        }
    }

    private void handleMoveToNode(Player p, JSONMessage jsonMsg) {
        //Comprovacions de seguretat
        MoveToNode_IN message = objectMapper.treeToValue(jsonMsg.data, MoveToNode_IN.class);
       boolean floorValida = game.getCurrentFloor() != message.floorNum -1;
       //La room ha de ser positiva i menor igual al numero de nodes.
       boolean roomValida = message.roomId >= 0 && message.roomId < game.getFloors()[game.getCurrentFloor()].rooms.size();
       //També hem de comprovar que la room és == current o ==current-1 o ==current+1
        roomValida = roomValida && (game.getCurrentRoomId()==message.roomId || game.getCurrentRoomId()==message.roomId-1 || game.getCurrentRoomId()==message.roomId+1);

        if(!floorValida){
            ActionResult_OUT result = new ActionResult_OUT(false, INVALID_FLOOR.value);
            game.send(p.getSession(), new JSONMessage(game.getId(), result));
            System.out.println("[StateMapVisualization] [" + game.getId() + "] ERROR en la floor enviada.");
        }
        if(!roomValida) {
            ActionResult_OUT result = new ActionResult_OUT(false, INVALID_ROOM.value);
            game.send(p.getSession(), new JSONMessage(game.getId(), result));
            System.out.println("[StateMapVisualization] ["+game.getId() + "] ERROR en la room enviada.");
        }
        if(!floorValida || !roomValida){
            return;
        }
        System.out.println("[StateMapVisualization] [" + game.getId() + "] Movent-se des de node "+ game.getCurrentFloor()+"."+game.getCurrentRoomId()+ " cap al node "+ message.floorNum+"."+message.roomId);
        //TODO: Actualitzar isCurrentRoom abans de canviar de sala


        game.setCurrentFloor(message.floorNum);
        game.setCurrentRoomId(message.roomId);

        //Enviar missatge
        MapUpdate_OUT mapUpdate = new MapUpdate_OUT(Arrays.asList(game.getFloors()));


    }
}
