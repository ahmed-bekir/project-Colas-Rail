package esprit.project_cloasrail.repository;

import esprit.project_cloasrail.model.Station;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StationRepository extends JpaRepository<Station, String> {
}