package net.iesmila.dam.yumeworld.server.websocket;

import net.iesmila.dam.yumeworld.server.components.GameManager;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
public class WSHandler extends TextWebSocketHandler {

    private final GameManager gameManager;

    public WSHandler(GameManager gameManager) {
        this.gameManager = gameManager;
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        gameManager.handleIncoming(session, message.getPayload());
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session)
    {
        System.out.println("nova connexió establerta --> "+session.getId());
        gameManager.onConnect(session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status){
        System.out.println("connexió tancada --> "+session.getId());
    }



//    @Override
//    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
//        System.out.println("He rebut: " + message.getPayload() + " de l'usuari " + session.getId());
//
//        try {
//            session.sendMessage(new TextMessage("Hola que tal!!!"));
//
//        } catch (IOException ex) {
//            ex.printStackTrace();
//        }
//    }
}