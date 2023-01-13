package de.unistuttgart.towercrushbackend.data.websockets;

import de.unistuttgart.towercrushbackend.data.Question;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class Round {

    private Question question;
    private List<Vote> teamAVotes;
    private List<Vote> teamBVotes;

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
