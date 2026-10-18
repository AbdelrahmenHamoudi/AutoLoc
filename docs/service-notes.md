# Notes - Couche Service, IoC et injection de dependances (Atelier 4)

Package racine du projet : `tn.esprit.abdelrahmenhamoudi4cce11`
(equivalent de `tn.esprit.autoloc` dans l'enonce).

## A. Modes d'injection compares (etape 2)

| Critere | Constructeur (`@RequiredArgsConstructor` + `final`) | Attribut (`@Autowired` sur le champ) |
|---|---|---|
| Champ `final` possible ? | Oui | Non (Spring affecte le champ apres la construction) |
| Dependance visible dans la signature de la classe ? | Oui, dans le constructeur | Non, cachee dans un champ prive |
| Classe utilisable avec `new` hors conteneur sans dependance `null` ? | Oui : on est oblige de passer le repository (`new ContratServiceImpl(repo)`) | Non : `new ContratServiceImpl()` compile, mais `contratRepository` vaut `null` -> `NullPointerException` a l'appel |
| Signale par SonarQube for IDE ? | Non | Oui : java:S6813 "Field dependency injection should be avoided" |

Conclusion : l'injection par constructeur garantit un objet toujours complet et immuable. C'est la convention de tous les services AutoLoc.

## B. Services

| Service | Dependances injectees | Mode | Justification |
|---|---|---|---|
| ContratServiceImpl | IContratRepository | Constructeur | CRUD complet ; refuse un montant total negatif. La suppression supprime les paiements (cascade ALL + orphanRemoval sur `paiements`). |
| PaiementServiceImpl | IPaiementRepository | Constructeur | Lecture seule (findById, findAll) : un paiement est cree, modifie ou supprime via son Contrat (composition). |
| AgenceServiceImpl | IAgenceRepository | Constructeur | CRUD ; refuse une agence sans nom ou sans ville (colonnes `nullable = false`). |
| EmployeServiceImpl | IEmployeRepository | Constructeur | CRUD ; refuse un employe sans nom, prenom ou role. |
| VehiculeServiceImpl | IVehiculeRepository | Constructeur | CRUD ; refuse un tarif journalier absent ou negatif. |
| EquipementServiceImpl | IEquipementRepository | Constructeur | CRUD ; refuse un equipement sans libelle (le modele n'a pas d'attribut quantite). |
| ClientServiceImpl | IClientRepository | Constructeur | CRUD ; refuse un client sans nom, e-mail ou numero de permis. |
| ReservationServiceImpl | IReservationRepository | Constructeur | CRUD ; refuse une date de fin anterieure a la date de debut. |
| MaintenanceServiceImpl | IMaintenanceRepository | Constructeur | CRUD ; refuse une date de debut absente ou une date de fin anterieure a la date de debut (le modele n'a pas d'attribut cout). |

Regles communes a tous les services :
- `@Service` + `@RequiredArgsConstructor`, une seule dependance `private final I...Repository` typee par interface ; aucun `new`, aucun `@Autowired`.
- `create` refuse un identifiant non `null` (sinon `save` ferait un `merge` et pourrait ecraser une ligne existante).
- `findById` leve `ResourceNotFoundException` au lieu de renvoyer un `Optional` ou `null`.
- `update` charge l'existant, recopie les attributs simples (pas l'identifiant, pas les collections ni les associations : chapitre 9), puis `save`.
- `deleteById` verifie l'existence, car `deleteById` du repository ne leve aucune exception si l'id est absent.
- Les services sont sans etat : seul champ = le repository `final`.

Le doublon `IVehiculeServices` / `service.impls.VehiculeServicesImpl` a ete supprime : `VehiculeRestController` depend desormais de l'interface `IVehiculeService` (et non d'une classe concrete) et appelle `update(Long id, Vehicule vehicule)`.

## C. Messages d'erreur (etape 3)

**Message 1** - `NoSuchBeanDefinitionException` (enveloppee dans `UnsatisfiedDependencyException`).
Bean concerne : `ContratController`, qui demande un bean de type `IContratService` que le conteneur ne trouve pas.
Causes probables cote `ContratServiceImpl` :
1. l'annotation `@Service` a ete oubliee : la classe n'est pas un bean ;
2. la classe est placee hors du package racine `tn.esprit.abdelrahmenhamoudi4cce11` (ou de ses sous-packages) : le scan ne la detecte pas.
Correction : ajouter `@Service` et/ou deplacer la classe dans le package `...service`.

**Message 2** - `NoUniqueBeanDefinitionException`.
Bean concerne : `NotificationController` attend un seul bean, mais deux implementations (`emailNotificateur`, `smsNotificateur`) ont le meme type.
Deux corrections :
1. `@Primary` **sur la classe** choisie par defaut (ex. `@Primary` sur `EmailNotificateur`) ;
2. `@Qualifier("smsNotificateur")` **sur le parametre du constructeur** de `NotificationController`, pour designer explicitement le bean voulu.
(Avec Lombok `@RequiredArgsConstructor`, le `@Qualifier` doit etre recopie sur le constructeur : il faut alors ecrire le constructeur a la main ou configurer `lombok.copyableAnnotations`.)

