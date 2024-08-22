package de.unistuttgart.towercrushbackend.data.websockets;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.UUID;
import javax.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * This class contains all the vote specific info
 */
@NoArgsConstructor
@AllArgsConstructor
@Data //NOSONAR
@Entity
public class Vote {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @JsonIgnore
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "player_id")
    private Player player;

    private String answer;

    public Vote(final Player player, final String answer) {
        this.player = player;
        this.answer = answer;
    }
}
