package net.iesmila.dam.yumeworld.server.api.models;

import java.util.List;

/**
 * Estat runtime d'un enemic durant un combat concret.
 * Local a StateCombat (es perd quan el combat acaba).
 *
 * combatId és un identificador d'INSTÀNCIA dins del combat: dos enemics del
 * mateix tipus (mateix enemyId) tenen combatIds diferents (0, 1, 2...).
 * Quan un jugador apunta a un enemic concret, envia el combatId, no el enemyId.
 */
public class EnemyCombatState {

    private int combatId;        // identificador d'instància dins del combat (0, 1, 2...)
    private int enemyId;         // FK a ENEMY (tipus d'enemic)

    private int currentHp;
    private int maxHp;
    private int currentMp;
    private int maxMp;

    private int atk;
    private int def;
    private int level;

    private List<Integer> actionIds; // accions disponibles (de YUMEDANT_ACT)

    public EnemyCombatState() {}

    public EnemyCombatState(int combatId, int enemyId, int maxHp, int maxMp,
                            int atk, int def, int level, List<Integer> actionIds) {
        this.combatId = combatId;
        this.enemyId = enemyId;
        this.maxHp = maxHp;
        this.currentHp = maxHp;
        this.maxMp = maxMp;
        this.currentMp = maxMp;
        this.atk = atk;
        this.def = def;
        this.level = level;
        this.actionIds = actionIds;
    }

    // Getters
    public int getCombatId() { return combatId; }
    public int getEnemyId() { return enemyId; }
    public int getCurrentHp() { return currentHp; }
    public int getMaxHp() { return maxHp; }
    public int getCurrentMp() { return currentMp; }
    public int getMaxMp() { return maxMp; }
    public int getAtk() { return atk; }
    public int getDef() { return def; }
    public int getLevel() { return level; }
    public List<Integer> getActionIds() { return actionIds; }

    // Setters
    public void setCombatId(int combatId) { this.combatId = combatId; }
    public void setEnemyId(int enemyId) { this.enemyId = enemyId; }
    public void setCurrentHp(int currentHp) { this.currentHp = currentHp; }
    public void setMaxHp(int maxHp) { this.maxHp = maxHp; }
    public void setCurrentMp(int currentMp) { this.currentMp = currentMp; }
    public void setMaxMp(int maxMp) { this.maxMp = maxMp; }
    public void setAtk(int atk) { this.atk = atk; }
    public void setDef(int def) { this.def = def; }
    public void setLevel(int level) { this.level = level; }
    public void setActionIds(List<Integer> actionIds) { this.actionIds = actionIds; }

    /** L'enemic està viu si li queda HP. */
    public boolean isAlive() {
        return currentHp > 0;
    }

    /** Aplica dany, capat a 0. Retorna el HP previ. */
    public int takeDamage(int amount) {
        int oldHp = this.currentHp;
        this.currentHp = Math.max(0, this.currentHp - amount);
        return oldHp;
    }
}
