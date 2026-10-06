package tn.esprit.abdelrahmenhamoudi4cce11.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.abdelrahmenhamoudi4cce11.domain.Maintenance;

public interface IMaintenanceRepository extends JpaRepository<Maintenance, Long> {
}