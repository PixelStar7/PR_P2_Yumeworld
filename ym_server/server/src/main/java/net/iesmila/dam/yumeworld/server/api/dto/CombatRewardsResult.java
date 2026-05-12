package net.iesmila.dam.yumeworld.server.api.dto;

import java.util.List;

/**
 * Resultat agregat de les recompenses d'un combat. El retorna LootService
 * i el consumeix StateCombatRewards (o equivalent) per construir CombatRewards_OUT.
 *
 * NOTA: el camp 'coinsGained' queda pendent. La columna en_given_coins existirà
 * a la taula ENEMY de la BD, però fins que no hi hagi botiga al joc no es
 * llegirà al servidor. Quan toqui, només cal afegir el camp aquí i omplir-lo
 * al servei.
 */
public class CombatRewardsResult {

    public int xpGained;
    public List<LootInfo> loot;

    public CombatRewardsResult() {}

    public CombatRewardsResult(int xpGained, List<LootInfo> loot) {
        this.xpGained = xpGained;
        this.loot = loot;
    }
}
