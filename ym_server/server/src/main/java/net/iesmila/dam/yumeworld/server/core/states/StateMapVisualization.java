package net.iesmila.dam.yumeworld.server.core.states;

import net.iesmila.dam.yumeworld.server.api.dto.FloorInfo;
import net.iesmila.dam.yumeworld.server.api.dto.RoomInfo;
import net.iesmila.dam.yumeworld.server.api.messages.JSONMessage;
import net.iesmila.dam.yumeworld.server.api.messages.generic.out.ActionResult_OUT;
import net.iesmila.dam.yumeworld.server.api.messages.mapexploration.in.MoveToNode_IN;
import net.iesmila.dam.yumeworld.server.api.messages.mapexploration.out.MapUpdate_OUT;
import net.iesmila.dam.yumeworld.server.api.models.Player;
import net.iesmila.dam.yumeworld.server.core.GameInstance;
import net.iesmila.dam.yumeworld.server.core.GameMessage;
import net.iesmila.dam.yumeworld.server.core.enums.RoomTypes;
import net.iesmila.dam.yumeworld.server.db.repository.RepositoryProvider;
import tools.jackson.databind.ObjectMapper;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import static net.iesmila.dam.yumeworld.server.core.enums.Codes.*;

/**
 * Estat on els jugadors veuen el mapa generat i trien quina sala visiten
 * a cada pis.
 *
 * Aquest estat pot ser visitat múltiples vegades durant la partida (cada cop
 * que el grup torna al mapa després d'una sala). El mapa es genera un sol cop
 * a StateMapCreation i persisteix a GameInstance.
 *
 * Flux:
 *   1. Entrada: si tornem d'una sub-state (currentRoomId >= 0), marquem la
 *      sala visitada com is_cleared=true. Després broadcast MapUpdate_OUT
 *      amb floors + currentFloor + currentRoomId.
 *   2. tick() espera MoveToNode_IN. El primer en arribar guanya.
 *      - Validacions: identitat del jugador, floor vàlid, room vàlid
 *        (adjacència aplicada).
 *   3. Si vàlid: actualitzar currentFloor/currentRoomId al GameInstance,
 *      broadcast MapUpdate_OUT (feedback immediat) i transició a la sub-state
 *      segons el RoomType.
 *
 * Adjacència:
 *   - Primera entrada (currentFloor == -1): qualsevol node del pis 0.
 *   - Després: floor destí = currentFloor + 1; room destí adjacent
 *     (|roomId - currentRoomId| <= 1) o, si el pis destí té un sol node
 *     (boss), qualsevol previ hi pot anar.
 */
public class StateMapVisualization extends State {

    public StateMapVisualization(GameInstance game, ObjectMapper om, RepositoryProvider rp) {
        super(game, om, rp);

        System.out.println("[StateMapVisualization] [" + game.getId() + "] Inicialitzant estat de visualització del mapa.");

        FloorInfo[] floors = game.getFloors();
        if (floors == null || floors.length == 0) {
            System.err.println("[StateMapVisualization] [" + game.getId() + "] ERROR - No hi ha mapa al GameInstance. StateMapCreation no s'ha executat correctament?");
            return;
        }

        // Si tornem d'una sub-state (sala visitada), marquem-la com a completada
        int curFloor = game.getCurrentFloor();
        int curRoom = game.getCurrentRoomId();
        if (curFloor >= 0 && curFloor < floors.length && curRoom >= 0) {
            FloorInfo floor = floors[curFloor];
            if (curRoom < floor.rooms.size()) {
                RoomInfo room = floor.rooms.get(curRoom);
                if (!room.is_cleared) {
                    room.is_cleared = true;
                    System.out.println("[StateMapVisualization] [" + game.getId() + "] Sala " + curFloor + "." + curRoom + " marcada com a is_cleared=true.");
                }
            }
        }

        // Broadcast inicial del mapa amb la posició actual del grup
        MapUpdate_OUT mapUpdate = new MapUpdate_OUT(Arrays.asList(floors), curFloor, curRoom);
        game.broadcast(new JSONMessage(game.getId(), mapUpdate));
        System.out.println("[StateMapVisualization] [" + game.getId() + "] MapUpdate_OUT broadcasted (" + floors.length + " pisos, currentFloor=" + curFloor + ", currentRoomId=" + curRoom + ").");
        System.out.println("[StateMapVisualization] [" + game.getId() + "] >>>>> CONSTRUCTOR FINAL OK - sortint cap a processLoop <<<<<");
    }

