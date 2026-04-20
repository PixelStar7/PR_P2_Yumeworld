package net.iesmila.dam.yumeworld.server.db.repository;

import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import net.iesmila.dam.yumeworld.server.db.repository.*;

@Component
public class RepositoryProvider {
    // Aqui anem posant tots els repositoris que anem creant

    @Autowired
    public YuCharacterRepository RepoYuCharacter;
}
