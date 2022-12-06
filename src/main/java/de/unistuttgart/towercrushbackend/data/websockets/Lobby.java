package de.unistuttgart.towercrushbackend.data.websockets;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;


@NoArgsConstructor
@AllArgsConstructor
@Data
public class Lobby {
    private Set<Player> teamA = new HashSet<>();
    private Set<Player> teamB = new HashSet<>();
    private Set<Player> players = new HashSet<>();
    private LocalDateTime createdAt;

    public void addTeamAPlayer(final Player player) {
        this.teamA.add(player);
    }

    public void addTeamBPlayer(final Player player) {
        this.teamB.add(player);
    }

    public void removeTeamAPlayer(final Player player) {
        this.teamA.remove(player);
    }

    public void removeTeamBPlayer(final Player player) {
        this.teamB.remove(player);
    }

    public void addPlayer(final Player player) {
        this.players.add(player);
    }

    public void removePlayer(final Player player) {
        this.players.remove(player);
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
}
