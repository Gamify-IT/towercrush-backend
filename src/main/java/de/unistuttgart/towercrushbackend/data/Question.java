package de.unistuttgart.towercrushbackend.data;

import java.util.Set;
import java.util.UUID;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

/**
 * The Question.class contains the question related information
 */
@Entity
@Data
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Question {

    @Id
    @GeneratedValue(generator = "uuid")
    UUID id;

    String text;
    String rightAnswer;

    @ElementCollection
    Set<String> wrongAnswers;

    public Question(final String text, final String rightAnswer, final Set<String> wrongAnswers) {
        this.text = text;
        this.rightAnswer = rightAnswer;
        this.wrongAnswers = wrongAnswers;
    }
}
