# Notes - Couche Repository (Atelier 3)

## Choix des interfaces

| Interface | Etend | Justification |
|---|---|---|
| IContratRepository | JpaRepository<Contrat, Long> | CRUD complet, findAll renvoie une List, saveAndFlush disponible. La suppression d'un contrat passe par delete/deleteById : la cascade ALL et l'orphanRemoval suppriment ses paiements. |
| IPaiementRepository | JpaRepository<Paiement, Long> | Utilise pour lire les paiements. Composition : la creation ou le retrait d'un paiement passe par le Contrat. |
| IAgenceRepository | JpaRepository<Agence, Long> | CRUD complet et pagination pour lister les agences. |
| IEmployeRepository | JpaRepository<Employe, Long> | CRUD complet ; getReferenceById utile pour affecter une agence sans la charger. |
| IVehiculeRepository | JpaRepository<Vehicule, Long> | CRUD complet ; tri et pagination utiles pour le catalogue (ex. tri par tarifJournalier). |
| IEquipementRepository | JpaRepository<Equipement, Long> | CRUD complet sur un referentiel d'equipements partages. |
| IClientRepository | JpaRepository<Client, Long> | CRUD complet ; pagination utile pour la liste des clients. |
| IReservationRepository | JpaRepository<Reservation, Long> | CRUD complet ; la cascade ALL vers Contrat s'applique via delete/deleteById. |
| IMaintenanceRepository | JpaRepository<Maintenance, Long> | CRUD complet pour l'historique d'entretien des vehicules. |

## A retenir
- `Iterable` vs `List` : CrudRepository.findAll() renvoie un Iterable ; JpaRepository (via ListCrudRepository) renvoie une List, plus pratique (size, get, stream).
- `save` : id null -> INSERT ; id renseigne -> SELECT puis UPDATE (merge). Utiliser l'objet retourne.
- `findById` renvoie un Optional : ne jamais appeler get() sans verifier la presence.
- `deleteById` ne leve pas d'exception si l'id n'existe pas (Spring Data 3).
- Les suppressions en lot (deleteAllInBatch, deleteAllByIdInBatch) contournent le contexte de persistance : pas de cascade ni d'orphanRemoval. A eviter sur Contrat.
- Pas d'annotation @Repository : Spring Data genere le proxy automatiquement.
- Verification : au demarrage, les logs affichent "Found 9 JPA repository interfaces".

## Anomalies SonarQube for IDE corrigees

| Anomalie | Regle / explication | Correction apportee |
|---|---|---|
| Imports avec `*` (`import jakarta.persistence.*;`, `import lombok.*;`) dans les 9 entites | java:S2208 - Wildcard imports should not be used : les imports `*` masquent les dependances reelles et peuvent creer des conflits de noms. | Remplacement par des imports explicites (Entity, Table, Id, Column, ManyToOne..., Getter, Setter, AccessLevel...). |
| Test `contextLoads()` sans assertion | java:S2699 - Tests should include assertions : un test sans assertion ne verifie rien et passe toujours. | Injection de `ApplicationContext` et ajout de `assertThat(context).isNotNull();`. |
| Methode `contextLoads()` vide | java:S1186 - Methods should not be empty : une methode vide sans commentaire est suspecte (oubli ou code mort). | Corrigee par l'ajout de l'assertion ci-dessus : la methode n'est plus vide. |