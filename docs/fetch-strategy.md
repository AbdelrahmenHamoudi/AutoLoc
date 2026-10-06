# Strategie de fetch et de cascade - AutoLoc

| Association | Fetch | Cascade | Justification |
|---|---|---|---|
| Contrat -> Paiement | LAZY | ALL + orphanRemoval | Un paiement n'existe que rattache a son contrat (composition) : supprimer le contrat supprime ses paiements, et retirer un paiement de la collection le supprime en base. LAZY car les paiements ne sont pas utiles a chaque lecture d'un contrat. |
| Agence -> Vehicule | LAZY | Aucune | Un vehicule survit a la fermeture de son agence (il peut etre reaffecte). Charger une agence ne doit pas charger toute sa flotte. |
| Agence -> Employe | LAZY | Aucune | Un employe peut etre mute dans une autre agence ; supprimer l'agence ne doit pas supprimer les employes. |
| Vehicule <-> Equipement | LAZY | Aucune | Les equipements (GPS, siege bebe...) sont partages entre plusieurs vehicules : supprimer un vehicule ne doit pas supprimer un equipement. Un Set evite les doublons et les suppressions/reinsertions inutiles de la table de jointure vehicule_equipement. |
| Client -> Reservation | LAZY | PERSIST | Une reservation creee pour un client peut etre enregistree avec lui ; pas de REMOVE pour conserver l'historique des reservations. |
| Reservation -> Vehicule | LAZY | Aucune | Le vehicule existe independamment des reservations ; la reservation ne fait que le referencer. Association unidirectionnelle. |
| Reservation <-> Contrat | LAZY | ALL (cote Reservation) | Le contrat est issu de la reservation et suit son cycle de vie : enregistrer ou supprimer la reservation propage l'operation au contrat. |
| Vehicule -> Maintenance | LAZY | PERSIST | Une maintenance planifiee peut etre enregistree avec son vehicule ; pas de REMOVE pour garder l'historique d'entretien. |

## Regle generale
- `@ManyToOne` et `@OneToOne` sont EAGER par defaut : ils sont forces en LAZY pour eviter de charger toute la chaine d'objets lies.
- `@OneToMany` et `@ManyToMany` restent LAZY (valeur par defaut).
- Une cascade REMOVE / ALL n'est utilisee que pour une composition (l'enfant ne peut pas vivre sans son parent).
- Lombok cible (@Getter/@Setter), sans @Data, pour eviter les boucles infinies dans toString/hashCode.