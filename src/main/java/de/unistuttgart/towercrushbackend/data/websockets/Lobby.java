package de.unistuttgart.towercrushbackend.data.websockets;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import javax.persistence.ElementCollection;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * This class contains all the infos that need to be saved for a lobby of tower crush
 */
@AllArgsConstructor
@Data
public class Lobby {

    @ElementCollection
    private Map<String, Team> teams;

    private Set<Player> players = new HashSet<>();

    private Set<Player> readyPlayers = new HashSet<>();

    private boolean started = false;
    private LocalDateTime createdAt;

    private String lobbyName;

    private static String TEAM_A_NAME = "teamA";

    private static String TEAM_B_NAME = "teamB";

    public Lobby() {
        this.teams = new HashMap<>();
        this.teams.put(TEAM_A_NAME, new Team());
        this.teams.put(TEAM_B_NAME, new Team());
    }

    public void addPlayer(final Player player) {
        this.players.add(player);
    }

    public void removePlayer(final UUID playerUUID) {
        final Player player = findPlayer(playerUUID);
        removePlayerFromTeams(player);
        players.remove(player);
        readyPlayers.remove(player);
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

    public void removePlayerFromTeams(final Player player) {
        this.teams.forEach((teamName, team) -> team.getPlayers().remove(player));
    }

    public void addPlayertoTeam(final Player player, final String team) {
        this.removePlayerFromTeams(player);
        this.teams.get(team).getPlayers().add(player);
    }

    public Player findPlayer(final UUID playerUUID) {
        final Optional<Player> returnPlayer =
            this.players.stream().filter(player -> player.getKey().equals(playerUUID)).findFirst();
        return returnPlayer.orElse(null);
    }
}
