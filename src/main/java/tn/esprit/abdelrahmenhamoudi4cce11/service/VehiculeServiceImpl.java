package tn.esprit.abdelrahmenhamoudi4cce11.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.abdelrahmenhamoudi4cce11.domain.Vehicule;
import tn.esprit.abdelrahmenhamoudi4cce11.exception.ResourceNotFoundException;
import tn.esprit.abdelrahmenhamoudi4cce11.repository.IVehiculeRepository;

@Service
@RequiredArgsConstructor
public class VehiculeServiceImpl implements IVehiculeService {

    private static final String RESSOURCE = "Vehicule";

    private final IVehiculeRepository vehiculeRepository;

    @Override
    public Vehicule create(Vehicule vehicule) {
        if (vehicule.getIdVehicule() != null) {
            throw new IllegalArgumentException("Un nouvel objet Vehicule ne doit pas avoir d'identifiant");
        }
        valider(vehicule);
        return vehiculeRepository.save(vehicule);
    }

    @Override
    public Vehicule findById(Long id) {
        return vehiculeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(RESSOURCE, id));
    }

    @Override
    public List<Vehicule> findAll() {
        return vehiculeRepository.findAll();
    }

    @Override
    public Vehicule update(Long id, Vehicule vehicule) {
        valider(vehicule);
        Vehicule existant = findById(id);
        existant.setImmatriculation(vehicule.getImmatriculation());
        existant.setMarque(vehicule.getMarque());
        existant.setModele(vehicule.getModele());
        existant.setCategorie(vehicule.getCategorie());
        existant.setTarifJournalier(vehicule.getTarifJournalier());
        existant.setStatut(vehicule.getStatut());
        return vehiculeRepository.save(existant);
    }

    @Override
    public void deleteById(Long id) {
        if (!vehiculeRepository.existsById(id)) {
            throw new ResourceNotFoundException(RESSOURCE, id);
        }
        vehiculeRepository.deleteById(id);
    }

    private void valider(Vehicule vehicule) {
        if (vehicule.getTarifJournalier() == null || vehicule.getTarifJournalier().signum() < 0) {
            throw new IllegalArgumentException("Le tarif journalier du vehicule doit etre positif ou nul");
        }
    }
}
