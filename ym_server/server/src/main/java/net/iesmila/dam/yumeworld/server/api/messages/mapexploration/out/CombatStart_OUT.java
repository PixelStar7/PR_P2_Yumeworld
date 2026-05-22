package net.iesmila.dam.yumeworld.server.api.messages.mapexploration.out;

import net.iesmila.dam.yumeworld.server.api.messages.MessageBody;

/**
 * Missatge que s'envia al client perquè pugui transicionar entre l'escena de mapa a l'escena de combat.
 */
public class CombatStart_OUT extends MessageBody {

    public final static String TYPE = "COMBAT_START";

    @Override
    public String getMessageType() {
        return TYPE;
    }
}
