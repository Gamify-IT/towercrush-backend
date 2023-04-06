package de.unistuttgart.towercrushbackend.data.websockets;

/**
 * Enum to identify what a message is for in the frontend
 */
public enum Purpose {
    CHAT_MESSAGE,
    DEVELOPER_MESSAGE,
    UPDATE_LOBBY_MESSAGE,
    UPDATE_GAME_MESSAGE,
    JOIN_TEAM_MESSAGE,
}
