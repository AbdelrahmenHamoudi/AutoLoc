package tn.esprit.abdelrahmenhamoudi4cce11.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.abdelrahmenhamoudi4cce11.domain.Maintenance;
import tn.esprit.abdelrahmenhamoudi4cce11.exception.ResourceNotFoundException;
import tn.esprit.abdelrahmenhamoudi4cce11.repository.IMaintenanceRepository;

@Service
@RequiredArgsConstructor
public class MaintenanceServiceImpl implements IMaintenanceService {

    private static final String RESSOURCE = "Maintenance";

    private final IMaintenanceRepository maintenanceRepository;

    @Override
    public Maintenance create(Maintenance maintenance) {
        if (maintenance.getIdMaintenance() != null) {
            throw new IllegalArgumentException("Un nouvel objet Maintenance ne doit pas avoir d'identifiant");
        }
        valider(maintenance);
        return maintenanceRepository.save(maintenance);
    }

    @Override
    public Maintenance findById(Long id) {
        return maintenanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(RESSOURCE, id));
    }

    @Override
    public List<Maintenance> findAll() {
        return maintenanceRepository.findAll();
    }

    @Override
    public Maintenance update(Long id, Maintenance maintenance) {
        valider(maintenance);
        Maintenance existant = findById(id);
        existant.setDateDebut(maintenance.getDateDebut());
        existant.setDateFin(maintenance.getDateFin());
        existant.setDescription(maintenance.getDescription());
        return maintenanceRepository.save(existant);
    }

    @Override
    public void deleteById(Long id) {
        if (!maintenanceRepository.existsById(id)) {
            throw new ResourceNotFoundException(RESSOURCE, id);
        }
        maintenanceRepository.deleteById(id);
    }

    private void valider(Maintenance maintenance) {
        if (maintenance.getDateDebut() == null) {
            throw new IllegalArgumentException("La date de debut de la maintenance est obligatoire");
        }
        if (maintenance.getDateFin() != null && maintenance.getDateFin().isBefore(maintenance.getDateDebut())) {
            throw new IllegalArgumentException("La date de fin de la maintenance ne peut pas etre anterieure a la date de debut");
        }
    }
}
