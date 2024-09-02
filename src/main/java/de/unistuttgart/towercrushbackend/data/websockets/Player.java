package de.unistuttgart.towercrushbackend.data.websockets;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.UUID;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * This class contains all the player specific infos
 */
@NoArgsConstructor
@AllArgsConstructor
@Data //NOSONAR
@Entity
public class Player {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @JsonIgnore
    private UUID id;

    private String playerName;
    private UUID key;

    public Player(final String playerName) {
        this.playerName = playerName;
        this.key = UUID.randomUUID();
    }

    public Player(final String playerName, final UUID key) {
        this.playerName = playerName;
        this.key = key;
    }

    public boolean equalsUUID(final Player player) {
        return this.key.equals(player.getKey());
    }
}
