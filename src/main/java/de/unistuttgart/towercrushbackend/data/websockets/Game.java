package de.unistuttgart.towercrushbackend.data.websockets;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.*;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
public class Game {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @JsonIgnore
    private UUID id;
    private String lobbyName;
    @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Player> teamA;
    @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Player> teamB;
    @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
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

    @ElementCollection
    private Map<String, Integer> correctAnswerCount;

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
