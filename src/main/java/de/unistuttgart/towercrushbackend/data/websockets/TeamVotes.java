package de.unistuttgart.towercrushbackend.data.websockets;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * This class contains the votes for a team
 */
@AllArgsConstructor
@Data
@Embeddable
@Entity
public class TeamVotes {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @JsonIgnore
    private UUID id;

    @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Vote> votes;

    public TeamVotes() {
        this.votes = new ArrayList<>();
    }
}
