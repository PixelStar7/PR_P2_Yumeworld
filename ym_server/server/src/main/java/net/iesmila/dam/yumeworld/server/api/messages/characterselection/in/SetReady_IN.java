package net.iesmila.dam.yumeworld.server.api.messages.characterselection.in;

import net.iesmila.dam.yumeworld.server.api.messages.MessageBody;

public class SetReady_IN extends MessageBody {
    // Quan un jugador clica Ready
    public int playerId;
    public boolean isReady;

    public final static String TYPE = "SET_READY";

    @Override
    public String getMessageType() {
        return TYPE;
    }
}
