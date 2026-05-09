package net.iesmila.dam.yumeworld.server.api.messages.mapexploration.out;

public class BossInfo_OUT extends NodeInfo_OUT {
    public final static String TYPE = "BOSS_INFO";

    @Override
    public String getMessageType() {
        return TYPE;
    }

    // Aquí pots afegir camps específics per a la informació d'un combat de boss, per exemple:
    // public String bossName;
    // public List<BossAbility> abilities;
}
