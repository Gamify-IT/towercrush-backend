package de.unistuttgart.towercrushbackend.data.websockets;

import de.unistuttgart.towercrushbackend.data.Question;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class Round {
    private Question question;
    private List<Vote> teamA;
    private List<Vote> teamB;

    public Round(final Question questionParam) {
        this.question = questionParam;
        this.teamA = new ArrayList<>();
        this.teamB = new ArrayList<>();
    }
}
