package net.iesmila.dam.yumeworld.server.db.model;

import jakarta.persistence.*;

@Entity
@Table(name = "ALTERED_STATE")
public class AlteredState {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "as_id")
    private Integer id;

    @Column(name = "as_name", length = 50)
    private String name;

    @Column(name = "as_icon", length = 500)
    private String icon;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
