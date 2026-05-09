package net.iesmila.dam.yumeworld.server.api.messages.mapexploration.out;

import net.iesmila.dam.yumeworld.server.api.messages.MessageBody;

public abstract class NodeInfo_OUT extends MessageBody {
    // El TYPE aquí pot ser genèric o es pot deixar que cada subclasse el defineixi
    // Per ara, el mantindrem aquí com a genèric, però les subclasses tindran el seu propi TYPE específic.
    public final static String TYPE = "NODE_INFO";

    @Override
    public String getMessageType() {
        return TYPE;
    }
}
