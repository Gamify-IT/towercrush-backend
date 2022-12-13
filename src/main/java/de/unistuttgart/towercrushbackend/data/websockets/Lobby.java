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
public class Lobby {
    private Set<Player> teamA = new HashSet<>();
    private Set<Player> teamB = new HashSet<>();
    private Set<Player> players = new HashSet<>();
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
        return players.stream().map(Player::getPlayer).collect(Collectors.toSet());
    }

    public void removePlayerTeams(Player player) {
        this.teamA.remove(player);
        this.teamB.remove(player);
    }

    public void addPlayerToTeamA(Player player) {
        this.removePlayerTeams(player);
        this.teamA.add(player);
    }

    public void addPlayerToTeamB(Player player) {
        this.removePlayerTeams(player);
        this.teamB.add(player);
    }

    public Player findPlayer(UUID playerUUID) {
        return this.players.stream().filter(player -> player.getKey().equals(playerUUID)).findFirst().get();
    }

    public boolean isPlayerInTeamA(Player player) {
        return this.teamA.contains(player);
    }

    public boolean isPlayerInTeamB(Player player) {
        return this.teamB.contains(player);
    }
}
