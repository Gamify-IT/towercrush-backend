package de.unistuttgart.towercrushbackend.service.websockets;

import de.unistuttgart.towercrushbackend.data.websockets.Lobby;
import de.unistuttgart.towercrushbackend.data.websockets.Player;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
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
                    break;
                }
            }
        });
        return lobbyReturn[0];
    }

    public Player getPlayerFromLobby(final String lobbyName, final UUID playerUUID) {
        final Lobby lobby = lobbyMap.get(lobbyName);
        return lobby.findPlayer(playerUUID);
    }

    public void switchPlayerToTeam(final String lobby, final Player player, final String toTeam) {
        final Lobby lobbyTemp = lobbyMap.get(lobby);
        lobbyTemp.removePlayerFromTeams(player);
        lobbyTemp.addPlayertoTeam(player, toTeam);
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

    public void changeReady(final String lobby, final Player player) {
        if (lobbyMap.get(lobby).getReadyPlayers().contains(player)) {
            lobbyMap.get(lobby).getReadyPlayers().remove(player);
        } else {
            lobbyMap.get(lobby).getReadyPlayers().add(player);
        }
    }

    public List<Lobby> getLobbies() {
        return new ArrayList<>(this.lobbyMap.values());
    }

    public void startGame(final String lobby) {
        lobbyMap.get(lobby).setStarted(true);
    }
}
