package net.iesmila.dam.yumeworld.server.api.messages.chest.in;

import net.iesmila.dam.yumeworld.server.api.messages.MessageBody;

/**
 * Missatge d'entrada que envia el client quan un jugador interactua amb un cofre
 * dins de l'estat StateChest. Per gestionar les normes de qui pot obrir-lo
 * (host, primer en arribar, etc.) ho farà el servidor en validar el missatge.
 */
public class OpenChest_IN extends MessageBody {
    public final static String TYPE = "OPEN_CHEST";

    public long playerId; // Jugador que demana obrir el cofre

    @Override
    public String getMessageType() {
        return TYPE;
    }
}
