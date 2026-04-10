package net.iesmila.dam.yumeworld.server.components;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import net.iesmila.dam.yumeworld.server.api.records.User;
import net.iesmila.dam.yumeworld.server.api.records.characterselection.response.lobby_update;
import net.iesmila.dam.yumeworld.server.db.model.YuCharacter;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.TextMessage;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;


import java.io.IOException;

import java.util.concurrent.atomic.AtomicBoolean;

import java.util.UUID;

/**
 * Classe que representa una partida i tot el seu estat:
 *  - jugadors connectats
 *  - cua de missatges
 *  - estat intern del joc (@TODO)
 */
public class GameInstance {

    /**
     * Les connexions dels jugadors de la partida
     */
    private final List<WebSocketSession> playerSession;

    /**
     * Llista de missatges per gestionar-ho (INBOX)
     */
    private final BlockingQueue<GameMessage> queue = new LinkedBlockingQueue<>();

    /**
     * Executor de fils
     */
    private final ExecutorService executor;

    /**
     * Mutex per controlar que el joc només tingui 1 fil en execució
     */
    private final AtomicBoolean threadIsRunning = new AtomicBoolean(false);

    /**
     * Indica si la partida està en curs (true) o ha acabat (false)
     */
    private volatile boolean gameActive = true;

    /**
     * Id de la partida (UUID)
     */
    private final String id;


    // Variables d'informació de la partida

    // Eina de Spring per llegir JSON
    private final ObjectMapper objectMapper = new ObjectMapper();

    // Variable que controla en quin punt del diagrama estem
    private volatile GameState currentState = GameState.CHARACTER_SELECTION;

    // Map per associar jugadors amb els seus personatges
    private HashMap<Integer, Integer> userCharacterMap = new HashMap<>();

    // Llista de personatges jugables
    private List<YuCharacter> Characters = new ArrayList<>();

    // Llista d'usuaris a la partida
    private List<User> Users = new ArrayList<>();



    public GameInstance(List<WebSocketSession> players, ExecutorService executor) {
        this.id = UUID.randomUUID().toString();
        this.playerSession = players;
        this.executor = executor;

        // Inicialitzar llista de Characters fent una select a la BD


        System.out.println("Starting the game instance.");
    }

    /** Iniciar fil de la partida */
    public void start(){
        if(threadIsRunning.compareAndSet(false, true)){
            System.out.println("The thread has been started.");
            executor.submit(this::processLoop);
        }
    }

    /** Encuar missatge d'un player */
    public void enqueue(GameMessage message) {
        queue.offer(message);
    }
    /** retorna l'ID de la partida */
    public String getId() {
        return id;
    }

    // Aqui van tots els estats del joc
    private void processLoop() {
        try {
            while (gameActive) {

                GameMessage msg = queue.poll(15, TimeUnit.SECONDS);

                if (msg != null) {
                    String payload = msg.payload();
                    System.out.println("Game instance: Message received");

                    try {
                        // Llegim el text pla com a JSON real
                        JsonNode json = objectMapper.readTree(payload);

                        // Extraiem quina "acció" vol fer l'usuari
                        String action = json.has("action") ? json.get("action").asText() : "UNKNOWN";

                        // Extraiem les "dades" associades a aquesta acció (si en té)
                        JsonNode data = json.has("data") ? json.get("data") : null;

                        // Estats del servidor
                        switch(action) {

                            // -- FASE DE LOBBY --
                            case "GAME_START": game_start(); break;
                            case "SELECT_CHARACTER": select_character(data); break;



                            default:
                                System.out.println("Acció no reconeguda: " + action);
                                break;
                        }


                    } catch (Exception ex) {
                        System.out.println("Error processant el JSON del client: " + ex.getMessage());
                    }



                } else {
                    System.out.println("Game instance: timeout in message reception.");
                    broadcast("Timeout occurred");
                }
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /** Funció d'utilitat per enviar un missatge a una connexió concreta */
    private void send(WebSocketSession session, String text) {
        try {
            synchronized (session) {
                session.sendMessage(new TextMessage(text));
            }
        } catch (IOException e) {
            // handle disconnect
        }
    }
    /** Funció d'utilitat per enviar un missatge a tots els ususaris de la partida */
    private void broadcast(String text) {
        for (WebSocketSession player : playerSession) {
            send(player, text);
        }
    }

    public void stop() {
        gameActive = false;
    }



    // =====================================================
    // MÈTODES DE RESPONSE DEL SERVIDOR
    // =====================================================

    private void game_start() {

    }


    private void select_character(JsonNode data) {
        if (currentState == GameState.CHARACTER_SELECTION) {
            int userId = data.get("userId").asInt();
            int characterId = data.get("characterId").asInt();

            //Comprovar que el personatge estigui disponible
            if (userCharacterMap.containsValue(characterId)) {
                //Assignar el personatge al jugador
                userCharacterMap.put(userId, characterId);
            }

            // Enviar actualització del lobby
            lobby_update lu = new lobby_update(id, Users, Characters);
            broadcast(lu.toString());

//            if (tots estan escollits) {
//                game_start();
//            }

        }
    }
}
