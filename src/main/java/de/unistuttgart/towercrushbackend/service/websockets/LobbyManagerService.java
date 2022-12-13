package de.unistuttgart.towercrushbackend.service.websockets;

import de.unistuttgart.towercrushbackend.data.websockets.Lobby;
import de.unistuttgart.towercrushbackend.data.websockets.Player;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
@Transactional
public class LobbyManagerService {

    private final Map<String, Lobby> lobbyMap;

    public LobbyManagerService() {
        this.lobbyMap = new ConcurrentHashMap<>();
    }

    public boolean containsLobby(final String lobby) {
        return lobbyMap.containsKey(lobby);
    }

    public void createLobby(final String lobbyName) {
        final Lobby newLobby = new Lobby();
        newLobby.setLobbyName(lobbyName);
        this.lobbyMap.put(lobbyName, newLobby);
    }

    public Lobby getLobby(final String lobby) {
        return this.lobbyMap.get(lobby);
    }

    public void createLobbyIfNotExist(final String lobby) {
        if (!containsLobby(lobby)) {
            this.createLobby(lobby);
        }
    }

    public void addPlayer(final String lobby, final Player player) {
        lobbyMap.get(lobby).addPlayer(player);
    }

    public String getLobbyFromPlayer(final UUID player) {
        final String[] lobbyReturn = new String[1];
        lobbyMap.forEach((lobbyName, lobby) -> {
            for (final Player playerTemp : lobby.getPlayers()) {
                if (playerTemp.getKey().toString().equals(player.toString())) {
                    lobbyReturn[0] = lobbyName;
                }
            }
        });
        return lobbyReturn[0];
    }

    public Player getPlayerFromLobby(final String lobbyName, final UUID playerUUID) {
        final Lobby lobby = lobbyMap.get(lobbyName);
        return lobby.findPlayer(playerUUID);
    }

    public void switchPlayerToTeamA(final String lobby, final Player player) {
        final Lobby lobbyTemp = lobbyMap.get(lobby);
        lobbyTemp.removePlayerTeams(player);
        lobbyTemp.addPlayerToTeamA(player);
    }

    public void switchPlayerToTeamB(final String lobby, final Player player) {
        final Lobby lobbyTemp = lobbyMap.get(lobby);
        lobbyTemp.removePlayerTeams(player);
        lobbyTemp.addPlayerToTeamB(player);
    }

    public boolean lobbyExists(final String lobby) {
        return lobbyMap.containsKey(lobby);
    }

    public void removePlayerFromList(final String lobby, final UUID playerToRemove) {
        lobbyMap.get(lobby).removePlayer(playerToRemove);
        if (lobbyMap.get(lobby).getPlayers().isEmpty()) {
            lobbyMap.remove(lobby);
        }
    }

    public List<Lobby> getLobbies() {
        return new ArrayList<Lobby>(this.lobbyMap.values());
    }
}
