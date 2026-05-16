package net.iesmila.dam.yumeworld.server.api.messages.mapexploration.out;

import net.iesmila.dam.yumeworld.server.api.messages.MessageBody;
import net.iesmila.dam.yumeworld.server.api.dto.FloorInfo;

import java.util.List;

/**
 * Broadcast que envia el servidor amb l'estat del mapa de la partida.
 *
 * S'envia en dues situacions:
 *   1. Al constructor de StateMapVisualization (en entrar al mapa des de
 *      StateMapCreation o tornant d'una sub-state). Si tornem d'una sub-state,
 *      el RoomInfo visitat ja porta is_cleared=true.
 *   2. Després d'un MoveToNode_IN vàlid, just abans de transicionar a la
 *      sub-state. Així el client veu l'actualització del seu node escollit
 *      (currentFloor/currentRoomId nous) abans no rebi l'*Info_OUT específic.
 *
 * currentFloor/currentRoomId són -1 si encara no s'ha visitat cap node
 * (estat inicial després de StateMapCreation).
 */
public class MapUpdate_OUT extends MessageBody {
    public final static String TYPE = "MAP_UPDATE";

    public List<FloorInfo> floors;
    public int currentFloor;     // on és el grup ARA (-1 = encara cap node visitat)
    public int currentRoomId;    // node dins del pis actual (-1 = no definit)

    public MapUpdate_OUT() {}

    public MapUpdate_OUT(List<FloorInfo> floors, int currentFloor, int currentRoomId) {
        this.floors = floors;
        this.currentFloor = currentFloor;
        this.currentRoomId = currentRoomId;
    }

    @Override
    public String getMessageType() {
        return TYPE;
    }
}
