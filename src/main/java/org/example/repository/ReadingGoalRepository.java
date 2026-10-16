
package org.example.repository;

import org.example.model.ReadingGoal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ReadingGoalRepository extends JpaRepository<ReadingGoal, UUID> {

    Optional<ReadingGoal> findByUser_Id(UUID userId);

    boolean existsByUser_Id(UUID userId);
}
