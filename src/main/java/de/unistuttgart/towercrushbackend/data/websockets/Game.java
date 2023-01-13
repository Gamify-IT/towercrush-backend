package de.unistuttgart.towercrushbackend.data.websockets;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class Game {
    private Set<Player> teamA;
    private Set<Player> teamB;
    private List<Round> rounds;
    private UUID configurationId;
    private int currentQuestionTeamA;
    private int currentQuestionTeamB;
    private int teamATowerSize;
    private int teamBTowerSize;
}
