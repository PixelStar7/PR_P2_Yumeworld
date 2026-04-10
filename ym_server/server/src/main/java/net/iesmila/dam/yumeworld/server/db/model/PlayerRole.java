package net.iesmila.dam.yumeworld.server.db.model;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "PLAYERROLE")
public class PlayerRole {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pr_id")
    private Integer id;

    @Column(name = "pr_name", length = 50)
    private String name;

    @Column(name = "pr_desc")
    private String description;

    @Column(name = "pr_hp", nullable = false)
    private Integer hp = 0;

    @Column(name = "pr_mp", nullable = false)
    private Integer mp = 0;

    @Column(name = "pr_atk", nullable = false)
    private Integer atk = 0;

    @Column(name = "pr_def", nullable = false)
    private Integer def = 0;

    @ManyToMany
    @JoinTable(
        name = "PLAYERROLE_ABILITY",
        joinColumns = @JoinColumn(name = "ra_playerrole_id"),
        inverseJoinColumns = @JoinColumn(name = "ra_ability_id")
    )
    private List<Ability> abilities;

    // Getters and setters (omitted for brevity)
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public List<Ability> getAbilities() { return abilities; }
    public void setAbilities(List<Ability> abilities) { this.abilities = abilities; }
}
