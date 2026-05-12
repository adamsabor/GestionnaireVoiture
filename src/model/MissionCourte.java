package model;

import model.interfaces.Facturable;
import java.time.LocalDate;

/**
 * Mission courte : trajet aller-retour dans la même journée, moins de 300 km.
 *
 * Tarification : forfait de base + coût kilométrique fixe.
 * Le départ et l'arrivée sont le même jour.
 */
public class MissionCourte extends Mission implements Facturable {

    private static final long serialVersionUID = 1L;

    private static final double FORFAIT_BASE  = 120.0;  // Coût fixe minimum
    private static final double COUT_PAR_KM   = 0.30;   // Coût variable

    public MissionCourte(String titre, String lieuDepart, String lieuArrivee,
                         LocalDate date, double distanceKm) {
        // Départ et fin le même jour pour une mission courte
        super(titre, lieuDepart, lieuArrivee, date, date, distanceKm);
    }

    @Override public String getTypeMission() { return "Courte"; }

    @Override
    public double getDureeEstimeeHeures() {
        // Vitesse moyenne 80 km/h + 1h de chargement/déchargement
        return (getDistanceKm() / 80.0) + 1.0;
    }

    // ===================== Facturable =====================

    @Override
    public double calculerCout() {
        return FORFAIT_BASE + (getDistanceKm() * COUT_PAR_KM);
    }

    @Override
    public String genererResume() {
        return String.format("Mission courte '%s' — %.0f km — %.1f h — Coût : %.2f €",
            getTitre(), getDistanceKm(), getDureeEstimeeHeures(), calculerCout());
    }
}