**Message 3** - Dependance circulaire (`BeanCurrentlyInCreationException`).
`ClientServiceImpl` depend de `ReservationServiceImpl` qui depend de `ClientServiceImpl`.
Avec l'injection par constructeur, pour construire A il faut d'abord B, et pour construire B il faut d'abord A : aucun des deux ne peut etre instancie, et Spring ne peut pas fournir d'objet a moitie construit. Le cycle est donc impossible a contourner (ce qui est une bonne chose : il revele un defaut de conception).
Pistes : faire dependre les services des **repositories** et non l'un de l'autre (ex. `ReservationServiceImpl` utilise `IClientRepository`), ou extraire la responsabilite commune dans un troisieme service dont les deux dependent.

## D. Anomalies SonarQube for IDE

Anomalies reellement relevees sur `ContratServiceImpl` pendant les experiences des etapes 2 et 4.

| Regle | Message affiche | Explication | Correction apportee |
|---|---|---|---|
| java:S6813 (etape 2) - Field dependency injection should be avoided | Remove this field injection and use constructor injection instead. (ligne 15, col. 4) | `@Autowired private IContratRepository contratRepository;` : champ non `final`, dependance cachee, `null` si la classe est creee avec `new` hors conteneur. | Retour a `@RequiredArgsConstructor` + `private final IContratRepository contratRepository;`. |
| java:S112 (etape 4) | Replace generic exceptions with specific library exceptions or a custom exception. | `throw new RuntimeException("Contrat introuvable")` dans `deleteById` : une exception generique ne dit rien sur la nature de l'erreur et ne peut pas etre interceptee specifiquement. | Retour a `throw new ResourceNotFoundException(RESSOURCE, id)` (exception metier du package `exception`). |
| java:S1128 (etape 4) | Remove this unused import 'java.util.Optional' | Import ajoute sans etre utilise : bruit, et fausse indication de dependance. | Suppression de l'import. |
| java:S1128 (etape 4) | Remove this unused import 'tn.esprit.abdelrahmenhamoudi4cce11.exception.ResourceNotFoundException' | Consequence du remplacement de l'exception metier par `RuntimeException` dans `findById` et `deleteById` : plus aucune utilisation de la classe. | Retour a `ResourceNotFoundException(RESSOURCE, id)` dans les deux methodes : l'import est de nouveau utilise. |
| java:S125 (etape 4) | This block of commented-out lines of code should be removed. | `// List<Contrat> liste = contratRepository.findAll();` dans `findAll` : du code commente n'est jamais execute, vieillit mal et brouille la lecture (l'historique Git suffit). | Suppression de la ligne commentee. |

Remarque sur S112 : la regle n'est pas levee quand la `RuntimeException` est seulement creee dans la lambda de `orElseThrow` (`findById`) ; elle l'est sur une instruction `throw` explicite (`deleteById`).

Apres restauration, la constante `RESSOURCE` est de nouveau utilisee deux fois (`findById` et `deleteById`) et le panneau ne signale plus aucune de ces anomalies.

## E. Questions de comprehension

1. **Ou est le `new` de `ContratServiceImpl` et qui l'execute ?**
   Il n'apparait nulle part dans notre code : c'est le conteneur Spring (`ApplicationContext`, cree par `SpringApplication.run`) qui detecte la classe grace a `@Service` lors du scan, appelle son constructeur par reflexion et lui passe le proxy `IContratRepository`.

2. **Pourquoi un controleur dependra-t-il de `IContratService` et non de `ContratServiceImpl` ?**
   Pour dependre d'une abstraction (couplage faible) : on peut changer d'implementation (autre classe, mock pour les tests, proxy transactionnel genere par Spring) sans modifier le controleur.

3. **Pourquoi un service singleton doit-il rester sans etat ? Exemple de bug.**
   Une seule instance est partagee par toutes les requetes/threads. Si `ContratServiceImpl` avait un champ `private Contrat contratCourant;`, deux requetes simultanees l'ecraseraient : l'utilisateur A pourrait modifier ou voir le contrat de l'utilisateur B (condition de concurrence).

4. **Pourquoi `update` charge-t-il l'entite existante avant `save` ?**
   Passer directement l'objet recu a `save` ferait un `merge` qui ecrase toutes les colonnes (les attributs non envoyes deviennent `null`) et, avec `orphanRemoval = true`, une collection `paiements` vide supprimerait tous les paiements du contrat. Charger puis recopier ne modifie que les champs voulus et verifie au passage l'existence (`ResourceNotFoundException`).

5. **`@Component` vs `@Bean` ? `@Primary` vs `@Qualifier` ? Pourquoi `IPaiementService` n'a ni creation, ni modification, ni suppression ?**
   - `@Component` se pose sur **une classe** que l'on ecrit : elle est detectee par le scan. `@Bean` se pose sur **une methode** d'une classe `@Configuration` : on cree soi-meme l'objet, utile pour une classe qu'on ne peut pas annoter (bibliotheque, JDK) ou une construction particuliere.
   - `@Primary` se pose sur le bean et le designe comme **choix par defaut** quand plusieurs candidats existent ; `@Qualifier("nom")` se pose au **point d'injection** et designe explicitement le bean voulu (il l'emporte sur `@Primary`).
   - Paiement est en composition avec Contrat (`cascade = ALL`, `orphanRemoval = true`) : son cycle de vie est gere par le Contrat. Creer, modifier ou supprimer un paiement passe donc par le Contrat ; `IPaiementService` n'expose que la lecture.
