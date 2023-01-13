package de.unistuttgart.towercrushbackend.service.websockets;

import de.unistuttgart.towercrushbackend.data.Configuration;
import de.unistuttgart.towercrushbackend.data.Question;
import de.unistuttgart.towercrushbackend.data.websockets.Game;
import de.unistuttgart.towercrushbackend.data.websockets.Player;
import de.unistuttgart.towercrushbackend.data.websockets.Round;
import de.unistuttgart.towercrushbackend.data.websockets.Vote;
import de.unistuttgart.towercrushbackend.repositories.ConfigurationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional
public class GameService {

    @Autowired
    LobbyManagerService lobbyManagerService;

    @Autowired
    ConfigurationRepository configurationRepository;

    private final Map<String, Game> games;

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
        final Game game = new Game(teamA, teamB, tempRounds, configurationId, 0, 0, tempRounds.size() * 10, tempRounds.size() * 10);
        if (!games.containsKey(lobby)) {
            games.put(lobby, game);
        }
    }

    public void nextQuestion(final String lobby, final String team) {
        final Game tempGame = games.get(lobby);
        if (team.equals("teamA")) {
            final int currentQuestionNumber = tempGame.getCurrentQuestionTeamA();
            if (tempGame.getCurrentQuestionTeamA() < tempGame.getRounds().size()) {
                tempGame.setCurrentQuestionTeamA(currentQuestionNumber + 1);
            }
        } else {
            final int currentQuestionNumber = tempGame.getCurrentQuestionTeamB();
            if (tempGame.getCurrentQuestionTeamB() < tempGame.getRounds().size()) {
                tempGame.setCurrentQuestionTeamB(currentQuestionNumber + 1);
            }
        }
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
}
