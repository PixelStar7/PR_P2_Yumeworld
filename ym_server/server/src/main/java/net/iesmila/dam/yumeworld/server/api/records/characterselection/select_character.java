package net.iesmila.dam.yumeworld.server.api.records.characterselection;

import net.iesmila.dam.yumeworld.server.api.model.Player;
import net.iesmila.dam.yumeworld.server.api.model.User;

import java.util.List;

public record select_character(
        int UserId,
        int characterId
){}

/*
{
  "action": "SELECT_CHARACTER",
  "data": {
      "userId" = 1
      "characterId" = 1
  }
}
*/
