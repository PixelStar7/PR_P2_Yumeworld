package net.iesmila.dam.yumeworld.server.api.messages.combat.out;

import net.iesmila.dam.yumeworld.server.api.messages.MessageBody;

public class TakeDamage_OUT extends MessageBody {
    //List de id del(s) personatge(s) que pren mal
    //quant mal
    public final static String TYPE = "TAKE_DAMAGE";

    @Override
    public String getMessageType() {
        return "";
    }

}

