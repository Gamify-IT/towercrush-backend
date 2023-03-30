package de.unistuttgart.towercrushbackend.clients;

import de.unistuttgart.towercrushbackend.data.OverworldResultDTO;
import feign.Headers;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * This client's purpose is to send an OverworldResultDTO to the Overworld-Backend when a Player finished (won) a Game.
 */
@FeignClient(value = "resultClient", url = "${overworld.url}/internal")
public interface ResultClient {
    /**
     * Submits the resultDTO to the Overworld-Backend
     *
     * @param accessToken the users access token
     * @param resultDTO   the player submitted result, trimmed down to the data needed for the overworld
     */
    @PostMapping("/submit-game-pass")
    @Headers("Content-Type: application/json")
    void submit(@CookieValue("access_token") final String accessToken, OverworldResultDTO resultDTO);
}
