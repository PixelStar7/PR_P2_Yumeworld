package net.iesmila.dam.yumeworld.server.api.messages.mapexploration.out;

public class CombatInfo_OUT extends NodeInfo_OUT {
    public final static String TYPE = "COMBAT_INFO";

    @Override
    public String getMessageType() {
        return TYPE;
    }

    // Aquí pots afegir camps específics per a la informació d'un combat, per exemple:
    // public List<EnemyInfo> enemies;
    // public String combatType; // e.g., "normal", "elite"
}
