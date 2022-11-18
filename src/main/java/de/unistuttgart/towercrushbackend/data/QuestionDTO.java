package de.unistuttgart.towercrushbackend.data;

import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.lang.Nullable;

/**
 * The QuestionDTO.class contains the question related information
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class QuestionDTO {

    @Nullable
    UUID id;

    String text;
    String rightAnswer;
    Set<String> wrongAnswers;

    public QuestionDTO(final String text, final String rightAnswer, final Set<String> wrongAnswers) {
        this.text = text;
        this.rightAnswer = rightAnswer;
        this.wrongAnswers = wrongAnswers;
    }

    public boolean equalsContent(final QuestionDTO other) {
        if (this == other) return true;
        if (other == null) return false;
        return (
            Objects.equals(text, other.text) &&
            Objects.equals(rightAnswer, other.rightAnswer) &&
            Objects.equals(wrongAnswers, other.wrongAnswers)
        );
    }
}
