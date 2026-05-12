package model;

import java.time.LocalDate;

/**
 * Classe abstraite représentant un véhicule de la flotte.
 *
 * Contient toutes les informations communes (immatriculation, marque, km, état).
 * Les sous-classes (léger, lourd, spécial) implémentent les interfaces métier
 * et définissent leur comportement spécifique.
 *
 * Hiérarchie :
 *   Vehicule (abstrait)
 *     ├── VehiculeLeger  → implements Assignable, Maintenable, Facturable
 *     ├── VehiculeLourd  → implements Assignable, Maintenable, Facturable
 *     └── VehiculeSpecial → implements Maintenable, Facturable
 */
public abstract class Vehicule extends Entite {

    private static final long serialVersionUID = 1L;

    private String immatriculation;
    private String marque;
    private String modele;
    private int    annee;
    private int    kilometrage;
    private EtatVehicule etat;
    private LocalDate dateAcquisition;
    private double coutAcquisition;

    protected Vehicule(String immatriculation, String marque, String modele,
                       int annee, double coutAcquisition) {
        super();
        this.immatriculation  = immatriculation;
        this.marque           = marque;
        this.modele           = modele;
        this.annee            = annee;
        this.coutAcquisition  = coutAcquisition;
        this.etat             = EtatVehicule.DISPONIBLE;
        this.dateAcquisition  = LocalDate.now();
        this.kilometrage      = 0;
    }

    // --- Méthodes abstraites que chaque sous-classe doit définir ---

    /** Retourne le libellé du type : "Léger", "Lourd" ou "Spécial". */
    public abstract String getTypeVehicule();

    /**
     * Coût moyen par kilomètre parcouru (carburant + usure).
     * Varie selon le gabarit du véhicule.
     */
    public abstract double getCoutKilometre();

    // --- Getters / Setters ---

    public String getImmatriculation() { return immatriculation; }
    public void setImmatriculation(String v) { this.immatriculation = v; }

    public String getMarque() { return marque; }
    public void setMarque(String v) { this.marque = v; }

    public String getModele() { return modele; }
    public void setModele(String v) { this.modele = v; }

    public int getAnnee() { return annee; }
    public void setAnnee(int v) { this.annee = v; }

    public int getKilometrage() { return kilometrage; }
    public void setKilometrage(int v) { this.kilometrage = v; }

    /** Incrémente le compteur kilométrique. */
    public void ajouterKilometres(int km) { this.kilometrage += km; }

    public EtatVehicule getEtat() { return etat; }
    public void setEtat(EtatVehicule v) { this.etat = v; }

    public LocalDate getDateAcquisition() { return dateAcquisition; }
    public void setDateAcquisition(LocalDate v) { this.dateAcquisition = v; }

    public double getCoutAcquisition() { return coutAcquisition; }
    public void setCoutAcquisition(double v) { this.coutAcquisition = v; }

    @Override
    public String getDescription() {
        return String.format("%s — %s %s (%s)", immatriculation, marque, modele, getTypeVehicule());
    }
}
