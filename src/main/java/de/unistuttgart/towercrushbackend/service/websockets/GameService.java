package de.unistuttgart.towercrushbackend.service.websockets;

import com.fasterxml.jackson.core.JsonProcessingException;
import de.unistuttgart.towercrushbackend.data.Configuration;
import de.unistuttgart.towercrushbackend.data.Question;
import de.unistuttgart.towercrushbackend.data.websockets.*;
import de.unistuttgart.towercrushbackend.repositories.ConfigurationRepository;
import de.unistuttgart.towercrushbackend.repositories.GameResultRepository;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * This class handles the overall game run through
 */
@Service
@Slf4j
@Transactional
public class GameService {

    @Autowired
    LobbyManagerService lobbyManagerService;

    @Autowired
    ConfigurationRepository configurationRepository;

    @Autowired
    SimpMessagingTemplate simpMessagingTemplate;

    @Autowired
    WebsocketService websocketService;

    @Autowired
    GameResultRepository gameResultRepository;

    ExecutorService executorService = Executors.newFixedThreadPool(1);
    Future<?> timeUpdate;

    private final Map<String, Game> games;
    static final String LOBBY_TOPIC = "/topic/lobby/";

    private static final String TEAM_A_NAME = "teamA";

    private static final String TEAM_B_NAME = "teamB";
    private static final int TIME_PER_QUESTION = 10;
    private static final int CORRECT_ANSWER_BONUS = TIME_PER_QUESTION / 2;
    private static final int WRONG_ANSWER_MALUS = -(TIME_PER_QUESTION * 2);

    public GameService() {
        games = new ConcurrentHashMap<>();
    }

    /**
     * This method creates and initializes a game
     *
     * @param lobby           that starts a game
     * @param configurationId configurationId that determines which questions are loaded
     */
    public void createGame(final String lobby, final UUID configurationId) {
        final List<Round> tempRounds = new ArrayList<>();
        final Optional<Configuration> configuration = configurationRepository.findById(configurationId);
        if (configuration.isEmpty()) {
            throw new NoSuchElementException("unknown configurationId: " + configurationId);
        }
        for (final Question question : configuration.get().getQuestions()) {
            tempRounds.add(new Round(question));
        }
        final Team teamA = lobbyManagerService.getLobby(lobby).getTeams().get(TEAM_A_NAME);
        final Team teamB = lobbyManagerService.getLobby(lobby).getTeams().get(TEAM_B_NAME);
        final Game game = new Game(
            lobby,
            teamA,
            teamB,
            tempRounds,
            configurationId,
            (long) tempRounds.size() * TIME_PER_QUESTION
        );
        if (!games.containsKey(lobby)) {
            games.put(lobby, game);
        }
        this.startTask();
    }

    public Game getGameForLobby(final String lobby) {
        return this.games.get(lobby);
    }

    /**
     * This method searches for the right round and handles voting
     *
     * @param lobby    lobby in that the player voted
     * @param team     team in that the player voted
     * @param question question the player voted
     * @param player   player that voted
     * @param answer   ander the player voted for
     */
    public void putVote(
        final String lobby,
        final String team,
        final UUID question,
        final Player player,
        final String answer
    ) {
        log.info(
            "lobby {} team {} question {} player {} answer {}",
            lobby,
            team,
            question,
            player.getPlayerName(),
            answer
        );
        final Game game = games.get(lobby);
        if (game != null) {
            final List<Round> rounds = new ArrayList<>(game.getRounds());
            for (final Round round : rounds) {
                if (round.getQuestion().getId().equals(question)) {
                    final List<Vote> tempVotes = round.getTeamVotes().get(team).getVotes();
                    removeOldVotesFromPlayer(team, player, round, tempVotes);
                    tempVotes.add(new Vote(player, answer));
                    checkIfWholeTeamVoted(team, game, round, tempVotes);
                }
            }
            games.get(lobby).setRounds(rounds);
        }
    }

    /**
     * This method removes all votes a player had for the current question
     *
     * @param team      team in that the player is in
     * @param player    player whos votes should be deleted
     * @param round     round in which all votes should be deleted for the player
     * @param tempVotes all votes - from all players of the same team as the player is in - for this round
     */
    private void removeOldVotesFromPlayer(
        final String team,
        final Player player,
        final Round round,
        final List<Vote> tempVotes
    ) {
        final Set<Vote> voteToDelete = round
            .getTeamVotes()
            .get(team)
            .getVotes()
            .stream()
            .filter(vote -> vote.getPlayer().equalsUUID(player))
            .collect(Collectors.toSet());
        tempVotes.removeAll(voteToDelete);
    }

