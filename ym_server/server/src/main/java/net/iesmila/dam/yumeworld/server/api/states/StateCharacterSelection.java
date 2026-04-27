package net.iesmila.dam.yumeworld.server.api.states;

import net.iesmila.dam.yumeworld.server.api.messages.JSONMessage;
import net.iesmila.dam.yumeworld.server.api.messages.characterselection.in.HostStartGame_IN;
import net.iesmila.dam.yumeworld.server.api.messages.characterselection.in.PickCharacterMessage_IN;
import net.iesmila.dam.yumeworld.server.api.messages.characterselection.in.SetReady_IN;
import net.iesmila.dam.yumeworld.server.api.messages.characterselection.minimodels.CharacterInfo;
import net.iesmila.dam.yumeworld.server.api.messages.characterselection.out.GameStart_OUT;
import net.iesmila.dam.yumeworld.server.api.messages.characterselection.out.LobbyUpdate_OUT;
import net.iesmila.dam.yumeworld.server.api.messages.characterselection.minimodels.PlayerInfo;
import net.iesmila.dam.yumeworld.server.api.messages.generic.ActionResult_OUT;
import net.iesmila.dam.yumeworld.server.api.models.Player;
import net.iesmila.dam.yumeworld.server.components.GameInstance;
import net.iesmila.dam.yumeworld.server.components.GameMessage;
import net.iesmila.dam.yumeworld.server.db.model.YuCharacter;
import net.iesmila.dam.yumeworld.server.db.repository.RepositoryProvider;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static net.iesmila.dam.yumeworld.server.components.enums.Codes.*;

/**
 * Estat on els jugadors han de seleccionar el seu personatge abans de començar la partida.
 * Aquest estat gestiona la llista de personatges disponibles i l'assignació de cada personatge
 * als jugadors que el sol·licitin. Un cop tots els personatges han estat seleccionats,
 * es passa al següent estat de la partida.
 */
public class StateCharacterSelection extends State {

    LobbyUpdate_OUT m;

