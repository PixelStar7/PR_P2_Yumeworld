package net.iesmila.dam.yumeworld.server.db.repository;

import net.iesmila.dam.yumeworld.server.db.model.Biome;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BiomeRepository extends JpaRepository<Biome, Integer> {

    /**
     * Retorna tots els biomes ordenats per bi_order ascendent. El servei de
     * generació del mapa fa servir aquesta ordenació per repartir els biomes
     * equitativament entre els pisos: ordre més baix = bioma més suau (primer
     * pis), ordre més alt = bioma més intens (boss).
     */
    List<Biome> findAllByOrderByOrderAsc();
}
