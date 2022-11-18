package de.unistuttgart.towercrushbackend.data;

import java.util.UUID;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

/**
 * The RoundResult.class contains the round result related information
 */
@Entity
@Data
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RoundResult {

    @Id
    @GeneratedValue(generator = "uuid")
    UUID id;

    @ManyToOne
    Question question;

    String answer;

    public RoundResult(final Question question, final String answer) {
        this.question = question;
        this.answer = answer;
    }
}
