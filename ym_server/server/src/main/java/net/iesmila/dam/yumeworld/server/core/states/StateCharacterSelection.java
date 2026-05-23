package net.iesmila.dam.yumeworld.server.core.states;

import net.iesmila.dam.yumeworld.server.api.messages.JSONMessage;
import net.iesmila.dam.yumeworld.server.api.messages.characterselection.in.PickCharacter_IN;
import net.iesmila.dam.yumeworld.server.api.dto.CharacterInfo;
import net.iesmila.dam.yumeworld.server.api.messages.characterselection.out.GameStart_OUT;
import net.iesmila.dam.yumeworld.server.api.messages.characterselection.out.LobbyStart_OUT;
import net.iesmila.dam.yumeworld.server.api.messages.characterselection.out.LobbyUpdate_OUT;
import net.iesmila.dam.yumeworld.server.api.dto.PlayerInfo;
import net.iesmila.dam.yumeworld.server.api.messages.generic.out.ActionResult_OUT;
import net.iesmila.dam.yumeworld.server.api.models.Player;
import net.iesmila.dam.yumeworld.server.api.models.PlayerCharacterState;
import net.iesmila.dam.yumeworld.server.core.GameInstance;
import net.iesmila.dam.yumeworld.server.core.GameMessage;
import net.iesmila.dam.yumeworld.server.db.model.PlayerRole;
import net.iesmila.dam.yumeworld.server.db.model.YuCharacter;
import net.iesmila.dam.yumeworld.server.db.repository.RepositoryProvider;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static net.iesmila.dam.yumeworld.server.core.enums.Codes.*;

/**
 * Estat on els jugadors han de seleccionar el seu personatge abans de començar la partida.
 * Aquest estat gestiona la llista de personatges disponibles i l'assignació de cada personatge
 * als jugadors que el sol·licitin. Un cop tots els jugadors tenen un personatge assignat,
 * la partida comença automàticament (no hi ha "Ready" explícit).
 */
public class StateCharacterSelection extends State {

    LobbyUpdate_OUT m;

    public StateCharacterSelection(GameInstance game, ObjectMapper objectMapper, RepositoryProvider repoProvider) {
        super(game, objectMapper, repoProvider); // Passem la instància única cap amunt

        // GameInstance és l'encarregat de crear l'objectMapper.

        //Fem un broadcast de LobbyStart_OUT per al nostre estimat client
        game.broadcast(new JSONMessage(game.getId(), new LobbyStart_OUT(game.getPlayers().size())));
        System.out.println("[StateCharacterSelection] [" + game.getId() + "] LobbyStart_OUT broadcasted correctament a l'inici.");

        // Omplim el Missatge de sortida LobbyUpdate_OUT
        System.out.println("[StateCharacterSelection] [" + game.getId() + "] Inicialitzant estat i preparant missatge LobbyUpdate inicial.");
        OmplirMissatgeSortida(game, repoProvider);
    }


    private void OmplirMissatgeSortida(GameInstance game, RepositoryProvider repoProvider) {
        m = new LobbyUpdate_OUT();

        List<Player> llistaGI = game.getPlayers();
        m.players = new ArrayList<>();
        for (Player p : llistaGI) {
            PlayerInfo infop = new PlayerInfo();
            infop.id = p.getId();
            infop.name = p.getName();
            m.players.add(infop);
        }
        System.out.println("[StateCharacterSelection] [" + game.getId() + "] " + m.players.size() + " jugadors carregats al LobbyUpdate.");

        List<YuCharacter> llistaBd = repoProvider.RepoYuCharacter.findAll();

        m.characters = new ArrayList<>();
        for (YuCharacter yu : llistaBd) {
            CharacterInfo infoc = new CharacterInfo();
            infoc.characterId = yu.getId();
            infoc.name = yu.getName();
            infoc.imageUrl = yu.getImage();
            infoc.isSelected = false;
            // selectedPlayerId queda a -1 per defecte (cap jugador)
            m.characters.add(infoc);
        }
        System.out.println("[StateCharacterSelection] [" + game.getId() + "] " + m.characters.size() + " personatges carregats al LobbyUpdate.");
        game.broadcast(new JSONMessage(game.getId(), m));
        System.out.println("[StateCharacterSelection] [" + game.getId() + "] LobbyUpdate_OUT broadcasted correctament a l'inici.");
    }


