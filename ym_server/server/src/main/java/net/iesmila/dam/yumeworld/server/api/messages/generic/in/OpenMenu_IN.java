package net.iesmila.dam.yumeworld.server.api.messages.generic.in;

import net.iesmila.dam.yumeworld.server.api.messages.MessageBody;

public class OpenMenu_IN extends MessageBody {
    public final static String TYPE = "OPEN_MENU";

    @Override
    public String getMessageType() {
        return TYPE;
    }
}
