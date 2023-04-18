package de.unistuttgart.towercrushbackend.controller.websockets;

import com.fasterxml.jackson.core.JsonProcessingException;
import de.unistuttgart.towercrushbackend.data.websockets.*;
import de.unistuttgart.towercrushbackend.service.websockets.GameService;
import de.unistuttgart.towercrushbackend.service.websockets.LobbyManagerService;
import de.unistuttgart.towercrushbackend.service.websockets.WebsocketService;
import java.security.Principal;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
@Slf4j
public class WebsocketController {

    @Autowired
    SimpMessagingTemplate simpMessagingTemplate;

    @Autowired
    LobbyManagerService lobbyManagerService;

    @Autowired
    WebsocketService websocketService;

    @Autowired
    GameService gameService;

    static final String NEW_PLAYER_QUEUE = "/queue/private/messages";
    static final String LOBBY_TOPIC = "/topic/lobby/";

    /**
     * This method initialices the fronted after a user joined a lobby
     *
     * @param lobby lobby name the user wants to join to
     * @param user  user that wants to join a lobby
     * @throws JsonProcessingException if the information that should be sent could not be parsed
     */
    @MessageMapping("/get/infos/on/join/{lobby}")
    public void getInfosOnJoinLobby(@DestinationVariable final String lobby, final Principal user)
        throws JsonProcessingException {
        log.info("Send lobby infos to newly joined User: " + user.getName());
        final String sendTo = user.getName();
        final Message updateLobbyMassage = new UpdateLobbyMassage(lobbyManagerService.getLobby(lobby));
        final MessageWrapper updateLobbyMassageWrapped = websocketService.wrapMessage(
            updateLobbyMassage,
            Purpose.UPDATE_LOBBY_MESSAGE
        );
        simpMessagingTemplate.convertAndSendToUser(
            sendTo,
            WebsocketController.NEW_PLAYER_QUEUE,
            updateLobbyMassageWrapped
        );
    }

    /**
     * THis method calls the switch Player method when the user requested to join a team
     *
     * @param lobby lobby name in that the user changed his team
     * @param team  the team the user wants to join
     * @param user  user that wants to change his team
     * @throws JsonProcessingException if the information that should be sent could not be parsed
     */
    @MessageMapping("/lobby/{lobby}/join/team/{team}")
    public void joinTeam(
        @DestinationVariable final String lobby,
        @DestinationVariable final String team,
        final Principal user
    ) throws JsonProcessingException {
        log.info("player '{}' joined team '{}' in lobby '{}'", user.getName(), team, lobby);
        final UUID playerUUID = UUID.fromString(user.getName());
        final Player player = lobbyManagerService.getPlayerFromLobby(lobby, playerUUID);
        lobbyManagerService.switchPlayerToTeam(lobby, player, team);
        broadcastLobbyUpdate(lobby);
    }

    /**
     * This method calls the chnage Ready method for a user in a lobby that changed his "ready" status
     *
     * @param lobby lobby in that the user is
     * @param user  user that changed his ready status
     * @throws JsonProcessingException if the information that should be sent could not be parsed
     */
    @MessageMapping("/lobby/{lobby}/change-ready")
    public void changeReady(@DestinationVariable final String lobby, final Principal user)
        throws JsonProcessingException {
        final UUID playerUUID = UUID.fromString(user.getName());
        final Player player = lobbyManagerService.getPlayerFromLobby(lobby, playerUUID);
        log.info("lobby '{}' player '{}' changedReady", lobby, player.getPlayerName());
        lobbyManagerService.changeReady(lobby, player);
        broadcastLobbyUpdate(lobby);
    }

    /**
     * This method calls the start game method
     *
     * @param lobby lobby that started
     * @throws JsonProcessingException if the information that should be sent could not be parsed
     */
    @MessageMapping("/lobby/{lobby}/start-game")
    public void startGame(@DestinationVariable final String lobby) throws JsonProcessingException {
        log.info("lobby '{}' started", lobby);
        lobbyManagerService.startGame(lobby);
        broadcastLobbyUpdate(lobby);
    }

