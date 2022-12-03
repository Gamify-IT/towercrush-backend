package de.unistuttgart.towercrushbackend.controller.websockets;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.unistuttgart.towercrushbackend.data.websockets.JoinTeamMessage;
import de.unistuttgart.towercrushbackend.data.websockets.MessageWrapper;
import de.unistuttgart.towercrushbackend.data.websockets.Purpose;
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
    final String destination = "/topic/messages";
    final String lobbyDestination = "/topic/lobbies/";

    @MessageMapping("/lobby/{lobby}/join/team/{team}/player/{player}")
    public void joinTeam(@DestinationVariable final String lobby, @DestinationVariable final String team, @DestinationVariable final String player) throws JsonProcessingException {
        log.info("player: " + player + ", joined team: " + team + ", in lobby: " + lobby);
        final JoinTeamMessage joinTeamMessage = new JoinTeamMessage(team, player);
        final String jsonString = convertObjectToJson(joinTeamMessage);
        final MessageWrapper joinTeamMessageWrapped = new MessageWrapper(jsonString, Purpose.JOIN_TEAM_MESSAGE);
        simpMessagingTemplate.convertAndSend(lobbyDestination + lobby, joinTeamMessageWrapped);
    }

    @MessageMapping("/start/lobby/{lobby}")
    public void startLobby(@DestinationVariable final String lobby) {
        log.info("start lobby");
        //todo this methods will be implemented later
    }

    private String convertObjectToJson(final Object object) throws JsonProcessingException {
        final ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.writeValueAsString(object);
    }

}