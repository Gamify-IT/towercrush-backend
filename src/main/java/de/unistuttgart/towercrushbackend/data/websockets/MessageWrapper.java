package de.unistuttgart.towercrushbackend.data.websockets;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

/**
 * This class defines in what kind of way a Message (data) should be used in the frontend
 */
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MessageWrapper {

    private String data;
    private Purpose purpose;
}
