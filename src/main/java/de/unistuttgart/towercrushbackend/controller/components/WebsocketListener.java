package de.unistuttgart.towercrushbackend.controller.components;

import com.fasterxml.jackson.core.JsonProcessingException;
import de.unistuttgart.towercrushbackend.data.websockets.*;
import de.unistuttgart.towercrushbackend.service.websockets.LobbyManagerService;
import de.unistuttgart.towercrushbackend.service.websockets.WebsocketService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
@Slf4j
public class WebsocketListener {

    @Autowired
    LobbyManagerService lobbyManagerService;
    @Autowired
    WebsocketService websocketService;
    @Autowired
    SimpMessagingTemplate simpMessagingTemplate;
    final String topicDestination = "/topic/lobby/";

    @EventListener
    private void handleSessionConnected(final SessionConnectEvent event) throws JsonProcessingException {
        final Map<String, List<String>> headerMap = (Map<String, List<String>>) event.getMessage().getHeaders().get("nativeHeaders");
        final String lobby = headerMap.get("lobby").get(0);
        final String player = headerMap.get("player").get(0);
        final StompHeaderAccessor sha = StompHeaderAccessor.wrap(event.getMessage());
        final UUID playerUUID = UUID.fromString(sha.getUser().getName());
        log.info("new player joined with UUID: " + playerUUID);
        final Player newPlayer = new Player(player, playerUUID);
        lobbyManagerService.createLobbyIfNotExist(lobby);
        lobbyManagerService.addPlayer(lobby, newPlayer);
        final Message joinLobbyMessage = new JoinLobbyMessage(lobbyManagerService.getLobby(lobby).getPlayerNames());
        final MessageWrapper joinLobbyMessageWrapped = websocketService.wrapMessage(joinLobbyMessage, Purpose.JOIN_LOBBY_MESSAGE);
        simpMessagingTemplate.convertAndSend(topicDestination + lobby, joinLobbyMessageWrapped);
    }

    @EventListener
    private void handleSessionSubscription(final SessionSubscribeEvent event) {
        log.info("new subscription: " + event.toString());
    }
}