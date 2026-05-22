package net.iesmila.dam.yumeworld.server.db.repository;

import net.iesmila.dam.yumeworld.server.db.model.EnemyFloor;
import net.iesmila.dam.yumeworld.server.db.model.EnemyFloorId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EnemyFloorRepository extends JpaRepository<EnemyFloor, EnemyFloorId> {

    /**
     * Retorna tots els EnemyFloor d'un pis concret, amb l'Enemy (i el Yumedant)
     * precarregat per evitar LazyInitializationException quan el StateCombat
     * llegeix HP/MP/atk/def i els action IDs fora de la transacció.
     *
     * El LEFT JOIN FETCH e.acts carrega les accions disponibles de cada enemic
     * (via YUMEDANT_ACT) per omplir EnemyCombatState.actionIds al constructor de StateCombat.
     */
    @Query("SELECT ef FROM EnemyFloor ef JOIN FETCH ef.enemy e LEFT JOIN FETCH e.acts WHERE ef.floor.id = :floorId")
    List<EnemyFloor> findByFloorId(@Param("floorId") int floorId);
}
