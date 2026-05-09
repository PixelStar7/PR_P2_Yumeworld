package net.iesmila.dam.yumeworld.server.api.messages.mapexploration.in;

import net.iesmila.dam.yumeworld.server.api.messages.MessageBody;

/**
 * Missatge d'entrada que envia el client quan un jugador clica un node del mapa.
 * El primer jugador que envia un MoveToNode_IN vàlid decideix on va el grup.
 */
public class MoveToNode_IN extends MessageBody {
    public final static String TYPE = "MOVE_TO_NODE";

    public long playerId;   // Qui fa la petició
    public int floorNum;    // Pis on vol moure's el grup
    public int roomId;      // Node concret triat dins del pis

    @Override
    public String getMessageType() {
        return TYPE;
    }
}
