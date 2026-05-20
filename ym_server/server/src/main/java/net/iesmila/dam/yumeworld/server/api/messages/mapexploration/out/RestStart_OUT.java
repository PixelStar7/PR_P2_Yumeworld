package net.iesmila.dam.yumeworld.server.api.messages.mapexploration.out;

import net.iesmila.dam.yumeworld.server.api.messages.MessageBody;


/**
 * Missatge que s'envia al client perquè pugui transicionar entre l'escena de mapa a l'escena de descans.
 */
public class RestStart_OUT extends MessageBody {
    public final static String TYPE = "REST_START";

    @Override
    public String getMessageType() {
        return TYPE;
    }
}
