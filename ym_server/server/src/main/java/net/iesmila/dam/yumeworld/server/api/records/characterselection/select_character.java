package net.iesmila.dam.yumeworld.server.api.records.characterselection;

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
