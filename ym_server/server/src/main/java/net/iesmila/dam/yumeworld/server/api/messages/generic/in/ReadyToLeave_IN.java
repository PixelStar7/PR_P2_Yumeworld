package net.iesmila.dam.yumeworld.server.api.messages.generic.in;

import net.iesmila.dam.yumeworld.server.api.messages.MessageBody;

/**
 * Missatge d'entrada que envia el client quan un jugador confirma que vol
 * sortir d'una sala (rest, chest, etc.). El servidor recopila aquests
 * missatges de cada jugador i, quan tots els membres del grup han enviat
 * el seu ReadyToLeave_IN, fa la transició cap a StateMapVisualization
 * amb un broadcast de ReturnToMap_OUT.
 */
public class ReadyToLeave_IN extends MessageBody {
    public final static String TYPE = "READY_TO_LEAVE";

    public long playerId; // Jugador que confirma que vol sortir

    @Override
    public String getMessageType() {
        return TYPE;
    }
}