    /**
     * This method calls the method putVote, after a player clicked on an answer
     *
     * @param lobby    lobby name in that the team is in that the user is
     * @param team     team name in that the user is
     * @param question question the user answered
     * @param answer   answer the user voted for
     * @param user     user that voted for an answer
     * @throws JsonProcessingException if the information that should be sent could not be parsed
     */
    @MessageMapping("/lobby/{lobby}/team/{team}/question/{question}/vote/answer/{answer}")
    public void voteAnswer(
        @DestinationVariable final String lobby,
        @DestinationVariable final String team,
        @DestinationVariable final UUID question,
        @DestinationVariable final String answer,
        final Principal user
    ) throws JsonProcessingException {
        final UUID playerUUID = UUID.fromString(user.getName());
        final Player player = lobbyManagerService.getPlayerFromLobby(lobby, playerUUID);
        gameService.putVote(lobby, team, question, player, answer);
        broadcastGameUpdate(lobby);
    }

    /**
     * This method calls the create Game method
     *
     * @param lobby           the lobby should start the game
     * @param configurationId minigame configuration id
     * @throws JsonProcessingException if the information that should be sent could not be parsed
     */
    @MessageMapping("/init/Game/{lobby}/configurationId/{configurationId}")
    public void initGame(@DestinationVariable final String lobby, @DestinationVariable final UUID configurationId)
        throws JsonProcessingException {
        gameService.createGame(lobby, configurationId);
        broadcastGameUpdate(lobby);
    }

    /**
     * This method updates the question for the "team" in the "lobby"
     *
     * @param lobby lobby in that a team gets a new question
     * @param team  team in lobby that gets new question
     * @throws JsonProcessingException if the information that should be sent could not be parsed
     */
    @MessageMapping("/next/Question/{lobby}/team/{team}")
    public void nextQuestion(@DestinationVariable final String lobby, @DestinationVariable final String team)
        throws JsonProcessingException {
        gameService.evaluateAnswers(lobby, team);
        if (gameService.hasNextQuestion(lobby, team)) {
            gameService.nextQuestion(lobby, team);
            broadcastGameUpdate(lobby);
        } else {
            gameService.setWinner(lobby);
            broadcastGameUpdate(lobby);
            gameService.deleteGame(lobby);
        }
    }

    /**
     * This method sends all players in the lobby the new lobby list infos
     *
     * @param lobby lobby that gets informed
     * @throws JsonProcessingException if the information that should be sent could not be parsed
     */
    private void broadcastLobbyUpdate(final String lobby) throws JsonProcessingException {
        final Message updateLobbyMassage = new UpdateLobbyMassage(lobbyManagerService.getLobby(lobby));
        final MessageWrapper updateLobbyMassageWrapped = websocketService.wrapMessage(
            updateLobbyMassage,
            Purpose.UPDATE_LOBBY_MESSAGE
        );
        simpMessagingTemplate.convertAndSend(WebsocketController.LOBBY_TOPIC + lobby, updateLobbyMassageWrapped);
    }

    /**
     * This method sends all players in the lobby the new game infos
     *
     * @param lobby lobby that gets informed
     * @throws JsonProcessingException if the information that should be sent could not be parsed
     */
    private void broadcastGameUpdate(final String lobby) throws JsonProcessingException {
        if (gameService.getGameForLobby(lobby) == null) {
            return;
        }
        final UpdateGameMessage updateGameMessage = new UpdateGameMessage(gameService.getGameForLobby(lobby));
        final MessageWrapper updateLobbyMassageWrapped = websocketService.wrapMessage(
            updateGameMessage,
            Purpose.UPDATE_GAME_MESSAGE
        );
        simpMessagingTemplate.convertAndSend(WebsocketController.LOBBY_TOPIC + lobby, updateLobbyMassageWrapped);
    }
}
