package tn.esprit.abdelrahmenhamoudi4cce11.service;

import java.util.List;
import tn.esprit.abdelrahmenhamoudi4cce11.domain.Maintenance;

public interface IMaintenanceService {

    Maintenance create(Maintenance maintenance);

    Maintenance findById(Long id);

    List<Maintenance> findAll();

    Maintenance update(Long id, Maintenance maintenance);

    void deleteById(Long id);
}
