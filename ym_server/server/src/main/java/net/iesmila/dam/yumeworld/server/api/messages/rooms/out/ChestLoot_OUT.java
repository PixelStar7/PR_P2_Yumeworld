package net.iesmila.dam.yumeworld.server.api.messages.rooms.out;

import net.iesmila.dam.yumeworld.server.api.messages.MessageBody;

public class ChestLoot_OUT extends MessageBody { // Changed to public
    public final static String TYPE = "CHEST_LOOT"; // Changed type to uppercase and consistent naming

    @Override
    public String getMessageType() {
        return TYPE;
    }
    // Envia què ha tocat al cofre
}
