package model;

import model.interfaces.Facturable;
import model.interfaces.Trackable;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * Mission longue : trajet sur plusieurs jours (plus de 300 km ou nuitée).
 *
 * Implémente Trackable : chaque étape du trajet est enregistrée
 * pour permettre un suivi en temps réel depuis l'interface.
 *
 * Tarification : coût kilométrique + indemnité journalière chauffeur.
 */
public class MissionLongue extends Mission implements Trackable, Facturable {

    private static final long serialVersionUID = 1L;

    private static final double COUT_PAR_KM   = 0.45;
    private static final double INDEMNITE_JOUR = 80.0;  // Indemnité journalière chauffeur

    private String       positionActuelle;
    private final List<String> itineraire;  // Historique des positions enregistrées

    public MissionLongue(String titre, String lieuDepart, String lieuArrivee,
                         LocalDate dateDebut, LocalDate dateFin, double distanceKm) {
        super(titre, lieuDepart, lieuArrivee, dateDebut, dateFin, distanceKm);
        this.positionActuelle = lieuDepart;
        this.itineraire       = new ArrayList<>();
        this.itineraire.add(lieuDepart);
    }

    @Override public String getTypeMission() { return "Longue"; }

    @Override
    public double getDureeEstimeeHeures() {
        // 90 km/h sur autoroute + 30 % de marge (pauses réglementaires)
        return (getDistanceKm() / 90.0) * 1.3;
    }

    /** Nombre de jours de la mission (minimum 1). */
    private long getNombreJours() {
        long jours = ChronoUnit.DAYS.between(getDateDebut(), getDateFin()) + 1;
        return Math.max(jours, 1);
    }

    // ===================== Trackable =====================

    @Override
    public void updatePosition(String position) {
        this.positionActuelle = position;
        itineraire.add(position);
    }

    @Override public String getPositionActuelle()  { return positionActuelle; }
    @Override public List<String> getItineraire()  { return new ArrayList<>(itineraire); }

    // ===================== Facturable =====================

    @Override
    public double calculerCout() {
        return (getDistanceKm() * COUT_PAR_KM) + (getNombreJours() * INDEMNITE_JOUR);
    }

    @Override
    public String genererResume() {
        return String.format("Mission longue '%s' — %.0f km — %d jour(s) — Coût : %.2f €",
            getTitre(), getDistanceKm(), getNombreJours(), calculerCout());
    }
}