    /**
     * This method checks if everyone voted and handles if so
     *
     * @param team      team that gets checked if everyone voted
     * @param game      game in which the team is in
     * @param round     round that is currently open to vote for
     * @param tempVotes all votes - from all players of the same team as the player is in - for this round
     */
    private void checkIfWholeTeamVoted(
        final String team,
        final Game game,
        final Round round,
        final List<Vote> tempVotes
    ) {
        if (tempVotes.size() == game.getTeams().get(team).getPlayers().size()) {
            round.getTeamReadyForNextQuestion().put(team, true);
        }
    }

    /**
     * @param lobby
     * @param team
     */
    public void evaluateAnswers(final String lobby, final String team) {
        final Game tempGame = games.get(lobby);
        if (tempGame == null) {
            log.error("evaluate answer but game doesnt exist, maybe ended");
        } else {
            final int currentQuestionNumber = tempGame.getCurrentQuestion().get(team);
            final Map<String, Long> counts = tempGame
                .getRounds()
                .get(currentQuestionNumber)
                .getTeamVotes()
                .get(team)
                .getVotes()
                .stream()
                .collect(Collectors.groupingBy(Vote::getAnswer, Collectors.counting()));
            final String correctAnswer = tempGame.getRounds().get(currentQuestionNumber).getQuestion().getRightAnswer();
            final long correctAnswerVotes = counts.get(correctAnswer) == null ? 0 : counts.get(correctAnswer);
            counts.remove(correctAnswer);

            final int towerChange = calculateTowerChange(counts, correctAnswerVotes);
            handleTowerChange(team, tempGame, towerChange);
        }
    }

    /**
     * This method contains all the logic that happens with the calculated tower change
     *
     * @param team        team that wants to evaluate answers
     * @param tempGame    game in that the team is in
     * @param towerChange tower change, positive if the majority was right, negative if not
     */
    private void handleTowerChange(final String team, final Game tempGame, final int towerChange) {
        if (towerChange > 0) {
            tempGame.getCorrectAnswerCount().put(team, tempGame.getCorrectAnswerCount().get(team) + 1);
        }
        tempGame.getAnswerPoints().put(team, tempGame.getAnswerPoints().get(team) + towerChange);
    }

    /**
     * This method calculates the change depending if the majority was right or not
     *
     * @param counts             map <key = answer, value = votes for the answer>
     * @param correctAnswerVotes long, numer of votes for the correct answer
     * @return tower change as int
     */
    private int calculateTowerChange(final Map<String, Long> counts, final long correctAnswerVotes) {
        int towerChange = CORRECT_ANSWER_BONUS;
        for (final Map.Entry<String, Long> entry : counts.entrySet()) {
            if (entry.getValue() >= correctAnswerVotes) {
                towerChange = WRONG_ANSWER_MALUS;
            }
        }
        return towerChange;
    }

    /**
     * This method checks if a team has another question or not
     *
     * @param lobby lobby in that the team is in
     * @param team  team that wants to check if it has another question
     * @return boolean true if the team has another question
     */
    public boolean hasNextQuestion(final String lobby, final String team) {
        log.info("has team {} next question", team);
        if (!this.games.containsKey(lobby)) {
            return false;
        } else {
            final Game tempGame = games.get(lobby);
            return tempGame.getCurrentQuestion().get(team) < tempGame.getRounds().size() - 1;
        }
    }

    /**
     * This method increases th current question nummer for the team.
     *
     * @param lobby lobby in that the team is in
     * @param team  eam that wants to get the next
     */
    public void nextQuestion(final String lobby, final String team) {
        log.info("next question for team: {}", team);
        final Game tempGame = games.get(lobby);
        if (tempGame == null) {
            return;
        }
        final int currentQuestionNumber = tempGame.getCurrentQuestion().get(team);
        tempGame.getCurrentQuestion().put(team, currentQuestionNumber + 1);
    }

    /**
     * This method sets the winner team of a lobby
     *
     * @param lobby that wants to evaluate who won
     */
    public void setWinner(final String lobby) {
        final Game tempGame = games.get(lobby);
        if (tempGame.getCorrectAnswerCount().get(TEAM_A_NAME) > tempGame.getCorrectAnswerCount().get(TEAM_B_NAME)) {
            tempGame.setWinnerTeam(TEAM_A_NAME);
            log.info("team teamA won");
        } else if (
            Objects.equals(
                tempGame.getCorrectAnswerCount().get(TEAM_A_NAME),
                tempGame.getCorrectAnswerCount().get(TEAM_B_NAME)
            )
        ) {
            tempGame.setWinnerTeam("draw");
            log.info("team draw won");
        } else {
            tempGame.setWinnerTeam(TEAM_B_NAME);
            log.info("team teamB won");
        }
    }

    public void deleteGame(final String lobby) {
        this.games.remove(lobby);
    }