    /**
     * Mètode que s'executa continuament mentre aquest estat estigui actiu.
     * S'encarrega d'esperar i processar els missatges entrants.
     */
    @Override
    public void tick() {

        GameMessage message = game.pollMessage(5, TimeUnit.SECONDS);

        if (message != null) {
            try {
                JSONMessage gm = objectMapper.readValue(message.payload(), JSONMessage.class);
                System.out.println("[StateCharacterSelection] [" + game.getId() + "] TICK - Rebut missatge de tipus: " + gm.messageType + " del jugador " + message.user().getId());

                if (PickCharacter_IN.TYPE.equals(gm.messageType)) {
                    handlePickCharacter(message.user(), gm);
                } else {
                    System.out.println("[StateCharacterSelection] [" + game.getId() + "] Tipus de missatge desconegut o no esperat en aquest estat: " + gm.messageType);
                }

            } catch (Exception e) {
                System.out.println("[StateCharacterSelection] [" + game.getId() + "] Error parsejant el missatge: " + e.getMessage());
            }
        }
    }

    /**
     * Comprova si tots els jugadors tenen un personatge assignat.
     * Si és així, fa broadcast de GameStart_OUT i fa la transició a StateMapCreation.
     * Aquest mètode substitueix la fase "Ready": quan tothom ha triat, comença la partida.
     */
    private void checkAllPickedAndStart() {
        boolean allPicked = true;
        for (PlayerInfo p : m.players) {
            // Per a cada jugador, comprovem que existeix algun CharacterInfo seleccionat per ell
            boolean hasCharacter = m.characters.stream()
                    .anyMatch(c -> c.isSelected && c.selectedPlayerId == p.id);
            if (!hasCharacter) {
                allPicked = false;
                break; // Optimització: si en falta un, ja no cal seguir
            }
        }

        if (allPicked) {
            System.out.println("[StateCharacterSelection] [" + game.getId() + "] >>> TOTS ELS JUGADORS TENEN PERSONATGE <<<");

            // Inicialitzem el PlayerCharacterState de cada jugador a partir del YuCharacter triat.
            // Aquests estats viuran al GameInstance durant tota la partida.
            initializePlayerStates();

            System.out.println("[StateCharacterSelection] [" + game.getId() + "] Iniciant transició cap a l'estat StateMapCreation...");
            game.broadcast(new JSONMessage(game.getId(), new GameStart_OUT()));
            // El mapa només es genera un cop. StateMapCreation s'encarrega de generar-lo,
            // desar-lo al GameInstance i fer la transició a StateMapVisualization.
            game.setCurrentState(new StateMapCreation(game, objectMapper, repoProvider));
        } else {
            System.out.println("[StateCharacterSelection] [" + game.getId() + "] Encara falten jugadors per triar personatge.");
        }
    }

