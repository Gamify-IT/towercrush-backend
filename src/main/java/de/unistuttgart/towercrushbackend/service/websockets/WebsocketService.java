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

    /**
     * This method wraps a message and the purpose of the message inta a wrapped Message
     *
     * @param message message for the fronted
     * @param purpose purpose that the fronted understand what to do
     * @return Wrapped message
     * @throws JsonProcessingException if the information that should be sent could not be parsed
     */
    public MessageWrapper wrapMessage(final Message message, final Purpose purpose) throws JsonProcessingException {
        final String jsonString = convertObjectToJson(message);
        return new MessageWrapper(jsonString, purpose);
    }

    /**
     * This method converts an object into json string
     *
     * @param object to convert
     * @return string as json
     * @throws JsonProcessingException if the information that should be sent could not be parsed
     */
    private String convertObjectToJson(final Object object) throws JsonProcessingException {
        final ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.writeValueAsString(object);
    }
}
