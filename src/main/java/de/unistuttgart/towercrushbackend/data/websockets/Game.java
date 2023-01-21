package de.unistuttgart.towercrushbackend.data.websockets;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class Game {
    private String lobbyName;
    private Set<Player> teamA;
    private Set<Player> teamB;
    private List<Round> rounds;
    private UUID configurationId;
    private int currentQuestionTeamA;
    private int currentQuestionTeamB;
    private long initialTowerSize;
    private int teamATowerSize;
    private int teamBTowerSize;
    private int teamAAnswerPoints;
    private int teamBAnswerPoints;
    private String winnerTeam;
    @JsonIgnore
    private LocalDateTime startedGame;

    private HashMap<String, Integer> correctAnswerCount;

    public Game(final String lobbyName, final Set<Player> teamA, final Set<Player> teamB, final List<Round> rounds, final UUID configurationId, final long initialTowerSize) {
        this.lobbyName = lobbyName;
        this.teamA = teamA;
        this.teamB = teamB;
        this.rounds = rounds;
        this.configurationId = configurationId;
        this.currentQuestionTeamA = 0;
        this.currentQuestionTeamB = 0;
        this.initialTowerSize = initialTowerSize;
        this.teamATowerSize = (int) initialTowerSize;
        this.teamBTowerSize = (int) initialTowerSize;
        this.teamAAnswerPoints = 0;
        this.teamBAnswerPoints = 0;
        this.winnerTeam = "";
        this.startedGame = LocalDateTime.now();
        this.correctAnswerCount = new HashMap<>();
        this.correctAnswerCount.put("teamA", 0);
        this.correctAnswerCount.put("teamB", 0);
    }
}
