package net.iesmila.dam.yumeworld.server.db.model;

import jakarta.persistence.*;

import java.io.Serializable;

@Entity
@Table(name = "ABILITY")
@PrimaryKeyJoinColumn(name = "ab_action_id")
@DiscriminatorValue("A")
public class Ability extends Act implements Serializable {
    @Column(name = "ab_cost", nullable = false)
    private Integer cost = 10;

    // Getters and setters
    public Integer getCost() { return cost; }
    public void setCost(Integer cost) { this.cost = cost; }
}
