package net.iesmila.dam.yumeworld.server.api.model;

import jakarta.persistence.*;

@Entity
@Table(name = "ENEMY_DROPS")
public class EnemyDrop {
    @EmbeddedId
    private EnemyDropId id;

    @ManyToOne
    @MapsId("itemId")
    @JoinColumn(name = "ed_item_id")
    private Item item;

    @ManyToOne
    @MapsId("enemyId")
    @JoinColumn(name = "ed_enemy_id")
    private Enemy enemy;

    @Column(name = "ed_chance", nullable = false)
    private Float chance = 0.05f;

    public EnemyDropId getId() { return id; }
    public void setId(EnemyDropId id) { this.id = id; }
    public Item getItem() { return item; }
    public void setItem(Item item) { this.item = item; }
    public Enemy getEnemy() { return enemy; }
    public void setEnemy(Enemy enemy) { this.enemy = enemy; }
    public Float getChance() { return chance; }
    public void setChance(Float chance) { this.chance = chance; }
}
