package net.iesmila.dam.yumeworld.server.api.messages.generic;

import net.iesmila.dam.yumeworld.server.api.messages.MessageBody;

public class Login_OUT extends MessageBody {

    public final static String TYPE = "RESULT";
    public long playerId;

    public Login_OUT(int playerId) {
        this.playerId = playerId;
    }

    @Override
    public String getMessageType() {
        return TYPE;
    }
}
