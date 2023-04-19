package de.unistuttgart.towercrushbackend.service.websockets;

import de.unistuttgart.towercrushbackend.WireMockConfig;
import de.unistuttgart.towercrushbackend.data.websockets.MessageWrapper;
import de.unistuttgart.towercrushbackend.data.websockets.Purpose;
import de.unistuttgart.towercrushbackend.data.websockets.UpdateGameMessage;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("test")
@EnableConfigurationProperties
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { WireMockConfig.class })
public class WebsocketServiceTest {
    private final WebsocketService websocketService = new WebsocketService();

    @Test
    void wrapMessage() throws Exception {
        final UpdateGameMessage m = new UpdateGameMessage();
        final MessageWrapper expectedResult = new MessageWrapper("{\"game\":null}", Purpose.UPDATE_GAME_MESSAGE);

        MessageWrapper result = websocketService.wrapMessage(m, Purpose.UPDATE_GAME_MESSAGE);

        Assertions.assertThat(result).usingRecursiveComparison().isEqualTo(expectedResult);
    }
}
