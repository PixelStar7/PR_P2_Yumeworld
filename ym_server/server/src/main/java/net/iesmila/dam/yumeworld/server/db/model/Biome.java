package net.iesmila.dam.yumeworld.server.db.model;

import jakarta.persistence.*;

import java.io.Serializable;

@Entity
@Table(name = "BIOME")
public class Biome implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "bi_id")
    private Integer id;

    @Column(name = "bi_name", nullable = false, length = 50, unique = true)
    private String name;

    @Column(name = "bi_order", nullable = false)
    private Integer order;

    @Column(name = "bi_background", length = 500)
    private String background;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Integer getOrder() { return order; }
    public void setOrder(Integer order) { this.order = order; }
    public String getBackground() { return background; }
    public void setBackground(String background) { this.background = background; }
}