    @Override
    public void tick() {
        // DIAGNÒSTIC: confirmem que el tick s'executa
        System.out.println("[StateMapVisualization] [" + game.getId() + "] tick() entrant a pollMessage. (curFloor=" + game.getCurrentFloor() + ")");

        GameMessage message = game.pollMessage(5, TimeUnit.SECONDS);
        if (message == null) {
            System.out.println("[StateMapVisualization] [" + game.getId() + "] tick() pollMessage retornat null (timeout 5s).");
            return;
        }

        try {
            JSONMessage gm = objectMapper.readValue(message.payload(), JSONMessage.class);
            System.out.println("[StateMapVisualization] [" + game.getId() + "] TICK - Rebut missatge de tipus: " + gm.messageType + " del jugador " + message.user().getId());

            if (MoveToNode_IN.TYPE.equals(gm.messageType)) {
                handleMoveToNode(message.user(), gm);
            } else {
                System.out.println("[StateMapVisualization] [" + game.getId() + "] Tipus de missatge desconegut o no esperat en aquest estat: " + gm.messageType);
            }
        } catch (Exception e) {
            System.err.println("[StateMapVisualization] [" + game.getId() + "] Error parsejant el missatge: " + e.getMessage());
        }
    }

    /**
     * Processa la petició de moure el grup a un node concret. El primer
     * MoveToNode_IN vàlid mou tot el grup; els subseqüents arriben quan ja
     * estem en una altra state i es desestimen.
     */
    private void handleMoveToNode(Player p, JSONMessage jsonMsg) {
        MoveToNode_IN msg = objectMapper.treeToValue(jsonMsg.data, MoveToNode_IN.class);

        // Validació: identitat del jugador
        if (p.getId() != msg.playerId) {
            System.out.println("[StateMapVisualization] [" + game.getId() + "] FAIL - Sessió " + p.getId() + " diu ser jugador " + msg.playerId);
            game.send(p.getSession(), new JSONMessage(game.getId(),
                    new ActionResult_OUT(false, INVALID_PLAYER.value)));
            return;
        }

        FloorInfo[] floors = game.getFloors();
        int curFloor = game.getCurrentFloor();
        int curRoom = game.getCurrentRoomId();

        // Validació: pis destí ha de ser vàlid
        if (!isValidFloor(msg.floorNum, curFloor, floors)) {
            System.out.println("[StateMapVisualization] [" + game.getId() + "] FAIL - Pis " + msg.floorNum + " no vàlid (curFloor=" + curFloor + ").");
            game.send(p.getSession(), new JSONMessage(game.getId(),
                    new ActionResult_OUT(false, INVALID_FLOOR.value)));
            return;
        }

        // Validació: room destí ha de ser vàlida (existeix + adjacència)
        if (!isValidRoom(msg.floorNum, msg.roomId, curFloor, curRoom, floors)) {
            System.out.println("[StateMapVisualization] [" + game.getId() + "] FAIL - Room " + msg.roomId + " no vàlida al pis " + msg.floorNum + " (curRoom=" + curRoom + ").");
            game.send(p.getSession(), new JSONMessage(game.getId(),
                    new ActionResult_OUT(false, INVALID_ROOM.value)));
            return;
        }

        // Actualitzem la posició del grup
        System.out.println("[StateMapVisualization] [" + game.getId() + "] Movent-se de " + curFloor + "." + curRoom + " → " + msg.floorNum + "." + msg.roomId);
        game.setCurrentFloor(msg.floorNum);
        game.setCurrentRoomId(msg.roomId);

        // Resposta individual de confirmació al jugador que ha fet la petició
        game.send(p.getSession(), new JSONMessage(game.getId(),
                new ActionResult_OUT(true, OK.value)));

        // Broadcast MapUpdate_OUT amb la nova posició (feedback immediat al client)
        MapUpdate_OUT mapUpdate = new MapUpdate_OUT(Arrays.asList(floors), msg.floorNum, msg.roomId);
        game.broadcast(new JSONMessage(game.getId(), mapUpdate));

        // Determinem el tipus de sala i transicionem a la sub-state adequada
        RoomInfo targetRoom = floors[msg.floorNum].rooms.get(msg.roomId);
        transitionToRoom(targetRoom.type);
    }

