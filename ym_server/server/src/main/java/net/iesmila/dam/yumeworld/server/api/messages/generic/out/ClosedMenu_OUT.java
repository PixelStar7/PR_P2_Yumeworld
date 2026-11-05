package net.iesmila.dam.yumeworld.server.api.messages.generic.out;

import net.iesmila.dam.yumeworld.server.api.messages.MessageBody;

public class ClosedMenu_OUT extends MessageBody {
    public final static String TYPE = "MENU_CLOSED_RESULT";

    @Override
    public String getMessageType() {
        return TYPE;
    }
}
