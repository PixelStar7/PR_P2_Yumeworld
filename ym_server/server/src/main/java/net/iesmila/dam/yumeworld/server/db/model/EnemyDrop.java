package net.iesmila.dam.yumeworld.server.db.model;

import jakarta.persistence.*;

import java.io.Serializable;

@Entity
@Table(name = "ENEMY_DROPS")
public class EnemyDrop implements Serializable {
    @EmbeddedId
    private EnemyDropId id;

    @ManyToOne(fetch = FetchType.LAZY) // Millora rendiment: no carregues l'ítem si no el demanes
    @MapsId("itemId")
    @JoinColumn(name = "ed_item_id")
    private Item item;

    @ManyToOne(fetch = FetchType.LAZY)
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

    // Mètode d'ajuda per sincronitzar id i entitats
    public void setEntities(Enemy enemy, Item item) {
        this.enemy = enemy;
        this.item = item;
        this.id = new EnemyDropId(item.getId(), enemy.getId());
    }
}
