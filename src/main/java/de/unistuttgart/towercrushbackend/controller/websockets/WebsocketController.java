package de.unistuttgart.towercrushbackend.controller.websockets;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
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
    Future<?> submittedTask;
    Map<String, Future<?>> submittedLobbies = new HashMap<>();

    @MessageMapping("/start/globalMessages")
    public void startTask(){
        if ( submittedTask != null ){
            simpMessagingTemplate.convertAndSend(destination,
                "Task already started");
            return;
        }
        log.info("Started task");
        simpMessagingTemplate.convertAndSend(destination,
            "Started task");
        submittedTask = executorService.submit(() -> {
            while(true){
                log.info("doing messages");
                simpMessagingTemplate.convertAndSend(destination,
                    LocalDateTime.now().toString()
                        +": doing messages");
                Thread.sleep(10000);
            }
        });
    }

    @MessageMapping("/stop/globalMessages")
    @SendTo("/topic/messages")
    public String stopTask(){
        if ( submittedTask == null ){
            return "Task not running";
        }
        try {
            if(submittedTask != null){
                log.info("Running task: " + submittedTask);
            }
            submittedTask.cancel(true);
            submittedTask = null;
            log.info("Stopped task");
        }catch (Exception ex){
            ex.printStackTrace();
            return "Error occurred while stopping task due to: "
                + ex.getMessage();
        }
        return "Stopped task";
    }

    @MessageMapping("/start/GameLobby/{lobbyName}")
    public void startGame(@DestinationVariable String lobbyName) {
        log.info("Started task");
        simpMessagingTemplate.convertAndSend(gameDestination + lobbyName,
            "Started game: " + lobbyName);
        submittedLobbies.put(lobbyName, executorService.submit(() -> {
            while (true) {
                simpMessagingTemplate.convertAndSend(gameDestination + lobbyName,
                    LocalDateTime.now().toString()
                        + ": doing game: " + lobbyName);
                Thread.sleep(10000);
            }
        }));
    }

    @MessageMapping("/stop/GameLobby/{lobbyName}")
    public void stopGame(@DestinationVariable String lobbyName){
        try {
            submittedLobbies.get(lobbyName).cancel(true);
            submittedLobbies.put(lobbyName, null);
            log.info("Stopped game: " + lobbyName);
        }catch (Exception ex){
            ex.printStackTrace();
            simpMessagingTemplate.convertAndSend(gameDestination + lobbyName,
                "Error occurred while stopping task due to: "
                    + ex.getMessage());
        }
        simpMessagingTemplate.convertAndSend(gameDestination + lobbyName,
            "Stopped game: " + lobbyName);
    }
}