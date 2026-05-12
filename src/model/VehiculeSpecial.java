package model;

import model.interfaces.Facturable;
import model.interfaces.Maintenable;

import java.util.ArrayList;
import java.util.List;

/**
 * Véhicule spécial : ambulances, dépanneuses, engins de chantier…
 *
 * N'est PAS Assignable (il n'est pas envoyé en mission standard),
 * mais reste Maintenable et Facturable.
 * Entretien très fréquent (5 000 km) à cause de l'équipement embarqué.
 */
public class VehiculeSpecial extends Vehicule implements Maintenable, Facturable {

    private static final long serialVersionUID = 1L;

    private static final int SEUIL_ENTRETIEN_KM = 5_000;

    private String           specialisation;       // "Ambulance", "Dépanneuse", "Grue"…
    private final List<Entretien> historiqueEntretiens;
    private int              kmAuDernierEntretien;

    public VehiculeSpecial(String immatriculation, String marque, String modele,
                           int annee, double coutAcquisition, String specialisation) {
        super(immatriculation, marque, modele, annee, coutAcquisition);
        this.specialisation       = specialisation;
        this.historiqueEntretiens = new ArrayList<>();
        this.kmAuDernierEntretien = 0;
    }

    @Override public String getTypeVehicule()  { return "Spécial"; }
    @Override public double getCoutKilometre() { return 1.20; }  // Équipements spéciaux = coût élevé

    public String getSpecialisation()        { return specialisation; }
    public void setSpecialisation(String v)  { this.specialisation = v; }

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
        return String.format("Spécial [%s] %s — %d km — Coût : %.2f €",
            specialisation, getImmatriculation(), getKilometrage(), calculerCout());
    }
}
