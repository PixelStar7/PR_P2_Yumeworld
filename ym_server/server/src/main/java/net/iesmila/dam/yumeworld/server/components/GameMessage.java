package net.iesmila.dam.yumeworld.server.components;

import org.springframework.web.socket.WebSocketSession;

public record GameMessage (WebSocketSession session, String payload){}
