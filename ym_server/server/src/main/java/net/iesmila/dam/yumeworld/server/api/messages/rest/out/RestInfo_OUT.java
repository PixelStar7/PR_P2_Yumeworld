package net.iesmila.dam.yumeworld.server.api.messages.rest.out;

import net.iesmila.dam.yumeworld.server.api.messages.MessageBody;

/**
 * Subclasse de NodeInfo_OUT que s'envia en entrar a un node de descans.
 * Conté l'estat inicial de la foguera (apagada en entrar; el primer
 * LightCampfire_IN d'algun jugador la commuta a true).
 */
public class RestInfo_OUT extends MessageBody {
    public final static String TYPE = "REST_INFO";

    public boolean campfireLit;  // false en entrar; true quan algú l'ha encès

    public RestInfo_OUT() {
        this.campfireLit = false;
    }

    public RestInfo_OUT(boolean campfireLit) {
        this.campfireLit = campfireLit;
    }

    @Override
    public String getMessageType() {
        return TYPE;
    }
}
