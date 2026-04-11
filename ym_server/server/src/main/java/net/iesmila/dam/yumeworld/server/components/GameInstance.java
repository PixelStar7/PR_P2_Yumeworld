package net.iesmila.dam.yumeworld.server.components;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import net.iesmila.dam.yumeworld.server.api.records.User;
import net.iesmila.dam.yumeworld.server.api.records.characterselection.request.select_character;
import net.iesmila.dam.yumeworld.server.api.records.characterselection.response.lobby_update;
import net.iesmila.dam.yumeworld.server.components.enums.ChestState;
import net.iesmila.dam.yumeworld.server.components.enums.CombatState;
import net.iesmila.dam.yumeworld.server.components.enums.GameState;
import net.iesmila.dam.yumeworld.server.components.enums.RestState;
import net.iesmila.dam.yumeworld.server.db.model.YuCharacter;
import org.springframework.boot.jackson.autoconfigure.JacksonProperties;
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

    // Variables que controlen en quin punt del diagrama estem
    private volatile GameState currentState = GameState.CHARACTER_SELECTION;
    private volatile CombatState currentCombatState = null;
    private volatile ChestState currentChestState = null;

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

                            // -- FASE DE LOBBY (SELECCIÓ) --
                            case "SELECT_CHARACTER": select_character(data); break;
                            case "SET_READY": set_ready(data); break;
                            case "HOST_START_GAME": host_start_game(data); break;

                            // -- FASE DE MAPA --
                            case "MOVE_TO_NODE": move_to_node(data); break;
                            case "OPEN_MENU": open_menu(data); break;
                            case "CLOSE_MENU": close_menu(data); break;

                            // -- FASE DE VOTACIÓ --
                            case "SUBMIT_VOTE": submit_vote(data); break;

                            // -- FASE DE COMBAT --
                            case "COMBAT_ACTION": combat_action(data); break;

                            // -- HABITACIONS (COFRE / DESCANS) --
                            case "LEAVE_ROOM": leave_room(data); break;

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
    // MÈTODES DE CHARACTER SELECTION
    // =====================================================

    // Requests Servidor

    private void select_character(JsonNode data) {
        if (currentState == GameState.CHARACTER_SELECTION) {

            // Llegir el JSON a través del Record
            try {
                // Creem el record de request
                var requestData = objectMapper.treeToValue(data, select_character.class);

                int userId = data.get("userId").asInt();
                int characterId = data.get("characterId").asInt();

                // Si el personatge ja ha sigut triat per un altre jugador...
                boolean jaTriat = userCharacterMap.entrySet().stream()
                        .anyMatch(entry -> entry.getValue() == characterId && entry.getKey() != userId);

                if (!jaTriat) {
                    // Es pot escollir! Guardem l'elecció o actualitzem
                    userCharacterMap.put(userId, characterId);

                    // @TODO: Aquí s'hauria de modificar la llista 'Characters' (canviar el 'isAvailable' a false)
                    // i modificar la llista 'Users' (posar el 'selectedYumedant') perquè el JSON que envieu al lobby_update surti bé.

                    System.out.println("El jugador " + userId + " ha triat el personatge " + characterId);

                } else {
                    System.out.println("El personatge " + characterId + " ja està agafat!");
                    // Enviar error al jugador que ho ha intentat?
                }

            } catch (Exception ex) {
                System.out.println("Error llegint dades de select_character: " + ex.getMessage());
                return;
            }

            // Enviar l'estat actualitzat del Lobby a TOTHOM
            enviarLobbyUpdate();
        }
    }

    private void set_ready(JsonNode data) {
        // @TODO: Marcar el jugador com a ready
    }

    private void host_start_game(JsonNode data) {
        // @TODO: Canviar de GameState, el host ha decidit començar la partida
    }


    // Responses Servidor

    private void enviarGameStart() {
        // @TODO: Enviar El començament de partida i canviar el GameState
    }

    private void enviarLobbyUpdate() {
        try {
            // Construïm el Record de resposta
            lobby_update lu = new lobby_update(this.id, Users, Characters);

            // El transformem a JSON real:
            // ("{"action": "LOBBY_UPDATE", "data": {...}}")
            Object responseNode = new Object() {
                public final String action = "LOBBY_UPDATE";
                public final lobby_update data = lu;
            };

            String textJson = objectMapper.writeValueAsString(responseNode);
            broadcast(textJson);

        } catch (Exception ex) {
            System.out.println("Error enviant lobby_update: " + ex.getMessage());
        }
    }


    // =====================================================
    // MÈTODES DE FASE DE MAPA
    // =====================================================

    // Requests Servidor

    private void move_to_node(JsonNode data) {
        // @TODO: Moure l'equip de jugadors al següent node escollit
    }

    private void open_menu(JsonNode data) {
        // @TODO: Obrir el menu per als jugadors
    }

    private void close_menu(JsonNode data) {
        // @TODO: Tancar el menu per als jugadors
    }


    // Responses Servidor

    private void enviarMapUpdate() {
        // @TODO: Enviar l'actualització del mapa als jugadors
    }

    private void enviarNodeEntered() {
        // @TODO: Enviar que s'ha entrat en un node i a quin als jugadors
    }


    // =====================================================
    // MÈTODES DE FASE DE VOTACIÓ
    // =====================================================

    // Requests Servidor

    private void submit_vote(JsonNode data) {
        // @TODO: Guardar el vot enregistrat pel jugador
    }


    // Responses Servidor

    private void enviarVoteUpdate() {
        // @TODO: Enviar actualització en temps real dels vots dels jugadors de l'equip
    }

    private void enviarVotingResult() {
        // @TODO: Enviar resultats de la votació als jugadors
    }


    // =====================================================
    // MÈTODES DE FASE DE COMBAT
    // =====================================================

    // Requests Servidor

    private void combat_action(JsonNode data) {
        // @TODO: Guardar i gestionar l'acció que rebem del jugador al que l'hi toca
    }


    // Responses Servidor

    private void enviarCombatStart() {
        // @TODO: Enviar als jugadors la llista d'enemics que hi han en el combat que acaben de trobar
    }

    private void enviarActionResult() {
        // @TODO: Enviar als jugadors els resultats de l'acció del jugador que ha actuat
    }

    private void enviarTurnUpdate() {
        // @TODO: Enviar als jugadors a qui l'hi toca ara
    }

    private void enviarCombatEnd() {
        // @TODO: Enviar als jugadors els resultats de la batalla (Victòria o Derrota)
    }


    // =====================================================
    // MÈTODES DE LES HABITACIONS (CHEST / REST)
    // =====================================================

    // Requests Servidor

    private void leave_room(JsonNode data) {
        // @TODO: Gestionar el sortir de la sala en la que es troben els jugadors
    }


    // Responses Servidor

    private void enviarChestLoot() {
        // @TODO: Enviar als jugadors el loot rebut del cofre que han trobat (Només en ChestRoom)
    }

    private void enviarStatsRecovered() {
        // @TODO: Enviar als jugadors els punts de vida i de manà que han restaurat
    }
}
