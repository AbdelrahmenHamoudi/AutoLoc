package tn.esprit.abdelrahmenhamoudi4cce11.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.abdelrahmenhamoudi4cce11.domain.Reservation;
import tn.esprit.abdelrahmenhamoudi4cce11.exception.ResourceNotFoundException;
import tn.esprit.abdelrahmenhamoudi4cce11.repository.IReservationRepository;

@Service
@RequiredArgsConstructor
public class ReservationServiceImpl implements IReservationService {

    private static final String RESSOURCE = "Reservation";

    private final IReservationRepository reservationRepository;

    @Override
    public Reservation create(Reservation reservation) {
        if (reservation.getIdReservation() != null) {
            throw new IllegalArgumentException("Un nouvel objet Reservation ne doit pas avoir d'identifiant");
        }
        valider(reservation);
        return reservationRepository.save(reservation);
    }

    @Override
    public Reservation findById(Long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(RESSOURCE, id));
    }

    @Override
    public List<Reservation> findAll() {
        return reservationRepository.findAll();
    }

    @Override
    public Reservation update(Long id, Reservation reservation) {
        valider(reservation);
        Reservation existant = findById(id);
        existant.setDateDebut(reservation.getDateDebut());
        existant.setDateFin(reservation.getDateFin());
        existant.setStatut(reservation.getStatut());
        return reservationRepository.save(existant);
    }

    @Override
    public void deleteById(Long id) {
        if (!reservationRepository.existsById(id)) {
            throw new ResourceNotFoundException(RESSOURCE, id);
        }
        reservationRepository.deleteById(id);
    }

    private void valider(Reservation reservation) {
        if (reservation.getDateDebut() == null || reservation.getDateFin() == null
                || reservation.getDateFin().isBefore(reservation.getDateDebut())) {
            throw new IllegalArgumentException("La date de fin de la reservation ne peut pas etre anterieure a la date de debut");
        }
    }
}
