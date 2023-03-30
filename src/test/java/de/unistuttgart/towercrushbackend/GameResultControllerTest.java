package de.unistuttgart.towercrushbackend;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import de.unistuttgart.gamifyit.authentificationvalidator.JWTValidatorService;
import de.unistuttgart.towercrushbackend.data.OverworldResultDTO;
import de.unistuttgart.towercrushbackend.data.mapper.ConfigurationMapper;
import de.unistuttgart.towercrushbackend.repositories.ConfigurationRepository;
import de.unistuttgart.towercrushbackend.repositories.GameResultRepository;
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

import javax.servlet.http.Cookie;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("test")
@EnableConfigurationProperties
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {WireMockConfig.class})
class GameResultControllerTest {

    private final String API_URL = "/results";

    @MockBean
    JWTValidatorService jwtValidatorService;

    Cookie cookie = new Cookie("access_token", "testToken");

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ConfigurationMapper configurationMapper;

    @Autowired
    private ConfigurationRepository configurationRepository;

    @Autowired
    private GameResultRepository gameResultRepository;

    @Autowired
    private WireMockServer mockResultsService;

    private ObjectMapper objectMapper;
//    private Configuration initialConfig;
//    private ConfigurationDTO initialConfigDTO;
//    private Question initialQuestion1;
//    private Question initialQuestion2;

    @BeforeEach
    public void createBasicData() {
//        ResultMocks.setupMockBooksResponse(mockResultsService);
//        configurationRepository.deleteAll();
//        initialQuestion1 = new Question();
//        initialQuestion1.setText("Are you cool?");
//        initialQuestion1.setRightAnswer("Yes");
//        initialQuestion1.setWrongAnswers(Set.of("No", "Maybe"));
//
//        initialQuestion2 = new Question();
//        initialQuestion2.setText("Is this game cool?");
//        initialQuestion2.setRightAnswer("Yes");
//        initialQuestion2.setWrongAnswers(Set.of("No", "Maybe"));
//
//        final Configuration configuration = new Configuration();
//        configuration.setQuestions(Set.of(initialQuestion1, initialQuestion2));
//
//        initialConfig = configurationRepository.save(configuration);
//        initialConfigDTO = configurationMapper.configurationToConfigurationDTO(initialConfig);
//        initialConfig
//            .getQuestions()
//            .stream()
//            .filter(question -> question.getText().equals(initialQuestion1.getText()))
//            .forEach(question -> initialQuestion1 = question);
//        initialConfig
//            .getQuestions()
//            .stream()
//            .filter(question -> question.getText().equals(initialQuestion2.getText()))
//            .forEach(question -> initialQuestion2 = question);
//
        objectMapper = new ObjectMapper();
//
//        doNothing().when(jwtValidatorService).validateTokenOrThrow("testToken");
//        when(jwtValidatorService.extractUserId("testToken")).thenReturn("testUser");
    }
//
//    @AfterEach
//    void deleteBasicData() {
//        gameResultRepository.deleteAll();
//        configurationRepository.deleteAll();
//    }

    @Test
    void saveGameResult() throws Exception {
        final OverworldResultDTO overworldResultDTO = new OverworldResultDTO("TOWERCRUSH", UUID.randomUUID(), 100, UUID.randomUUID().toString());

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
