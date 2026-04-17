package net.iesmila.dam.yumeworld.server.api.states;

import net.iesmila.dam.yumeworld.server.components.GameInstance;

public abstract class State {

    protected GameInstance game;

    public State(GameInstance game) {
        this.game = game;
    }

    public abstract void tick();
}
