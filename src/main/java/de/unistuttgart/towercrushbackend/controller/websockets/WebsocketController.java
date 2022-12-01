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

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Controller
@Slf4j
public class WebsocketController {

    @Autowired
    SimpMessagingTemplate simpMessagingTemplate;
    String destination = "/topic/messages";
    String gameDestination = "/topic/games/";

    ExecutorService executorService =
        Executors.newFixedThreadPool(3);
    Map<String, Future<?>> submittedLobbies = new HashMap<>();
    @MessageMapping("/start/lobbyName/{lobbyName}")
    public void startGame(@DestinationVariable String lobbyName) {
        log.info("Started task");
        MessageWrapper startMessage = new MessageWrapper("Started game: " + lobbyName, Purpose.CHAT_MESSAGE);
        simpMessagingTemplate.convertAndSend(gameDestination + lobbyName,
            startMessage);
        submittedLobbies.put(lobbyName, executorService.submit(() -> {
            while (true) {
                MessageWrapper idleMessage = new MessageWrapper(LocalDateTime.now()
                    + ": doing game: " + lobbyName, Purpose.CHAT_MESSAGE);
                simpMessagingTemplate.convertAndSend(gameDestination + lobbyName,
                    idleMessage);
                Thread.sleep(10000);
            }
        }));
    }

    @MessageMapping("/lobbyName/{lobbyName}/team/{teamName}/player/{playerName}")
    public void joinTeam(@DestinationVariable String lobbyName, @DestinationVariable String teamName, @DestinationVariable String playerName) throws JsonProcessingException {
        log.info("join team");
        JoinTeamMessage joinTeamMessage = new JoinTeamMessage(teamName,playerName);
        String jsonString = convertObjectToJson(joinTeamMessage);
        MessageWrapper joinTeamMessageWrapped = new MessageWrapper(jsonString, Purpose.JOIN_TEAM_MESSAGE);
        simpMessagingTemplate.convertAndSend(gameDestination + lobbyName,
            joinTeamMessageWrapped);
    }

    private String convertObjectToJson(Object object) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.writeValueAsString(object);
    }

    @MessageMapping("/stop/lobbyName/{lobbyName}")
    public void stopGame(@DestinationVariable String lobbyName){
        try {
            submittedLobbies.get(lobbyName).cancel(true);
            submittedLobbies.put(lobbyName, null);
            log.info("Stopped game: " + lobbyName);
        }catch (Exception ex){
            ex.printStackTrace();
            MessageWrapper errorMessage = new MessageWrapper("Error occurred while stopping task due to: "
                + ex.getMessage(), Purpose.CHAT_MESSAGE);
            simpMessagingTemplate.convertAndSend(gameDestination + lobbyName,
                errorMessage);
        }
        MessageWrapper stopMessage = new MessageWrapper("Stopped game: " + lobbyName, Purpose.CHAT_MESSAGE);
        simpMessagingTemplate.convertAndSend(gameDestination + lobbyName,
            stopMessage);
    }

}