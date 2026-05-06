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
    private double chanceRest = 0.20;   // 20%
    private double chanceChest = 0.15;  // 15%
    // La resta serà combat
    private int numColumns=5;

    public FloorInfo generateFloor(int floorNum, int numColumns, String biomeName) {
        List<RoomInfo> rooms = new ArrayList<>();
        int idCounter = 0;

        for (int row = 0; row < 4; row++) {
            for (int col = 0; col < numColumns; col++) {
                String type = determineRoomType(row);

                // La primera sala (0,0) sol ser la 'current' en començar
                boolean isCurrent = (row == 0 && col == 0);

                rooms.add(new RoomInfo(idCounter++, type, false, isCurrent));
            }
        }
        return new FloorInfo(floorNum, "Pis " + floorNum, biomeName, rooms);
    }

    private String determineRoomType(int row) {
        if (row == 0) return RoomType.COMBAT.name();
        if (row == 3) return RoomType.BOSS_COMBAT.name();

        // Lògica per a files 1 i 2
        double rand = Math.random();
        if (rand < chanceRest) {
            return RoomType.REST_EVENT.name();
        } else if (rand < (chanceRest + chanceChest)) {
            return RoomType.CHEST_EVENT.name();
        } else {
            return RoomType.COMBAT.name();
        }
    }
}
