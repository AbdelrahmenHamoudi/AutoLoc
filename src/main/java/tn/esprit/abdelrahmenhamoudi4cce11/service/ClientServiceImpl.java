package tn.esprit.abdelrahmenhamoudi4cce11.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.abdelrahmenhamoudi4cce11.domain.Client;
import tn.esprit.abdelrahmenhamoudi4cce11.exception.ResourceNotFoundException;
import tn.esprit.abdelrahmenhamoudi4cce11.repository.IClientRepository;

@Service
@RequiredArgsConstructor
public class ClientServiceImpl implements IClientService {

    private static final String RESSOURCE = "Client";

    private final IClientRepository clientRepository;

    @Override
    public Client create(Client client) {
        if (client.getIdClient() != null) {
            throw new IllegalArgumentException("Un nouvel objet Client ne doit pas avoir d'identifiant");
        }
        valider(client);
        return clientRepository.save(client);
    }

    @Override
    public Client findById(Long id) {
        return clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(RESSOURCE, id));
    }

    @Override
    public List<Client> findAll() {
        return clientRepository.findAll();
    }

    @Override
    public Client update(Long id, Client client) {
        valider(client);
        Client existant = findById(id);
        existant.setNom(client.getNom());
        existant.setPrenom(client.getPrenom());
        existant.setEmail(client.getEmail());
        existant.setTelephone(client.getTelephone());
        existant.setNumPermis(client.getNumPermis());
        existant.setDateInscription(client.getDateInscription());
        return clientRepository.save(existant);
    }

    @Override
    public void deleteById(Long id) {
        if (!clientRepository.existsById(id)) {
            throw new ResourceNotFoundException(RESSOURCE, id);
        }
        clientRepository.deleteById(id);
    }

    private void valider(Client client) {
        if (isBlank(client.getNom()) || isBlank(client.getEmail()) || isBlank(client.getNumPermis())) {
            throw new IllegalArgumentException("Le nom, l'e-mail et le numero de permis du client sont obligatoires");
        }
    }

    private static boolean isBlank(String valeur) {
        return valeur == null || valeur.isBlank();
    }
}
