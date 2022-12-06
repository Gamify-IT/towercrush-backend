package de.unistuttgart.towercrushbackend.service.websockets;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.unistuttgart.towercrushbackend.data.websockets.Message;
import de.unistuttgart.towercrushbackend.data.websockets.MessageWrapper;
import de.unistuttgart.towercrushbackend.data.websockets.Purpose;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@Transactional
public class WebsocketService {
    public MessageWrapper wrapMessage(final Message message, final Purpose purpose) throws JsonProcessingException {
        final String jsonString = convertObjectToJson(message);
        return new MessageWrapper(jsonString, purpose);
    }

    private String convertObjectToJson(final Object object) throws JsonProcessingException {
        final ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.writeValueAsString(object);
    }
}
