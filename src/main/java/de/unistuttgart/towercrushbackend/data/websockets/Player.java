package de.unistuttgart.towercrushbackend.data.websockets;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class Player {
    private String playerName;
    private UUID key;

    public boolean equalsUUID(final Player player) {
        return this.key.equals(player.getKey());
    }
}
