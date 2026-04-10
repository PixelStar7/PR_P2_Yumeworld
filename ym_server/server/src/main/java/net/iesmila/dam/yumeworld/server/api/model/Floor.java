package net.iesmila.dam.yumeworld.server.api.model;

import jakarta.persistence.*;

@Entity
@Table(name = "FLOOR")
public class Floor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "fl_id")
    private Integer id;

    @Column(name = "fl_num", nullable = false)
    private Integer num;

    @Column(name = "fl_name", nullable = false, length = 20)
    private String name;

    @ManyToOne
    @JoinColumn(name = "fl_biome_id", nullable = false)
    private Biome biome;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getNum() { return num; }
    public void setNum(Integer num) { this.num = num; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Biome getBiome() { return biome; }
    public void setBiome(Biome biome) { this.biome = biome; }
}
