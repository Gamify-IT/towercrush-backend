package de.unistuttgart.towercrushbackend.controller.websockets;

import com.fasterxml.jackson.core.JsonProcessingException;
import de.unistuttgart.towercrushbackend.data.websockets.*;
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
    static final String NEW_PLAYER_QUEUE = "/queue/private/messages";
    static final String LOBBY_TOPIC = "/topic/lobby/";


    @MessageMapping("/get/infos/on/join/{lobby}")
    public void getInfosOnJoinLobby(@DestinationVariable final String lobby, @Header("simpSessionId") final String sessionId, final Principal user) throws JsonProcessingException {
        log.info("Send lobby infos to newly joined player, sessionID: " + sessionId + ", User: " + user.getName());
        final String sendTo = user.getName();
        final Message updateLobbyMassage = new UpdateLobbyMassage(lobbyManagerService.getLobby(lobby));
        final MessageWrapper updateLobbyMassageWrapped = websocketService.wrapMessage(updateLobbyMassage, Purpose.UPDATE_LOBBY_MESSAGE);
        simpMessagingTemplate.convertAndSendToUser(sendTo, WebsocketController.NEW_PLAYER_QUEUE, updateLobbyMassageWrapped);
    }

    @MessageMapping("/lobby/{lobby}/join/team/{team}")
    public void joinTeam(@DestinationVariable final String lobby, @DestinationVariable final String team, final Principal user) throws JsonProcessingException {
        log.info("player '{}' joined team '{}' in lobby '{}'", user.getName(), team, lobby);
        final UUID playerUUID = UUID.fromString(user.getName());
        final Player player = lobbyManagerService.getPlayerFromLobby(lobby, playerUUID);
        if (team.equals("Alpha")) {
            lobbyManagerService.switchPlayerToTeamA(lobby, player);
        } else if (team.equals("Beta")) {
            lobbyManagerService.switchPlayerToTeamB(lobby, player);
        } else {
            log.error("Team '{}' does not exist", team);
        }
        broadcastLobbyUpdate(lobby);
    }

    @MessageMapping("/start/lobby/{lobby}")
    public void startLobby(@DestinationVariable final String lobby) {
        log.info("start lobby");
        //todo this methods will be implemented later
    }

    /**
     * This method sends all players in the lobby the new lobby list infos
     *
     * @param lobby lobby that gets informed
     * @throws JsonProcessingException
     */
    private void broadcastLobbyUpdate(final String lobby) throws JsonProcessingException {
        final Message updateLobbyMassage = new UpdateLobbyMassage(lobbyManagerService.getLobby(lobby));
        final MessageWrapper updateLobbyMassageWrapped = websocketService.wrapMessage(updateLobbyMassage, Purpose.UPDATE_LOBBY_MESSAGE);
        simpMessagingTemplate.convertAndSend(WebsocketController.LOBBY_TOPIC + lobby, updateLobbyMassageWrapped);
    }
}
