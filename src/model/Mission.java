package model;

import java.time.LocalDate;

/**
 * Classe abstraite représentant une mission de transport.
 *
 * Une mission relie un point de départ à un point d'arrivée,
 * avec une date, une distance et un statut évolutif.
 * Elle peut être associée à un véhicule et un chauffeur.
 *
 * Hiérarchie :
 *   Mission (abstraite)
 *     ├── MissionCourte → implements Facturable
 *     └── MissionLongue → implements Trackable, Facturable
 */
public abstract class Mission extends Entite {

    private static final long serialVersionUID = 1L;

    private String       titre;
    private String       lieuDepart;
    private String       lieuArrivee;
    private LocalDate    dateDebut;
    private LocalDate    dateFin;
    private double       distanceKm;
    private StatutMission statut;
    private String       notes;

    // Associations — transient pour éviter les problèmes de sérialisation circulaire
    private transient Vehicule  vehiculeAssocie;
    private transient Chauffeur chauffeurAssocie;

    // IDs gardés séparément pour la persistance
    private String vehiculeId;
    private String chauffeurId;

    protected Mission(String titre, String lieuDepart, String lieuArrivee,
                      LocalDate dateDebut, LocalDate dateFin, double distanceKm) {
        super();
        this.titre       = titre;
        this.lieuDepart  = lieuDepart;
        this.lieuArrivee = lieuArrivee;
        this.dateDebut   = dateDebut;
        this.dateFin     = dateFin;
        this.distanceKm  = distanceKm;
        this.statut      = StatutMission.PLANIFIEE;
        this.notes       = "";
    }

    // --- Méthodes abstraites ---

    /** Durée estimée en heures (vitesse moyenne + marges selon le type). */
    public abstract double getDureeEstimeeHeures();

    /** Libellé court du type : "Courte" ou "Longue". */
    public abstract String getTypeMission();

    // --- Getters / Setters ---

    public String getTitre()                { return titre; }
    public void   setTitre(String v)        { this.titre = v; }

    public String getLieuDepart()           { return lieuDepart; }
    public void   setLieuDepart(String v)   { this.lieuDepart = v; }

    public String getLieuArrivee()          { return lieuArrivee; }
    public void   setLieuArrivee(String v)  { this.lieuArrivee = v; }

    public LocalDate getDateDebut()         { return dateDebut; }
    public void      setDateDebut(LocalDate v) { this.dateDebut = v; }

    public LocalDate getDateFin()           { return dateFin; }
    public void      setDateFin(LocalDate v) { this.dateFin = v; }

    public double getDistanceKm()           { return distanceKm; }
    public void   setDistanceKm(double v)   { this.distanceKm = v; }

    public StatutMission getStatut()        { return statut; }
    public void          setStatut(StatutMission v) { this.statut = v; }

    public String getNotes()                { return notes; }
    public void   setNotes(String v)        { this.notes = v; }

    public Vehicule getVehiculeAssocie()    { return vehiculeAssocie; }
    public void setVehiculeAssocie(Vehicule v) {
        this.vehiculeAssocie = v;
        this.vehiculeId = (v != null) ? v.getId() : null;
    }

    public Chauffeur getChauffeurAssocie()  { return chauffeurAssocie; }
    public void setChauffeurAssocie(Chauffeur c) {
        this.chauffeurAssocie = c;
        this.chauffeurId = (c != null) ? c.getId() : null;
    }

    public String getVehiculeId()   { return vehiculeId; }
    public String getChauffeurId()  { return chauffeurId; }

    @Override
    public String getDescription() {
        return String.format("[%s] %s : %s → %s (%s)", getTypeMission(), titre, lieuDepart, lieuArrivee, statut);
    }
}
