package de.unistuttgart.towercrushbackend.controller.components;

import lombok.extern.slf4j.Slf4j;

import java.security.Principal;

/**
 * This Class overrides the User implementation used for the websocket subscriptions.
 */
@Slf4j
public class User implements Principal {

    private final String userUUID;

    public User(final String userUUID) {
        this.userUUID = userUUID;
    }

    @Override
    public String getName() {
        return userUUID;
    }
}
