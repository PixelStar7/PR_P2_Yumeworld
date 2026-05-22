package net.iesmila.dam.yumeworld.server.api.dto;

import net.iesmila.dam.yumeworld.server.api.models.PlayerCharacterState;

/**
 * DTO d'estadístiques finals d'un jugador per al GameSummary_OUT.
 * S'envia un per cada jugador al final de la partida.
 *
 * Camps:
 *   - playerId, characterId: identificació.
 *   - currentHp/maxHp: salut final.
 *   - level/exp: progressió aconseguida.
 *   - itemsCollected: nombre d'objectes a l'inventari del jugador.
 *
 * TODO: ampliar amb damageDealt, enemiesDefeated, etc. quan tinguem un
 * GameStats acumulatiu al GameInstance.
 */
public class PlayerSummary {

    public long playerId;
    public int characterId;
    public int currentHp;
    public int maxHp;
    public int level;
    public int exp;
    public int itemsCollected;

    public PlayerSummary() {}

    public PlayerSummary(long playerId, int characterId, int currentHp, int maxHp,
                         int level, int exp, int itemsCollected) {
        this.playerId = playerId;
        this.characterId = characterId;
        this.currentHp = currentHp;
        this.maxHp = maxHp;
        this.level = level;
        this.exp = exp;
        this.itemsCollected = itemsCollected;
    }

    public static PlayerSummary from(PlayerCharacterState state) {
        return new PlayerSummary(
                state.getPlayerId(),
                state.getCharacterId(),
                state.getCurrentHp(),
                state.getMaxHp(),
                state.getLevel(),
                state.getExp(),
                state.getInventoryItemIds() != null ? state.getInventoryItemIds().size() : 0
        );
    }
}
