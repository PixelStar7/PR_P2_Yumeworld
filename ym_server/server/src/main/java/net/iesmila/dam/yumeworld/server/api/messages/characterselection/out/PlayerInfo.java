package net.iesmila.dam.yumeworld.server.api.messages.characterselection.out;

public class PlayerInfo {

    public long id;
    public String name;

    public PlayerInfo(){}

    public PlayerInfo(long id, String name) {
        this.id = id;
        this.name = name;
    }
}
