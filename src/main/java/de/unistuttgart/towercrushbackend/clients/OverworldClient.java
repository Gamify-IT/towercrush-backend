package de.unistuttgart.towercrushbackend.clients;

import de.unistuttgart.towercrushbackend.data.KeybindingDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * This client sends a Request to the Overworld-Backend at the beginning of the game to retrieve
 * keybinding statistics for a player.
 */
@FeignClient(name = "overworldClient", url = "${overworld.url}/players")
public interface OverworldClient {
    /**
     * Retrieves the keybinding statistics for a specific player.
     *
     * @param playerId the player id, whose keybinding statistics are to be retrieved
     * @param binding the specific keybinding to retrieve statistics
     * @param accessToken the users access token
     */
    @GetMapping("/{playerId}/keybindings/{binding}")
    KeybindingDTO getKeybindingStatistic(
            @PathVariable("playerId") final String playerId,
            @PathVariable("binding") final String binding,
            @CookieValue("access_token") final String accessToken
    );
}