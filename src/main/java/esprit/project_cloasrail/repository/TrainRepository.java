// src/main/java/esprit/project_cloasrail/repository/TrainRepository.java
package esprit.project_cloasrail.repository;

import esprit.project_cloasrail.model.Train;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface TrainRepository extends JpaRepository<Train, String> {
    Optional<Train> findByTrainNumber(String trainNumber);
}