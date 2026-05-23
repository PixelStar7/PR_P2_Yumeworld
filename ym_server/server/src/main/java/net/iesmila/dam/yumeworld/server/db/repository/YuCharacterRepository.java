package net.iesmila.dam.yumeworld.server.db.repository;

import net.iesmila.dam.yumeworld.server.db.model.YuCharacter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface YuCharacterRepository extends JpaRepository<YuCharacter, Integer> {

    /**
     * Carrega un YuCharacter amb les seves accions disponibles (YUMEDANT_ACT) precarregades.
     * S'usa a StateCombat per construir el mapa validActionMpCosts de cada jugador
     * al inici del combat, evitant LazyInitializationException fora de transacció.
     *
     * @param id pl_yumedant_id del personatge
     * @return el personatge amb la llista d'accions carregada, o empty si no existeix
     */
    @Query("SELECT c FROM YuCharacter c LEFT JOIN FETCH c.acts WHERE c.id = :id")
    Optional<YuCharacter> findByIdWithActs(@Param("id") Integer id);

    /**
     * Carrega un YuCharacter amb el seu rol i les habilitats associades al rol
     * (PLAYERROLE_ABILITY) precarregades. S'usa a StateCombat juntament amb
     * {@link #findByIdWithActs} per oferir al jugador totes les accions del seu rol
     * a més de les que té assignades directament al personatge (YUMEDANT_ACT).
     *
     * Es fa en una consulta separada de {@link #findByIdWithActs} per evitar
     * MultipleBagFetchException de Hibernate (no es poden fer JOIN FETCH a dues
     * col·leccions distintes a la mateixa query).
     *
     * @param id pl_yumedant_id del personatge
     * @return el personatge amb el rol i les seves habilitats, o empty si no existeix
     */
    @Query("SELECT c FROM YuCharacter c LEFT JOIN FETCH c.role r LEFT JOIN FETCH r.abilities WHERE c.id = :id")
    Optional<YuCharacter> findByIdWithRoleAbilities(@Param("id") Integer id);
}
