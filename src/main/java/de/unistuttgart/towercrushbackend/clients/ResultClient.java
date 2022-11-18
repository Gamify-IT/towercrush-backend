package de.unistuttgart.towercrushbackend.clients;

import de.unistuttgart.towercrushbackend.data.OverworldResultDTO;
import feign.Headers;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * This client submits OverworldResultDTOs to the overworld backend
 */
@FeignClient(value = "resultClient", url = "${overworld.url}/internal")
public interface ResultClient {
    @PostMapping("/submit-game-pass")
    @Headers("Content-Type: application/json")
    void submit(OverworldResultDTO resultDTO);
}
