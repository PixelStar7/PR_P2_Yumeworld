package net.iesmila.dam.yumeworld.server.api.messages.mapexploration.out;

public class ChestInfo_OUT extends NodeInfo_OUT {
    public final static String TYPE = "CHEST_INFO";

    @Override
    public String getMessageType() {
        return TYPE;
    }

    // Aquí pots afegir camps específics per a la informació d'un cofre, per exemple:
    // public List<Item> possibleLoot;
    // public int rarity;
}
