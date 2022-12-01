package de.unistuttgart.towercrushbackend.controller.components;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;
import org.springframework.web.socket.messaging.SessionUnsubscribeEvent;

@Component
@Slf4j
public class WebsocketListener {
    @EventListener(SessionSubscribeEvent.class)
    public void onSessionSubscribeEvent(SessionSubscribeEvent event) {
        log.info("OnSessionSubscribeEvent: " + event.toString());
        log.info("OnSessionSubscribeEvent2 : " + event.getMessage());
        log.info("OnSessionSubscribeEvent3 : " + event.getUser());
    }

    @EventListener(SessionUnsubscribeEvent.class)
    public void onSessionUnsubscribeEvent(SessionUnsubscribeEvent event){
        log.info("OnSessionSubscribeEvent: " + event.toString());
        log.info("OnSessionSubscribeEvent2 : " + event.getMessage());
        log.info("OnSessionSubscribeEvent3 : " + event.getUser());
    }
}