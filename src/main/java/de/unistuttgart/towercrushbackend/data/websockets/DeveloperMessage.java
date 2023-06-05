package de.unistuttgart.towercrushbackend.data.websockets;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.List;

/**
 * This Class contains the infos that are necessary to display the lobby infos in the frontend
 */

@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DeveloperMessage implements Message {

    List<Lobby> lobbyList;
}
