package de.unistuttgart.towercrushbackend.data.mapper;

import de.unistuttgart.towercrushbackend.data.Question;
import de.unistuttgart.towercrushbackend.data.QuestionDTO;
import java.util.Set;
import org.mapstruct.Mapper;

/**
 * This mapper maps the QuestionDTO objects (used from external clients) and Question objects (used from internal code)
 */
@Mapper(componentModel = "spring")
public interface QuestionMapper {
    QuestionDTO questionToQuestionDTO(final Question question);

    Question questionDTOToQuestion(final QuestionDTO questionDTO);

    Set<Question> questionDTOsToQuestions(final Set<QuestionDTO> questionDTOs);

    Set<QuestionDTO> questionsToQuestionDTOs(final Set<Question> questions);
}
