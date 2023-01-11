package de.unistuttgart.towercrushbackend.data.websockets;

import de.unistuttgart.towercrushbackend.data.Question;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
