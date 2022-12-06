package de.unistuttgart.towercrushbackend.controller.components;

import lombok.extern.slf4j.Slf4j;

import java.security.Principal;

@Slf4j
public
class StompPrincipal implements Principal {

    private final String name;

    StompPrincipal(final String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }
}
