package net.iesmila.dam.yumeworld.server.api.messages.generic.out;

import net.iesmila.dam.yumeworld.server.api.messages.MessageBody;

/**
 * Broadcast que envia el servidor quan detecta que un jugador ha perdut la
 * connexió. Com que no hi haurà reconnexió, aquesta notificació serveix
 * perquè els clients mostrin un avís i el servidor pugui forçar el final
 * de la partida.
 */
public class PlayerDisconnected_OUT extends MessageBody {
    public final static String TYPE = "PLAYER_DISCONNECTED";

    public long playerId; // Jugador que s'ha desconnectat

    @Override
    public String getMessageType() {
        return TYPE;
    }
}
