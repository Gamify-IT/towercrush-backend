package de.unistuttgart.towercrushbackend.service.websockets;

import de.unistuttgart.towercrushbackend.data.websockets.MessageWrapper;
import de.unistuttgart.towercrushbackend.data.websockets.Purpose;
import de.unistuttgart.towercrushbackend.data.websockets.UpdateGameMessage;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


class WebsocketServiceTest {
    private WebsocketService websocketService;

    @BeforeEach
    void setUp() {
        websocketService = new WebsocketService();
    }

    @Test
    void wrapMessage() throws Exception {
        final UpdateGameMessage m = new UpdateGameMessage();
        final MessageWrapper expectedResult = new MessageWrapper("{\"game\":null}", Purpose.UPDATE_GAME_MESSAGE);

        MessageWrapper result = websocketService.wrapMessage(m, Purpose.UPDATE_GAME_MESSAGE);

        Assertions.assertThat(result).usingRecursiveComparison().isEqualTo(expectedResult);
    }
}
