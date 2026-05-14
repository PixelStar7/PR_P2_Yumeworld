package net.iesmila.dam.yumeworld.server.api.messages.rooms.in;

import net.iesmila.dam.yumeworld.server.api.messages.MessageBody;

/**
 * Missatge d'entrada que envia el client quan un jugador clica la foguera
 * a StateRest. El primer en arribar al servidor decideix la curació del grup;
 * el servidor valida que la foguera no estigui ja encesa.
 */
public class LightCampfire_IN extends MessageBody {
    public final static String TYPE = "LIGHT_CAMPFIRE";

    public long playerId; // Jugador que ha clicat la foguera

    @Override
    public String getMessageType() {
        return TYPE;
    }
}
