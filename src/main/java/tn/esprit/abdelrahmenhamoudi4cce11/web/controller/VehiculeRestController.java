package tn.esprit.abdelrahmenhamoudi4cce11.web.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tn.esprit.abdelrahmenhamoudi4cce11.domain.Vehicule;
import tn.esprit.abdelrahmenhamoudi4cce11.service.IVehiculeService;

@RestController
@RequestMapping("/vehicules")
@RequiredArgsConstructor
public class VehiculeRestController {

    private final IVehiculeService vehiculeService;

    @PostMapping("/add")
    public Vehicule add(@RequestBody Vehicule vehicule) {
        return vehiculeService.create(vehicule);
    }

    @GetMapping("/get/{idVehicule}")
    public Vehicule get(@PathVariable Long idVehicule) {
        return vehiculeService.findById(idVehicule);
    }

    @GetMapping("/all")
    public List<Vehicule> getAll() {
        return vehiculeService.findAll();
    }

    @PutMapping("/update/{idVehicule}")
    public Vehicule update(@PathVariable Long idVehicule, @RequestBody Vehicule vehicule) {
        return vehiculeService.update(idVehicule, vehicule);
    }

    @DeleteMapping("/delete/{idVehicule}")
    public void delete(@PathVariable Long idVehicule) {
        vehiculeService.deleteById(idVehicule);
    }
}
