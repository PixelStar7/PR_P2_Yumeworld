package net.iesmila.dam.yumeworld.server.api.model;

import jakarta.persistence.*;

@Entity
@Table(name = "ABILITY")
@PrimaryKeyJoinColumn(name = "ab_action_id")
@DiscriminatorValue("A")
public class Ability extends Act {
    @Column(name = "ab_cost", nullable = false)
    private Integer cost = 10;

    // Getters and setters
    public Integer getCost() { return cost; }
    public void setCost(Integer cost) { this.cost = cost; }
}
