package net.iesmila.dam.yumeworld.server.api.model;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "YUMEDANT")
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class Yumedant {
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
    public List<Act> getActs() { return acts; }
    public void setActs(List<Act> acts) { this.acts = acts; }
}
