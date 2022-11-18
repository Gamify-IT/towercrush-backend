package de.unistuttgart.towercrushbackend.data;

import java.util.Objects;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.lang.Nullable;

/**
 * The RoundResultDTO.class contains the round result related information
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RoundResultDTO {

    @Nullable
    UUID id;

    UUID questionUUId;
    String answer;

    public RoundResultDTO(final UUID questionUUId, final String answer) {
        this.questionUUId = questionUUId;
        this.answer = answer;
    }

    public boolean equalsContent(final RoundResultDTO other) {
        if (this == other) return true;
        if (other == null) return false;
        return (Objects.equals(questionUUId, other.questionUUId) && Objects.equals(answer, other.answer));
    }
}
