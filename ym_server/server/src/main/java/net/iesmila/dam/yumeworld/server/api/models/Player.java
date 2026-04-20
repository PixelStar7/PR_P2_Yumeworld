package net.iesmila.dam.yumeworld.server.api.models;

import org.springframework.web.socket.WebSocketSession;

/**
 * Representa l'estructura d'un Jugador de la partida
 */
public class Player {
    long id;
    WebSocketSession session;
    String name;

    public Player(long id, WebSocketSession session) {
        this.id = id;
        this.session = session;
    }

    public long getId() {
        return id;
    }

    public WebSocketSession getSession() {
        return session;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
