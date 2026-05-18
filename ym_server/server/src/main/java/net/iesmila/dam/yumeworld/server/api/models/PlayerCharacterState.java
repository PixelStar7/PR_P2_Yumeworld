package net.iesmila.dam.yumeworld.server.api.models;

import java.util.ArrayList;
import java.util.List;

/**
 * Estat de joc d'un personatge controlat per un jugador durant una partida.
 * Viu al GameInstance des que tothom ha triat personatge fins al final de la partida.
 *
 * El HP/MP/exp aquí són els VALORS RUNTIME (canviables durant la partida);
 * el maxHp/maxMp són les fites originals del YuCharacter triat.
 *
 * Camps deliberadament fora de moment (els afegirem quan toqui):
 *   - alteredStates: quan hi hagi efectes alterats a combat.
 *   - cards / deck: quan hi hagi sistema de cartes.
 */
public class PlayerCharacterState {

    private long playerId;       // Jugador propietari
    private int characterId;     // YuCharacter triat (clau a la BD)

    private int currentHp;
    private int maxHp;
    private int currentMp;
    private int maxMp;

    private int level;
    private int exp;

    private int atk;             // Estàtic pel personatge (de moment)
    private int def;             // Estàtic pel personatge (de moment)

    /**
     * Inventari del jugador durant la partida. S'omple amb els itemIds
     * que el jugador agafa a StateChest i a StateCombatRewards (1 item
     * per sala). Persistirà fins al final de la partida.
     *
     * TODO: usar-lo des de StateCombat per habilitats consumibles.
     */
    private List<Integer> inventoryItemIds = new ArrayList<>();

    public PlayerCharacterState() {}

    /**
     * Constructor a partir de la fitxa base del personatge (Yumedant + YuCharacter).
     * El HP/MP comencen al màxim, l'XP a 0.
     */
    public PlayerCharacterState(long playerId, int characterId,
                                int maxHp, int maxMp,
                                int level, int atk, int def) {
        this.playerId = playerId;
        this.characterId = characterId;
        this.maxHp = maxHp;
        this.currentHp = maxHp;
        this.maxMp = maxMp;
        this.currentMp = maxMp;
        this.level = level;
        this.exp = 0;
        this.atk = atk;
        this.def = def;
    }

    // Getters
    public long getPlayerId() { return playerId; }
    public int getCharacterId() { return characterId; }
    public int getCurrentHp() { return currentHp; }
    public int getMaxHp() { return maxHp; }
    public int getCurrentMp() { return currentMp; }
    public int getMaxMp() { return maxMp; }
    public int getLevel() { return level; }
    public int getExp() { return exp; }
    public int getAtk() { return atk; }
    public int getDef() { return def; }
    public List<Integer> getInventoryItemIds() { return inventoryItemIds; }

    // Setters
    public void setPlayerId(long playerId) { this.playerId = playerId; }
    public void setCharacterId(int characterId) { this.characterId = characterId; }
    public void setCurrentHp(int currentHp) { this.currentHp = currentHp; }
    public void setMaxHp(int maxHp) { this.maxHp = maxHp; }
    public void setCurrentMp(int currentMp) { this.currentMp = currentMp; }
    public void setMaxMp(int maxMp) { this.maxMp = maxMp; }
    public void setLevel(int level) { this.level = level; }
    public void setExp(int exp) { this.exp = exp; }
    public void setAtk(int atk) { this.atk = atk; }
    public void setDef(int def) { this.def = def; }
    public void setInventoryItemIds(List<Integer> inventoryItemIds) { this.inventoryItemIds = inventoryItemIds; }

    /** Afegeix un item a l'inventari (sense duplicats? Permetem duplicats per ara). */
    public void addItemToInventory(int itemId) {
        this.inventoryItemIds.add(itemId);
    }

    /**
     * Aplica curació al HP, capada al maxHp. Retorna el HP anterior.
     */
    public int healHp(int amount) {
        int oldHp = this.currentHp;
        this.currentHp = Math.min(this.maxHp, this.currentHp + amount);
        return oldHp;
    }

    /**
     * Aplica curació al MP, capada al maxMp. Retorna el MP anterior.
     */
    public int healMp(int amount) {
        int oldMp = this.currentMp;
        this.currentMp = Math.min(this.maxMp, this.currentMp + amount);
        return oldMp;
    }
}
