package tn.esprit.abdelrahmenhamoudi4cce11.service;

import java.util.List;
import tn.esprit.abdelrahmenhamoudi4cce11.domain.Client;

public interface IClientService {

    Client create(Client client);

    Client findById(Long id);

    List<Client> findAll();

    Client update(Long id, Client client);

    void deleteById(Long id);
}
