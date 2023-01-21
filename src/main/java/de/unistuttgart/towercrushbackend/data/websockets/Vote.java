package de.unistuttgart.towercrushbackend.data.websockets;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Data
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