    /**
     * Comprova si el pis destí és vàlid.
     *  - Primera entrada (curFloor==-1): només pis 0.
     *  - Després: el següent al pis actual i dins del rang del mapa.
     */
    private boolean isValidFloor(int targetFloor, int curFloor, FloorInfo[] floors) {
        if (curFloor == -1) {
            return targetFloor == 0;
        }
        if (targetFloor != curFloor + 1) return false;
        if (targetFloor >= floors.length) return false;
        return true;
    }

    /**
     * Comprova si la room destí és vàlida.
     *  - Ha d'existir dins del pis destí.
     *  - Primera entrada (curFloor==-1): qualsevol node del pis 0 és vàlid.
     *  - Després: adjacent al room actual (|roomId - curRoom| <= 1), o
     *    qualsevol si el pis destí només té 1 node (cas del boss).
     *
     * Assumeix que isValidFloor ja ha estat comprovat.
     */
    private boolean isValidRoom(int targetFloor, int targetRoomId, int curFloor, int curRoom, FloorInfo[] floors) {
        int numColumnsDest = floors[targetFloor].rooms.size();
        if (targetRoomId < 0 || targetRoomId >= numColumnsDest) return false;

        // Primera entrada: qualsevol node del pis 0
        if (curFloor == -1) return true;

        // Pis destí té només 1 node (boss): tothom hi pot anar
        if (numColumnsDest == 1) return true;

        // Adjacència estàndard
        return Math.abs(targetRoomId - curRoom) <= 1;
    }

    /**
     * Determina la sub-state segons el RoomType de la sala destí i transiciona.
     */
    private void transitionToRoom(String roomTypeStr) {
        RoomTypes roomType;
        try {
            roomType = RoomTypes.valueOf(roomTypeStr);
        } catch (Exception e) {
            System.err.println("[StateMapVisualization] [" + game.getId() + "] ERROR - RoomType desconegut: " + roomTypeStr);
            return;
        }

        switch (roomType) {
            case COMBAT:
                System.out.println("[StateMapVisualization] [" + game.getId() + "] Transició a StateCombat (normal).");
                game.setCurrentState(new StateCombat(game, objectMapper, repoProvider, false));
                break;
            case BOSS_COMBAT:
                System.out.println("[StateMapVisualization] [" + game.getId() + "] Transició a StateCombat (BOSS).");
                game.setCurrentState(new StateCombat(game, objectMapper, repoProvider, true));
                break;
            case REST_EVENT:
                System.out.println("[StateMapVisualization] [" + game.getId() + "] Transició a StateRest.");
                game.setCurrentState(new StateRest(game, objectMapper, repoProvider));
                break;
            case CHEST_EVENT:
                System.out.println("[StateMapVisualization] [" + game.getId() + "] Transició a StateChest.");
                game.setCurrentState(new StateChest(game, objectMapper, repoProvider));
                break;
            default:
                System.err.println("[StateMapVisualization] [" + game.getId() + "] ERROR - RoomType no gestionat: " + roomType);
        }
    }
}
