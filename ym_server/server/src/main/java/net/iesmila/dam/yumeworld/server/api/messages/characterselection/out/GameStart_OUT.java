package net.iesmila.dam.yumeworld.server.api.messages.characterselection.out;

import net.iesmila.dam.yumeworld.server.api.messages.MessageBody;

public class GameStart_OUT extends MessageBody {

    public final static String TYPE = "GAME_START";

    public GameStart_OUT() {
    }

    @Override
    public String getMessageType() {
        return TYPE;
    }
}
