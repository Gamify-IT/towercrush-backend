package de.unistuttgart.towercrushbackend.data.websockets;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.unistuttgart.towercrushbackend.data.Question;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * This class contains all the infos that need to be saved for one round of tower crush
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
public class Round {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @JsonIgnore
    private UUID id;
    @ManyToOne
    @JoinColumn(name = "question_id")
    private Question question;

    private static String TEAM_A_NAME = "teamA";

    private static String TEAM_B_NAME = "teamB";

    @ElementCollection
    private Map<String, TeamVotes> teamVotes;
    @ElementCollection
    private Map<String, Boolean> teamReadyForNextQuestion;

    public Round(final Question questionParam) {
        this.question = questionParam;
        this.teamVotes = new HashMap<>();
        this.teamVotes.put(TEAM_A_NAME, new TeamVotes());
        this.teamVotes.put(TEAM_B_NAME, new TeamVotes());
        this.teamReadyForNextQuestion = new HashMap<>();
        this.teamReadyForNextQuestion.put(TEAM_A_NAME, false);
        this.teamReadyForNextQuestion.put(TEAM_B_NAME, false);
    }
}
