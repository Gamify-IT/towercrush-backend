package de.unistuttgart.towercrushbackend.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

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
