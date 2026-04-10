package net.iesmila.dam.yumeworld.server.api.model;

import jakarta.persistence.*;

@Entity
@Table(name = "ITEM")
@PrimaryKeyJoinColumn(name = "it_action_id")
@DiscriminatorValue("I")
public class Item extends Act {
    // Specific item fields or methods if necessary
}
