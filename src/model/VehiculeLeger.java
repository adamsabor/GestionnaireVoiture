package model;

import exception.VehiculeIndisponibleException;
import model.interfaces.Assignable;
import model.interfaces.Facturable;
import model.interfaces.Maintenable;

import java.util.ArrayList;
import java.util.List;

/**
 * Véhicule léger : voitures, camionnettes (moins de 3,5 tonnes).
 * Nécessite un permis B. Entretien tous les 15 000 km.
 */
public class VehiculeLeger extends Vehicule implements Assignable, Maintenable, Facturable {

    private static final long serialVersionUID = 1L;

    // Au-delà de ce kilométrage depuis le dernier entretien, une révision est nécessaire
    private static final int SEUIL_ENTRETIEN_KM = 15_000;

    private Mission          missionActuelle;
    private final List<Entretien> historiqueEntretiens;
    private int              kmAuDernierEntretien;

    public VehiculeLeger(String immatriculation, String marque, String modele,
                         int annee, double coutAcquisition) {
        super(immatriculation, marque, modele, annee, coutAcquisition);
        this.historiqueEntretiens  = new ArrayList<>();
        this.kmAuDernierEntretien  = 0;
    }

    @Override public String getTypeVehicule()   { return "Léger"; }
    @Override public double getCoutKilometre()  { return 0.25; }   // 25 cts/km

    // ===================== Assignable =====================

    @Override
    public void assignerMission(Mission mission) throws VehiculeIndisponibleException {
        if (!estDisponible()) {
            throw new VehiculeIndisponibleException(
                "Le véhicule " + getImmatriculation() + " n'est pas disponible (état : " + getEtat() + ")"
            );
        }
        this.missionActuelle = mission;
        setEtat(EtatVehicule.EN_MISSION);
    }

    @Override
    public void terminerMission() {
        if (missionActuelle != null) {
            ajouterKilometres((int) missionActuelle.getDistanceKm());
        }
        this.missionActuelle = null;
        setEtat(EtatVehicule.DISPONIBLE);
    }

    @Override public boolean estDisponible()     { return getEtat() == EtatVehicule.DISPONIBLE; }
    @Override public Mission getMissionActuelle() { return missionActuelle; }

    // ===================== Maintenable =====================

    @Override
    public void ajouterEntretien(Entretien entretien) {
        historiqueEntretiens.add(entretien);
        kmAuDernierEntretien = getKilometrage();
        // Une fois l'entretien enregistré, le véhicule redevient disponible
        if (getEtat() == EtatVehicule.EN_MAINTENANCE) setEtat(EtatVehicule.DISPONIBLE);
    }

    @Override
    public Entretien getProchainEntretien() {
        return historiqueEntretiens.stream()
            .filter(e -> !e.estRealise())
            .findFirst()
            .orElse(null);
    }

    @Override public List<Entretien> getHistoriqueEntretiens() { return new ArrayList<>(historiqueEntretiens); }

    @Override
    public boolean necessiteEntretien() {
        return (getKilometrage() - kmAuDernierEntretien) >= SEUIL_ENTRETIEN_KM;
    }

    // ===================== Facturable =====================

    @Override public double calculerCout()   { return getKilometrage() * getCoutKilometre(); }

    @Override
    public String genererResume() {
        return String.format("Léger %s — %d km — Coût total : %.2f €",
            getImmatriculation(), getKilometrage(), calculerCout());
    }
}
