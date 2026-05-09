package net.iesmila.dam.yumeworld.server.api.messages.rooms.out;

import net.iesmila.dam.yumeworld.server.api.messages.MessageBody;

/**
 * Broadcast que envia el servidor quan un jugador ha acabat la seva acció
 * de descans dins de StateRest. Quan tots els jugadors hagin enviat el seu
 * LeaveRoom_IN després d'aquest missatge, l'estat farà la transició cap
 * a StateMapVisualization.
 */
public class PlayerFinishedRest_OUT extends MessageBody {
    public final static String TYPE = "PLAYER_FINISHED_REST";

    public int playerId; // Jugador que ha acabat el descans

    @Override
    public String getMessageType() {
        return TYPE;
    }
}
