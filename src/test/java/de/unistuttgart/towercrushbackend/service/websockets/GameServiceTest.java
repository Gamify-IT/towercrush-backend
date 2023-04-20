package de.unistuttgart.towercrushbackend.service.websockets;

import static org.junit.jupiter.api.Assertions.*;

import de.unistuttgart.towercrushbackend.data.Configuration;
import de.unistuttgart.towercrushbackend.data.Question;
import de.unistuttgart.towercrushbackend.data.websockets.Lobby;
import de.unistuttgart.towercrushbackend.data.websockets.Player;
import de.unistuttgart.towercrushbackend.repositories.ConfigurationRepository;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class GameServiceTest {

    public static final String TEST_LOBBY_NAME = "testLobby";
    public static final String TEST_PLAYER_NAME = "testPlayer";
    public static final String DOES_NOT_EXIST = "doesNotExist";
    private static final String TEAM_A_NAME = "teamA";

    @Autowired
    GameService gameService;

    @Autowired
    private ConfigurationRepository configurationRepository;

    @Autowired
    LobbyManagerService lobbyManagerService;

    private Configuration testConfiguration;

    private Lobby testLobby;
    private Player testPlayer;

    @BeforeEach
    void createTestLobby() {
        testPlayer = new Player(TEST_PLAYER_NAME);
        lobbyManagerService.createLobby(TEST_LOBBY_NAME);
        lobbyManagerService.addPlayer(TEST_LOBBY_NAME, testPlayer);
        lobbyManagerService.switchPlayerToTeam(TEST_LOBBY_NAME, testPlayer, TEAM_A_NAME);
        lobbyManagerService.changeReady(TEST_LOBBY_NAME, testPlayer);
        testLobby = lobbyManagerService.getLobby(TEST_LOBBY_NAME);
    }

    @AfterEach
    void deleteTestLobby() {
        lobbyManagerService.removePlayerFromList(TEST_LOBBY_NAME, testPlayer.getKey());
    }

    @BeforeEach
    void createConfiguration() {
        final Question initialQuestion1 = new Question();
        initialQuestion1.setText("Are you cool?");
        initialQuestion1.setRightAnswer("Yes");
        initialQuestion1.setWrongAnswers(Set.of("No", "Maybe"));

        final Question initialQuestion2 = new Question();
        initialQuestion2.setText("Is this game cool?");
        initialQuestion2.setRightAnswer("Yes");
        initialQuestion2.setWrongAnswers(Set.of("No", "Maybe"));

        testConfiguration = new Configuration();
        testConfiguration.setQuestions(Set.of(initialQuestion1, initialQuestion2));

        configurationRepository.save(testConfiguration);
    }

    @AfterEach
    void deleteConfiguration() {
        configurationRepository.deleteAll();
    }

    @Test
    void createGame() {
        gameService.createGame(TEST_LOBBY_NAME, testConfiguration.getId());
        assertNotNull(gameService.getGameForLobby(TEST_LOBBY_NAME));
    }

    @Test
    void getGameForLobby() {
        gameService.createGame(TEST_LOBBY_NAME, testConfiguration.getId());
        assertNotNull(gameService.getGameForLobby(TEST_LOBBY_NAME));
    }

    @Test
    void putVote() {}

    @Test
    void evaluateAnswers() {}

    @Test
    void hasNextQuestion() {}

    @Test
    void nextQuestion() {}

    @Test
    void setWinner() {}

    @Test
    void deleteGame() {}

    @Test
    void startTask() {}

    @Test
    void removePlayerFromGame() {}
}
