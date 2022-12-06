package de.unistuttgart.towercrushbackend.controller.websockets;

import com.fasterxml.jackson.core.JsonProcessingException;
import de.unistuttgart.towercrushbackend.controller.components.StompPrincipal;
import de.unistuttgart.towercrushbackend.data.websockets.*;
import de.unistuttgart.towercrushbackend.service.websockets.LobbyManagerService;
import de.unistuttgart.towercrushbackend.service.websockets.WebsocketService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
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
    final String lobbyDestination = "/topic/lobbies/";


    @MessageMapping("/get/infos/on/join/{lobby}")
    public void getInfosOnJoinLobby(final SimpMessageHeaderAccessor sha, @DestinationVariable final String lobby) throws JsonProcessingException {
        log.info("Send lobby infos to newly joined player");
        final Message joinLobbyMessage = new JoinLobbyMessage(lobbyManagerService.getLobby(lobby).getPlayerNames());
        final MessageWrapper joinLobbyMessageWrapped = websocketService.wrapMessage(joinLobbyMessage, Purpose.JOIN_LOBBY_MESSAGE);
        simpMessagingTemplate.convertAndSendToUser(((StompPrincipal) sha.getHeader("simpUser")).getName(), lobbyDestination + lobby, joinLobbyMessageWrapped);
    }

    @MessageMapping("/lobby/{lobby}/join/team/{team}/player/{player}")
    public void joinTeam(@DestinationVariable final String lobby, @DestinationVariable final String team, @DestinationVariable final String player) throws JsonProcessingException {
        log.info("player '{}' joined team '{}' in lobby '{}'", player, team, lobby);
        final JoinTeamMessage joinTeamMessage = new JoinTeamMessage(team, player);
        final MessageWrapper joinTeamMessageWrapped = websocketService.wrapMessage(joinTeamMessage, Purpose.JOIN_TEAM_MESSAGE);
        simpMessagingTemplate.convertAndSend(lobbyDestination + lobby, joinTeamMessageWrapped);
    }

    @MessageMapping("/start/lobby/{lobby}")
    public void startLobby(@DestinationVariable final String lobby) {
        log.info("start lobby");
        //todo this methods will be implemented later
    }
}
