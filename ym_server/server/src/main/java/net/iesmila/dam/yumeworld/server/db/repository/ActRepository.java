package net.iesmila.dam.yumeworld.server.db.repository;

import net.iesmila.dam.yumeworld.server.db.model.Act;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositori per a la taula ACT.
 *
 * El mètode principal carrega l'acció amb tota la cadena d'efectes de cop
 * (JOIN FETCH), evitant LazyInitializationException quan StateCombat processa
 * una acció fora d'una transacció Spring.
 *
 * Cadena carregada:
 *   Act → ActionEffect → Effect
 *   Act → ActionEffect → ParameterValue  (via EAGER a ActionEffect)
 */
@Repository
public interface ActRepository extends JpaRepository<Act, Integer> {

    /**
     * Carrega un Act amb els seus ActionEffects i Effects precarregats.
     * Els ParameterValues es carreguen automàticament perquè ActionEffect.parameterValues
     * és FetchType.EAGER.
     *
     * @param id ac_id de la taula ACT
     * @return l'acció amb efectes, o empty si no existeix
     */
    @Query("SELECT a FROM Act a LEFT JOIN FETCH a.actionEffects ae LEFT JOIN FETCH ae.effect WHERE a.id = :id")
    Optional<Act> findByIdWithEffects(@Param("id") Integer id);
}
