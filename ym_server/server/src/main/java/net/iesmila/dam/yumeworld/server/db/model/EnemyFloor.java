package net.iesmila.dam.yumeworld.server.db.model;

import jakarta.persistence.*;

@Entity
@Table(name = "ENEMY_FLOORS")
public class EnemyFloor {
    @EmbeddedId
    private EnemyFloorId id;

    @ManyToOne
    @MapsId("enemyId")
    @JoinColumn(name = "enfl_enemy_id")
    private Enemy enemy;

    @ManyToOne
    @MapsId("floorId")
    @JoinColumn(name = "enfl_floor_id")
    private Floor floor;

    @Column(name = "enfl_quantity", nullable = false)
    private Integer quantity;

    public EnemyFloorId getId() { return id; }
    public void setId(EnemyFloorId id) { this.id = id; }
    public Enemy getEnemy() { return enemy; }
    public void setEnemy(Enemy enemy) { this.enemy = enemy; }
    public Floor getFloor() { return floor; }
    public void setFloor(Floor floor) { this.floor = floor; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
}
