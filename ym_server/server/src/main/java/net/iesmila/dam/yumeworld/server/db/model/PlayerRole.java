package net.iesmila.dam.yumeworld.server.db.model;

import jakarta.persistence.*;

import java.io.Serializable;
import java.util.List;

@Entity
@Table(name = "PLAYERROLE")
public class PlayerRole implements Serializable {
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

    // Getters and setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Integer getHp() { return hp; }
    public void setHp(Integer hp) { this.hp = hp; }
    public Integer getMp() { return mp; }
    public void setMp(Integer mp) { this.mp = mp; }
    public Integer getAtk() { return atk; }
    public void setAtk(Integer atk) { this.atk = atk; }
    public Integer getDef() { return def; }
    public void setDef(Integer def) { this.def = def; }
    public List<Ability> getAbilities() { return abilities; }
    public void setAbilities(List<Ability> abilities) { this.abilities = abilities; }
}
