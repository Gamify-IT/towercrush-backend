package de.unistuttgart.towercrushbackend.data.websockets;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.unistuttgart.towercrushbackend.data.Question;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.*;

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

    @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Vote> teamAVotes;

    @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Vote> teamBVotes;
    @ElementCollection
    private Map<String, Boolean> teamReadyForNextQuestion;

    public Round(final Question questionParam) {
        this.question = questionParam;
        this.teamAVotes = new ArrayList<>();
        this.teamBVotes = new ArrayList<>();
        this.teamReadyForNextQuestion = new HashMap<>();
        this.teamReadyForNextQuestion.put("teamA", false);
        this.teamReadyForNextQuestion.put("teamB", false);
    }
}
