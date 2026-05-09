package net.iesmila.dam.yumeworld.server.api.messages.mapexploration.out;

public class RestInfo_OUT extends NodeInfo_OUT {
    public final static String TYPE = "REST_INFO";

    @Override
    public String getMessageType() {
        return TYPE;
    }

    // Aquí pots afegir camps específics per a la informació d'un node de descans, per exemple:
    // public int hpRecovered;
    // public int mpRecovered;
}
