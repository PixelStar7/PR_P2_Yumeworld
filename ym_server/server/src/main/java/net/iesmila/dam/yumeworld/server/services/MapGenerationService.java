package net.iesmila.dam.yumeworld.server.services;

import net.iesmila.dam.yumeworld.server.api.messages.mapexploration.minimodels.FloorInfo;
import net.iesmila.dam.yumeworld.server.api.messages.mapexploration.minimodels.RoomInfo;
import net.iesmila.dam.yumeworld.server.core.enums.RoomType;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class MapGenerationService implements IMapGenerationService {

    // Percentatges de probabilitat (es podrien treure d'un fitxer .properties)
    private final double chanceRest = 0.20;   // 20%
    private final double chanceChest = 0.15;  // 15%

    @Override
    public FloorInfo generateFloor(int floorNum, int numRows, int numColumns, String biomeName) {
        List<RoomInfo> rooms = new ArrayList<>();
        int idCounter = 0;

        for (int row = 0; row < numRows; row++) {
            // Determinar el bioma per a la fila actual
            String currentBiome = determineBiome(row, numRows);

            for (int col = 0; col < numColumns; col++) {
                String type = determineRoomType(row, numRows);
                
                // Nota: Estem passant currentBiome si ho necessites més endavant,
                // de moment l'estic guardant com una dada més de generació.
                rooms.add(new RoomInfo(idCounter++, type, false, false));
            }
        }
        return new FloorInfo(floorNum, "Pis " + floorNum, biomeName, rooms);
    }

    @Override
    public FloorInfo generateRandomFloor(int floorNum, int numRows, int numColumns, String biomeName) {
        // Mètode per implementar si cal lògica diferent a generateFloor
        return generateFloor(floorNum, numRows, numColumns, biomeName); 
    }

    @Override
    public FloorInfo generateTypeFloor(int floorNum, int numRows, int numColumns, RoomType type, String biomeName) {
         List<RoomInfo> rooms = new ArrayList<>();
        int idCounter = 0;

        for (int row = 0; row < numRows; row++) {
            for (int col = 0; col < numColumns; col++) {
                //No assignem isCurrent perquè no hi ha un punt d'entrada definit --> false.
                rooms.add(new RoomInfo(idCounter++, type.name(), false, false));
            }
        }
        return new FloorInfo(floorNum, "Pis " + floorNum, biomeName, rooms);
    }

    private String determineRoomType(int row, int totalRows) {
        if (row == 0) return RoomType.COMBAT.name(); // Primera fila: combat
        if (row == totalRows - 1) return RoomType.BOSS_COMBAT.name(); // Última fila: boss

        // Lògica per a files intermèdies
        double rand = Math.random();
        if (rand < chanceRest) {
            return RoomType.REST_EVENT.name();
        } else if (rand < (chanceRest + chanceChest)) {
            return RoomType.CHEST_EVENT.name();
        } else {
            return RoomType.COMBAT.name();
        }
    }

    private String determineBiome(int row, int totalRows) {
        String[] biomes = {"numbness", "lightSleep", "deepSleep", "remSleep", "nightmare"};
        
        // Evitem divisió per zero i valors fora de rang si només hi ha 1 fila
        if (totalRows <= 1) {
            return biomes[0];
        }

        // Calculem la proporció de la fila actual respecte al total.
        // row va de 0 a (totalRows - 1)
        double ratio = (double) row / (totalRows - 1);

        // Multipliquem la proporció per la quantitat de biomes i ho convertim en index.
        // Necessitem multiplicar per (biomes.length - 1) per no sortir dels límits
        // i afegir Math.round() per a una distribució més equitativa
        int biomeIndex = (int) Math.round(ratio * (biomes.length - 1));

        // Per assegurar que no ens passem de l'índex màxim de l'array
        biomeIndex = Math.max(0, Math.min(biomeIndex, biomes.length - 1));

        return biomes[biomeIndex];
    }
}
