package de.unistuttgart.towercrushbackend.data.websockets;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import javax.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * This class contains all the team specific info
 */
@AllArgsConstructor
@Data //NOSONAR
@Embeddable
@Entity
public class Team {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @JsonIgnore
    private UUID id;

    @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Player> players;

    private Integer lifes = 4;

    public Team() {
        this.players = new HashSet<>();
    }
}
