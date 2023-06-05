package de.unistuttgart.towercrushbackend.data.websockets;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * This class contains all infos that need to be saved for one played game of tower crush
 */
@NoArgsConstructor
@AllArgsConstructor
@SuppressWarnings("squid:S1234")
@Data
@Entity
public class Game {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @JsonIgnore
    private UUID id;

    private String lobbyName;

    @ElementCollection
    private Map<String, Team> teams;

    @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Round> rounds;

    private UUID configurationId;

    @ElementCollection
    private Map<String, Integer> currentQuestion;

    private long initialTowerSize;

    @ElementCollection
    private Map<String, Integer> towerSize;

    @ElementCollection
    private Map<String, Integer> answerPoints;

    private String winnerTeam;

    @JsonIgnore
    private LocalDateTime startedGame;

    @ElementCollection
    private Map<String, Integer> correctAnswerCount;

    private static final String TEAM_A_NAME = "teamA";
    private static final String TEAM_B_NAME = "teamB";

    public Game(
        final String lobbyName,
        final Team teamA,
        final Team teamB,
        final List<Round> rounds,
        final UUID configurationId,
        final long initialTowerSize
    ) {
        this.lobbyName = lobbyName;
        this.teams = new HashMap<>();
        this.teams.put(TEAM_A_NAME, teamA);
        this.teams.put(TEAM_B_NAME, teamB);
        this.rounds = rounds;
        this.configurationId = configurationId;
        this.currentQuestion = new HashMap<>();
        this.currentQuestion.put(TEAM_A_NAME, 0);
        this.currentQuestion.put(TEAM_B_NAME, 0);
        this.initialTowerSize = initialTowerSize;
        this.towerSize = new HashMap<>();
        this.towerSize.put(TEAM_A_NAME, (int) initialTowerSize);
        this.towerSize.put(TEAM_B_NAME, (int) initialTowerSize);
        this.answerPoints = new HashMap<>();
        this.answerPoints.put(TEAM_A_NAME, 0);
        this.answerPoints.put(TEAM_B_NAME, 0);
        this.winnerTeam = "";
        this.startedGame = LocalDateTime.now();
        this.correctAnswerCount = new HashMap<>();
        this.correctAnswerCount.put(TEAM_A_NAME, 0);
        this.correctAnswerCount.put(TEAM_B_NAME, 0);
    }
}
