package controller;

import exception.*;
import model.*;
import model.interfaces.Assignable;
import model.interfaces.Facturable;
import model.interfaces.Maintenable;
import util.DonneesFlotte;
import util.Persistance;
import util.Registre;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Contrôleur central de la flotte.
 *
 * Contient quatre Registre<T> (un par type d'entité) et coordonne
 * toutes les opérations métier : CRUD, assignation de missions,
 * planification d'entretiens, calcul de statistiques.
 *
 * Les vues ne touchent pas directement aux registres :
 * elles passent toujours par ce contrôleur.
 */
public class GestionnaireFlotte {

    // Registres génériques — type borné : T extends Entite
    private final Registre<Vehicule>  vehicules;
    private final Registre<Mission>   missions;
    private final Registre<Chauffeur> chauffeurs;
    private final Registre<Entretien> entretiens;

    public GestionnaireFlotte() {
        this.vehicules  = new Registre<>();
        this.missions   = new Registre<>();
        this.chauffeurs = new Registre<>();
        this.entretiens = new Registre<>();
    }

    // ================================================================
    //  VÉHICULES
    // ================================================================

    public void ajouterVehicule(Vehicule v)         { vehicules.ajouter(v); }

    /**
     * Suppression sécurisée : refuse si le véhicule est en mission.
     * @return false si la suppression est bloquée, true sinon
     */
    public boolean supprimerVehicule(String id) {
        return vehicules.trouverParId(id)
            .filter(v -> v.getEtat() != EtatVehicule.EN_MISSION)
            .map(v -> vehicules.supprimer(id))
            .orElse(false);
    }

    public Optional<Vehicule> trouverVehicule(String id) { return vehicules.trouverParId(id); }

    public List<Vehicule> getTousLesVehicules()     { return vehicules.getTous(); }

    /**
     * Filtrage multicritères des véhicules via Stream.
     * Chaque critère null/vide est ignoré (= "tous").
     */
    public List<Vehicule> filtrerVehicules(String type, EtatVehicule etat, String marque) {
        return vehicules.filtrer(v ->
            (type   == null || type.isBlank()   || v.getTypeVehicule().equalsIgnoreCase(type)) &&
            (etat   == null                     || v.getEtat() == etat) &&
            (marque == null || marque.isBlank() || v.getMarque().toLowerCase().contains(marque.toLowerCase()))
        );
    }

    /** Véhicules triés par kilométrage décroissant (le plus usé en premier). */
    public List<Vehicule> getVehiculesTrisParKm() {
        return vehicules.getTousTries(Comparator.comparingInt(Vehicule::getKilometrage).reversed());
    }

    // ================================================================
    //  CHAUFFEURS
    // ================================================================

    public void ajouterChauffeur(Chauffeur c)       { chauffeurs.ajouter(c); }

    /** Refuse la suppression si le chauffeur est en mission. */
    public boolean supprimerChauffeur(String id) {
        return chauffeurs.trouverParId(id)
            .filter(Chauffeur::isDisponible)
            .map(c -> chauffeurs.supprimer(id))
            .orElse(false);
    }

    public Optional<Chauffeur> trouverChauffeur(String id) { return chauffeurs.trouverParId(id); }

    public List<Chauffeur> getTousLesChauffeurs()   { return chauffeurs.getTous(); }

    /**
     * Filtre les chauffeurs par disponibilité, permis et/ou nom.
     * Tous les critères sont optionnels (null = ignorer le critère).
     */
    public List<Chauffeur> filtrerChauffeurs(Boolean disponible, TypePermis permis, String nom) {
        return chauffeurs.filtrer(c ->
            (disponible == null || c.isDisponible() == disponible) &&
            (permis     == null || c.aLePermis(permis)) &&
            (nom        == null || nom.isBlank() || c.getNomComplet().toLowerCase().contains(nom.toLowerCase()))
        );
    }

    // ================================================================
    //  MISSIONS
    // ================================================================

    public void ajouterMission(Mission m)           { missions.ajouter(m); }

    /** Refuse la suppression si la mission est en cours. */
    public boolean supprimerMission(String id) {
        return missions.trouverParId(id)
            .filter(m -> m.getStatut() != StatutMission.EN_COURS)
            .map(m -> missions.supprimer(id))
            .orElse(false);
    }

    public Optional<Mission> trouverMission(String id) { return missions.trouverParId(id); }

    public List<Mission> getToutesLesMissions()     { return missions.getTous(); }

    /** Filtre les missions par statut, lieu (départ ou arrivée) et type. */
    public List<Mission> filtrerMissions(StatutMission statut, String lieu, String type) {
        return missions.filtrer(m ->
            (statut == null || m.getStatut() == statut) &&
            (lieu   == null || lieu.isBlank() ||
                m.getLieuDepart().toLowerCase().contains(lieu.toLowerCase()) ||
                m.getLieuArrivee().toLowerCase().contains(lieu.toLowerCase())) &&
            (type   == null || type.isBlank() || m.getTypeMission().equalsIgnoreCase(type))
        );
    }

    /**
     * Affecte un véhicule et un chauffeur à une mission.
     *
     * Vérifie :
     *  1. Le chauffeur est disponible
     *  2. Le chauffeur a le bon permis selon le gabarit du véhicule
     *  3. Le véhicule est disponible (Assignable)
     *
     * @throws MissionDejaAssigneeException   si le chauffeur est déjà en mission
     * @throws ChauffeurSansPermisException   si le permis est insuffisant
     * @throws VehiculeIndisponibleException  si le véhicule n'est pas libre
     */
    public void assignerMission(Mission mission, Vehicule vehicule, Chauffeur chauffeur)
            throws MissionDejaAssigneeException, ChauffeurSansPermisException, VehiculeIndisponibleException {

        if (!chauffeur.isDisponible()) {
            throw new MissionDejaAssigneeException(
                chauffeur.getNomComplet() + " est déjà en mission."
            );
        }

        // Contrôle du permis selon le type de véhicule
        if (vehicule instanceof VehiculeLourd
                && !chauffeur.aLePermis(TypePermis.C)
                && !chauffeur.aLePermis(TypePermis.CE)) {
            throw new ChauffeurSansPermisException(chauffeur.getNomComplet(), TypePermis.C);
        }

        if (!(vehicule instanceof Assignable assignable)) {
            throw new VehiculeIndisponibleException("Ce type de véhicule ne peut pas être assigné.");
        }

        assignable.assignerMission(mission);
        mission.setVehiculeAssocie(vehicule);
        mission.setChauffeurAssocie(chauffeur);
        mission.setStatut(StatutMission.EN_COURS);
        chauffeur.prendreEnCharge(mission);
    }

    /** Termine une mission, libère le véhicule et le chauffeur. */
    public void terminerMission(String missionId) {
        missions.trouverParId(missionId).ifPresent(m -> {
            m.setStatut(StatutMission.TERMINEE);
            if (m.getVehiculeAssocie() instanceof Assignable a) a.terminerMission();
            if (m.getChauffeurAssocie() != null) {
                m.getChauffeurAssocie().finirMission((int) m.getDistanceKm());
            }
        });
    }

    /** Annule une mission planifiée et libère les ressources. */
    public void annulerMission(String missionId) {
        missions.trouverParId(missionId).ifPresent(m -> {
            if (m.getStatut() == StatutMission.EN_COURS) {
                if (m.getVehiculeAssocie() instanceof Assignable a) a.terminerMission();
                if (m.getChauffeurAssocie() != null) m.getChauffeurAssocie().setDisponible(true);
            }
            m.setStatut(StatutMission.ANNULEE);
        });
    }

    // ================================================================
    //  ENTRETIENS
    // ================================================================

    public void ajouterEntretien(Entretien e) {
        entretiens.ajouter(e);
        // Propage l'entretien au véhicule concerné via l'interface Maintenable
        vehicules.trouverParId(e.getVehiculeId()).ifPresent(v -> {
            if (v instanceof Maintenable maintenable) {
                maintenable.ajouterEntretien(e);
                // Met le véhicule en maintenance si l'entretien n'est pas encore réalisé
                if (!e.estRealise() && v.getEtat() == EtatVehicule.DISPONIBLE) {
                    v.setEtat(EtatVehicule.EN_MAINTENANCE);
                }
            }
        });
    }

    public boolean supprimerEntretien(String id)    { return entretiens.supprimer(id); }

    public Optional<Entretien> trouverEntretien(String id) { return entretiens.trouverParId(id); }

    public List<Entretien> getTousLesEntretiens()   { return entretiens.getTous(); }

    public List<Entretien> getEntretiensEnRetard() {
        return entretiens.filtrer(Entretien::estEnRetard);
    }

    /** Liste les véhicules dont le kilométrage dépasse le seuil d'entretien. */
    public List<Vehicule> getVehiculesSansEntretienRecent() {
        return vehicules.filtrer(v -> v instanceof Maintenable m && m.necessiteEntretien());
    }

    // ================================================================
    //  STATISTIQUES (utilise l'API Stream)
    // ================================================================

    /** Répartition du nombre de véhicules par état. */
    public Map<EtatVehicule, Long> getStatsParEtat() {
        return vehicules.getTous().stream()
            .collect(Collectors.groupingBy(Vehicule::getEtat, Collectors.counting()));
    }

    /** Kilométrage total cumulé de toute la flotte. */
    public int getKilometrageTotal() {
        return vehicules.getTous().stream()
            .mapToInt(Vehicule::getKilometrage)
            .sum();
    }

    /** Coût total des missions terminées facturables. */
    public double getCoutTotalMissions() {
        return missions.getTous().stream()
            .filter(m -> m.getStatut() == StatutMission.TERMINEE)
            .filter(m -> m instanceof Facturable)
            .mapToDouble(m -> ((Facturable) m).calculerCout())
            .sum();
    }

    /** Coût total de tous les entretiens réalisés. */
    public double getCoutTotalEntretiens() {
        return entretiens.getTous().stream()
            .filter(Entretien::estRealise)
            .mapToDouble(Entretien::getCout)
            .sum();
    }

    /** Nombre moyen de missions par chauffeur. */
    public double getMoyenneMissionsParChauffeur() {
        long total = chauffeurs.getNombre();
        if (total == 0) return 0;
        long totalMissions = chauffeurs.getTous().stream()
            .mapToLong(Chauffeur::getNombreMissions)
            .sum();
        return (double) totalMissions / total;
    }

    /** Le chauffeur ayant parcouru le plus de kilomètres. */
    public Optional<Chauffeur> getMeilleurChauffeur() {
        return chauffeurs.getTous().stream()
            .max(Comparator.comparingInt(Chauffeur::getNombreKmTotal));
    }

    /** Missions du mois courant. */
    public List<Mission> getMissionsDuMois() {
        LocalDate debut = LocalDate.now().withDayOfMonth(1);
        LocalDate fin   = debut.plusMonths(1).minusDays(1);
        return missions.filtrer(m ->
            !m.getDateDebut().isBefore(debut) && !m.getDateDebut().isAfter(fin)
        );
    }

    // ================================================================
    //  PERSISTANCE
    // ================================================================

    /** Sauvegarde toutes les données dans le fichier binaire. */
    public void sauvegarder() {
        DonneesFlotte d = new DonneesFlotte();
        d.vehicules  = vehicules.getTous();
        d.chauffeurs = chauffeurs.getTous();
        d.missions   = missions.getTous();
        d.entretiens = entretiens.getTous();
        Persistance.sauvegarder(d);
    }

    /** Charge les données depuis le fichier binaire. */
    public void charger() {
        DonneesFlotte d = Persistance.charger();
        d.vehicules.forEach(vehicules::ajouter);
        d.chauffeurs.forEach(chauffeurs::ajouter);
        d.entretiens.forEach(entretiens::ajouter);

        // Pour les missions, on restaure les références vehicule/chauffeur après chargement
        d.missions.forEach(m -> {
            missions.ajouter(m);
            if (m.getVehiculeId()  != null) vehicules.trouverParId(m.getVehiculeId()).ifPresent(m::setVehiculeAssocie);
            if (m.getChauffeurId() != null) chauffeurs.trouverParId(m.getChauffeurId()).ifPresent(m::setChauffeurAssocie);
        });
    }

    // Accesseurs aux registres (utilisés par les panneaux pour les ComboBox)
    public Registre<Vehicule>  getRegistreVehicules()  { return vehicules; }
    public Registre<Mission>   getRegistreMissions()   { return missions; }
    public Registre<Chauffeur> getRegistreChauffeurs() { return chauffeurs; }
    public Registre<Entretien> getRegistreEntretiens() { return entretiens; }
}
