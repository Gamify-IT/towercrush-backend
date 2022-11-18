package de.unistuttgart.towercrushbackend.data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import javax.persistence.*;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

/**
 * The GameResult.class contains all data that is saved after one towercrush game
 */
@Entity
@Data
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class GameResult {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    private int questionCount;
    private float score;

    @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<RoundResult> correctAnsweredQuestions;

    @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<RoundResult> wrongAnsweredQuestions;

    private UUID configurationAsUUID;
    private String playerId;
    private LocalDateTime playedTime;

    public GameResult(
        final int questionCount,
        final float score,
        final List<RoundResult> correctAnsweredQuestions,
        final List<RoundResult> wrongAnsweredQuestions,
        final UUID configurationAsUUID,
        final String playerId
    ) {
        this.questionCount = questionCount;
        this.score = score;
        this.correctAnsweredQuestions = correctAnsweredQuestions;
        this.wrongAnsweredQuestions = wrongAnsweredQuestions;
        this.configurationAsUUID = configurationAsUUID;
        this.playerId = playerId;
        this.playedTime = LocalDateTime.now();
    }
}
