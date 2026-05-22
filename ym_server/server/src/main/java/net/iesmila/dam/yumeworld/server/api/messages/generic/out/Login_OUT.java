package net.iesmila.dam.yumeworld.server.api.messages.generic.out;

import net.iesmila.dam.yumeworld.server.api.messages.MessageBody;

public class Login_OUT extends MessageBody {

    public final static String TYPE = "LOGIN";
    public long playerId;

    public Login_OUT(long playerId) {
        this.playerId = playerId;
    }

    @Override
    public String getMessageType() {
        return TYPE;
    }
}
