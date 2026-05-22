package net.iesmila.dam.yumeworld.server.db.repository;

import net.iesmila.dam.yumeworld.server.db.model.EnemyBiome;
import net.iesmila.dam.yumeworld.server.db.model.EnemyBiomeId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EnemyBiomeRepository extends JpaRepository<EnemyBiome, EnemyBiomeId> {

    /**
     * Retorna tots els EnemyBiome d'un bioma concret, amb l'Enemy (i el
     * Yumedant) precarregat per evitar LazyInitializationException quan
     * StateCombat llegeix HP/MP/atk/def i els action IDs fora de la transacció.
     *
     * El LEFT JOIN FETCH e.acts carrega les accions disponibles de cada enemic
     * (via YUMEDANT_ACT) per omplir EnemyCombatState.actionIds al constructor
     * de StateCombat.
     */
    @Query("SELECT eb FROM EnemyBiome eb JOIN FETCH eb.enemy e LEFT JOIN FETCH e.acts WHERE eb.biome.id = :biomeId")
    List<EnemyBiome> findByBiomeId(@Param("biomeId") int biomeId);
}
