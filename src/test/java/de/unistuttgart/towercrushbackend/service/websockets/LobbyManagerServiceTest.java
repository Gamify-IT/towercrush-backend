package de.unistuttgart.towercrushbackend.service.websockets;

import static org.junit.jupiter.api.Assertions.*;

import de.unistuttgart.towercrushbackend.data.websockets.Lobby;
import de.unistuttgart.towercrushbackend.data.websockets.Player;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class LobbyManagerServiceTest {

    public static final String TEST_LOBBY_NAME = "testLobby";
    public static final String TEST_PLAYER_NAME = "testPlayer";
    public static final String DOES_NOT_EXIST = "doesNotExist";
    private static final String TEAM_A_NAME = "teamA";
    LobbyManagerService lobbyManagerService;

    @BeforeEach
    void setUp() {
        lobbyManagerService = new LobbyManagerService();
    }

    @Test
    void containsLobby() {
        // test
        final boolean result = lobbyManagerService.containsLobby(DOES_NOT_EXIST);

        // evaluate
        assertFalse(result);
    }

    @Test
    void createLobby() {
        // test
        lobbyManagerService.createLobby(TEST_LOBBY_NAME);

        // evaluate
        final boolean result = lobbyManagerService.containsLobby(TEST_LOBBY_NAME);
        assertTrue(result);
    }

    @Test
    void getLobby() {
        // setup
        lobbyManagerService.createLobby(TEST_LOBBY_NAME);

        // test
        final Lobby result = lobbyManagerService.getLobby(TEST_LOBBY_NAME);

        // evaluate
        assertEquals(TEST_LOBBY_NAME, result.getLobbyName());
    }

    @Test
    void createLobbyIfNotExist() {
        // test
        lobbyManagerService.createLobbyIfNotExist(TEST_LOBBY_NAME);

        // evaluate
        final boolean result = lobbyManagerService.containsLobby(TEST_LOBBY_NAME);
        assertTrue(result);
    }

    @Test
    void addPlayer() {
        // setup
        final Player player = new Player(TEST_PLAYER_NAME);
        lobbyManagerService.createLobby(TEST_LOBBY_NAME);
        final Lobby lobby = lobbyManagerService.getLobby(TEST_LOBBY_NAME);

        // test
        lobbyManagerService.addPlayer(TEST_LOBBY_NAME, player);

        // evaluate
        assertEquals(1, lobby.getPlayers().size());
        assertTrue(lobby.getPlayers().contains(player));
    }

    @Test
    void getLobbyFromPlayer() {
        // setup
        final Player player = new Player(TEST_PLAYER_NAME);
        lobbyManagerService.createLobby(TEST_LOBBY_NAME);
        lobbyManagerService.addPlayer(TEST_LOBBY_NAME, player);

        // test
        final String result = lobbyManagerService.getLobbyFromPlayer(player.getKey());

        // evaluate
        assertEquals(TEST_LOBBY_NAME, result);
    }

    @Test
    void getPlayerFromLobby() {
        // setup
        final Player player = new Player(TEST_PLAYER_NAME);
        lobbyManagerService.createLobby(TEST_LOBBY_NAME);
        lobbyManagerService.addPlayer(TEST_LOBBY_NAME, player);

        // test
        final Player result = lobbyManagerService.getPlayerFromLobby(TEST_LOBBY_NAME, player.getKey());

        // evaluate
        assertEquals(player, result);
    }

    @Test
    void switchPlayerToTeam() {
        // setup
        final Player player = new Player(TEST_PLAYER_NAME);
        lobbyManagerService.createLobby(TEST_LOBBY_NAME);
        lobbyManagerService.addPlayer(TEST_LOBBY_NAME, player);

        // test
        lobbyManagerService.switchPlayerToTeam(TEST_LOBBY_NAME, player, TEAM_A_NAME);

        // evaluate
        assertTrue(
            lobbyManagerService.getLobby(TEST_LOBBY_NAME).getTeams().get(TEAM_A_NAME).getPlayers().contains(player)
        );
    }

    @Test
    void lobbyExists_returnsNullIfNotExists() {
        // test
        final boolean result = lobbyManagerService.lobbyExists(DOES_NOT_EXIST);

        // evaluate
        assertFalse(result);
    }

    @Test
    void lobbyExists_returnsTrueIfExists() {
        // setup
        lobbyManagerService.createLobby(TEST_LOBBY_NAME);

        // test
        final boolean result = lobbyManagerService.lobbyExists(TEST_LOBBY_NAME);

        // evaluate
        assertTrue(result);
    }

    @Test
    void removePlayerFromList() {
        // setup
        final Player player = new Player(TEST_PLAYER_NAME);
        lobbyManagerService.createLobby(TEST_LOBBY_NAME);
        lobbyManagerService.addPlayer(TEST_LOBBY_NAME, player);

        // precondition
        assertNotNull(lobbyManagerService.getLobby(TEST_LOBBY_NAME));
        assertTrue(lobbyManagerService.getLobby(TEST_LOBBY_NAME).getPlayers().contains(player));

        // test
        lobbyManagerService.removePlayerFromList(TEST_LOBBY_NAME, player.getKey());

        // evaluate
        assertNull(lobbyManagerService.getLobby(TEST_LOBBY_NAME));
        assertNull(lobbyManagerService.getLobbyFromPlayer(player.getKey()));
    }

    @Test
    void changeReady() {
        // setup
        final Player player = new Player(TEST_PLAYER_NAME);
        lobbyManagerService.createLobby(TEST_LOBBY_NAME);
        lobbyManagerService.addPlayer(TEST_LOBBY_NAME, player);

        // precondition
        assertTrue(lobbyManagerService.getLobby(TEST_LOBBY_NAME).getReadyPlayers().isEmpty());

        // test
        lobbyManagerService.changeReady(TEST_LOBBY_NAME, player);

        // evaluate
        assertTrue(lobbyManagerService.getLobby(TEST_LOBBY_NAME).getReadyPlayers().contains(player));
    }

    @Test
    void getLobbies() {
        // setup
        lobbyManagerService.createLobby(TEST_LOBBY_NAME);

        // test
        final List<Lobby> result = lobbyManagerService.getLobbies();

        // evaluate
        assertEquals(TEST_LOBBY_NAME, result.get(0).getLobbyName());
    }

    @Test
    void startGame() {
        // setup
        final Player player = new Player(TEST_PLAYER_NAME);
        lobbyManagerService.createLobby(TEST_LOBBY_NAME);
        lobbyManagerService.addPlayer(TEST_LOBBY_NAME, player);

        // precondition
        assertFalse(lobbyManagerService.getLobby(TEST_LOBBY_NAME).isStarted());

        // test
        lobbyManagerService.startGame(TEST_LOBBY_NAME);

        // evaluate
        assertTrue(lobbyManagerService.getLobby(TEST_LOBBY_NAME).isStarted());
    }
}
