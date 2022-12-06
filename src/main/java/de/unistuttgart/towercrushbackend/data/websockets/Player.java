package de.unistuttgart.towercrushbackend.data.websockets;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class Player {
    private String player;
    private UUID key;
}
