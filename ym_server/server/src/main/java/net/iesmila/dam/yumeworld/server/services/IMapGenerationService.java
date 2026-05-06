package net.iesmila.dam.yumeworld.server.services;

import net.iesmila.dam.yumeworld.server.api.messages.mapexploration.minimodels.FloorInfo;

public interface IMapGenerationService {

    FloorInfo generateFloor(int floorNum, int numColumns, String biomeName);
}
