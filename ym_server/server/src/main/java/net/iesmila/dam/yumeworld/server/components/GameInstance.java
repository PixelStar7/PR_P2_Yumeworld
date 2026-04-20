package net.iesmila.dam.yumeworld.server.components;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import net.iesmila.dam.yumeworld.server.api.messages.JSONMessage;
import net.iesmila.dam.yumeworld.server.api.messages.characterselection.in.PickCharacterMessage_IN;
import net.iesmila.dam.yumeworld.server.api.models.Player;
import net.iesmila.dam.yumeworld.server.api.states.State;
import net.iesmila.dam.yumeworld.server.api.states.StateCharacterSelection;
import net.iesmila.dam.yumeworld.server.components.enums.ChestState;
import net.iesmila.dam.yumeworld.server.components.enums.CombatState;
import net.iesmila.dam.yumeworld.server.components.enums.GameState;
import net.iesmila.dam.yumeworld.server.db.model.YuCharacter;
import net.iesmila.dam.yumeworld.server.db.repository.RepositoryProvider;
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
     * Els jugadors de la partida
     */
    private final List<Player> players;

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

    // Variables que controlen en quin punt del diagrama estem
    private volatile State currentState;

    // Map per associar jugadors amb els seus personatges
    // private HashMap<Integer, Integer> userCharacterMap = new HashMap<>();

    protected ObjectMapper mapper = new ObjectMapper();

    // Bossa de Repositoris de la BD
    private final RepositoryProvider repoProvider;


    public GameInstance(List<Player> players, ExecutorService executor, RepositoryProvider repoProvider) {
        this.repoProvider = repoProvider;
        this.id = UUID.randomUUID().toString();
        this.players = players;
        this.executor = executor;
        System.out.println("Starting the game instance.");

        // Assignem estat actual
        currentState = new StateCharacterSelection(this, mapper, repoProvider);
    }

    // Getters + Setters
    public List<Player> getPlayers() {
        return players;
    }

    public void setCurrentState(State currentState) {
        this.currentState = currentState;
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

        while (gameActive) {
            try {
                currentState.tick();
            } catch (Exception e) {
                System.out.println("Error en el game loop:");
                e.printStackTrace();
            }
        }
    }

    /** Funció d'utilitat per enviar un missatge a una connexió concreta */
    public void send(WebSocketSession session, JSONMessage jsonMsg) {
        ObjectMapper mapper = new ObjectMapper();
        send(session, mapper.writeValueAsString(jsonMsg));
    }

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
    public void broadcast(JSONMessage jsonMsg) {
        ObjectMapper mapper = new ObjectMapper();
        broadcast(mapper.writeValueAsString(jsonMsg));
    }

    public void broadcast(String text) {
        for (Player player : players) {
            send(player.getSession(), text);
        }
    }

    public void stop() {
        gameActive = false;
    }

    /**
     * Mètode per llegir un missatge de la cua de missatges entrants al joc
     * @param time de timeout
     * @param timeUnit
     * @return el missatge, o null si no hi ha pendents durant el temps estipulat
     */
    public GameMessage pollMessage(int time, TimeUnit timeUnit) {
        try {
            return queue.poll(time, timeUnit);

        } catch (InterruptedException ex) {
            ex.printStackTrace();
            return null;
        }
    }



    // =====================================================
    // MÈTODES DE CHARACTER SELECTION
    // =====================================================

    // Requests Servidor



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
