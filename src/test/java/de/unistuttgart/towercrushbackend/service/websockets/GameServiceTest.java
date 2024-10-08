/*
package de.unistuttgart.towercrushbackend.service.websockets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import de.unistuttgart.towercrushbackend.data.Configuration;
import de.unistuttgart.towercrushbackend.data.Question;
import de.unistuttgart.towercrushbackend.data.websockets.*;
import de.unistuttgart.towercrushbackend.repositories.ConfigurationRepository;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
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
    private static final String TEAM_A_NAME = "teamA";

    @Autowired
    private GameService gameService;

    @Autowired
    private ConfigurationRepository configurationRepository;

    @Autowired
    private LobbyManagerService lobbyManagerService;

    private Configuration testConfiguration;
    private Lobby testLobby;
    private Player testPlayer;

    @BeforeEach
    void createTestLobby() {
        testPlayer = new Player(TEST_PLAYER_NAME);
        lobbyManagerService.createLobby(TEST_LOBBY_NAME, UUID.randomUUID());
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

        testConfiguration = new Configuration();
        testConfiguration.setQuestions(Set.of(initialQuestion1));

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
    void putVote() {
        // setup
        gameService.createGame(TEST_LOBBY_NAME, testConfiguration.getId());
        final Game game = gameService.getGameForLobby(TEST_LOBBY_NAME);
        final Question testQuestion = game.getRounds().get(0).getQuestion();

        // test
        gameService.putVote(
            TEST_LOBBY_NAME,
            TEAM_A_NAME,
            testQuestion.getId(),
            testPlayer,
            testQuestion.getRightAnswer()
        );

        // evaluate
        assertEquals(testLobby.getLobbyName(), game.getLobbyName());

        final List<Round> rounds = game.getRounds();
        final List<Vote> votes = game.getRounds().get(0).getTeamVotes().get(TEAM_A_NAME).getVotes();
        assertEquals(1, votes.size());
        assertEquals(testPlayer, votes.get(0).getPlayer());
        assertEquals(testQuestion.getRightAnswer(), votes.get(0).getAnswer());
    }

    @Test
    void evaluateAnswers() {
        // setup
        gameService.createGame(TEST_LOBBY_NAME, testConfiguration.getId());
        final Question testQuestion = testConfiguration.getQuestions().iterator().next();

        // test
        gameService.putVote(
            TEST_LOBBY_NAME,
            TEAM_A_NAME,
            testQuestion.getId(),
            testPlayer,
            testQuestion.getRightAnswer()
        );
        gameService.evaluateAnswers(TEST_LOBBY_NAME, TEAM_A_NAME);

        // evaluate
        final Game game = gameService.getGameForLobby(TEST_LOBBY_NAME);
        final Map<String, Integer> correctAnswerCount = game.getCorrectAnswerCount();
        assertEquals(1, correctAnswerCount.get(TEAM_A_NAME).intValue());
    }

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
*/
