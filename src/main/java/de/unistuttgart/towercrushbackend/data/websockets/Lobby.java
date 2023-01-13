package de.unistuttgart.towercrushbackend.data.websockets;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class Lobby {

    private Set<Player> teamA = new HashSet<>();
    private Set<Player> teamB = new HashSet<>();
    private Set<Player> players = new HashSet<>();

    private Set<Player> readyPlayers = new HashSet<>();

    private boolean started = false;
    private LocalDateTime createdAt;

    private String lobbyName;

    public void addPlayer(final Player player) {
        this.players.add(player);
    }

    public void removePlayer(final UUID playerUUID) {
        final Player player = findPlayer(playerUUID);
        removePlayerTeams(player);
        players.remove(player);
    }

    public void setCreationDate() {
        this.createdAt = LocalDateTime.now();
    }

    public LocalDateTime getCreationDate() {
        return this.createdAt;
    }

    public Set<String> getPlayerNames() {
        return players.stream().map(Player::getPlayerName).collect(Collectors.toSet());
    }

    public void removePlayerTeams(final Player player) {
        this.teamA.remove(player);
        this.teamB.remove(player);
    }

    public void addPlayerToTeamA(final Player player) {
        this.removePlayerTeams(player);
        this.teamA.add(player);
    }

    public void addPlayerToTeamB(final Player player) {
        this.removePlayerTeams(player);
        this.teamB.add(player);
    }

    public Player findPlayer(final UUID playerUUID) {
        final Optional<Player> returnPlayer =
            this.players.stream().filter(player -> player.getKey().equals(playerUUID)).findFirst();
        return returnPlayer.orElse(null);
    }

    public boolean isPlayerInTeamA(final Player player) {
        return this.teamA.contains(player);
    }

    public boolean isPlayerInTeamB(final Player player) {
        return this.teamB.contains(player);
    }
}
