package net.iesmila.dam.yumeworld.server.websocket;

import org.springframework.context.annotation.Bean;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

public class WSConfiguration implements WebSocketConfigurer {

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(WSHandler(), "/ws");
    }

    @Bean
    public WebSocketHandler WSHandler() {
        return new WSHandler();
    }
}