    public void startTask() {
        if (timeUpdate != null) {
            return;
        }
        timeUpdate =
            executorService.submit(() -> {
                boolean teamWon;
                int currentOpenGames = 1;
                while (currentOpenGames > 0) {
                    log.info("update tower");
                    currentOpenGames = this.games.entrySet().size();
                    for (final Map.Entry<String, Game> entry : this.games.entrySet()) {
                        log.info("update tower for each game");
                        final Game game = entry.getValue();
                        updateTowerSize(game);
                        teamWon = isTeamWon(game);
                        updateFrontend(game);
                        if (teamWon) {
                            saveAndDeleteGame(game);
                        }
                    }
                    sleep(1000);
                }
                timeUpdate = null;
            });
    }

    /**
     * This method calculates the new size of the towers
     *
     * @param game the game which the towers are in
     */
    private void updateTowerSize(final Game game) {
        game
            .getTowerSize()
            .put(
                TEAM_A_NAME,
                (int) (
                    game.getAnswerPoints().get(TEAM_A_NAME) +
                    (
                        game.getInitialTowerSize() -
                        (ChronoUnit.SECONDS.between(game.getStartedGame(), LocalDateTime.now()))
                    )
                )
            );
        game
            .getTowerSize()
            .put(
                TEAM_B_NAME,
                (int) (
                    game.getAnswerPoints().get(TEAM_B_NAME) +
                    (
                        game.getInitialTowerSize() -
                        (ChronoUnit.SECONDS.between(game.getStartedGame(), LocalDateTime.now()))
                    )
                )
            );
    }

    /**
     * Checks if a tower is destroyed (0) and sets the winner
     *
     * @param game the game to check for the winner
     * @return true if a team won
     */
    private boolean isTeamWon(final Game game) {
        boolean teamWon = false;
        if (game.getTowerSize().get(TEAM_B_NAME) == 0 && game.getTowerSize().get(TEAM_A_NAME) == 0) {
            log.info("set draw");
            game.setWinnerTeam("draw");
            teamWon = true;
        } else if (game.getTowerSize().get(TEAM_B_NAME) <= 0) {
            log.info("set winner a");
            game.setWinnerTeam(TEAM_A_NAME);
            teamWon = true;
        } else if (game.getTowerSize().get(TEAM_A_NAME) <= 0) {
            log.info("set winner b");
            game.setWinnerTeam(TEAM_B_NAME);
            teamWon = true;
        }
        return teamWon;
    }

    /**
     * This method updates the frontend with the new game status
     *
     * @param game the game to update
     */
    private void updateFrontend(final Game game) {
        final UpdateGameMessage updateGameMessage = new UpdateGameMessage(game);
        final MessageWrapper updateLobbyMassageWrapped;
        try {
            updateLobbyMassageWrapped = websocketService.wrapMessage(updateGameMessage, Purpose.UPDATE_GAME_MESSAGE);
            simpMessagingTemplate.convertAndSend(
                GameService.LOBBY_TOPIC + game.getLobbyName(),
                updateLobbyMassageWrapped
            );
        } catch (final JsonProcessingException e) {
            log.error("could not parse the message, therefore the frontend could not be informed! error: ", e);
        }
    }

    /**
     * This method saves the game in the DB and deletes it from the games list
     *
     * @param game the game to save and delete
     */
    private void saveAndDeleteGame(final Game game) {
        log.info("remove game");
        try {
            gameResultRepository.save(this.games.get(game.getLobbyName()));
        } catch (final Exception e) {
            log.info("Game results were null: " + e);
            timeUpdate.cancel(true);
            timeUpdate = null;
        }
        this.games.remove(game.getLobbyName());
    }

    /**
     * Let a thread sleep
     *
     * @param sleepTime sleep time
     */
    private void sleep(final int sleepTime) {
        try {
            Thread.sleep(sleepTime);
        } catch (final InterruptedException e) {
            log.error("could not sleep  {} seconds", sleepTime, e);
        }
    }

    /**
     * This method removes a player from a game, if no players left remove game
     *
     * @param lobby      the lobby to remove the player from
     * @param playerUUID the player to remove
     */
    public void removePlayerFromGame(final String lobby, final UUID playerUUID) {
        final Player playerToRemove = lobbyManagerService.getLobby(lobby).findPlayer(playerUUID);
        final Game game = games.get(lobby);
        if (game != null) {
            int playerCount = 0;
            for (final Map.Entry<String, Team> entry : game.getTeams().entrySet()) {
                entry.getValue().getPlayers().remove(playerToRemove);
                playerCount += entry.getValue().getPlayers().size();
            }
            if (playerCount == 0) {
                games.remove(lobby);
            }
        }
    }
}
