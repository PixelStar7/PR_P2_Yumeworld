package net.iesmila.dam.yumeworld.server.api.messages.generic.out;

import net.iesmila.dam.yumeworld.server.api.messages.MessageBody;
import net.iesmila.dam.yumeworld.server.api.dto.CharacterModification;

/**
 * Broadcast genèric que envia el servidor quan els stats d'un personatge canvien
 * (curació al descans, dany rebut en combat, level up, etc.). Embolica un
 * CharacterModification amb la informació antiga i nova.
 */
public class CharacterUpdate_OUT extends MessageBody {
    public final static String TYPE = "CHARACTER_UPDATE";

    public CharacterModification modification;

    public CharacterUpdate_OUT() {}

    public CharacterUpdate_OUT(CharacterModification modification) {
        this.modification = modification;
    }

    @Override
    public String getMessageType() {
        return TYPE;
    }
}
