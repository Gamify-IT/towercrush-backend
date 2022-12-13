package de.unistuttgart.towercrushbackend.service.websockets;

import de.unistuttgart.towercrushbackend.data.websockets.Lobby;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
@Transactional
public class GameService {
    private final Map<String, Integer> counters;

    public GameService() {
        counters = new ConcurrentHashMap<>();
    }

    public int getCounter(final String lobby) {
        return counters.getOrDefault(lobby, 0);
    }

    public void setCounter(final String lobby, final int counter) {
        counters.put(lobby, counter);
    }
}
