package net.iesmila.dam.yumeworld.server.services.impl;

import net.iesmila.dam.yumeworld.server.api.dto.FloorInfo;
import net.iesmila.dam.yumeworld.server.api.dto.RoomInfo;
import net.iesmila.dam.yumeworld.server.core.enums.RoomType;
import net.iesmila.dam.yumeworld.server.services.IMapGenerationService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class MapGenerationService implements IMapGenerationService {

    // Percentatges de probabilitat (es podrien treure d'un fitxer .properties)
    private final double chanceRest = 0.20;   // 20%
    private final double chanceChest = 0.20;  // 20%

    @Override
    public FloorInfo generateFloor(int floorNum, int totalFloors, int numColumns) {
        List<RoomInfo> rooms = new ArrayList<>();

        // El bioma és per pis: es calcula a partir de la posició del pis dins del mapa.
        String biomeName = determineBiome(floorNum, totalFloors);

        // Un pis és una única fila de nodes (només se'n visitarà un).
        // El tipus de cada node es decideix individualment perquè el jugador
        // pugui triar entre opcions diferents (combat, descans, cofre...).
        for (int col = 0; col < numColumns; col++) {
            String type = determineRoomType(floorNum, totalFloors);
            rooms.add(new RoomInfo(col, type, false));
        }

        return new FloorInfo(floorNum, "Pis " + floorNum, biomeName, rooms);
    }

    @Override
    public FloorInfo generateTypeFloor(int floorNum, int totalFloors, int numColumns, RoomType type) {
        List<RoomInfo> rooms = new ArrayList<>();

        // El bioma és per pis: es calcula a partir de la posició del pis dins del mapa.
        String biomeName = determineBiome(floorNum, totalFloors);

        // Tots els nodes del pis comparteixen el tipus indicat.
        for (int col = 0; col < numColumns; col++) {
            rooms.add(new RoomInfo(col, type.name(), false));
        }

        return new FloorInfo(floorNum, "Pis " + floorNum, biomeName, rooms);
    }

    /**
     * Determina el tipus d'una sala a partir de la posició del pis dins del mapa.
     * El primer pis sempre és COMBAT, l'últim sempre BOSS_COMBAT, i als pisos
     * intermedis s'aplica probabilitat per tenir descans, cofre o combat.
     */
    private String determineRoomType(int floorNum, int totalFloors) {
        if (floorNum == 0) return RoomType.COMBAT.name();             // Primer pis: combat
        if (floorNum == totalFloors - 1) return RoomType.BOSS_COMBAT.name(); // Últim pis: boss

        // Lògica per a pisos intermedis
        double rand = Math.random();
        if (rand < chanceRest) {
            return RoomType.REST_EVENT.name();
        } else if (rand < (chanceRest + chanceChest)) {
            return RoomType.CHEST_EVENT.name();
        } else {
            return RoomType.COMBAT.name();
        }
    }

    /**
     * Calcula el bioma associat a un pis a partir de la seva posició dins del
     * total de pisos del mapa. El primer pis rep el bioma més suau (numbness)
     * i l'últim el més intens (nightmare). Els pisos intermedis reben els biomes
     * intermedis distribuïts proporcionalment.
     */
    private String determineBiome(int floorNum, int totalFloors) {
        String[] biomes = {"numbness", "lightSleep", "deepSleep", "remSleep", "nightmare"};

        // Evitem divisió per zero i valors fora de rang si només hi ha 1 pis
        if (totalFloors <= 1) {
            return biomes[0];
        }

        // Calculem la proporció del pis actual respecte al total.
        // floorNum va de 0 a (totalFloors - 1)
        double ratio = (double) floorNum / (totalFloors - 1);

        // Multipliquem la proporció per (biomes.length - 1) per no sortir dels límits
        // i fem servir Math.round() per a una distribució més equitativa.
        int biomeIndex = (int) Math.round(ratio * (biomes.length - 1));

        // Per assegurar que no ens passem de l'índex màxim de l'array
        biomeIndex = Math.max(0, Math.min(biomeIndex, biomes.length - 1));

        return biomes[biomeIndex];
    }
}
