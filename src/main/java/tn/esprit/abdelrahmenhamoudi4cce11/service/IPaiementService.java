package tn.esprit.abdelrahmenhamoudi4cce11.service;

import java.util.List;
import tn.esprit.abdelrahmenhamoudi4cce11.domain.Paiement;

public interface IPaiementService {

    Paiement findById(Long id);

    List<Paiement> findAll();
}
