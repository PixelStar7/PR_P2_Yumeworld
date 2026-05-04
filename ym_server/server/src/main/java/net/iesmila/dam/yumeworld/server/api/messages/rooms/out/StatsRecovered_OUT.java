package net.iesmila.dam.yumeworld.server.api.messages.rooms.out;

import net.iesmila.dam.yumeworld.server.api.messages.MessageBody;

public class StatsRecovered_OUT extends MessageBody {

    public final static String TYPE = "STATS_RECOVERED"; // Changed type to uppercase and consistent naming

    @Override
    public String getMessageType() {
        return TYPE;
    }
    // Envia quanta vida/manà recuperen els jugadors
}
