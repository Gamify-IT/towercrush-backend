package de.unistuttgart.towercrushbackend.data;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.lang.Nullable;

import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * The ConfigurationDTO.class contains all data that has to be stored to configure a towercrush game
 */
@Data //NOSONAR
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ConfigurationDTO {

    @Nullable
    UUID id;

    Set<QuestionDTO> questions;

    public ConfigurationDTO(final Set<QuestionDTO> questions) {
        this.questions = questions;
    }

    public boolean equalsContent(final ConfigurationDTO other) {
        if (this == other) return true;
        if (other == null) return false;
        return Objects.equals(questions, other.questions);
    }
}
