package net.iesmila.dam.yumeworld.server.services;

import net.iesmila.dam.yumeworld.server.api.messages.mapexploration.minimodels.FloorInfo;
import net.iesmila.dam.yumeworld.server.core.enums.RoomType;

public interface IMapGenerationService {

    FloorInfo generateFloor(int floorNum, int numRows, int numColumns, String biomeName);

    FloorInfo generateRandomFloor(int floorNum, int numRows, int numColumns, String biomeName);

    FloorInfo generateTypeFloor(int floorNum, int numRows, int numColumns, RoomType type, String biomeName);
}
