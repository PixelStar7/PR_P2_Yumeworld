package net.iesmila.dam.yumeworld.server.api.dto;

public class CharacterInfo {

    public int characterId;
    public String name;
    public String imageUrl;
    public long selectedPlayerId = -1;   // -1 vol dir "encara no l'ha agafat ningú"
    public boolean isSelected;

    public CharacterInfo() {}

    public CharacterInfo(int characterId, String name, String imageUrl, long selectedPlayerId, boolean isSelected) {
        this.characterId = characterId;
        this.name = name;
        this.imageUrl = imageUrl;
        this.selectedPlayerId = selectedPlayerId;
        this.isSelected = isSelected;
    }
}
