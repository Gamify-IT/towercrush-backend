package de.unistuttgart.towercrushbackend.repositories;

import de.unistuttgart.towercrushbackend.data.GameResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GameResultRepository extends JpaRepository<GameResult, Long> {}
