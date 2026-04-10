package net.iesmila.dam.yumeworld.server.api.records.characterselection;

public record player_ready(
        int UserId,
        boolean isReady) {}


/*
{
  "action": "PLAYER_READY",
  "data": {
      "userId" = 1
      "isReady" = true
  }
}
*/