package net.iesmila.dam.yumeworld.server.api.records;

import org.springframework.web.socket.WebSocketSession;

//public record User (
//        int id,
//        WebSocketSession session,
//        String name
//){}

/**
 * Representa l'estructura d'un Jugador de la partida
 */
public class Player {
    long id;
    WebSocketSession session;
    String name;

    public Player(long id, WebSocketSession session, String name) {
        this.id = id;
        this.session = session;
        this.name = name;
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
