package net.iesmila.dam.yumeworld.server.api.messages.characterselection.minimodels;

public class PlayerInfo {

    public long id;
    public String name;
    public boolean isReady;


    public PlayerInfo(){}

    public PlayerInfo(long id, String name) {
        this.id = id;
        this.name = name;
        this.isReady = false;

    }
}
