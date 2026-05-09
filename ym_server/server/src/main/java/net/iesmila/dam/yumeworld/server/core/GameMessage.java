package net.iesmila.dam.yumeworld.server.core;

import net.iesmila.dam.yumeworld.server.api.models.Player;

// public record GameMessage (WebSocketSession session, String payload){}
public record GameMessage (Player user, String payload){}
