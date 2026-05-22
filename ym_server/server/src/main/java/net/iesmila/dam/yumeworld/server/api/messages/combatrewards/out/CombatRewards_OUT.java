package net.iesmila.dam.yumeworld.server.api.messages.combatrewards.out;

import net.iesmila.dam.yumeworld.server.api.dto.LootInfo;
import net.iesmila.dam.yumeworld.server.api.messages.MessageBody;

import java.util.List;

/**
 * Broadcast que envia el servidor en entrar a l'estat StateCombatRewards.
 * Conté l'XP guanyada (igual per a tots els jugadors) i la llista d'items
 * disponibles per agafar (el flux és igual que al cofre: cada jugador
 * tria un item amb PickItem_IN).
 *
 * NOTA: en versions futures podem afegir 'coinsGained' quan hi hagi
 * sistema de botiga. De moment només XP i items.
 */
public class CombatRewards_OUT extends MessageBody {
    public final static String TYPE = "COMBAT_REWARDS";

    public int xpGained;            // XP guanyada (igual per a tots)
    public List<LootInfo> loot;     // Items disponibles per agafar (un per jugador)

    public CombatRewards_OUT() {}

    public CombatRewards_OUT(int xpGained, List<LootInfo> loot) {
        this.xpGained = xpGained;
        this.loot = loot;
    }

    @Override
    public String getMessageType() {
        return TYPE;
    }
}
