package net.iesmila.dam.yumeworld.server.api.messages.characterselection.out;

public class CharacterInfo {

    public int characterId;
    public String name;
    public String imageUrl;
    public int selectedPlayerId;
    public boolean isSelected;

    public CharacterInfo(){}

    public CharacterInfo(int characterId, String name, String imageUrl, int selectedPlayerId, boolean isSelected) {
        this.characterId = characterId;
        this.name = name;
        this.imageUrl = imageUrl;
        this.selectedPlayerId = selectedPlayerId;
        this.isSelected = isSelected;
    }
}
