package net.iesmila.dam.yumeworld.server.api.models;

import net.iesmila.dam.yumeworld.server.api.dto.CharacterModification;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

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

    private int atk;
    private int def;

    /**
     * Bonus d'estadística per pujada de nivell associat al rol del personatge
     * (pr_hp / pr_mp / pr_atk / pr_def de PLAYERROLE). Si és > 0, el rol
     * afavoreix aquell atribut i s'afegeix un boost addicional (+1 o +2)
     * sobre el boost base aleatori en cada pujada de nivell.
     */
    private int roleHpBonus;
    private int roleMpBonus;
    private int roleAtkBonus;
    private int roleDefBonus;

    /**
     * XP base per nivell (fórmula quadràtica: BASE_XP * level^2).
     * Modifica aquesta constant per ajustar la corba de progressió
     * sense haver de tocar cap altra part del codi.
     */
    private static final int BASE_XP = 100;

    /**
     * Inventari del jugador durant la partida. S'omple amb els itemIds
     * que el jugador agafa a StateChest i a StateCombatRewards (1 item
     * per sala). Persistirà fins al final de la partida.
     *
     * TODO: usar-lo des de StateCombat per habilitats consumibles.
     */
    private List<Integer> inventoryItemIds = new ArrayList<>();

    /**
     * Mapa d'accions vàlides per a aquest personatge durant el combat.
     * Clau: actionId (ac_id a la taula ACT).
     * Valor: cost en MP (0 si és l'atac bàsic o una acció gratuïta).
     *
     * S'omple a StateCombat.initializePlayerActions() al inici del combat
     * llegint YUMEDANT_ACT + ABILITY.ab_cost des de la BD.
     * Serveix per validar que el jugador envia un actionId legítim del seu
     * personatge i que té prou MP per executar-lo.
     */
    private Map<Integer, Integer> validActionMpCosts = new HashMap<>();

    public PlayerCharacterState() {}

    /**
     * Constructor a partir de la fitxa base del personatge (Yumedant + YuCharacter).
     * El HP/MP comencen al màxim, l'XP a 0.
     *
     * @param roleHpBonus  pr_hp del PLAYERROLE (> 0 → rol afavoreix HP en level-up)
     * @param roleMpBonus  pr_mp del PLAYERROLE
     * @param roleAtkBonus pr_atk del PLAYERROLE
     * @param roleDefBonus pr_def del PLAYERROLE
     */
    public PlayerCharacterState(long playerId, int characterId,
                                int maxHp, int maxMp,
                                int level, int atk, int def,
                                int roleHpBonus, int roleMpBonus,
                                int roleAtkBonus, int roleDefBonus) {
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
        this.roleHpBonus  = roleHpBonus;
        this.roleMpBonus  = roleMpBonus;
        this.roleAtkBonus = roleAtkBonus;
        this.roleDefBonus = roleDefBonus;
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
    public Map<Integer, Integer> getValidActionMpCosts() { return validActionMpCosts; }

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
    public void setValidActionMpCosts(Map<Integer, Integer> validActionMpCosts) { this.validActionMpCosts = validActionMpCosts; }

    /** Afegeix un item a l'inventari (sense duplicats? Permetem duplicats per ara). */
    public void addItemToInventory(int itemId) {
        this.inventoryItemIds.add(itemId);
    }

    /**
     * Registra una acció com a vàlida per a aquest personatge amb el seu cost MP.
     * Cridat des de StateCombat.initializePlayerActions().
     */
    public void registerAction(int actionId, int mpCost) {
        this.validActionMpCosts.put(actionId, mpCost);
    }

    /** Retorna true si l'actionId pertany al personatge d'aquest jugador. */
    public boolean isActionValid(int actionId) {
        return validActionMpCosts.containsKey(actionId);
    }

    /**
     * Retorna el cost MP d'una acció, o 0 si no existeix al mapa.
     * Comprova primer amb isActionValid() per distingir "acció no vàlida" de "acció gratuïta".
     */
    public int getActionMpCost(int actionId) {
        return validActionMpCosts.getOrDefault(actionId, 0);
    }

    // Getters per als bonus de rol
    public int getRoleHpBonus()  { return roleHpBonus; }
    public int getRoleMpBonus()  { return roleMpBonus; }
    public int getRoleAtkBonus() { return roleAtkBonus; }
    public int getRoleDefBonus() { return roleDefBonus; }

    // Setters per als bonus de rol
    public void setRoleHpBonus(int roleHpBonus)   { this.roleHpBonus  = roleHpBonus; }
    public void setRoleMpBonus(int roleMpBonus)   { this.roleMpBonus  = roleMpBonus; }
    public void setRoleAtkBonus(int roleAtkBonus) { this.roleAtkBonus = roleAtkBonus; }
    public void setRoleDefBonus(int roleDefBonus) { this.roleDefBonus = roleDefBonus; }

    // ─── Level-up ──────────────────────────────────────────────────────────────

    /**
     * XP necessària per pujar del nivell actual al següent.
     * Fórmula quadràtica: BASE_XP * level^2. Per canviar la corba de progressió
     * n'hi ha prou de modificar aquest mètode (o la constant BASE_XP).
     */
    private int xpToNextLevel() {
        return BASE_XP * level * level;
    }

    /**
     * Afegeix XP al personatge i aplica tants nivells com correspongui.
     * L'XP sobrant d'una pujada es trasllada al nivell nou (no es perd).
     *
     * @param xp  XP a afegir (ha de ser >= 0)
     * @param rng Random del GameInstance (per a reproductibilitat de seed)
     * @return    Llista de CharacterModification — un element per cada nivell pujat,
     *            buit si no n'hi ha. El cridador ha de fer broadcast de
     *            CharacterUpdate_OUT per a cadascun.
     */
    public List<CharacterModification> addXpAndCheckLevelUp(int xp, Random rng) {
        List<CharacterModification> mods = new ArrayList<>();
        exp += xp;

        while (exp >= xpToNextLevel()) {
            exp -= xpToNextLevel();   // l'XP sobrant passa al nou nivell
            CharacterModification mod = applyLevelUp(rng);
            mods.add(mod);
        }
        return mods;
    }

    /**
     * Puja un nivell i aplica els boosts d'estadística.
     *
     * Cada atribut rep:
     *   • boost base: +1 o +2 aleatori
     *   • boost de rol: +1 o +2 aleatori addicional si el bonus de rol > 0
     *
     * Retorna un CharacterModification amb l'estat antic/nou per a la notificació.
     */
    private CharacterModification applyLevelUp(Random rng) {
        CharacterModification mod = new CharacterModification();
        mod.id        = characterId;
        mod.old_level = level;
        mod.old_exp   = exp;
        mod.old_hp    = maxHp;
        mod.old_mp    = maxMp;

        int hpBoost  = (rng.nextInt(2) + 1) + (roleHpBonus  > 0 ? rng.nextInt(2) + 1 : 0);
        int mpBoost  = (rng.nextInt(2) + 1) + (roleMpBonus  > 0 ? rng.nextInt(2) + 1 : 0);
        int atkBoost = (rng.nextInt(2) + 1) + (roleAtkBonus > 0 ? rng.nextInt(2) + 1 : 0);
        int defBoost = (rng.nextInt(2) + 1) + (roleDefBonus > 0 ? rng.nextInt(2) + 1 : 0);

        maxHp += hpBoost;
        maxMp += mpBoost;
        atk   += atkBoost;
        def   += defBoost;
        level++;

        mod.new_level = level;
        mod.new_exp   = exp;
        mod.new_hp    = maxHp;
        mod.new_mp    = maxMp;

        return mod;
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
