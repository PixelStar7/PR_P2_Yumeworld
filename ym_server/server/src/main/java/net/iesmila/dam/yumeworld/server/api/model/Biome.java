package net.iesmila.dam.yumeworld.server.api.model;

import jakarta.persistence.*;

@Entity
@Table(name = "BIOME")
public class Biome {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "bi_id")
    private Integer id;

    @Column(name = "bi_background", length = 500)
    private String background;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getBackground() { return background; }
    public void setBackground(String background) { this.background = background; }
}
