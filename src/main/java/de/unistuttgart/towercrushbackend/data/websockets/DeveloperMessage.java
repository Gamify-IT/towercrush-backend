package de.unistuttgart.towercrushbackend.data.websockets;

import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

/**
 * This Class contains the infos that are necessary to display the lobby infos in the frontend
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DeveloperMessage implements Message {

    List<Lobby> lobbyList;
}
