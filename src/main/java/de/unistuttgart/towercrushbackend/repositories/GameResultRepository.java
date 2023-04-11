package de.unistuttgart.towercrushbackend.repositories;

import de.unistuttgart.towercrushbackend.data.websockets.Game;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GameResultRepository extends JpaRepository<Game, UUID> {}
