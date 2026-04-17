package net.iesmila.dam.yumeworld.server.api.messages.generic;

import net.iesmila.dam.yumeworld.server.api.messages.MessageBody;

public class ActionResult_OUT extends MessageBody {

    public final static String TYPE = "RESULT";

    public boolean success;
    public int errorCode;

    public ActionResult_OUT(boolean success, int errorCode) {
        this.success = success;
        this.errorCode = errorCode;
    }

    @Override
    public String getMessageType() {
        return TYPE;
    }
}
