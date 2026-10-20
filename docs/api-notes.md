# Notes - Spring MVC, Postman et SpringDoc (Atelier 5)

Package racine : `tn.esprit.abdelrahmenhamoudi4cce11` (equivalent de `tn.esprit.autoloc` dans l'enonce).
Application : `http://localhost:8081`, prefixe commun `/api` defini une seule fois
(`server.servlet.context-path=/api`). Documentation : `/api/swagger-ui.html` et `/api/v3/api-docs`
(SpringDoc `springdoc-openapi-starter-webmvc-ui` 3.0.3, derniere 3.0.x stable pour Spring Boot 4).

Package `web.controller` : 9 controleurs (un par service) + `GlobalExceptionHandler` + `OpenApiConfig`.
Chaque controleur : `@RestController`, `@RequestMapping` sans `/api`, `@RequiredArgsConstructor`,
un seul champ `private final I...Service` (aucun acces a un repository, aucun `@Autowired`).

Les captures d'écran (Postman, Swagger, SonarQube, Runner) sont dans le rapport PDF rendu sur Blackboard.

## A. Endpoints

Les 404 viennent de `ResourceNotFoundException`, les 400 de `IllegalArgumentException` (regles metier
de l'Atelier 4), traduites par `GlobalExceptionHandler` en corps JSON `{ "statut", "message", "horodatage" }`.
Le controleur ne produit jamais lui-meme ces codes.

| Controleur | Verbe et route | Service appele | Succes | Erreurs |
|---|---|---|---|---|
| ContratController | GET /api/contrats | findAll() | 200 | - |
| ContratController | GET /api/contrats/{id} | findById(id) | 200 | 404 |
| ContratController | POST /api/contrats | create(contrat) | 201 | 400 (id fourni, montant negatif) |
| ContratController | PUT /api/contrats/{id} | update(id, contrat) | 200 | 404, 400 (montant negatif) |
| ContratController | DELETE /api/contrats/{id} | deleteById(id) | 204 | 404 |
| PaiementController | GET /api/paiements | findAll() | 200 | - |
| PaiementController | GET /api/paiements/{id} | findById(id) | 200 | 404 |
| PaiementController | POST/PUT/DELETE /api/paiements... | aucun (lecture seule) | - | 405 |
| AgenceController | GET /api/agences | findAll() | 200 | - |
| AgenceController | GET /api/agences/{id} | findById(id) | 200 | 404 |
| AgenceController | POST /api/agences | create(agence) | 201 | 400 (nom ou ville vide) |
| AgenceController | PUT /api/agences/{id} | update(id, agence) | 200 | 404, 400 |
| AgenceController | DELETE /api/agences/{id} | deleteById(id) | 204 | 404 |
| EmployeController | GET /api/employes | findAll() | 200 | - |
| EmployeController | GET /api/employes/{id} | findById(id) | 200 | 404 |
| EmployeController | POST /api/employes | create(employe) | 201 | 400 (nom, prenom ou role absent) |
| EmployeController | PUT /api/employes/{id} | update(id, employe) | 200 | 404, 400 |
| EmployeController | DELETE /api/employes/{id} | deleteById(id) | 204 | 404 |
| VehiculeController | GET /api/vehicules | findAll() | 200 | - |
| VehiculeController | GET /api/vehicules/{id} | findById(id) | 200 | 404 |
| VehiculeController | POST /api/vehicules | create(vehicule) | 201 | 400 (tarif journalier absent ou negatif) |
| VehiculeController | PUT /api/vehicules/{id} | update(id, vehicule) | 200 | 404, 400 |
| VehiculeController | DELETE /api/vehicules/{id} | deleteById(id) | 204 | 404 |
| EquipementController | GET /api/equipements | findAll() | 200 | - |
| EquipementController | GET /api/equipements/{id} | findById(id) | 200 | 404 |
| EquipementController | POST /api/equipements | create(equipement) | 201 | 400 (libelle vide) |
| EquipementController | PUT /api/equipements/{id} | update(id, equipement) | 200 | 404, 400 |
| EquipementController | DELETE /api/equipements/{id} | deleteById(id) | 204 | 404 |
| ClientController | GET /api/clients | findAll() | 200 | - |
| ClientController | GET /api/clients/{id} | findById(id) | 200 | 404 |
| ClientController | POST /api/clients | create(client) | 201 | 400 (nom, e-mail ou numero de permis vide) |
| ClientController | PUT /api/clients/{id} | update(id, client) | 200 | 404, 400 |
| ClientController | DELETE /api/clients/{id} | deleteById(id) | 204 | 404 |
| ReservationController | GET /api/reservations | findAll() | 200 | - |
| ReservationController | GET /api/reservations/{id} | findById(id) | 200 | 404 |
| ReservationController | POST /api/reservations | create(reservation) | 201 | 400 (date de fin anterieure) |
| ReservationController | PUT /api/reservations/{id} | update(id, reservation) | 200 | 404, 400 |
| ReservationController | DELETE /api/reservations/{id} | deleteById(id) | 204 | 404 |
| MaintenanceController | GET /api/maintenances | findAll() | 200 | - |
| MaintenanceController | GET /api/maintenances/{id} | findById(id) | 200 | 404 |
| MaintenanceController | POST /api/maintenances | create(maintenance) | 201 | 400 (date de debut absente, date de fin anterieure) |
| MaintenanceController | PUT /api/maintenances/{id} | update(id, maintenance) | 200 | 404, 400 |
| MaintenanceController | DELETE /api/maintenances/{id} | deleteById(id) | 204 | 404 |

Adaptations au modele : l'enonce cite une « quantite negative » (Equipement) et un « cout negatif »
(Maintenance), mais ces attributs n'existent pas dans le modele ; les 400 testes sont donc le libelle vide
et la date de fin anterieure. `VehiculeRestController` et `AgenceRestController` (routes d'action `/all`,
`/get/{id}`, `/add`...) ont ete remplaces par `VehiculeController` et `AgenceController` (routes au pluriel).

## B. Annotations Jackson

### Regle appliquee (section 1.4 de l'enonce)

| Association | Annotation retenue | Justification | Erreur qui l'a declenchee |
|---|---|---|---|
| `Contrat.paiements` / `Paiement.contrat` | `@JsonManagedReference("contrat-paiements")` / `@JsonBackReference("contrat-paiements")` | Composition (`cascade = ALL`, `orphanRemoval`) : les paiements sont crees dans le JSON du contrat. A la lecture du POST, Jackson renseigne `paiement.contrat`, sinon les paiements seraient enregistres avec un contrat nul. | Exigee par l'enonce (etape 5) ; verifiee par le Runner : « 2 paiements crees », puis presents dans `/paiements`. |
| `Agence.vehicules`, `Agence.employes` | `@JsonIgnore` | Enfant cree seul, qui designe son parent par `"agence": {"idAgence": ...}` : on ignore la liste cote inverse. | `Cannot lazily initialize collection of role '...Agence.vehicules' with key '1' (no session)` sur `GET /api/agences/1`. |
| `Equipement.vehicules` | `@JsonIgnore` | Cote inverse du `@ManyToMany` ; le vehicule designe ses equipements par identifiant. | `Cannot lazily initialize collection of role '...Equipement.vehicules' with key '1' (no session)` sur `GET /api/equipements`. |
| `Client.reservations` | `@JsonIgnore` | Cote inverse ; la reservation designe son client par `"client": {"idClient": ...}`. | `Cannot lazily initialize collection of role '...Client.reservations' with key '1' (no session)` sur `GET /api/clients`. |
| `Vehicule.maintenances` | `@JsonIgnore` | Cote inverse ; la maintenance designe son vehicule par identifiant. | Pas d'erreur observee avant l'annotation : appliquee par la regle de la section 1.4. |
| `Reservation.contrat` | `@JsonIgnore` | Cote inverse du `@OneToOne` ; le contrat designe sa reservation (`"reservation": {"idReservation": ...}`). Sans elle : contrat -> reservation -> contrat a l'infini. | Pas d'erreur observee avant l'annotation : appliquee par la regle de la section 1.4. |
| `Employe.agence`, `Vehicule.agence`, `Vehicule.equipements`, `Reservation.client`, `Reservation.vehicule`, `Maintenance.vehicule`, `Contrat.reservation` | aucune | Attributs proprietaires : ils doivent etre lus dans le corps du POST (`{"agence": {"idAgence": 2}}`). Conformement a l'enonce, pas de `@JsonBackReference` sur `Reservation.client` (le POST creerait une reservation sans client). | - |

### open-in-view : erreur observee et retour a la valeur par defaut

Le projet avait `spring.jpa.open-in-view=false`. Avec ce reglage, la session Hibernate est fermee a la
sortie du service ; Jackson serialise ensuite l'entite et echoue des qu'il touche une association LAZY :

- collection : `Cannot lazily initialize collection of role '...Contrat.paiements' with key '3' (no session)` ;
- proxy `@ManyToOne` : `Could not initialize proxy [tn.esprit.abdelrahmenhamoudi4cce11.domain.Agence#2] - no session`
  sur `GET /api/employes`.

L'enonce ne mentionne `open-in-view` nulle part et ne prevoit pas cette erreur : ses solutions pour un
proxy LAZY (`@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})`, page 6, ou
`fail-on-empty-beans=false`, page 4) supposent que la session est encore ouverte pendant la serialisation,
ce qui est la valeur par defaut de Spring Boot. Un `@EntityGraph(attributePaths = "paiements")` a d'abord
ete essaye sur `IContratRepository` (open-in-view conserve a `false`), puis abandonne : il aurait fallu des
graphes imbriques dans quatre autres repositories. Choix final : `spring.jpa.open-in-view=true` (valeur
supposee par l'enonce) et `IContratRepository` remis dans son etat de l'Atelier 3.

Avec `open-in-view=true` et Spring Boot 4 (Jackson 3), l'erreur `No serializer found ... ByteBuddyInterceptor`
annoncee par l'enonce n'est **pas** apparue : Jackson serialise le proxy et ecrit
`"hibernateLazyInitializer": {}` dans le JSON (ex. `GET /api/employes` :
`"agence": {"idAgence": 2, "nom": "AutoLoc Test", ..., "hibernateLazyInitializer": {}}`). Suivant la consigne
« annotez-les quand l'erreur apparait », aucun `@JsonIgnoreProperties` ni `fail-on-empty-beans=false` n'a ete
ajoute. Tous les GET (9 listes et consultations par identifiant) repondent 200 avec des donnees en base,
y compris un contrat lie a une reservation.

### @JsonBackReference ou @JsonIgnore ?

- `@JsonBackReference` (toujours associe a un `@JsonManagedReference` de meme nom) : la propriete n'est pas
  ecrite en sortie, mais Jackson la **renseigne a la lecture** a partir du parent qui la contient. C'est ce
  qu'il faut pour une composition : les paiements du JSON du contrat recoivent automatiquement leur contrat.
- `@JsonIgnore` : la propriete est ignoree **dans les deux sens** (ni ecrite, ni lue). Adapte a une liste
  cote inverse qu'on ne remplit jamais par le JSON. Mal place sur un attribut proprietaire, il empecherait
  le POST de recevoir l'association (ex. une reservation sans client).

## C. Postman et Swagger

| Outil | Ce qu'il apporte | Exemple concret dans l'atelier |
|---|---|---|
| Postman | Tester : requetes enregistrees et rejouables, assertions `pm.test`, variables d'environnement (`baseUrl`, `contratId`...), execution de toute la collection au Runner. | Partie A : Runner 20 tests / 20 reussis sur 16 requetes. Collection complete (`docs/postman/AutoLoc.postman_collection.json`) : 80 requetes, 94 tests, dossiers par entite, cas nominaux, 404 et 400, scenario de cascade Contrat/Paiement et 405 sur `POST /api/paiements`. Les tests ont revele que `"id": 5` (corps de l'enonce) est ignore par Jackson (201) : il faut `"idContrat": 5` pour obtenir le 400. |
| Swagger UI (SpringDoc) | Documenter et explorer : contrat OpenAPI genere depuis le code, toujours synchronise, essai des endpoints sans outil externe. | Des l'ajout de la dependance, `/api/v3/api-docs` listait les endpoints de `ContratController` et `PaiementController` sans annotation (groupes `contrat-controller`, `paiement-controller`) et les schemas deduits des entites (`Contrat`, `Paiement`, `ErreurReponse`). Apres `OpenApiConfig` et `@Tag`/`@Operation`/`@ApiResponse`/`@Parameter` : titre « AutoLoc API » 1.0.0, groupes « Contrats », « Paiements »... avec resumes et codes documentes. |

Les deux sont complementaires : Swagger decrit ce que l'API promet, Postman verifie qu'elle le tient.

## D. Anomalies SonarQube for IDE (web.controller)

Analyse « Analyze with SonarQube for IDE » sur le package `web.controller` (etape 12) : **59 anomalies dans
10 fichiers**, 3 regles.

| Regle | Message affiche | Nombre | Explication | Correction ou justification |
|---|---|---|---|---|
| java:S1710 - Annotation repetitions should not be wrapped | Remove the 'ApiResponses' wrapper from this annotation group | 42 (5 par controleur CRUD, 2 dans `PaiementController`) | Depuis Java 8, une annotation `@Repeatable` peut etre repetee directement ; le conteneur `@ApiResponses({...})` n'est plus necessaire et alourdit le code. `@ApiResponse` est bien `@Repeatable(ApiResponses.class)` (swagger-annotations 2.2.52). | **Corrigee** dans les 9 controleurs : `@ApiResponse(...)` repetee sans wrapper, import `ApiResponses` supprime. Verifie dans `/api/v3/api-docs` : les 42 operations gardent leurs codes (200/201/204, 400, 404) et leurs descriptions. |
| java:S8688 - Time-based ".now()" methods should specify a ZoneId or a Clock | Explicitly specify the time zone by passing a ZoneId or a Clock to the .now() method. | 1 (`GlobalExceptionHandler`) | `LocalDateTime.now()` depend implicitement du fuseau de la machine : le choix est cache et l'horodatage n'est pas testable. | **Corrigee** : `LocalDateTime.now(ZoneId.systemDefault()).withNano(0)` (meme valeur qu'avant, fuseau desormais explicite). |
| java:S4684 - Database Operations should not be vulnerable to mass assignment | Replace this persistent entity with a simple POJO or DTO object. | 16 (parametres `@RequestBody` de `create` et `update` dans les 8 controleurs CRUD) | Le JSON du client est lie directement a une entite JPA : il peut fixer des champs qu'il ne devrait pas controler (identifiant, associations) - « mass assignment ». | **Non corrigee, volontairement** : l'enonce precise que les controleurs exposent ici directement les entites JPA, « volontaire et provisoire : le Chapitre 10 introduit les DTO » (encadre IMPORTANT, section 1.3). Les services limitent deja le risque (`create` refuse un identifiant, `update` ne recopie que les attributs simples). |

Remarque : dans Swagger UI, chaque operation affiche aussi des reponses generiques 400 « Bad Request » et
404 « Not Found » : SpringDoc les deduit automatiquement du `GlobalExceptionHandler`
(`@RestControllerAdvice` + `@ResponseStatus`). Les `@ApiResponse` declarees sur les methodes remplacent ces
descriptions par les notres (« Contrat introuvable », « Donnees invalides »).

## E. Questions de comprehension

1. **Quel composant recoit toutes les requetes, et comment retrouve-t-il la methode du controleur ?**
   Le `DispatcherServlet`, point d'entree unique de Spring MVC. Il interroge le `HandlerMapping`, qui associe
   le verbe HTTP et l'URL (sans le context-path `/api`) a une methode annotee : `@RequestMapping("/contrats")`
   sur la classe + `@GetMapping("/{id}")` sur la methode. Les arguments sont ensuite resolus
   (`@PathVariable`, `@RequestBody` converti par Jackson) et la valeur retournee est ecrite en JSON.

2. **Pourquoi ContratController depend-il de IContratService et non de ContratRepository ni de ContratServiceImpl ?**
   Le controleur reste mince : les regles metier (identifiant refuse a la creation, montant negatif,
   `ResourceNotFoundException`) sont dans le service, pas dans la couche web ; passer par le repository les
   contournerait. Dependre de l'interface plutot que de l'implementation garde un couplage faible : Spring
   peut injecter n'importe quelle implementation (ou un proxy, un mock) sans modifier le controleur.

3. **Pourquoi un POST renvoie-t-il 201 et un DELETE 204 ? Pourquoi une ressource absente donne-t-elle 404 et non 500 ?**
   201 Created signale qu'une ressource a ete creee (le corps contient l'identifiant genere) ; 204 No Content
   signale un succes sans corps a renvoyer. Ces codes sont fixes par `@ResponseStatus`. Une ressource absente
   est une erreur du client (mauvais identifiant), pas une panne du serveur : le service leve
   `ResourceNotFoundException`, que `GlobalExceptionHandler` traduit en 404 avec un corps JSON lisible.
   Sans ce gestionnaire, l'exception remonterait en 500.

4. **Pourquoi PaiementController n'expose-t-il que des GET ? Quelle reponse recoit un POST /api/paiements ?**
   Paiement est en composition avec Contrat (`cascade = ALL`, `orphanRemoval = true`) : son cycle de vie est
   gere par le contrat (creation dans le JSON du contrat, suppression en cascade), et `IPaiementService` est en
   lecture seule. Un `POST /api/paiements` recoit **405 Method Not Allowed** (verifie au Runner).

5. **Quels risques presente l'exposition directe des entites JPA ? Pourquoi Contrat.paiements utilise-t-il @JsonManagedReference alors que Agence.vehicules utilise @JsonIgnore ?**
   Risques : l'API est couplee au schema de la base (renommer une colonne casse les clients), des champs
   internes sont exposes (`"hibernateLazyInitializer": {}` observe dans nos reponses), la serialisation peut
   boucler a l'infini ou declencher des chargements LAZY (erreurs « no session » observees), et un client peut
   envoyer des champs qu'il ne devrait pas controler (identifiant, associations). Les DTO du Chapitre 10
   corrigent cela.
   `Contrat.paiements` est une composition : les paiements sont crees dans le JSON du contrat, il faut donc
   que Jackson lise la liste et rattache chaque paiement a son contrat (`@JsonManagedReference` /
   `@JsonBackReference`). Un vehicule, lui, est cree seul et designe son agence par identifiant : la liste
   `Agence.vehicules` n'est jamais remplie par le JSON, on l'ignore (`@JsonIgnore`).
