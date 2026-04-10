package net.iesmila.dam.yumeworld.server.db.model;

import jakarta.persistence.*;

@Entity
@Table(name = "PARAMETER_TYPES")
public class ParameterType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pt_id")
    private Integer id;

    @Column(name = "pt_name", nullable = false, length = 20)
    private String name;

    @Column(name = "pt_type", nullable = false, length = 1)
    private String type;

    @Column(name = "pt_num_min")
    private Integer numMin;

    @Column(name = "pt_num_max")
    private Integer numMax;

    @Column(name = "pt_value_list")
    private String valueList;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
