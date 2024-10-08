package de.unistuttgart.towercrushbackend.data;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.persistence.*;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

/**
 * The Configuration.class contains all data that has to be stored to configure a towercrush game
 */
@Entity
@Data //NOSONAR
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Configuration {

    @Id
    @GeneratedValue(generator = "uuid")
    UUID id;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    Set<Question> questions;
    /**
     * The volume level that is setted by the player.
     */
    Integer volumeLevel;

    public Configuration(final Set<Question> questions) {
        this.questions = questions;
    }

    public void addQuestion(final Question question) {
        this.questions.add(question);
    }

    public void removeQuestion(final Question question) {
        this.questions.remove(question);
    }

    @Override
    public Configuration clone() {
        return new Configuration(this.questions.stream().map(Question::clone).collect(Collectors.toSet()));
    }
}
