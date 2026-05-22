package net.iesmila.dam.yumeworld.server.db.repository;

import net.iesmila.dam.yumeworld.server.db.model.EnemyDrop;
import net.iesmila.dam.yumeworld.server.db.model.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Integer> {

    /**
     * Retorna tots els EnemyDrop d'un enemic concret, amb l'Item i tots els
     * seus efectes precarregats (per evitar LazyInitializationException quan
     * el LootService construeixi LootInfo fora de la transacció).
     */
    @Query("SELECT DISTINCT ed FROM EnemyDrop ed " +
           "JOIN FETCH ed.item i " +
           "LEFT JOIN FETCH i.actionEffects ae " +
           "LEFT JOIN FETCH ae.effect " +
           "WHERE ed.enemy.id = :enemyId")
    List<EnemyDrop> findDropsByEnemyId(@Param("enemyId") int enemyId);

    /**
     * Retorna tots els Items del joc amb els seus efectes precarregats.
     * El LootService el fa servir per generar el contingut dels cofres
     * (pool global d'items).
     */
    @Query("SELECT DISTINCT i FROM Item i " +
           "LEFT JOIN FETCH i.actionEffects ae " +
           "LEFT JOIN FETCH ae.effect")
    List<Item> findAllWithEffects();
}
