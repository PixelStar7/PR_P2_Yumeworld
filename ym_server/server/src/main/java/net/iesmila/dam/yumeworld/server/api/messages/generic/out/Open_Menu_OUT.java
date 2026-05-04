package net.iesmila.dam.yumeworld.server.api.messages.generic.out;

import net.iesmila.dam.yumeworld.server.api.messages.MessageBody;

public class Open_Menu_OUT extends MessageBody {
    public final static String TYPE = "OPEN_MENU";

    @Override
    public String getMessageType() {
        return TYPE;
    }
}
