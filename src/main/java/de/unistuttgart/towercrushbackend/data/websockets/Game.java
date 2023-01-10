package de.unistuttgart.towercrushbackend.data.websockets;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class Game {
    private List<Round> rounds;
    private UUID configurationId;
    private int currentQuestionTeamA;
    private int currentQuestionTeamB;
    private int teamATowerSize;
    private int teamBTowerSize;
}
