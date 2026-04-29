package net.iesmila.dam.yumeworld.server.api.messages.rooms.out;

import net.iesmila.dam.yumeworld.server.api.messages.MessageBody;

 class ChestLoot_OUT extends MessageBody {
    public final static String TYPE = "chestLoot";

    @Override
    public String getMessageType() {
        return "";
    }
    // Envia què ha tocat al cofre
}
