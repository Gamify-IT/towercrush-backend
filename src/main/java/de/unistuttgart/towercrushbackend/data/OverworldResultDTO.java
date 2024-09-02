package de.unistuttgart.towercrushbackend.data;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * The OverworldResultDTO.class contains all the info that is sent to the Overworld-backend
 */
@Data //NOSONAR
@AllArgsConstructor
@NoArgsConstructor
public class OverworldResultDTO {

    String game;
    UUID configurationId;
    long score;
    String userId;
}
