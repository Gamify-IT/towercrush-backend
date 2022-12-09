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
    final String newPlayerQueue = "/queue/private/messages";
    final String lobbyTopic = "/topic/lobby/";

    @MessageMapping("/get/infos/on/join/{lobby}")
    public void getInfosOnJoinLobby(@DestinationVariable final String lobby, @Header("simpSessionId") final String sessionId, final Principal user) throws JsonProcessingException {
        log.info("Send lobby infos to newly joined player, sessionID: " + sessionId + ", User: " + user.getName());
        final String sendTo = user.getName();
        final Message joinLobbyMessage = new JoinLobbyMessage(lobbyManagerService.getLobby(lobby).getPlayerNames());
        final MessageWrapper joinLobbyMessageWrapped = websocketService.wrapMessage(joinLobbyMessage, Purpose.JOIN_LOBBY_MESSAGE);
        simpMessagingTemplate.convertAndSendToUser(sendTo, newPlayerQueue, joinLobbyMessageWrapped);
    }

    @MessageMapping("/lobby/{lobby}/join/team/{team}/player/{player}")
    public void joinTeam(@DestinationVariable final String lobby, @DestinationVariable final String team, @DestinationVariable final String player) throws JsonProcessingException {
        log.info("player '{}' joined team '{}' in lobby '{}'", player, team, lobby);
        final JoinTeamMessage joinTeamMessage = new JoinTeamMessage(team, player);
        final MessageWrapper joinTeamMessageWrapped = websocketService.wrapMessage(joinTeamMessage, Purpose.JOIN_TEAM_MESSAGE);
        simpMessagingTemplate.convertAndSend(lobbyTopic + lobby, joinTeamMessageWrapped);
    }

    @MessageMapping("/lobby/{lobby}/disconnect/player/{playerUUID}")
    public void disconnect(@DestinationVariable final String lobby, @DestinationVariable final String playerUUID) throws JsonProcessingException {
        log.info("player '{}' disconnected in lobby '{}'", playerUUID, lobby);
        lobbyManagerService.removePlayerFromList(lobby, UUID.fromString(playerUUID));
        final Message joinLobbyMessage = new JoinLobbyMessage(lobbyManagerService.getLobby(lobby).getPlayerNames());
        final MessageWrapper joinLobbyMessageWrapped = websocketService.wrapMessage(joinLobbyMessage, Purpose.JOIN_LOBBY_MESSAGE);
        simpMessagingTemplate.convertAndSend(lobbyTopic + lobby, joinLobbyMessageWrapped);
    }

    @MessageMapping("/start/lobby/{lobby}")
    public void startLobby(@DestinationVariable final String lobby) {
        log.info("start lobby");
        //todo this methods will be implemented later
    }
}
