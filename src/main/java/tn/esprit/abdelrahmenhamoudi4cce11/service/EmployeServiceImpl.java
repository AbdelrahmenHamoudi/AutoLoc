package tn.esprit.abdelrahmenhamoudi4cce11.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.abdelrahmenhamoudi4cce11.domain.Employe;
import tn.esprit.abdelrahmenhamoudi4cce11.exception.ResourceNotFoundException;
import tn.esprit.abdelrahmenhamoudi4cce11.repository.IEmployeRepository;

@Service
@RequiredArgsConstructor
public class EmployeServiceImpl implements IEmployeService {

    private static final String RESSOURCE = "Employe";

    private final IEmployeRepository employeRepository;

    @Override
    public Employe create(Employe employe) {
        if (employe.getIdEmploye() != null) {
            throw new IllegalArgumentException("Un nouvel objet Employe ne doit pas avoir d'identifiant");
        }
        valider(employe);
        return employeRepository.save(employe);
    }

    @Override
    public Employe findById(Long id) {
        return employeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(RESSOURCE, id));
    }

    @Override
    public List<Employe> findAll() {
        return employeRepository.findAll();
    }

    @Override
    public Employe update(Long id, Employe employe) {
        valider(employe);
        Employe existant = findById(id);
        existant.setNom(employe.getNom());
        existant.setPrenom(employe.getPrenom());
        existant.setRole(employe.getRole());
        return employeRepository.save(existant);
    }

    @Override
    public void deleteById(Long id) {
        if (!employeRepository.existsById(id)) {
            throw new ResourceNotFoundException(RESSOURCE, id);
        }
        employeRepository.deleteById(id);
    }

    private void valider(Employe employe) {
        if (isBlank(employe.getNom()) || isBlank(employe.getPrenom()) || employe.getRole() == null) {
            throw new IllegalArgumentException("Le nom, le prenom et le role de l'employe sont obligatoires");
        }
    }

    private static boolean isBlank(String valeur) {
        return valeur == null || valeur.isBlank();
    }
}
