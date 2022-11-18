package de.unistuttgart.towercrushbackend.service;

import de.unistuttgart.towercrushbackend.clients.ResultClient;
import de.unistuttgart.towercrushbackend.data.*;
import de.unistuttgart.towercrushbackend.repositories.GameResultRepository;
import de.unistuttgart.towercrushbackend.repositories.QuestionRepository;
import feign.FeignException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/**
 * This service handles the logic for the GameResultController.class
 */
@Service
@Slf4j
@Transactional
public class GameResultService {

    @Autowired
    ResultClient resultClient;

    @Autowired
    GameResultRepository gameResultRepository;

    @Autowired
    QuestionRepository questionRepository;

    /**
     * Cast list of question texts to a List of Questions
     *
     * @param roundResultDTOs list of RoundResults
     * @return a list of questions
     */
    public List<RoundResult> castQuestionList(final List<RoundResultDTO> roundResultDTOs) {
        final List<RoundResult> questionList = new ArrayList<>();
        for (final RoundResultDTO roundResultDTO : roundResultDTOs) {
            final Optional<Question> questionToAdd = questionRepository.findById(roundResultDTO.getQuestionUUId());
            if (questionToAdd.isEmpty()) {
                throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    String.format("There is no question with uuid %s.", roundResultDTO.getQuestionUUId())
                );
            } else {
                final RoundResult roundResult = new RoundResult(questionToAdd.get(), roundResultDTO.getAnswer());
                questionList.add(roundResult);
            }
        }
        return questionList;
    }

    /**
     * Casts a GameResultDTO to GameResult and saves it in the Database
     *
     * @param gameResultDTO extern gameResultDTO
     * @throws IllegalArgumentException if at least one of the arguments is null
     */
    public void saveGameResult(final GameResultDTO gameResultDTO, final String userId) {
        if (gameResultDTO == null || userId == null) {
            throw new IllegalArgumentException("gameResultDTO or userId is null");
        }
        final int resultScore = calculateResultScore(
            gameResultDTO.getCorrectAnsweredQuestions().size(),
            gameResultDTO.getQuestionCount()
        );
        final OverworldResultDTO resultDTO = new OverworldResultDTO(
            "TOWERCRUSH",
            gameResultDTO.getConfigurationAsUUID(),
            resultScore,
            userId
        );
        try {
            resultClient.submit(resultDTO);
            final List<RoundResult> correctQuestions =
                this.castQuestionList(gameResultDTO.getCorrectAnsweredQuestions());
            final List<RoundResult> wrongQuestions = this.castQuestionList(gameResultDTO.getWrongAnsweredQuestions());
            final GameResult result = new GameResult(
                gameResultDTO.getQuestionCount(),
                gameResultDTO.getScore(),
                correctQuestions,
                wrongQuestions,
                gameResultDTO.getConfigurationAsUUID(),
                userId
            );
            gameResultRepository.save(result);
        } catch (final FeignException.BadGateway badGateway) {
            final String warning =
                "The Overworld backend is currently not available. The result was NOT saved. Please try again later";
            log.error(warning + badGateway);
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, warning);
        } catch (final FeignException.NotFound notFound) {
            final String warning = "The result could not be saved. Unknown User";
            log.error(warning + notFound);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, warning);
        }
    }

    /**
     * calculates the score a player made
     *
     * @param correctAnswers    correct answer count
     * @param numberOfQuestions available question count
     * @return score as int in %
     * @throws IllegalArgumentException if correctAnswers < 0 || numberOfQuestions < correctAnswers
     */
    private int calculateResultScore(final int correctAnswers, final int numberOfQuestions) {
        if (correctAnswers < 0 || numberOfQuestions < correctAnswers) {
            throw new IllegalArgumentException(
                String.format(
                    "correctAnswers (%s) or numberOfQuestions (%s) is not possible",
                    correctAnswers,
                    numberOfQuestions
                )
            );
        }
        return (int) ((100.0 * correctAnswers) / numberOfQuestions);
    }
}
