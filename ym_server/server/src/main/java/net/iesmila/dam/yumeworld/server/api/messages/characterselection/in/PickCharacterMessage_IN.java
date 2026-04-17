package net.iesmila.dam.yumeworld.server.api.messages.characterselection.request;

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
