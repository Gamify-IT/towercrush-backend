package de.unistuttgart.towercrushbackend.repositories;

import de.unistuttgart.towercrushbackend.data.websockets.Game;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface GameResultRepository extends JpaRepository<Game, UUID> {
}
