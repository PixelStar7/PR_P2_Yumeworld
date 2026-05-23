package net.iesmila.dam.yumeworld.server.api.messages.chest.out;

import net.iesmila.dam.yumeworld.server.api.messages.MessageBody;

/**
 * Segon missatge enviat en entrar a l'estat de cofre, immediatament
 * després de ChestStart_OUT.
 *
 * Segueix el mateix patró estàndard que la resta d'estats:
 *   CHEST_START (transició d'escena, sense dades)
 *   CHEST_INFO  (dades inicials de l'estat)
 *
 * Camp:
 *   chestOpened — false en entrar; passa a true quan algú obre el cofre
 *                 (però llavors el client ja rep ChestOpened_OUT, de manera
 *                 que aquest camp és sempre false en el missatge inicial).
 */
public class ChestInfo_OUT extends MessageBody {
    public final static String TYPE = "CHEST_INFO";

    public boolean chestOpened;

    public ChestInfo_OUT() {
        this.chestOpened = false;
    }

    public ChestInfo_OUT(boolean chestOpened) {
        this.chestOpened = chestOpened;
    }

    @Override
    public String getMessageType() {
        return TYPE;
    }
}
