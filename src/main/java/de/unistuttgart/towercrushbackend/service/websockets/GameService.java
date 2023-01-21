package de.unistuttgart.towercrushbackend.service.websockets;

import de.unistuttgart.towercrushbackend.data.Configuration;
import de.unistuttgart.towercrushbackend.data.Question;
import de.unistuttgart.towercrushbackend.data.websockets.*;
import de.unistuttgart.towercrushbackend.repositories.ConfigurationRepository;
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

    ExecutorService executorService =
        Executors.newFixedThreadPool(1);
    Future<?> timeUpdate;

    private final Map<String, Game> games;
    static final String LOBBY_TOPIC = "/topic/lobby/";
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
        final Set<Player> teamA = lobbyManagerService.getLobby(lobby).getTeamA();
        final Set<Player> teamB = lobbyManagerService.getLobby(lobby).getTeamB();
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
        final List<Round> rounds = new ArrayList<>(game.getRounds());
        for (final Round round : rounds) {
            if (round.getQuestion().getId().equals(question)) {
                if (team.equals("teamA")) {
                    final Set<Vote> voteToDelete = round
                        .getTeamAVotes()
                        .stream()
                        .filter(vote -> vote.getPlayer().equalsUUID(player))
                        .collect(Collectors.toSet());
                    round.getTeamAVotes().removeAll(voteToDelete);
                    round.getTeamAVotes().add(new Vote(player, answer));
                    if (round.getTeamAVotes().size() == game.getTeamA().size()) {
                        round.getTeamReadyForNextQuestion().put("teamA", true);
                    }
                } else {
                    final Set<Vote> voteToDelete = round
                        .getTeamBVotes()
                        .stream()
                        .filter(vote -> vote.getPlayer().equalsUUID(player))
                        .collect(Collectors.toSet());
                    round.getTeamBVotes().removeAll(voteToDelete);
                    round.getTeamBVotes().add(new Vote(player, answer));
                    if (round.getTeamBVotes().size() == game.getTeamB().size()) {
                        round.getTeamReadyForNextQuestion().put("teamB", true);
                    }
                }
            }
        }
        games.get(lobby).setRounds(rounds);
    }

    public void evaluateAnswers(final String lobby, final String team) {
        final Game tempGame = games.get(lobby);
        if (tempGame == null) {
            log.error("evaluate answer but game doesnt exist, maybe ended");
        }
        if (team.equals("teamA")) {
            final int currentQuestionNumber = tempGame.getCurrentQuestionTeamA();
            final Map<String, Long> counts =
                tempGame.getRounds().get(currentQuestionNumber).getTeamAVotes().stream().collect(Collectors.groupingBy(Vote::getAnswer, Collectors.counting()));
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
            tempGame.setTeamAAnswerPoints(tempGame.getTeamAAnswerPoints() + towerChange);
        } else {
            final int currentQuestionNumber = tempGame.getCurrentQuestionTeamB();
            final Map<String, Long> counts =
                tempGame.getRounds().get(currentQuestionNumber).getTeamBVotes().stream().collect(Collectors.groupingBy(Vote::getAnswer, Collectors.counting()));
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
            tempGame.setTeamBAnswerPoints(tempGame.getTeamBAnswerPoints() + towerChange);
        }
    }

    public void nextQuestion(final String lobby, final String team) {
        final Game tempGame = games.get(lobby);
        if (team.equals("teamA")) {
            final int currentQuestionNumber = tempGame.getCurrentQuestionTeamA();
            if (tempGame.getCurrentQuestionTeamA() < tempGame.getRounds().size()) {
                tempGame.setCurrentQuestionTeamA(currentQuestionNumber + 1);
            } else {
                setWinner(tempGame);
            }
        } else {
            final int currentQuestionNumber = tempGame.getCurrentQuestionTeamB();
            if (tempGame.getCurrentQuestionTeamB() < tempGame.getRounds().size()) {
                tempGame.setCurrentQuestionTeamB(currentQuestionNumber + 1);
            } else {
                setWinner(tempGame);
            }
        }
    }

    private void setWinner(final Game tempGame) {
        if (tempGame.getCorrectAnswerCount().get("teamA") > tempGame.getCorrectAnswerCount().get("teamB")) {
            tempGame.setWinnerTeam("teamA");
        } else if (Objects.equals(tempGame.getCorrectAnswerCount().get("teamA"), tempGame.getCorrectAnswerCount().get("teamB"))) {
            tempGame.setWinnerTeam("draw");
        } else {
            tempGame.setWinnerTeam("teamB");
        }
    }

    public void startTask() {
        if (timeUpdate != null) {
            return;
        }
        timeUpdate = executorService.submit(() -> {
            boolean teamWon = false;
            while (true) {
                log.debug("update tower");
                for (final Map.Entry<String, Game> entry : this.games.entrySet()) {
                    final Game game = entry.getValue();
                    game.setTeamATowerSize((int) (game.getTeamAAnswerPoints() + (game.getInitialTowerSize() - (ChronoUnit.SECONDS.between(game.getStartedGame(), LocalDateTime.now())))));
                    game.setTeamBTowerSize((int) (game.getTeamBAnswerPoints() + (game.getInitialTowerSize() - (ChronoUnit.SECONDS.between(game.getStartedGame(), LocalDateTime.now())))));
                    if (game.getTeamBTowerSize() == 0 && game.getTeamATowerSize() == 0) {
                        game.setWinnerTeam("draw");
                        teamWon = true;
                    } else if (game.getTeamBTowerSize() <= 0) {
                        game.setWinnerTeam("teamA");
                        teamWon = true;
                    } else if (game.getTeamATowerSize() <= 0) {
                        game.setWinnerTeam("teamB");
                        teamWon = true;
                    }
                    final UpdateGameMessage updateGameMessage = new UpdateGameMessage(game);
                    final MessageWrapper updateLobbyMassageWrapped = websocketService.wrapMessage(
                        updateGameMessage,
                        Purpose.UPDATE_GAME_MESSAGE
                    );
                    simpMessagingTemplate.convertAndSend(GameService.LOBBY_TOPIC + game.getLobbyName(), updateLobbyMassageWrapped);
                    if (teamWon) {
                        log.debug("delete game");
                        this.games.remove(game.getLobbyName());
                        teamWon = false;
                    }
                }
                Thread.sleep(1000);
            }
        });
    }
}
