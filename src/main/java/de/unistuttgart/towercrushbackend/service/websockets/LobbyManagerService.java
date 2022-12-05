package de.unistuttgart.towercrushbackend.service.websockets;

import de.unistuttgart.towercrushbackend.data.websockets.Lobby;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
@Transactional
public class LobbyManagerService {

    private final Map<String, Lobby> lobbyMap;

    public LobbyManagerService() {
        this.lobbyMap = new HashMap<>();
    }

    public boolean containsLobby(final String lobby) {
        return lobbyMap.containsKey(lobby);
    }

    public void createLobby(final String lobby) {
        this.lobbyMap.put(lobby, new Lobby());
    }

    public Lobby getLobby(final String lobby) {
        return this.lobbyMap.get(lobby);
    }

    public void createLobbyIfNotExist(final String lobby) {
        if (!containsLobby(lobby)) {
            this.createLobby(lobby);
        }
    }

    public void addMember(final String lobby, final String player) {
        lobbyMap.get(lobby).addMember(player);
    }
}
