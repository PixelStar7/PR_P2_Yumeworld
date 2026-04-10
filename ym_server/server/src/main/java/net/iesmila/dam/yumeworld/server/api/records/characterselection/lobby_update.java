package net.iesmila.dam.yumeworld.server.api.records.characterselection;

import net.iesmila.dam.yumeworld.server.api.model.Player;
import net.iesmila.dam.yumeworld.server.api.model.User;

import java.util.List;

public record lobby_update (
        List<User> players,
        List<Player> characters
){}

/*
{
  "action": "LOBBY_UPDATE",
  "data": {
      "players": [
        { "id": 1, "name": "Sara", "selectedYumedant": 102, "isReady": true },
        { "id": 2, "name": "Pau", "selectedYumedant": null, "isReady": false }
      ],
      "characters": [
        { "id": 101, "name": "Michi", "type": "Warrior", "isAvailable": true },
        { "id": 102, "name": "Pingu", "type": "Mage", "isAvailable": false },
        { "id": 103, "name": "Pipa", "type": "Rogue", "isAvailable": true }
      ]
  }
}
*/

