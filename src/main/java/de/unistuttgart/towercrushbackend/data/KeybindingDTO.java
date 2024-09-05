package de.unistuttgart.towercrushbackend.data;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

/**
 * The KeybindingDTO.class contains all data that is retrieved from the overworld-backend
 */
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
public class KeybindingDTO {
    private String binding;
    private String key;
}
