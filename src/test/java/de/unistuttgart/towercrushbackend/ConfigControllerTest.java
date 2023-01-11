package de.unistuttgart.towercrushbackend;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.unistuttgart.gamifyit.authentificationvalidator.JWTValidatorService;
import de.unistuttgart.towercrushbackend.data.Configuration;
import de.unistuttgart.towercrushbackend.data.ConfigurationDTO;
import de.unistuttgart.towercrushbackend.data.Question;
import de.unistuttgart.towercrushbackend.data.QuestionDTO;
import de.unistuttgart.towercrushbackend.data.mapper.ConfigurationMapper;
import de.unistuttgart.towercrushbackend.data.mapper.QuestionMapper;
import de.unistuttgart.towercrushbackend.repositories.ConfigurationRepository;
import de.unistuttgart.towercrushbackend.repositories.QuestionRepository;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import javax.servlet.http.Cookie;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@AutoConfigureMockMvc
@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ConfigControllerTest {

    private final String API_URL = "/configurations";

    @MockBean
    JWTValidatorService jwtValidatorService;

    Cookie cookie = new Cookie("access_token", "testToken");

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ConfigurationMapper configurationMapper;

    @Autowired
    private QuestionMapper questionMapper;

    @Autowired
    private ConfigurationRepository configurationRepository;

    @Autowired
    private QuestionRepository questionRepository;

    private ObjectMapper objectMapper;
    private Configuration initialConfig;
    private ConfigurationDTO initialConfigDTO;

    @BeforeEach
    public void createBasicData() {
        configurationRepository.deleteAll();
        final Question questionOne = new Question();
        questionOne.setText("Are you cool?");
        questionOne.setRightAnswer("Yes");
        questionOne.setWrongAnswers(Set.of("No", "Maybe"));

        final Question questionTwo = new Question();
        questionTwo.setText("Is this game cool?");
        questionTwo.setRightAnswer("Yes");
        questionTwo.setWrongAnswers(Set.of("No", "Maybe"));

        final Configuration configuration = new Configuration();
        configuration.setQuestions(Set.of(questionOne, questionTwo));

        initialConfig = configurationRepository.save(configuration);
        initialConfigDTO = configurationMapper.configurationToConfigurationDTO(initialConfig);

        objectMapper = new ObjectMapper();

        doNothing().when(jwtValidatorService).validateTokenOrThrow("testToken");
    }

    @AfterAll
    public void deleteBasicData() {
        configurationRepository.deleteAll();
    }

    @Test
    void getConfigurations() throws Exception {
        final MvcResult result = mvc
            .perform(get(API_URL).cookie(cookie).contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn();

        final List<ConfigurationDTO> configurations = Arrays.asList(
            objectMapper.readValue(result.getResponse().getContentAsString(), ConfigurationDTO[].class)
        );

        assertSame(1, configurations.size());
        assertTrue(initialConfigDTO.equalsContent(configurations.get(0)));
    }

    @Test
    void getSpecificConfiguration_DoesNotExist_ThrowsNotFound() throws Exception {
        mvc
            .perform(get(API_URL + "/" + UUID.randomUUID()).cookie(cookie).contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }

    @Test
    void createConfiguration() throws Exception {
        final ConfigurationDTO newCreatedConfigurationDTO = new ConfigurationDTO(
            Set.of(new QuestionDTO("Is this a new configuration?", "Yes", Set.of("Maybe", "No")))
        );
        final String bodyValue = objectMapper.writeValueAsString(newCreatedConfigurationDTO);
        final MvcResult result = mvc
            .perform(post(API_URL).cookie(cookie).content(bodyValue).contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isCreated())
            .andReturn();

        final ConfigurationDTO newCreatedConfigurationDTOResponse = objectMapper.readValue(
            result.getResponse().getContentAsString(),
            ConfigurationDTO.class
        );

        assertNotNull(newCreatedConfigurationDTOResponse.getId());
        // because question object are not equals, we have to compare the content without id
        assertSame(
            newCreatedConfigurationDTO.getQuestions().size(),
            newCreatedConfigurationDTOResponse.getQuestions().size()
        );
        for (final QuestionDTO question : newCreatedConfigurationDTO.getQuestions()) {
            assertTrue(newCreatedConfigurationDTOResponse.getQuestions().stream().anyMatch(question::equalsContent));
        }
        assertSame(2, configurationRepository.findAll().size());
    }

    @Test
    void updateConfiguration() throws Exception {
        final Set<QuestionDTO> newQuestionsDTO = Set.of(
            new QuestionDTO("Is this a new configuration?", "Yes", Set.of("Maybe", "No"))
        );
        initialConfigDTO.setQuestions(newQuestionsDTO);
        final String bodyValue = objectMapper.writeValueAsString(initialConfigDTO);
        final MvcResult result = mvc
            .perform(
                put(API_URL + "/" + initialConfig.getId())
                    .cookie(cookie)
                    .content(bodyValue)
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andReturn();

        final ConfigurationDTO updatedConfigurationDTOResponse = objectMapper.readValue(
            result.getResponse().getContentAsString(),
            ConfigurationDTO.class
        );

        // because question object are not equals, we have to compare the content without id
        assertSame(initialConfigDTO.getQuestions().size(), updatedConfigurationDTOResponse.getQuestions().size());
        for (final QuestionDTO question : initialConfigDTO.getQuestions()) {
            assertTrue(updatedConfigurationDTOResponse.getQuestions().stream().anyMatch(question::equalsContent));
        }
        assertEquals(initialConfigDTO.getId(), updatedConfigurationDTOResponse.getId());
        assertSame(1, configurationRepository.findAll().size());
    }

    @Test
    void deleteConfiguration() throws Exception {
        final MvcResult result = mvc
            .perform(
                delete(API_URL + "/" + initialConfig.getId()).cookie(cookie).contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andReturn();

        final ConfigurationDTO deletedConfigurationDTOResponse = objectMapper.readValue(
            result.getResponse().getContentAsString(),
            ConfigurationDTO.class
        );

        assertEquals(initialConfigDTO.getId(), deletedConfigurationDTOResponse.getId());
        assertTrue(initialConfigDTO.equalsContent(deletedConfigurationDTOResponse));
        assertSame(0, configurationRepository.findAll().size());
        initialConfig.getQuestions().forEach(question -> assertFalse(questionRepository.existsById(question.getId())));
    }

    @Test
    void addQuestionToExistingConfiguration() throws Exception {
        final QuestionDTO addedQuestionDTO = new QuestionDTO(
            "What is this question about?",
            "Question",
            Set.of("Nothing", "Everything")
        );

        final String bodyValue = objectMapper.writeValueAsString(addedQuestionDTO);
        final MvcResult result = mvc
            .perform(
                post(API_URL + "/" + initialConfig.getId() + "/questions")
                    .content(bodyValue)
                    .cookie(cookie)
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isCreated())
            .andReturn();

        final QuestionDTO newAddedQuestionResponse = objectMapper.readValue(
            result.getResponse().getContentAsString(),
            QuestionDTO.class
        );

        assertTrue(addedQuestionDTO.equalsContent(newAddedQuestionResponse));
    }

    @Test
    void removeQuestionFromExistingConfiguration() throws Exception {
        final QuestionDTO removedQuestionDTO = initialConfigDTO.getQuestions().stream().findFirst().get();
        assertTrue(questionRepository.existsById(removedQuestionDTO.getId()));

        final MvcResult result = mvc
            .perform(
                delete(API_URL + "/" + initialConfig.getId() + "/questions/" + removedQuestionDTO.getId())
                    .cookie(cookie)
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andReturn();

        final QuestionDTO removedQuestionDTOResult = objectMapper.readValue(
            result.getResponse().getContentAsString(),
            QuestionDTO.class
        );

        assertEquals(removedQuestionDTO.getId(), removedQuestionDTOResult.getId());
        assertTrue(removedQuestionDTO.equalsContent(removedQuestionDTOResult));
        assertSame(
            initialConfig.getQuestions().size() - 1,
            configurationRepository.findById(initialConfig.getId()).get().getQuestions().size()
        );
        assertFalse(questionRepository.existsById(removedQuestionDTO.getId()));
    }

    @Test
    void updateQuestionFromExistingConfiguration() throws Exception {
        final Question updatedQuestion = initialConfig.getQuestions().stream().findFirst().get();
        final QuestionDTO updatedQuestionDTO = questionMapper.questionToQuestionDTO(updatedQuestion);
        final String newText = "Is this a new updated question?";
        updatedQuestionDTO.setText(newText);

        final String bodyValue = objectMapper.writeValueAsString(updatedQuestionDTO);
        final MvcResult result = mvc
            .perform(
                put(API_URL + "/" + initialConfig.getId() + "/questions/" + updatedQuestion.getId())
                    .content(bodyValue)
                    .cookie(cookie)
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andReturn();

        final QuestionDTO updatedQuestionResultDTO = objectMapper.readValue(
            result.getResponse().getContentAsString(),
            QuestionDTO.class
        );

        assertTrue(updatedQuestionDTO.equalsContent(updatedQuestionResultDTO));
        assertEquals(newText, updatedQuestionResultDTO.getText());
        assertEquals(newText, questionRepository.findById(updatedQuestion.getId()).get().getText());
    }
}