    /**
     * Inicialitza el PlayerCharacterState de cada jugador a partir del YuCharacter
     * que ha triat (HP/MP/atk/def/level es llegeixen de la BD via Yumedant).
     * Es desa al GameInstance perquè qualsevol estat posterior hi pugui accedir.
     */
    private void initializePlayerStates() {
        for (PlayerInfo p : m.players) {
            // Trobem quin personatge ha triat aquest jugador
            CharacterInfo selected = m.characters.stream()
                    .filter(c -> c.isSelected && c.selectedPlayerId == p.id)
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("Jugador " + p.id + " no té personatge"));

            // Llegim les estadístiques base + rol del personatge (amb JOIN FETCH per evitar LazyInit)
            YuCharacter yu = repoProvider.RepoYuCharacter.findByIdWithRoleAbilities(selected.characterId)
                    .orElseThrow(() -> new IllegalStateException("YuCharacter " + selected.characterId + " no existeix a la BD"));

            // Extraiem els bonus de rol (0 si el personatge no té rol assignat)
            PlayerRole role = yu.getRole();
            int roleHp  = role != null ? role.getHp()  : 0;
            int roleMp  = role != null ? role.getMp()  : 0;
            int roleAtk = role != null ? role.getAtk() : 0;
            int roleDef = role != null ? role.getDef() : 0;

            PlayerCharacterState state = new PlayerCharacterState(
                    p.id,
                    yu.getId(),
                    yu.getHp(),
                    yu.getMp(),
                    yu.getLevel(),
                    yu.getAtk(),
                    yu.getDef(),
                    roleHp, roleMp, roleAtk, roleDef
            );
            game.putPlayerState(p.id, state);
            System.out.println("[StateCharacterSelection] [" + game.getId() + "] Inicialitzat PlayerCharacterState per jugador " + p.id
                    + " (HP " + yu.getHp() + ", MP " + yu.getMp()
                    + ", rol: " + (role != null ? role.getName() : "cap") + ").");
        }
    }

    /**
     * Processa la petició d'un jugador per seleccionar un personatge determinat.
     * Valida l'identificador del jugador i el personatge sol·licitat. Si la selecció és
     * vàlida i el personatge no està ocupat, se li assigna al jugador.
     * Si després de l'assignació tots els jugadors tenen personatge, comença la partida.
     *
     * @param p El jugador que ha enviat el missatge.
     * @param jsonMsg El missatge JSON rebut que conté la petició de selecció.
     */
    private void handlePickCharacter(Player p, JSONMessage jsonMsg) {

        PickCharacter_IN message = objectMapper.treeToValue(jsonMsg.data, PickCharacter_IN.class);
        System.out.println("[StateCharacterSelection] [" + game.getId() + "] Processant PickCharacterMessage_IN: Jugador ID " + message.playerId + " demana Personatge ID " + message.characterId);

        // Verifiquem l'id del jugador
        boolean idPlayerValid = p.getId() == message.playerId;

        if (!idPlayerValid) {
            System.out.println("[StateCharacterSelection] [" + game.getId() + "] FAIL - Missatge erroni, el jugador de la sessió " + p.getId() + " diu ser el jugador ID: " + message.playerId);

            // Missatge individual
            ActionResult_OUT result = new ActionResult_OUT(false, INVALID_PLAYER.value);
            game.send(p.getSession(), new JSONMessage(game.getId(), result));
            return;
        }

        //Si l'Id del jugador és correcte...
        // Busquem personatge amb l'ID que volem ocupar
        Optional<CharacterInfo> oc = m.characters.stream().filter(x -> x.characterId == message.characterId).findFirst();

        if (oc.isEmpty()) {
            System.out.println("[StateCharacterSelection] [" + game.getId() + "] FAIL - El personatge amb id: " + message.characterId + " no existeix a la llista.");

            // Missatge individual
            ActionResult_OUT result = new ActionResult_OUT(false, INVALID_CHARACTER.value);
            game.send(p.getSession(), new JSONMessage(game.getId(), result));

        } else {
            // Si el trobem, mirem si ja està seleccionat
            CharacterInfo ci = oc.get();

            if (!ci.isSelected) {
                System.out.println("[StateCharacterSelection] [" + game.getId() + "] Personatge lliure. Assignant personatge ID " + message.characterId + " al jugador " + message.playerId);
                // PRIMER des-seleccionem qualsevol altre personatge que aquest jugador tingués seleccionat previament
                for(CharacterInfo charInfo : m.characters) {
                    if(charInfo.isSelected && charInfo.selectedPlayerId == message.playerId) {
                        charInfo.isSelected = false;
                        charInfo.selectedPlayerId = -1; // -1 = cap jugador
                        System.out.println("[StateCharacterSelection] [" + game.getId() + "] Jugador " + message.playerId + " ha perdut la selecció prèvia del personatge ID " + charInfo.characterId);
                    }
                }

                // SEGON, assignem el nou personatge
                ci.selectedPlayerId = message.playerId;
                ci.isSelected = true;

                // TERCER, enviem les respostes
                // Missatge individual de confirmació al jugador que ha fet l'acció
                ActionResult_OUT result = new ActionResult_OUT(true, OK.value);
                game.send(p.getSession(), new JSONMessage(game.getId(), result));

                // QUART, missatge a tothom amb l'estat global actualitzat
                JSONMessage ogm = new JSONMessage(game.getId(), m);
                game.broadcast(ogm);
                System.out.println("[StateCharacterSelection] [" + game.getId() + "] SUCCESS - Nova assignació feta i comunicada per Broadcast.");

                // CINQUÈ, comprovem si tots els jugadors ja tenen personatge → comença la partida
                checkAllPickedAndStart();
            } else {
                // Si està seleccionat per un altre, li diem que error
                System.out.println("[StateCharacterSelection] [" + game.getId() + "] FAIL - El personatge ID " + message.characterId + " ja està agafat pel jugador " + ci.selectedPlayerId);
                ActionResult_OUT result = new ActionResult_OUT(false, CHARACTER_ALREADY_SELECTED.value);
                game.send(p.getSession(), new JSONMessage(game.getId(), result));
            }
        }
    }
}
