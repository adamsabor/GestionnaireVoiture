    # Gestionnaire de Flotte de Véhicules

**Projet POO Avancée — ESIEE-IT**  
Groupe : Adam Sabor

---

## Prérequis

- **Java 17+** installé ([télécharger ici](https://www.oracle.com/java/technologies/downloads/))
- Vérifier l'installation : `java -version`

---

## Lancer le projet

### Option 1 — Terminal (recommandé)

```bash
# 1. Se placer à la racine du projet
cd GestionnaireVoiture

# 2. Compiler les sources
mkdir -p out
javac -encoding UTF-8 -d out -sourcepath src $(find src -name "*.java")

# 3. Lancer l'application
java -cp out Main
```

### Option 2 — IDE (IntelliJ IDEA / Eclipse)

1. Ouvrir le projet dans l'IDE
2. Marquer le dossier `src/` comme **source root**
3. Lancer la classe `Main.java`

> Au premier lancement, des **données de démonstration** sont injectées automatiquement.  
> Les données sont sauvegardées dans `resources/flotte.ser` à chaque fermeture.

---

## Domaine métier

Application de gestion d'une flotte de véhicules d'entreprise.  
Elle permet de gérer les véhicules (légers, lourds, spéciaux), les chauffeurs, les missions de transport
et le suivi de la maintenance. Toutes les données sont sauvegardées automatiquement à la fermeture.

---

## Technologie d'interface : Swing (Option A)

Choix justifié :
- **Application autonome** : aucun serveur nécessaire, on double-clique et ça marche.
- **JTable avec TableModel personnalisé** : tri dynamique sur toutes les colonnes par clic d'en-tête.
- **SwingUtilities.invokeLater()** utilisé dans `Main.java` pour respecter l'EDT Swing.
- **Aucun code métier dans les ActionListener** : tout délègue au `GestionnaireFlotte`.

---

## Fonctionnalités implémentées

| Fonctionnalité | Détail |
|---|---|
| CRUD complet | Véhicules, chauffeurs, missions, entretiens |
| Filtrage multicritères | Marque + type + état (véhicules) ; nom + dispo + permis (chauffeurs) ; lieu + statut + type (missions) |
| Tri dynamique | Clic sur n'importe quel en-tête de colonne (croissant/décroissant) |
| Statistiques | Répartition par état, km total, coûts, top chauffeurs, alertes entretien |
| Persistance | Sérialisation Java (`resources/flotte.ser`) — sauvegarde/chargement au lancement |
| Exceptions métier | `VehiculeIndisponibleException`, `ChauffeurSansPermisException`, `MissionDejaAssigneeException` |
| Assignation de mission | Contrôle permis + disponibilité véhicule + disponibilité chauffeur |
| Données de démo | Injectées automatiquement au premier lancement |

---

## Architecture OO — ce qui est évalué

### Classes abstraites (§2.1)

- **`Entite`** — racine de tout le modèle, porte l'ID unique.
- **`Vehicule`** — porte immatriculation, km, état. Force `getTypeVehicule()` et `getCoutKilometre()`.
- **`Mission`** — porte départ/arrivée/dates. Force `getTypeMission()` et `getDureeEstimeeHeures()`.

### Interfaces (§2.2)

- **`Assignable`** — véhicule peut recevoir/terminer une mission.
- **`Maintenable`** — véhicule peut planifier/lister des entretiens.
- **`Trackable`** — mission longue peut enregistrer des positions GPS.
- **`Facturable`** — mission ou entretien peut calculer son coût.

### Générique borné (§2.3)

```java
public class Registre<T extends Entite> { ... }
```

`Registre<Vehicule>`, `Registre<Chauffeur>`, etc. dans `GestionnaireFlotte`.  
Wildcard utilisé : `Comparator<? super T>` dans `getTousTries()`.

### Collections avancées (§2.4)

| Collection | Où | Pourquoi |
|---|---|---|
| `TreeMap<String, T>` | `Registre` | Accès par ID, tri alphabétique automatique |
| `ArrayList<T>` | `VehiculeLeger`, `Chauffeur`… | Listes ordonnées (historiques) |
| `List<TypePermis>` | `Chauffeur` | Permis multiples par chauffeur |

### Streams & Lambdas (§2.5)

```java
// Filtrage multicritères — GestionnaireFlotte.filtrerVehicules()
vehicules.filtrer(v ->
    (type == null || v.getTypeVehicule().equalsIgnoreCase(type)) &&
    (etat == null || v.getEtat() == etat) &&
    (marque == null || v.getMarque().toLowerCase().contains(marque.toLowerCase()))
);

// Agrégation — GestionnaireFlotte.getStatsParEtat()
vehicules.getTous().stream()
    .collect(Collectors.groupingBy(Vehicule::getEtat, Collectors.counting()));

// Tri + limite — StatistiquesPanel.buildTopChauffeurs()
chauffeurs.stream()
    .sorted((a, b) -> Integer.compare(b.getNombreKmTotal(), a.getNombreKmTotal()))
    .limit(3).toList();
```

---

## Carte du projet — métaphore

Imagine le projet comme une **entreprise de transport**.

```
GestionnaireVoiture/
│
├── src/
│   │
│   ├── Main.java
│   │   Le portier de l'entreprise. Il ouvre la porte (lance Swing sur l'EDT)
│   │   et laisse entrer le directeur (MainFrame).
│   │
│   ├── model/                        ← LE BUREAU DES RESSOURCES HUMAINES
│   │   │   C'est ici qu'on décrit qui existe dans l'entreprise.
│   │   │   Chaque fichier est une fiche de poste ou un contrat.
│   │   │
│   │   ├── Entite.java               Carte d'identité universelle.
│   │   │                             Tout le monde (véhicule, chauffeur, entretien)
│   │   │                             a un numéro d'employé unique (l'ID).
│   │   │
│   │   ├── Vehicule.java             La fiche de poste d'un véhicule (abstraite).
│   │   │                             Elle dit "tout véhicule a une plaque et un compteur",
│   │   │                             mais ne précise pas s'il s'agit d'un camion ou d'une voiture.
│   │   │
│   │   ├── VehiculeLeger.java        La voiture de société.
│   │   │                             Elle peut partir en mission (Assignable),
│   │   │                             passer au garage (Maintenable) et
│   │   │                             être facturée (Facturable).
│   │   │
│   │   ├── VehiculeLourd.java        Le camion.
│   │   │                             Même capacités que le léger,
│   │   │                             mais exige un chauffeur avec permis C.
│   │   │
│   │   ├── VehiculeSpecial.java      L'ambulance / la dépanneuse.
│   │   │                             Ne part pas en mission standard
│   │   │                             (pas Assignable), mais passe au garage.
│   │   │
│   │   ├── Mission.java              Le bon de commande (abstrait).
│   │   │                             Il décrit "aller de A à B", mais pas
│   │   │                             si c'est pour la journée ou plusieurs jours.
│   │   │
│   │   ├── MissionCourte.java        La livraison express.
│   │   │                             Tarif fixe + coût au km. Finie dans la journée.
│   │   │
│   │   ├── MissionLongue.java        Le grand voyage.
│   │   │                             Trackable : on note la position à chaque étape.
│   │   │                             Tarif km + indemnité journalière chauffeur.
│   │   │
│   │   ├── Chauffeur.java            Le salarié au volant.
│   │   │                             Il a ses permis, son compteur km cumulé
│   │   │                             et devient indisponible dès qu'il prend le volant.
│   │   │
│   │   ├── Entretien.java            Le rendez-vous au garage.
│   │   │                             Peut être planifié (futur) ou réalisé (passé).
│   │   │                             En retard = date dépassée et pas encore fait.
│   │   │
│   │   ├── EtatVehicule.java         Le panneau d'état sur le tableau de bord :
│   │   │                             DISPONIBLE / EN_MISSION / EN_MAINTENANCE / HORS_SERVICE
│   │   │
│   │   ├── StatutMission.java        Le statut sur le bon de commande :
│   │   │                             PLANIFIEE / EN_COURS / TERMINEE / ANNULEE
│   │   │
│   │   ├── TypePermis.java           Les catégories de permis de conduire : B, C, D, CE.
│   │   │
│   │   └── interfaces/               ← LES CONTRATS DE COMPÉTENCES
│   │       │   Ce que quelqu'un SAIT FAIRE, indépendamment de ce qu'il est.
│   │       │
│   │       ├── Assignable.java       Contrat : "je peux recevoir une mission".
│   │       ├── Maintenable.java      Contrat : "je peux passer au garage".
│   │       ├── Trackable.java        Contrat : "on peut me suivre en temps réel".
│   │       └── Facturable.java       Contrat : "je génère un coût chiffrable".
│   │
│   ├── exception/                    ← LES ALARMES
│   │   │   Quand quelque chose tourne mal, une alarme est levée (checked exception).
│   │   │   Elles forcent le développeur à gérer le problème explicitement.
│   │   │
│   │   ├── VehiculeIndisponibleException.java   Alarme : véhicule non libre.
│   │   ├── ChauffeurSansPermisException.java    Alarme : permis insuffisant.
│   │   └── MissionDejaAssigneeException.java    Alarme : chauffeur déjà en route.
│   │
│   ├── util/                         ← LA BOÎTE À OUTILS
│   │   │
│   │   ├── Registre.java             Le grand classeur générique.
│   │   │                             Registre<Vehicule>, Registre<Chauffeur>…
│   │   │                             Il stocke, filtre et trie n'importe quelle
│   │   │                             collection d'entités. Cœur du système.
│   │   │
│   │   ├── DonneesFlotte.java        La boîte de transport pour la sauvegarde.
│   │   │                             Contient les 4 listes, sérialisée d'un bloc.
│   │   │
│   │   ├── Persistance.java          Le archiviste.
│   │   │                             Il sait écrire (sauvegarder) et lire (charger)
│   │   │                             la boîte de transport depuis le disque.
│   │   │
│   │   └── DonneesTest.java          Le scénario de démonstration.
│   │                                 Crée des véhicules, chauffeurs et missions
│   │                                 fictifs au premier lancement.
│   │
│   ├── controller/                   ← LA DIRECTION
│   │   │
│   │   └── GestionnaireFlotte.java   Le directeur général.
│   │                                 Il connaît tout le monde (4 Registre<T>),
│   │                                 prend toutes les décisions métier,
│   │                                 et les vues ne font que lui transmettre
│   │                                 les demandes des utilisateurs.
│   │
│   └── view/                         ← LA SALLE D'ACCUEIL (interface graphique)
│       │   Les panneaux que l'utilisateur voit et manipule.
│       │   Aucun code métier ici : tout délègue au GestionnaireFlotte.
│       │
│       ├── MainFrame.java            La façade du bâtiment.
│       │                             Contient les 5 onglets (JTabbedPane),
│       │                             charge les données et gère la fermeture.
│       │
│       ├── VehiculePanel.java        Le guichet Véhicules.
│       │                             Table + filtres (marque/type/état) + CRUD.
│       │
│       ├── ChauffeurPanel.java       Le guichet RH.
│       │                             Table + filtres (nom/dispo/permis) + CRUD.
│       │
│       ├── MissionPanel.java         Le dispatch.
│       │                             Créer, lancer, terminer, annuler une mission.
│       │
│       ├── EntretienPanel.java       Le planning garage.
│       │                             Planifier, marquer réalisé, supprimer.
│       │
│       ├── StatistiquesPanel.java    Le tableau de bord direction.
│       │                             5 sections : répartition, indicateurs, alertes,
│       │                             top chauffeurs, missions du mois.
│       │
│       ├── VehiculeTableModel.java   Le présentoir de la table Véhicules.
│       │                             Traduit les objets Java en lignes/colonnes.
│       │
│       ├── ChauffeurTableModel.java  Idem pour les chauffeurs.
│       ├── MissionTableModel.java    Idem pour les missions.
│       └── EntretienTableModel.java  Idem pour les entretiens.
│
├── resources/
│   └── flotte.ser                    (généré au premier enregistrement)
│                                     Le coffre-fort : sauvegarde binaire de toute la flotte.
│
└── out/                              (généré par javac)
    Les .class compilés — ne pas versionner.
```

---

## Points importants pour la soutenance

### Pourquoi `Vehicule` est abstraite et pas une interface ?
Parce qu'elle **porte un état** (immatriculation, km, étatVehicule) commun à tous les véhicules.
Une interface ne peut pas avoir d'état mutable. On n'instancie jamais un "Vehicule générique" —
on crée toujours un VehiculeLeger, Lourd ou Special.

### Pourquoi `Assignable` est une interface et pas une classe abstraite ?
Parce que **VehiculeLeger et VehiculeLourd peuvent être assignés, mais pas VehiculeSpecial**.
Si c'était une classe abstraite, VehiculeSpecial hériterait d'une capacité qu'il n'a pas.
L'interface permet de "coller une étiquette" uniquement à ceux qui en ont besoin.

### Pourquoi `Registre<T extends Entite>` plutôt que `List<Vehicule>` ?
Parce qu'on réutilise exactement le **même code** pour véhicules, chauffeurs, missions et entretiens.
Sans le générique, il faudrait 4 classes quasi-identiques. Le bound `extends Entite` garantit
l'accès à `getId()` et à `equals()` pour toutes les opérations de recherche/suppression.

---

## Répartition des tâches

> À compléter avec vos coéquipiers.

| Membre | Contribution |
|---|---|
| Adam Sabor | Architecture OO, modèle complet, contrôleur, interface graphique |
| … | … |
| … | … |
