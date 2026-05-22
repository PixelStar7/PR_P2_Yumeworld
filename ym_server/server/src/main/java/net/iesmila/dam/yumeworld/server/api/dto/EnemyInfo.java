package net.iesmila.dam.yumeworld.server.api.dto;

import net.iesmila.dam.yumeworld.server.api.models.EnemyCombatState;
import net.iesmila.dam.yumeworld.server.db.model.Enemy;

/**
 * DTO d'un enemic visible al combat. S'envia al CombatStart_OUT i al
 * CombatUpdate_OUT perquè el client mostri els stats actuals.
 *
 * - combatId: identifica la instància dins del combat (per a accions).
 * - enemyId: tipus de l'enemic (FK BD).
 * - name/imageUrl: info estàtica del tipus, per renderitzar.
 * - currentHp/maxHp/currentMp/maxMp: stats actuals.
 *
 * TODO: afegir estats alterats (enverinat, etc.) quan toqui.
 */
public class EnemyInfo {

    public int combatId;
    public int enemyId;
    public String name;
    public String imageUrl;
    public int currentHp;
    public int maxHp;
    public int currentMp;
    public int maxMp;

    public EnemyInfo() {}

    public EnemyInfo(int combatId, int enemyId, String name, String imageUrl,
                     int currentHp, int maxHp, int currentMp, int maxMp) {
        this.combatId = combatId;
        this.enemyId = enemyId;
        this.name = name;
        this.imageUrl = imageUrl;
        this.currentHp = currentHp;
        this.maxHp = maxHp;
        this.currentMp = currentMp;
        this.maxMp = maxMp;
    }

    /**
     * Factory: construeix un EnemyInfo a partir d'un EnemyCombatState (runtime)
     * i d'una entitat Enemy (per llegir nom/imatge de la BD).
     */
    public static EnemyInfo from(EnemyCombatState state, Enemy enemy) {
        return new EnemyInfo(
                state.getCombatId(),
                state.getEnemyId(),
                enemy.getName(),
                enemy.getImage(),
                state.getCurrentHp(),
                state.getMaxHp(),
                state.getCurrentMp(),
                state.getMaxMp()
        );
    }
}
