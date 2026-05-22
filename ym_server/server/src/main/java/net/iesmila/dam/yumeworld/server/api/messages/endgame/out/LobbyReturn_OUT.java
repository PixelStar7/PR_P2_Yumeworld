package net.iesmila.dam.yumeworld.server.api.messages.endgame.out;

import net.iesmila.dam.yumeworld.server.api.messages.MessageBody;

/**
 * Broadcast final que envia el servidor quan tots els jugadors han confirmat
 * tornar al lobby (ReturnToLobby_IN). Senyal al client per transicionar
 * la UI cap al menú principal abans que el GameInstance es pari.
 */
public class LobbyReturn_OUT extends MessageBody {
    public final static String TYPE = "LOBBY_RETURN";

    @Override
    public String getMessageType() {
        return TYPE;
    }
}
