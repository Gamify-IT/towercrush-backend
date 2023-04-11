package de.unistuttgart.towercrushbackend;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import de.unistuttgart.gamifyit.authentificationvalidator.JWTValidatorService;
import de.unistuttgart.towercrushbackend.data.Configuration;
import de.unistuttgart.towercrushbackend.data.OverworldResultDTO;
import de.unistuttgart.towercrushbackend.data.Question;
import de.unistuttgart.towercrushbackend.repositories.ConfigurationRepository;
import java.io.IOException;
import java.util.Set;
import java.util.UUID;
import javax.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@AutoConfigureMockMvc
@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("test")
@EnableConfigurationProperties
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { WireMockConfig.class })
class GameResultControllerTest {

    private final String API_URL = "/results";

    @MockBean
    JWTValidatorService jwtValidatorService;

    Cookie cookie = new Cookie("access_token", "testToken");

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ConfigurationRepository configurationRepository;

    @Autowired
    private WireMockServer mockResultsService;

    private ObjectMapper objectMapper;

    @BeforeEach
    public void createBasicData() throws IOException {
        ResultMocks.setupMockBooksResponse(mockResultsService);
        final Question initialQuestion1 = new Question();
        initialQuestion1.setText("Are you cool?");
        initialQuestion1.setRightAnswer("Yes");
        initialQuestion1.setWrongAnswers(Set.of("No", "Maybe"));

        final Question initialQuestion2 = new Question();
        initialQuestion2.setText("Is this game cool?");
        initialQuestion2.setRightAnswer("Yes");
        initialQuestion2.setWrongAnswers(Set.of("No", "Maybe"));

        final Configuration configuration = new Configuration();
        configuration.setQuestions(Set.of(initialQuestion1, initialQuestion2));

        configurationRepository.save(configuration);

        objectMapper = new ObjectMapper();
        doNothing().when(jwtValidatorService).validateTokenOrThrow("testToken");
        when(jwtValidatorService.extractUserId("testToken")).thenReturn("testUser");
    }

    @Test
    void saveGameResult() throws Exception {
        final OverworldResultDTO overworldResultDTO = new OverworldResultDTO(
            "TOWERCRUSH",
            UUID.randomUUID(),
            100,
            "testUser"
        );

        final String bodyValue = objectMapper.writeValueAsString(overworldResultDTO);
        final MvcResult result = mvc
            .perform(post(API_URL).cookie(cookie).content(bodyValue).contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isCreated())
            .andReturn();

        final OverworldResultDTO createdGameResultDTO = objectMapper.readValue(
            result.getResponse().getContentAsString(),
            OverworldResultDTO.class
        );

        assertEquals(overworldResultDTO, createdGameResultDTO);
    }
}
