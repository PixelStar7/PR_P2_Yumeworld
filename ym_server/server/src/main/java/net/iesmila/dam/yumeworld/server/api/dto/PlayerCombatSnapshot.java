package net.iesmila.dam.yumeworld.server.api.dto;

import net.iesmila.dam.yumeworld.server.api.models.PlayerCharacterState;

/**
 * Snapshot reduït d'un jugador per al CombatUpdate_OUT.
 * Inclou només la informació que canvia durant el combat (HP/MP)
 * i identificadors per al client.
 */
public class PlayerCombatSnapshot {

    public long playerId;
    public int characterId;
    public int currentHp;
    public int maxHp;
    public int currentMp;
    public int maxMp;

    public PlayerCombatSnapshot() {}

    public PlayerCombatSnapshot(long playerId, int characterId,
                                int currentHp, int maxHp,
                                int currentMp, int maxMp) {
        this.playerId = playerId;
        this.characterId = characterId;
        this.currentHp = currentHp;
        this.maxHp = maxHp;
        this.currentMp = currentMp;
        this.maxMp = maxMp;
    }

    public static PlayerCombatSnapshot from(PlayerCharacterState state) {
        return new PlayerCombatSnapshot(
                state.getPlayerId(),
                state.getCharacterId(),
                state.getCurrentHp(),
                state.getMaxHp(),
                state.getCurrentMp(),
                state.getMaxMp()
        );
    }
}
