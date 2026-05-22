package net.iesmila.dam.yumeworld.server.db.model;

import jakarta.persistence.*;

import java.io.Serializable;
import java.util.List;

@Entity
@Table(name = "YUMEDANT")
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class Yumedant implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "yu_id")
    private Integer id;

    @Column(name = "yu_name", nullable = false)
    private String name;

    @Column(name = "yu_desc")
    private String description;

    @Column(name = "yu_level", nullable = false)
    private Integer level = 1;

    @Column(name = "yu_hp", nullable = false)
    private Integer hp = 1;

    @Column(name = "yu_mp", nullable = false)
    private Integer mp = 1;

    @Column(name = "yu_atk", nullable = false)
    private Integer atk = 1;

    @Column(name = "yu_def", nullable = false)
    private Integer def = 1;

    @Column(name = "yu_image", length = 500)
    private String image;

    @ManyToMany
    @JoinTable(
        name = "YUMEDANT_ACT",
        joinColumns = @JoinColumn(name = "ya_yumedant_id"),
        inverseJoinColumns = @JoinColumn(name = "ya_action_id")
    )
    private List<Act> acts;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Integer getLevel() { return level; }
    public void setLevel(Integer level) { this.level = level; }
    public Integer getHp() { return hp; }
    public void setHp(Integer hp) { this.hp = hp; }
    public Integer getMp() { return mp; }
    public void setMp(Integer mp) { this.mp = mp; }
    public Integer getAtk() { return atk; }
    public void setAtk(Integer atk) { this.atk = atk; }
    public Integer getDef() { return def; }
    public void setDef(Integer def) { this.def = def; }
    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }
    public List<Act> getActs() { return acts; }
    public void setActs(List<Act> acts) { this.acts = acts; }
}
