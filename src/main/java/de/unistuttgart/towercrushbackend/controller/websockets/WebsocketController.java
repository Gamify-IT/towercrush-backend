package de.unistuttgart.towercrushbackend.controller.websockets;

import com.fasterxml.jackson.core.JsonProcessingException;
import de.unistuttgart.towercrushbackend.data.websockets.*;
import de.unistuttgart.towercrushbackend.service.websockets.GameService;
import de.unistuttgart.towercrushbackend.service.websockets.LobbyManagerService;
import de.unistuttgart.towercrushbackend.service.websockets.WebsocketService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.UUID;

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

    @MessageMapping("/get/infos/on/join/{lobby}")
    public void getInfosOnJoinLobby(
        @DestinationVariable final String lobby,
        @Header("simpSessionId") final String sessionId,
        final Principal user
    ) throws JsonProcessingException {
        log.info("Send lobby infos to newly joined player, sessionID: " + sessionId + ", User: " + user.getName());
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

    @MessageMapping("/lobby/{lobby}/join/team/{team}")
    public void joinTeam(
        @DestinationVariable final String lobby,
        @DestinationVariable final String team,
        final Principal user
    ) throws JsonProcessingException {
        log.info("player '{}' joined team '{}' in lobby '{}'", user.getName(), team, lobby);
        final UUID playerUUID = UUID.fromString(user.getName());
        final Player player = lobbyManagerService.getPlayerFromLobby(lobby, playerUUID);
        if (team.equals("teamA")) {
            lobbyManagerService.switchPlayerToTeamA(lobby, player);
        } else if (team.equals("teamB")) {
            lobbyManagerService.switchPlayerToTeamB(lobby, player);
        } else {
            log.error("Team '{}' does not exist", team);
        }
        broadcastLobbyUpdate(lobby);
    }

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

        final UpdateGameMessage updateGameMessage = new UpdateGameMessage(gameService.getGameForLobby(lobby));
        final MessageWrapper updateLobbyMassageWrapped = websocketService.wrapMessage(
            updateGameMessage,
            Purpose.UPDATE_GAME_MESSAGE
        );
        simpMessagingTemplate.convertAndSend(WebsocketController.LOBBY_TOPIC + lobby, updateLobbyMassageWrapped);
    }

    @MessageMapping("/init/Game/{lobby}/configurationId/{configurationId}")
    public void initGame(
        @DestinationVariable final String lobby,
        @DestinationVariable final UUID configurationId,
        final Principal user
    ) throws JsonProcessingException {
        gameService.createGame(lobby, configurationId);
        final UpdateGameMessage updateGameMessage = new UpdateGameMessage(gameService.getGameForLobby(lobby));
        final MessageWrapper updateLobbyMassageWrapped = websocketService.wrapMessage(
            updateGameMessage,
            Purpose.UPDATE_GAME_MESSAGE
        );
        simpMessagingTemplate.convertAndSend(WebsocketController.LOBBY_TOPIC + lobby, updateLobbyMassageWrapped);
    }

    @MessageMapping("/next/Question/{lobby}/team/{team}")
    public void nextQuestion(
        @DestinationVariable final String lobby,
        @DestinationVariable final String team,
        final Principal user
    ) throws JsonProcessingException {
        gameService.nextQuestion(lobby, team);
        final UpdateGameMessage updateGameMessage = new UpdateGameMessage(gameService.getGameForLobby(lobby));
        final MessageWrapper updateLobbyMassageWrapped = websocketService.wrapMessage(
            updateGameMessage,
            Purpose.UPDATE_GAME_MESSAGE
        );
        simpMessagingTemplate.convertAndSend(WebsocketController.LOBBY_TOPIC + lobby, updateLobbyMassageWrapped);
    }

    /**
     * This method sends all players in the lobby the new lobby list infos
     *
     * @param lobby lobby that gets informed
     * @throws JsonProcessingException
     */
    private void broadcastLobbyUpdate(final String lobby) throws JsonProcessingException {
        final Message updateLobbyMassage = new UpdateLobbyMassage(lobbyManagerService.getLobby(lobby));
        final MessageWrapper updateLobbyMassageWrapped = websocketService.wrapMessage(
            updateLobbyMassage,
            Purpose.UPDATE_LOBBY_MESSAGE
        );
        simpMessagingTemplate.convertAndSend(WebsocketController.LOBBY_TOPIC + lobby, updateLobbyMassageWrapped);
    }
}
