package net.iesmila.dam.yumeworld.server.api.messages.generic.out;

import net.iesmila.dam.yumeworld.server.api.messages.MessageBody;
import net.iesmila.dam.yumeworld.server.core.enums.Codes; // Importar Codes

import static net.iesmila.dam.yumeworld.server.core.enums.Codes.OK;

public class ActionResult_OUT extends MessageBody {

    public final static String TYPE = "ACTION_RESULT";

    public boolean success;
    public int errorCode;
    public String message;

    // Constructor principal que requereix tots els camps
    public ActionResult_OUT(boolean success, int errorCode, String message) {
        this.success = success;
        this.errorCode = errorCode;
        this.message = message;
    }

    // Constructor de conveniència que deriva el missatge del Codes enum
    public ActionResult_OUT(boolean success, int errorCode) {
        this(success, errorCode, Codes.fromValue(errorCode).desc); // Utilitzem el desc del Code
    }

    // Constructor de conveniència per a èxit, utilitza el missatge per defecte de OK
    public ActionResult_OUT(boolean success) {
        this(success, OK.value, OK.desc);
    }

    @Override
    public String getMessageType() {
        return TYPE;
    }
}
