package de.unistuttgart.towercrushbackend.data;

import java.util.List;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.lang.Nullable;

/**
 * The GameResultDTO.class contains all data that is saved after one towercrush game
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class GameResultDTO {

    @Nullable
    private long id;

    private int questionCount;
    private float score;
    private List<RoundResultDTO> correctAnsweredQuestions;
    private List<RoundResultDTO> wrongAnsweredQuestions;
    private UUID configurationAsUUID;

    public GameResultDTO(
        final int questionCount,
        final int score,
        final List<RoundResultDTO> correctAnsweredQuestions,
        final List<RoundResultDTO> wrongAnsweredQuestions,
        final UUID configurationAsUUID
    ) {
        this.questionCount = questionCount;
        this.score = score;
        this.correctAnsweredQuestions = correctAnsweredQuestions;
        this.wrongAnsweredQuestions = wrongAnsweredQuestions;
        this.configurationAsUUID = configurationAsUUID;
    }

    public boolean equalsContent(final GameResultDTO other) {
        if (this == other) return true;
        if (other == null) return false;

        if (id != other.id) return false;
        if (questionCount != other.questionCount) return false;
        if (score != other.score) return false;
        if (!correctAnsweredQuestions.equals(other.correctAnsweredQuestions)) return false;
        if (!wrongAnsweredQuestions.equals(other.wrongAnsweredQuestions)) return false;
        return configurationAsUUID.equals(other.configurationAsUUID);
    }
}
