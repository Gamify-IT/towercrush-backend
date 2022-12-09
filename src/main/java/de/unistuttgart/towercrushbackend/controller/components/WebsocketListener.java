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
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Component
@Slf4j
public class WebsocketListener {

    @Autowired
    LobbyManagerService lobbyManagerService;
    @Autowired
    WebsocketService websocketService;
    @Autowired
    SimpMessagingTemplate simpMessagingTemplate;
    ExecutorService executorService =
        Executors.newFixedThreadPool(1);
    final String lobbyTopic = "/topic/lobby/";
    final String developerTopic = "/topic/developer/";
    Future<?> developerInfo;
    int developerCount = 0;

    @EventListener
    private void handleSessionConnected(final SessionConnectEvent event) throws JsonProcessingException {
        final Map<String, List<String>> headerMap = (Map<String, List<String>>) event.getMessage().getHeaders().get("nativeHeaders");
        final String lobby = headerMap.get("lobby").get(0);
        final String player = headerMap.get("player").get(0);
        if (lobby.equals("developer") && player.equals("developer")) {
            handleDeveloperJoined();
        } else {
            final StompHeaderAccessor sha = StompHeaderAccessor.wrap(event.getMessage());
            final UUID playerUUID = UUID.fromString(sha.getUser().getName());
            final Player newPlayer = new Player(player, playerUUID);
            addPlayerToList(lobby, newPlayer);
            sendLobbyBroadcastPlayerJoined(lobby);
            log.info("new player joined with UUID: " + playerUUID);
        }
    }

    private void handleDeveloperJoined() {
        developerCount++;
        if (developerInfo != null) {
            simpMessagingTemplate.convertAndSend(developerTopic,
                "sending developer infos already started");
            return;
        }
        simpMessagingTemplate.convertAndSend(developerTopic,
            "Started developer infos");
        developerInfo = executorService.submit(() -> {
            while (true) {
                log.info("sending developer infos");
                final Message developerMessage = new DeveloperMessage(lobbyManagerService.getLobbies());
                final MessageWrapper developerMessageWrapped = websocketService.wrapMessage(developerMessage, Purpose.DEVELOPER_MESSAGE);
                simpMessagingTemplate.convertAndSend(developerTopic, developerMessageWrapped);
                Thread.sleep(10000);
            }
        });
    }

    private void sendLobbyBroadcastPlayerJoined(final String lobby) throws JsonProcessingException {
        final Message joinLobbyMessage = new JoinLobbyMessage(lobbyManagerService.getLobby(lobby).getPlayerNames());
        final MessageWrapper joinLobbyMessageWrapped = websocketService.wrapMessage(joinLobbyMessage, Purpose.JOIN_LOBBY_MESSAGE);
        simpMessagingTemplate.convertAndSend(lobbyTopic + lobby, joinLobbyMessageWrapped);
    }

    private void addPlayerToList(final String lobby, final Player newPlayer) {
        lobbyManagerService.createLobbyIfNotExist(lobby);
        lobbyManagerService.addPlayer(lobby, newPlayer);
    }

    @EventListener
    private void handleSessionSubscription(final SessionSubscribeEvent event) {
        log.info("new subscription: " + event.toString());
    }

    @EventListener
    private void handleSessionDisconnection(final SessionDisconnectEvent event) throws JsonProcessingException {
        log.info("Disconnected: " + event.toString());
        final StompHeaderAccessor sha = StompHeaderAccessor.wrap(event.getMessage());
        if (sha.getUser().getName().equals("developer")) {
            developerCount--;
            if (developerCount == 0) {
                developerInfo.cancel(true);
                developerInfo = null;
            }
        } else {
            final UUID playerUUID = UUID.fromString(sha.getUser().getName());
            final String lobby = lobbyManagerService.getLobbyFromPlayer(playerUUID);
            lobbyManagerService.removePlayerFromList(lobby, playerUUID);
            final Message joinLobbyMessage = new JoinLobbyMessage(lobbyManagerService.getLobby(lobby).getPlayerNames());
            final MessageWrapper joinLobbyMessageWrapped = websocketService.wrapMessage(joinLobbyMessage, Purpose.JOIN_LOBBY_MESSAGE);
            simpMessagingTemplate.convertAndSend(lobbyTopic + lobby, joinLobbyMessageWrapped);
        }
    }
}