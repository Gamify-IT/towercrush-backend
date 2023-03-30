package de.unistuttgart.towercrushbackend.service;

import de.unistuttgart.towercrushbackend.clients.ResultClient;
import de.unistuttgart.towercrushbackend.data.OverworldResultDTO;
import de.unistuttgart.towercrushbackend.repositories.GameResultRepository;
import de.unistuttgart.towercrushbackend.repositories.QuestionRepository;
import feign.FeignException;
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
     * Send an OverworldResultDTO to the Overworld-Backend
     *
     * @param overworldResultDTO extern gameResultDTO
     * @throws IllegalArgumentException if at least one of the arguments is null
     */
    public void saveGameResult(final String accessToken, final OverworldResultDTO overworldResultDTO) {
        if (overworldResultDTO == null) {
            throw new IllegalArgumentException("overworldResultDTO or userId is null");
        }
        try {
            resultClient.submit(accessToken, overworldResultDTO);
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
}
