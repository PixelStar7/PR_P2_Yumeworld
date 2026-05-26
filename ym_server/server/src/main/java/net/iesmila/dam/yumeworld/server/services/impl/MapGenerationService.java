package net.iesmila.dam.yumeworld.server.services.impl;

import net.iesmila.dam.yumeworld.server.api.dto.FloorInfo;
import net.iesmila.dam.yumeworld.server.api.dto.RoomInfo;
import net.iesmila.dam.yumeworld.server.core.enums.RoomTypes;
import net.iesmila.dam.yumeworld.server.db.model.Biome;
import net.iesmila.dam.yumeworld.server.db.repository.BiomeRepository;
import net.iesmila.dam.yumeworld.server.services.IMapGenerationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class MapGenerationService implements IMapGenerationService {

    // Percentatges de probabilitat (es podrien treure d'un fitxer .properties)
    private final double chanceRest = 0.20;   // 20%
    private final double chanceChest = 0.20;  // 20%

    /**
     * Repositori de biomes. El servei llegeix tots els biomes ordenats per
     * bi_order i els reparteix equitativament entre els pisos: el primer
     * pis rep el bioma amb bi_order més baix i l'últim el bioma amb
     * bi_order més alt.
     *
     * NOTA: aquest servei és @Service i s'ha d'obtenir via Spring (autowired
     * dins de RepositoryProvider) — no l'instanciem amb 'new' enlloc.
     */
    private final BiomeRepository biomeRepository;

    @Autowired
    public MapGenerationService(BiomeRepository biomeRepository) {
        this.biomeRepository = biomeRepository;
    }

    @Override
    public FloorInfo generateFloor(int floorNum, int totalFloors, int numColumns) {
        List<RoomInfo> rooms = new ArrayList<>();

        // El bioma és per pis: es calcula a partir de la posició del pis dins del mapa.
        Biome biome = determineBiome(floorNum, totalFloors);

        // Un pis és una única fila de nodes (només se'n visitarà un).
        // El tipus de cada node es decideix individualment perquè el jugador
        // pugui triar entre opcions diferents (combat, descans, cofre...).
        for (int col = 0; col < numColumns; col++) {
            String type = determineRoomType(floorNum, totalFloors);
            rooms.add(new RoomInfo(col, type, false));
        }

        return buildFloorInfo(floorNum, biome, rooms);
    }

    @Override
    public FloorInfo generateTypeFloor(int floorNum, int totalFloors, int numColumns, RoomTypes type) {
        List<RoomInfo> rooms = new ArrayList<>();

        // El bioma és per pis: es calcula a partir de la posició del pis dins del mapa.
        Biome biome = determineBiome(floorNum, totalFloors);

        // Tots els nodes del pis comparteixen el tipus indicat.
        for (int col = 0; col < numColumns; col++) {
            rooms.add(new RoomInfo(col, type.name(), false));
        }

        return buildFloorInfo(floorNum, biome, rooms);
    }

    /**
     * Construeix el FloorInfo a partir del bioma (potser null si la BD està
     * buida) i la llista de sales. Si no hi ha bioma posem valors per defecte
     * perquè el client no rebi null pointers.
     */
    private FloorInfo buildFloorInfo(int floorNum, Biome biome, List<RoomInfo> rooms) {
        int biomeId = (biome != null) ? biome.getId() : -1;
        String biomeName = (biome != null) ? biome.getName() : "unknown";
        String biomeBg = (biome != null) ? biome.getBackground() : null;
        return new FloorInfo(floorNum, "Pis " + floorNum, biomeId, biomeName, biomeBg, rooms);
    }

    /**
     * Determina el tipus d'una sala a partir de la posició del pis dins del mapa.
     * El primer pis sempre és COMBAT, l'últim sempre BOSS_COMBAT, i als pisos
     * intermedis s'aplica probabilitat per tenir descans, cofre o combat.
     */
    private String determineRoomType(int floorNum, int totalFloors) {
        if (floorNum == 0) return RoomTypes.COMBAT.name();             // Primer pis: combat
        if (floorNum == totalFloors - 1) return RoomTypes.BOSS_COMBAT.name(); // Últim pis: boss

        // Lògica per a pisos intermedis
        double rand = Math.random();
        if (rand < chanceRest) {
            return RoomTypes.REST_EVENT.name();
        } else if (rand < (chanceRest + chanceChest)) {
            return RoomTypes.CHEST_EVENT.name();
        } else {
            return RoomTypes.COMBAT.name();
        }
    }

    /**
     * Calcula el bioma associat a un pis a partir de la seva posició dins del
     * total de pisos del mapa. El primer pis rep el bioma amb bi_order més
     * baix (més suau) i l'últim el bioma amb bi_order més alt (nightmare).
     * Els pisos intermedis reben els biomes intermedis distribuïts
     * proporcionalment.
     *
     * Els biomes es llegeixen de la BD ordenats per bi_order ascendent. Així
     * a l'editor pots afegir/treure biomes i el servidor automàticament els
     * incorpora a la distribució sense recompilar.
     */
    private Biome determineBiome(int floorNum, int totalFloors) {
        List<Biome> biomes = biomeRepository.findAllByOrderByOrderAsc();

        if (biomes == null || biomes.isEmpty()) {
            System.err.println("[MapGenerationService] WARN - No hi ha biomes a la BD. Retornant null.");
            return null;
        }

        // Evitem divisió per zero i valors fora de rang si només hi ha 1 pis o 1 bioma
        if (totalFloors <= 1 || biomes.size() == 1) {
            return biomes.get(0);
        }

        // Calculem la proporció del pis actual respecte al total.
        // floorNum va de 0 a (totalFloors - 1)
        double ratio = (double) floorNum / (totalFloors - 1);

        // Multipliquem la proporció per (biomes.size() - 1) per no sortir dels límits
        // i fem servir Math.round() per a una distribució més equitativa.
        int biomeIndex = (int) Math.round(ratio * (biomes.size() - 1));

        // Per assegurar que no ens passem de l'índex màxim
        biomeIndex = Math.max(0, Math.min(biomeIndex, biomes.size() - 1));

        return biomes.get(biomeIndex);
    }
}
