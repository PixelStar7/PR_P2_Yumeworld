package net.iesmila.dam.yumeworld.server.api.messages.combatrewards.out;

import net.iesmila.dam.yumeworld.server.api.messages.MessageBody;

import java.util.List;

/**
 * Missatge de sortida que envia el servidor en entrar a l'estat StateCombatRewards.
 * Conté el repartiment d'XP, monedes i loot per a cada jugador després d'un combat.
 */
public class CombatRewards_OUT extends MessageBody {
    public final static String TYPE = "COMBAT_REWARDS";

    public int xpGained;            // XP guanyada (compartida o individual segons disseny)
    public int coinsGained;         // Monedes guanyades
    public List<Integer> lootItemIds; // Llista d'IDs d'objectes/cartes obtingudes (taula ACT)

    @Override
    public String getMessageType() {
        return TYPE;
    }
}
