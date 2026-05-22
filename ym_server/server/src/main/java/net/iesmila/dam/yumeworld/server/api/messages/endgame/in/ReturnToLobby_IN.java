package net.iesmila.dam.yumeworld.server.api.messages.endgame.in;

import net.iesmila.dam.yumeworld.server.api.messages.MessageBody;

/**
 * Missatge d'entrada que envia el client quan un jugador confirma que
 * vol tornar al lobby des de la pantalla de fi de partida (StateEndGame).
 */
public class ReturnToLobby_IN extends MessageBody {
    public final static String TYPE = "RETURN_TO_LOBBY";

    public int playerId; // Jugador que vol tornar al lobby

    @Override
    public String getMessageType() {
        return TYPE;
    }
}
