package de.unistuttgart.towercrushbackend.controller.components;

import com.fasterxml.jackson.core.JsonProcessingException;
import de.unistuttgart.towercrushbackend.data.websockets.*;
import de.unistuttgart.towercrushbackend.service.websockets.GameService;
import de.unistuttgart.towercrushbackend.service.websockets.LobbyManagerService;
import de.unistuttgart.towercrushbackend.service.websockets.WebsocketService;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;

/**
 * The WebsocketListener handles websocket events like connections and subscriptions.
 */
@Component
@Slf4j
public class WebsocketListener {

    private static final String SHA_USER_IS_NOT_SET = "sha user is not set (null)";
    @Autowired
    LobbyManagerService lobbyManagerService;

    @Autowired
    GameService gameService;

    @Autowired
    WebsocketService websocketService;

    @Autowired
    SimpMessagingTemplate simpMessagingTemplate;

    ExecutorService executorService = Executors.newFixedThreadPool(1);
    static final String LOBBY_TOPIC = "/topic/lobby/";
    static final String DEVELOPER_TOPIC = "/topic/developer/";
    Future<?> developerTask;
    int developerCount = 0;

    /**
     * This method decides what happens if a connection is opened.
     * 1. connection is a developer
     * 2. connection is a player
     *
     * @param event event that gets triggerred if someone connects
     * @throws JsonProcessingException
     */
    @EventListener
    public void handleSessionConnected(final SessionConnectEvent event) throws JsonProcessingException {
        final Map<String, List<String>> headerMap = (Map<String, List<String>>) event
            .getMessage()
            .getHeaders()
            .get("nativeHeaders");
        if (headerMap != null) {
            final String lobby = headerMap.get("lobby").get(0);
            final String player = headerMap.get("player").get(0);
            final String developerLobbyName = "developer";
            if (lobby.equals(developerLobbyName) && player.equals(developerLobbyName)) {
                handleDeveloperJoined();
            } else {
                handlePlayerJoined(event, lobby, player);
            }
        } else {
            log.error("no \"nativeHeaders\" found");
        }
    }

    /**
     * When a player joined he will be added to the corresponding lobby
     *
     * @param event  connection opening event
     * @param lobby  lobby that the player joined
     * @param player player that joined
     * @throws JsonProcessingException if the information that should be sent could not be parsed
     */
    private void handlePlayerJoined(final SessionConnectEvent event, final String lobby, final String player)
        throws JsonProcessingException {
        final StompHeaderAccessor sha = StompHeaderAccessor.wrap(event.getMessage());
        if (sha.getUser() != null) {
            final UUID playerUUID = UUID.fromString(sha.getUser().getName());
            final Player newPlayer = new Player(player, playerUUID);
            addPlayerToList(lobby, newPlayer);
            broadcastLobbyUpdate(lobby);
            log.info("new player joined with UUID: " + playerUUID);
        } else {
            log.error(SHA_USER_IS_NOT_SET);
        }
    }

    /**
     * As long as atleast one developer joined, a service sends ever x seconds a messsage to all developers to inform them of the current lobby status
     */
    private void handleDeveloperJoined() {
        developerCount++;
        log.info("sending developer infos to single/multiple devs:" + developerCount);
        if (developerTask != null) {
            simpMessagingTemplate.convertAndSend(
                WebsocketListener.DEVELOPER_TOPIC,
                "sending developer infos already started"
            );
            return;
        }
        simpMessagingTemplate.convertAndSend(WebsocketListener.DEVELOPER_TOPIC, "Started developer infos");
        developerTask =
            executorService.submit(() -> {
                while (true) {
                    final Message developerMessage = new DeveloperMessage(lobbyManagerService.getLobbies());
                    final MessageWrapper developerMessageWrapped = websocketService.wrapMessage(
                        developerMessage,
                        Purpose.DEVELOPER_MESSAGE
                    );
                    simpMessagingTemplate.convertAndSend(WebsocketListener.DEVELOPER_TOPIC, developerMessageWrapped);
                    try {
                        Thread.sleep(1000);
                    } catch (final Exception e) {
                        throw new RuntimeException(e); //NOSONAR
                    }
                }
            });
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
        simpMessagingTemplate.convertAndSend(WebsocketListener.LOBBY_TOPIC + lobby, updateLobbyMassageWrapped);
    }

    /**
     * This method adds a player to the lobby
     *
     * @param lobby     lobby that the player joined
     * @param newPlayer the player that joined
     */
    private void addPlayerToList(final String lobby, final Player newPlayer) {
        lobbyManagerService.createLobbyIfNotExist(lobby);
        lobbyManagerService.addPlayer(lobby, newPlayer);
    }

    /**
     * This method just logs if someone subscribed to a topic/queue
     *
     * @param event SessionSubscribeEvent
     */
    @EventListener
    public void handleSessionSubscription(final SessionSubscribeEvent event) {
        log.info("new subscription: " + event.toString());
    }

    /**
     * This method decides what happens if a connection is closed
     *
     * @param event SessionDisconnectEvent
     * @throws JsonProcessingException if the information that should be sent could not be parsed
     */
    @EventListener
    public void handleSessionDisconnection(final SessionDisconnectEvent event) throws JsonProcessingException {
        log.info("Disconnected: " + event.toString());
        final StompHeaderAccessor sha = StompHeaderAccessor.wrap(event.getMessage());
        if (sha.getUser() != null) {
            if (sha.getUser().getName().equals("developer")) {
                handleDeveloperDisconnected();
            } else {
                handlePlayerDisconnected(sha);
            }
        } else {
            log.error(SHA_USER_IS_NOT_SET);
        }
    }

    /**
     * This method removes all infos that are no longer needed and informs the rest of the lobby
     *
     * @param sha header information
     * @throws JsonProcessingException if the information that should be sent could not be parsed
     */
    private void handlePlayerDisconnected(final StompHeaderAccessor sha) throws JsonProcessingException {
        if (sha.getUser() != null) {
            final UUID playerUUID = UUID.fromString(sha.getUser().getName());
            final String lobby = lobbyManagerService.getLobbyFromPlayer(playerUUID);
            gameService.removePlayerFromGame(lobby, playerUUID);
            lobbyManagerService.removePlayerFromList(lobby, playerUUID);
            if (lobbyManagerService.lobbyExists(lobby)) {
                broadcastLobbyUpdate(lobby);
            }
        } else {
            log.error(SHA_USER_IS_NOT_SET);
        }
    }

    /**
     * This method handles a developer that left the developer connection
     */
    private void handleDeveloperDisconnected() {
        developerCount--;
        if (developerCount <= 0) {
            log.info("no more devs online, stop infos");
            developerCount = 0;
            developerTask.cancel(true);
            developerTask = null;
        }
    }
}
