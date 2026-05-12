package model;

import exception.VehiculeIndisponibleException;
import model.interfaces.Assignable;
import model.interfaces.Facturable;
import model.interfaces.Maintenable;

import java.util.ArrayList;
import java.util.List;

/**
 * Véhicule lourd : camions, semi-remorques (plus de 3,5 tonnes).
 * Exige un permis C ou CE. Entretien tous les 10 000 km (usure plus forte).
 */
public class VehiculeLourd extends Vehicule implements Assignable, Maintenable, Facturable {

    private static final long serialVersionUID = 1L;

    private static final int SEUIL_ENTRETIEN_KM = 10_000;

    private Mission          missionActuelle;
    private final List<Entretien> historiqueEntretiens;
    private int              kmAuDernierEntretien;
    private double           chargeMaxTonnes;   // Capacité de charge utile

    public VehiculeLourd(String immatriculation, String marque, String modele,
                         int annee, double coutAcquisition, double chargeMaxTonnes) {
        super(immatriculation, marque, modele, annee, coutAcquisition);
        this.chargeMaxTonnes      = chargeMaxTonnes;
        this.historiqueEntretiens = new ArrayList<>();
        this.kmAuDernierEntretien = 0;
    }

    @Override public String getTypeVehicule()  { return "Lourd"; }
    @Override public double getCoutKilometre() { return 0.55; }   // Plus cher : gasoil + usure

    public double getChargeMaxTonnes()          { return chargeMaxTonnes; }
    public void setChargeMaxTonnes(double v)    { this.chargeMaxTonnes = v; }

    // ===================== Assignable =====================

    @Override
    public void assignerMission(Mission mission) throws VehiculeIndisponibleException {
        if (!estDisponible()) {
            throw new VehiculeIndisponibleException(
                "Le poids lourd " + getImmatriculation() + " n'est pas disponible (état : " + getEtat() + ")"
            );
        }
        this.missionActuelle = mission;
        setEtat(EtatVehicule.EN_MISSION);
    }

    @Override
    public void terminerMission() {
        if (missionActuelle != null) ajouterKilometres((int) missionActuelle.getDistanceKm());
        this.missionActuelle = null;
        setEtat(EtatVehicule.DISPONIBLE);
    }

    @Override public boolean estDisponible()      { return getEtat() == EtatVehicule.DISPONIBLE; }
    @Override public Mission getMissionActuelle()  { return missionActuelle; }

    // ===================== Maintenable =====================

    @Override
    public void ajouterEntretien(Entretien entretien) {
        historiqueEntretiens.add(entretien);
        kmAuDernierEntretien = getKilometrage();
        if (getEtat() == EtatVehicule.EN_MAINTENANCE) setEtat(EtatVehicule.DISPONIBLE);
    }

    @Override
    public Entretien getProchainEntretien() {
        return historiqueEntretiens.stream().filter(e -> !e.estRealise()).findFirst().orElse(null);
    }

    @Override public List<Entretien> getHistoriqueEntretiens() { return new ArrayList<>(historiqueEntretiens); }
    @Override public boolean necessiteEntretien() { return (getKilometrage() - kmAuDernierEntretien) >= SEUIL_ENTRETIEN_KM; }

    // ===================== Facturable =====================

    @Override public double calculerCout() { return getKilometrage() * getCoutKilometre(); }

    @Override
    public String genererResume() {
        return String.format("Lourd %s — %d km — Charge max : %.1f t — Coût : %.2f €",
            getImmatriculation(), getKilometrage(), chargeMaxTonnes, calculerCout());
    }
}
