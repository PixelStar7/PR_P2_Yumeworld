package net.iesmila.dam.yumeworld.server.db.repository;

import net.iesmila.dam.yumeworld.server.db.model.Enemy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EnemyRepository extends JpaRepository<Enemy, Integer> {

}
