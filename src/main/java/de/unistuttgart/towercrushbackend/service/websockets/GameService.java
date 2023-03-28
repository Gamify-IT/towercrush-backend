package de.unistuttgart.towercrushbackend.service.websockets;

import com.fasterxml.jackson.core.JsonProcessingException;
import de.unistuttgart.towercrushbackend.data.Configuration;
import de.unistuttgart.towercrushbackend.data.Question;
import de.unistuttgart.towercrushbackend.data.websockets.*;
import de.unistuttgart.towercrushbackend.repositories.ConfigurationRepository;
import de.unistuttgart.towercrushbackend.repositories.GameRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

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
    GameRepository gameRepository;

    ExecutorService executorService =
        Executors.newFixedThreadPool(1);
    Future<?> timeUpdate;

    private final Map<String, Game> games;
    static final String LOBBY_TOPIC = "/topic/lobby/";


    private static final String TEAM_A_NAME = "teamA";

    private static final String TEAM_B_NAME = "teamB";
    private static final int timePerQuestion = 10;
    private static final int correctAnswerBonus = timePerQuestion / 2;
    private static final int wrongAnswerMalus = -(timePerQuestion * 2);

    public GameService() {
        games = new ConcurrentHashMap<>();
    }

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
            (long) tempRounds.size() * timePerQuestion);
        if (!games.containsKey(lobby)) {
            games.put(lobby, game);
        }
        this.startTask();
    }

    public Game getGameForLobby(final String lobby) {
        return this.games.get(lobby);
    }

    public void putVote(
        final String lobby,
        final String team,
        final UUID question,
        final Player player,
        final String answer
    ) {
        log.info("lobby {} team {} question {} player {} answer {}", lobby, team, question, player.getPlayerName(), answer);
        final Game game = games.get(lobby);
        if (game.getWinnerTeam().isEmpty()) {
            final List<Round> rounds = new ArrayList<>(game.getRounds());
            for (final Round round : rounds) {
                if (round.getQuestion().getId().equals(question)) {
                    final Set<Vote> voteToDelete = round
                        .getTeamVotes()
                        .get(team)
                        .getVotes()
                        .stream()
                        .filter(vote -> vote.getPlayer().equalsUUID(player))
                        .collect(Collectors.toSet());
                    final List<Vote> tempVotes = round.getTeamVotes().get(team).getVotes();
                    tempVotes.removeAll(voteToDelete);
                    tempVotes.add(new Vote(player, answer));
                    if (tempVotes.size() == game.getTeams().get(team).getPlayers().size()) {
                        round.getTeamReadyForNextQuestion().put(team, true);
                    }
                }
            }
            games.get(lobby).setRounds(rounds);
        }
    }

    public void evaluateAnswers(final String lobby, final String team) {
        final Game tempGame = games.get(lobby);
        if (tempGame == null) {
            log.error("evaluate answer but game doesnt exist, maybe ended");
        } else {
            final int currentQuestionNumber = tempGame.getCurrentQuestion().get(team);
            final Map<String, Long> counts =
                tempGame.getRounds().get(currentQuestionNumber).getTeamVotes().get(team).getVotes().stream().collect(Collectors.groupingBy(Vote::getAnswer, Collectors.counting()));
            final String correctAnswer = tempGame.getRounds().get(currentQuestionNumber).getQuestion().getRightAnswer();
            final long correctAnswerVotes = counts.get(correctAnswer) == null ? 0 : counts.get(correctAnswer);
            counts.remove(correctAnswer);
            int towerChange = correctAnswerBonus;
            for (final Map.Entry<String, Long> entry : counts.entrySet()) {
                if (entry.getValue() >= correctAnswerVotes) {
                    towerChange = wrongAnswerMalus;
                }
            }
            if (towerChange > 0) {
                tempGame.getCorrectAnswerCount().put(team, tempGame.getCorrectAnswerCount().get(team) + 1);
            }
            tempGame.getAnswerPoints().put(team, tempGame.getAnswerPoints().get(team) + towerChange);
        }
    }

    public boolean hasNextQuestion(final String lobby, final String team) {
        log.info("has team {} next question", team);
        if (!this.games.containsKey(lobby)) {
            return false;
        } else {
            final Game tempGame = games.get(lobby);
            return tempGame.getCurrentQuestion().get(team) < tempGame.getRounds().size() - 1;
        }
    }

    public void nextQuestion(final String lobby, final String team) {
        log.info("next question for team: {}", team);
        final Game tempGame = games.get(lobby);
        if (tempGame == null) {
            return;
        }
        final int currentQuestionNumber = tempGame.getCurrentQuestion().get(team);
        tempGame.getCurrentQuestion().put(team, currentQuestionNumber + 1);
    }

    public void setWinner(final String lobby) {
        final Game tempGame = games.get(lobby);
        if (tempGame.getCorrectAnswerCount().get(TEAM_A_NAME) > tempGame.getCorrectAnswerCount().get(TEAM_B_NAME)) {
            tempGame.setWinnerTeam(TEAM_A_NAME);
            log.info("team teamA won");
        } else if (Objects.equals(tempGame.getCorrectAnswerCount().get(TEAM_A_NAME), tempGame.getCorrectAnswerCount().get(TEAM_B_NAME))) {
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
        timeUpdate = executorService.submit(() -> {
            boolean teamWon = false;
            int currentOpenGames = 1;
            while (currentOpenGames > 0) {
                log.info("update tower");
                currentOpenGames = this.games.entrySet().size();
                for (final Map.Entry<String, Game> entry : this.games.entrySet()) {
                    log.info("update tower for each game");
                    final Game game = entry.getValue();
                    game.getTowerSize().put(TEAM_A_NAME, (int) (game.getAnswerPoints().get(TEAM_A_NAME) + (game.getInitialTowerSize() - (ChronoUnit.SECONDS.between(game.getStartedGame(), LocalDateTime.now())))));
                    game.getTowerSize().put(TEAM_B_NAME, (int) (game.getAnswerPoints().get(TEAM_B_NAME) + (game.getInitialTowerSize() - (ChronoUnit.SECONDS.between(game.getStartedGame(), LocalDateTime.now())))));
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
                    final UpdateGameMessage updateGameMessage = new UpdateGameMessage(game);
                    final MessageWrapper updateLobbyMassageWrapped;
                    try {
                        updateLobbyMassageWrapped = websocketService.wrapMessage(
                            updateGameMessage,
                            Purpose.UPDATE_GAME_MESSAGE
                        );
                        simpMessagingTemplate.convertAndSend(GameService.LOBBY_TOPIC + game.getLobbyName(), updateLobbyMassageWrapped);
                    } catch (final JsonProcessingException e) {
                        log.error("could not parse the message, therefore the frontend could not be informed! error: ", e);
                    }
                    if (teamWon) {
                        log.info("remove game");
                        try {
                            gameRepository.save(this.games.get(game.getLobbyName()));
                        } catch (final Exception e) {
                            log.info("Game results were null: " + e);
                            timeUpdate.cancel(true);
                            timeUpdate = null;
                        }
                        this.games.remove(game.getLobbyName());
                        teamWon = false;
                    }
                }
                final int sleepTime = 1000;
                try {
                    Thread.sleep(1000);
                } catch (final InterruptedException e) {
                    log.error("could not sleep  {} seconds", sleepTime, e);
                }
            }
            timeUpdate = null;
        });
    }

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