    public StateCharacterSelection(GameInstance game, ObjectMapper objectMapper, RepositoryProvider repoProvider) {
        super(game, objectMapper, repoProvider); // Passem la instància única cap amunt

        // GameInstance és l'encarregat de crear l'objectMapper.

        // Omplim el Missatge de sortida LobbyUpdate_OUT
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

        List<YuCharacter> llistaBd = repoProvider.RepoYuCharacter.findAll();

        m.characters = new ArrayList<>();
        for (YuCharacter yu : llistaBd) {
            CharacterInfo infoc = new CharacterInfo();
            infoc.characterId = yu.getId();
            infoc.name = yu.getName();
            infoc.isSelected = false;
            m.characters.add(infoc);
        }
        game.broadcast(new JSONMessage(game.getId(), m));
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
                System.out.println("TICK: " + gm.messageType);

                if (PickCharacterMessage_IN.TYPE.equals(gm.messageType)) {
                    handlePickCharacter(message.user(), gm);

                } else if (SetReady_IN.TYPE.equals(gm.messageType)) {
                    handleSetReady(message.user(), gm);

                } else if (HostStartGame_IN.TYPE.equals(gm.messageType)) {
//                    handleHostStartGame(message.user(), gm);
                }

            } catch (Exception e) {
                System.out.println("Error parsejant el missatge: " + e.getMessage());
            }
        }
    }

    /**
     * Ha de llegir si el jugador està llest, actualitza la llista
     * i fa un game.broadcast(m) perquè tothom vegi l'actualització
     *  del lobby.
     * @param player jugador que envia el missatge.
     * @param gm missatge rebut.
     */
    private void handleSetReady(Player player, JSONMessage gm) {
        try{
            SetReady_IN message = objectMapper.treeToValue(gm.data, SetReady_IN.class);
            System.out.println("MESSAGE: Player Ready: " + message.playerId);

            // 1. Validació de seguretat: El jugador que envia és qui diu ser?
            if (player.getId() != message.playerId) {
                game.send(player.getSession(), new JSONMessage(game.getId(),
                        new ActionResult_OUT(false, UNVALID_PLAYER.value)));
                return;
            }

            // 2. Validació de lògica: El jugador ha seleccionat algun personatge abans d'estar llest?
            boolean hasCharacterSelected = m.characters.stream()
                .anyMatch(c -> c.isSelected && c.selectedPlayerId == message.playerId);

            if (!hasCharacterSelected) {
                // Li podem enviar el mateix error o un de nou demanant que triï personatge
                game.send(player.getSession(), new JSONMessage(game.getId(),
                        new ActionResult_OUT(false, UNVALID_CHARACTER.value))); // Faltaria un codi d'error específic, però aprofitem aquest
                return;
            }

            // 3. Canviem l'estat del jugador a ready
            for (PlayerInfo p : m.players){
                if(p.id == message.playerId){
                    p.isReady = true;
                    break; // Optimització: un cop el trobem, sortim del bucle
                }
            }

            // 4. Resposta de confirmació individual i Broadcast de l'estat actualitzat
            game.send(player.getSession(), new JSONMessage(game.getId(), new ActionResult_OUT(true, OK.value)));
            game.broadcast(new JSONMessage(game.getId(), m));

            // 5. Comprovem si tots els jugadors estan ready
            checkAllReadyAndStart();
        } catch (Exception e) {
            System.err.println("Error a handleSetReady: " + e.getMessage());
        }
    }

    private void checkAllReadyAndStart() {
        boolean allReady = true;
        for (PlayerInfo p : m.players){
            if(!p.isReady){
                allReady = false;
                break; // Optimització: si trobem un que no està ready, ja podem aturar el bucle
            }
        }

        //Si tothom està ready, ha de començar el joc.
        if(allReady){
            System.out.println("Tots els jugadors estan llestos. Iniciant transició...");
            game.broadcast(new JSONMessage(game.getId(), new GameStart_OUT()));
            game.setCurrentState(new StateMapVisualization(game, objectMapper, repoProvider));
        }
    }

    /**
     * Processa la petició d'un jugador per seleccionar un personatge determinat.
     * Valida l'identificador del jugador i el personatge sol·licitat. Si la selecció és
     * vàlida i el personatge no està ocupat, se li assigna al jugador.
     *
     * @param p El jugador que ha enviat el missatge.
     * @param jsonMsg El missatge JSON rebut que conté la petició de selecció.
     */
    private void handlePickCharacter(Player p, JSONMessage jsonMsg) {

        PickCharacterMessage_IN message = objectMapper.treeToValue(jsonMsg.data, PickCharacterMessage_IN.class);
        System.out.println("MESSAGE: Picked character: " + message.characterId);

        // Verifiquem l'id del jugador
        boolean idPlayerValid = p.getId() == message.playerId;

        if (!idPlayerValid) {
            System.out.println("Missatge erroni, el jugador amb id: " + message.playerId + " no existeix");

            // Missatge individual
            ActionResult_OUT result = new ActionResult_OUT(false, UNVALID_PLAYER.value);
            game.send(p.getSession(), new JSONMessage(game.getId(), result));
            return;
        }

        // Mirem si el jugador ja està marcat com a "Ready"
        // No hauria de poder canviar de personatge un cop ha confirmat
        boolean isPlayerReady = m.players.stream().anyMatch(playerInfo -> playerInfo.id == p.getId() && playerInfo.isReady);
        if (isPlayerReady) {
             System.out.println("El jugador " + p.getId() + " ja està ready, no pot canviar de personatge.");
             // Retornem algun error personalitzat, pots afegir PLAYER_ALREADY_READY al fitxer Codes
             ActionResult_OUT result = new ActionResult_OUT(false, 4); // Fent veure que 4 és el codi d'error
             game.send(p.getSession(), new JSONMessage(game.getId(), result));
             return;
        }


        //Si l'Id del jugador és correcte...
        // Busquem personatge amb l'ID que volem ocupar
        Optional<CharacterInfo> oc = m.characters.stream().filter(x -> x.characterId == message.characterId).findFirst();

        if (oc.isEmpty()) {
            System.out.println("Missatge erroni, el personatge amb id: " + message.characterId + " no existeix.");

            // Missatge individual
            ActionResult_OUT result = new ActionResult_OUT(false, UNVALID_CHARACTER.value);
            game.send(p.getSession(), new JSONMessage(game.getId(), result));

        } else {
            // Si el trobem, mirem si ja està seleccionat
            CharacterInfo ci = oc.get();

            if (!ci.isSelected) {
                // PRIMER des-seleccionem qualsevol altre personatge que aquest jugador tingués seleccionat previament
                for(CharacterInfo charInfo : m.characters) {
                    if(charInfo.isSelected && charInfo.selectedPlayerId == message.playerId) {
                        charInfo.isSelected = false;
                        charInfo.selectedPlayerId = 0; // O posar a null si el tipus de dades ho permet
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
            } else {
                // Si està seleccionat per un altre, li diem que error
                ActionResult_OUT result = new ActionResult_OUT(false, CHARACTER_ALREADY_SELECTED.value); // Asumint que existeix algun codi així, si no, substitueix pel codi d'error adient
                game.send(p.getSession(), new JSONMessage(game.getId(), result));
            }
        }
    }
}