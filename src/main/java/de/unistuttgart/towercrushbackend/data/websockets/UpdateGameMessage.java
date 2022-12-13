package de.unistuttgart.towercrushbackend.data.websockets;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;


@NoArgsConstructor
@AllArgsConstructor
@Data
public class UpdateGameMessage implements Message {
    private int counter;
}
