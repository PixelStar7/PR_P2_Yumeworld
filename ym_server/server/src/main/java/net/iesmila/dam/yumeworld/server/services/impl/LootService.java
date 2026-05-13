package net.iesmila.dam.yumeworld.server.services.impl;

import net.iesmila.dam.yumeworld.server.api.dto.CombatRewardsResult;
import net.iesmila.dam.yumeworld.server.api.dto.LootInfo;
import net.iesmila.dam.yumeworld.server.db.model.Enemy;
import net.iesmila.dam.yumeworld.server.db.model.EnemyDrop;
import net.iesmila.dam.yumeworld.server.db.model.Item;
import net.iesmila.dam.yumeworld.server.db.repository.EnemyRepository;
import net.iesmila.dam.yumeworld.server.db.repository.ItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

/**
 * Servei responsable de generar loot tant per cofres com per drops d'enemics.
 *
 * Tots els mètodes accepten una instància de {@link Random} per partida (la
 * GameInstance n'és la propietària). Així podem reproduir partides amb la
 * mateixa seed i mantenir la independència entre partides concurrents.
 *
 * PENDENTS DOCUMENTATS:
 * - Cofres especials (boss chest, elite chest): un sol cofre per ara.
 * - Filtre per pis del pool: el cofre fa servir tots els items existents.
 * - Repetició al cofre: actualment es permet repetició perquè el pool és
 *   petit. Quan tinguem prou items, valdria la pena passar a "sense
 *   repetició" (Fisher-Yates shuffle).
 * - Monedes (en_given_coins): la columna existirà a la BD però no es llegeix
 *   fins que no hi hagi botiga al joc.
 */
@Service
public class LootService {

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private EnemyRepository enemyRepository;

    /**
     * Genera el contingut d'un cofre. El nombre d'items es decideix per qui
     * crida el mètode (típicament un per jugador). Es permet repetició: si el
     * pool global té menys items que numItems, alguns es repetiran.
     *
     * @param numItems quants objectes ha de contenir el cofre
     * @param random   font d'aleatorietat de la partida
     * @return llista de LootInfo (mai null; buida si el pool global està buit)
     */
    public List<LootInfo> generateChestLoot(int numItems, Random random) {
        List<Item> pool = itemRepository.findAllWithEffects();
        List<LootInfo> result = new ArrayList<>();

        if (pool.isEmpty()) {
            System.err.println("[LootService] WARN - El pool d'items està buit, no es pot omplir el cofre.");
            return result;
        }

        // Repetició permesa: per cada slot, agafem un item aleatori del pool.
        // TODO: quan el catàleg d'items sigui prou gran, canviar a "sense
        // repetició" amb Collections.shuffle(pool, random) i agafar els
        // primers numItems.
        for (int i = 0; i < numItems; i++) {
            Item randomItem = pool.get(random.nextInt(pool.size()));
            result.add(LootInfo.fromItem(randomItem));
        }

        return result;
    }

    /**
     * Genera els drops agregats d'un combat sencer.
     *
     * Lògica del roll agregat (interpretació A — màxima probabilitat):
     *   1. Recollim tots els EnemyDrop dels enemics derrotats.
     *   2. Per a cada item únic, ens quedem la chance MÉS ALTA entre tots
     *      els enemics que el poden deixar caure.
     *   3. Fem UN sol roll per item contra aquesta chance màxima.
     *   4. Cada item només pot aparèixer com a molt una vegada per combat.
     *
     * @param enemyIds ids dels enemics derrotats
     * @param random   font d'aleatorietat de la partida
     * @return llista de LootInfo (mai null; pot ser buida)
     */
    public List<LootInfo> generateEnemyDropAggregated(List<Integer> enemyIds, Random random) {
        // Pas 1+2: recopilar item → chance màxima
        Map<Item, Float> maxChanceByItem = new HashMap<>();
        for (Integer enemyId : enemyIds) {
            List<EnemyDrop> drops = itemRepository.findDropsByEnemyId(enemyId);
            if (drops.isEmpty()) {
                System.err.println("[LootService] WARN - L'enemic " + enemyId + " no té cap entrada a ENEMY_DROPS.");
                continue;
            }
            for (EnemyDrop drop : drops) {
                Item item = drop.getItem();
                Float currentMax = maxChanceByItem.get(item);
                if (currentMax == null || drop.getChance() > currentMax) {
                    maxChanceByItem.put(item, drop.getChance());
                }
            }
        }

        // Pas 3+4: un roll per item amb la seva chance màxima
        List<LootInfo> result = new ArrayList<>();
        for (Map.Entry<Item, Float> entry : maxChanceByItem.entrySet()) {
            double roll = random.nextDouble(); // [0.0, 1.0)
            if (roll <= entry.getValue()) {
                result.add(LootInfo.fromItem(entry.getKey()));
            }
        }
        return result;
    }

    /**
     * Calcula totes les recompenses d'un combat: XP acumulada (suma dels
     * en_given_exp de tots els enemics) i loot agregat.
     *
     * Aquest és el mètode que cridarà StateCombat (o StateCombatRewards) per
     * construir el CombatRewards_OUT que rebrà el client.
     *
     * @param enemyIds ids dels enemics derrotats
     * @param random   font d'aleatorietat de la partida
     * @return resultat amb XP total + llista de loot
     */
    public CombatRewardsResult generateCombatRewards(List<Integer> enemyIds, Random random) {
        // XP: suma dels en_given_exp de cada enemic. Si un enemic no existeix
        // a la BD, es loggue un warning i s'ignora (0 XP per a aquell enemic).
        int totalXp = 0;
        for (Integer enemyId : enemyIds) {
            Optional<Enemy> opt = enemyRepository.findById(enemyId);
            if (opt.isEmpty()) {
                System.err.println("[LootService] WARN - L'enemic " + enemyId + " no existeix a la BD, no compta cap XP.");
                continue;
            }
            Integer xp = opt.get().getGivenExp();
            if (xp != null) {
                totalXp += xp;
            }
        }

        // Loot: roll agregat sobre tots els drops
        List<LootInfo> loot = generateEnemyDropAggregated(enemyIds, random);

        return new CombatRewardsResult(totalXp, loot);
    }
}
