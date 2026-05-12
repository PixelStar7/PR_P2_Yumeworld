package net.iesmila.dam.yumeworld.server.core.states;

import net.iesmila.dam.yumeworld.server.core.GameInstance;
import net.iesmila.dam.yumeworld.server.db.repository.RepositoryProvider;
import tools.jackson.databind.ObjectMapper;

public abstract class State {

    protected GameInstance game;
    protected ObjectMapper objectMapper;
    protected RepositoryProvider repoProvider;


    public State(GameInstance game, ObjectMapper objectMapper, RepositoryProvider repoProvider) {
        this.game = game;
        this.objectMapper = objectMapper;
        this.repoProvider = repoProvider;
    }

    public abstract void tick();
}
