package net.iesmila.dam.yumeworld.server.api.states;

import net.iesmila.dam.yumeworld.server.api.messages.JSONMessage;
import net.iesmila.dam.yumeworld.server.api.messages.mapexploration.minimodels.FloorInfo;
import net.iesmila.dam.yumeworld.server.core.GameInstance;
import net.iesmila.dam.yumeworld.server.core.GameMessage;
import net.iesmila.dam.yumeworld.server.db.repository.RepositoryProvider;
import net.iesmila.dam.yumeworld.server.services.IMapGenerationService;
import net.iesmila.dam.yumeworld.server.services.MapGenerationService;
import tools.jackson.databind.ObjectMapper;

import java.util.concurrent.TimeUnit;

public class StateMapCreation extends State{

    private int numFloors=5; //equival al num de files
    private int numColumnes = 5;
    FloorInfo[] floors = new FloorInfo[numFloors];
    //De moment tindrem els biomes hardcoded
    String[] biomes = {"numbness" , "lightSleep", "deepSleep", "remSleep", "nightmare"};


    public StateMapCreation(GameInstance game, ObjectMapper om, RepositoryProvider rp){
        super(game, om, rp); // Passem la instància única cap amunt
        //Cridar el MapGenerationService
        IMapGenerationService mapGenerationService = new MapGenerationService();

        for (int i = 0; i < numFloors; i++) {
            //    FloorInfo generateFloor(int floorNum, int numColumns, String biomeName);
            floors[i] = mapGenerationService.generateFloor(i, numColumnes);
        }




    }

    @Override
    public void tick() {
        GameMessage message = game.pollMessage(5, TimeUnit.SECONDS);
        if (message != null) {
            // Ha entrat un missatge
            JSONMessage gm = objectMapper.readValue(message.payload(), JSONMessage.class);
            System.out.println("TICK: " + gm.messageType);


        }


    }
}
