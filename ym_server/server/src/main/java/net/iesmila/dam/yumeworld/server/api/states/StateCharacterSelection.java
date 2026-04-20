package net.iesmila.dam.yumeworld.server.api.states;

import net.iesmila.dam.yumeworld.server.api.messages.JSONMessage;
import net.iesmila.dam.yumeworld.server.api.messages.characterselection.in.HostStartGame_IN;
import net.iesmila.dam.yumeworld.server.api.messages.characterselection.in.PickCharacterMessage_IN;
import net.iesmila.dam.yumeworld.server.api.messages.characterselection.in.SetReady_IN;
import net.iesmila.dam.yumeworld.server.api.messages.characterselection.minimodels.CharacterInfo;
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
                    pickCharacter(message.user(), gm);

                } else if (SetReady_IN.TYPE.equals(gm.messageType)) {
                    setReady(message.user(), gm);

                } else if (HostStartGame_IN.TYPE.equals(gm.messageType)) {
                    // hostStartGame(message.user(), gm);
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
    private void setReady(Player player, JSONMessage gm) {
        SetReady_IN message = objectMapper.treeToValue(gm.data, SetReady_IN.class);
        System.out.println("MESSAGE: Player Ready: " + message.playerId);



    }

    /**
     * Processa la petició d'un jugador per seleccionar un personatge determinat.
     * Valida l'identificador del jugador i el personatge sol·licitat. Si la selecció és
     * vàlida i el personatge no està ocupat, se li assigna al jugador.
     *
     * @param p El jugador que ha enviat el missatge.
     * @param jsonMsg El missatge JSON rebut que conté la petició de selecció.
     */
    private void pickCharacter(Player p, JSONMessage jsonMsg) {

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

        // Busquem personatge amb l'ID que volem ocupar
        Optional<CharacterInfo> oc = m.characters.stream().filter(x -> x.characterId == message.characterId).findFirst();

        if (oc.isEmpty()) {
            System.out.println("Missatge erroni, el personatge amb id: " + message.characterId + " no existeix.");

            // Missatge individual
            ActionResult_OUT result = new ActionResult_OUT(false, UNVALID_CHARACTER.value);
            game.send(p.getSession(), new JSONMessage(game.getId(), result));
            return;

        } else {

            // Si el trobem, mirem si ja està seleccionat
            CharacterInfo ci = oc.get();

            if (!ci.isSelected) {

                // Si no està seleccionat, assignem l'id del jugador al personatge
                ci.selectedPlayerId = message.playerId;
                ci.isSelected = true;

                // Missatge individual
                ActionResult_OUT result = new ActionResult_OUT(true, OK.value);
                game.send(p.getSession(), new JSONMessage(game.getId(), result));

                // Missatge a tothom amb l'actualització de les assignacions.
                JSONMessage ogm = new JSONMessage(game.getId(), m);
                game.broadcast(ogm);
            }
        }
    }
}
