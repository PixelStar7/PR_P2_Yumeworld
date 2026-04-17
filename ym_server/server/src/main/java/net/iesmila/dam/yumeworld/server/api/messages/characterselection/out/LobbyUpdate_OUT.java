package net.iesmila.dam.yumeworld.server.api.messages.characterselection.out;

import net.iesmila.dam.yumeworld.server.db.model.YuCharacter;
import java.util.List;

public record lobby_update (
        String gameId,
        List<PlayerInfo> players,
        List<CharacterInfo> characters
){}

/*
{
  "action": "LOBBY_UPDATE",
  "data": {
      "gameId": 1
      "players": [
        { "id": 1, "name": "Sara", "selectedYumedant": 102, "isReady": true },
        { "id": 2, "name": "Pau", "selectedYumedant": null, "isReady": false }
      ],
      "characters": [
        { "id": 101, "name": "Michi", "type": "Warrior", "isAvailable": true },
        { "id": 102, "name": "Pingu", "type": "Mage", "isAvailable": false , "playerId" : 1 },
        { "id": 103, "name": "Pipa", "type": "Rogue", "isAvailable": true }
      ]
  }
}
*/