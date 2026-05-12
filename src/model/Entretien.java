package model;

import java.time.LocalDate;

/**
 * Représente une opération de maintenance sur un véhicule.
 *
 * Un entretien peut être planifié (à venir) ou réalisé (passé).
 * S'il dépasse sa date prévue sans être réalisé, il est "en retard".
 */
public class Entretien extends Entite {

    private static final long serialVersionUID = 1L;

    private String    typeEntretien;    // "Vidange", "Révision", "Pneus", "Freins"…
    private LocalDate datePrevue;
    private LocalDate dateRealisee;     // null si pas encore fait
    private double    cout;
    private String    description;
    private String    vehiculeId;       // Référence au véhicule concerné
    private boolean   realise;

    public Entretien(String typeEntretien, LocalDate datePrevue,
                     double cout, String description, String vehiculeId) {
        super();
        this.typeEntretien = typeEntretien;
        this.datePrevue    = datePrevue;
        this.cout          = cout;
        this.description   = description;
        this.vehiculeId    = vehiculeId;
        this.realise       = false;
    }

    /** Marque l'entretien comme effectué aujourd'hui. */
    public void marquerRealise() {
        this.realise       = true;
        this.dateRealisee  = LocalDate.now();
    }

    /** Marque l'entretien comme effectué à une date précise. */
    public void marquerRealise(LocalDate date) {
        this.realise       = true;
        this.dateRealisee  = date;
    }

    /** Retourne true si l'entretien n'est pas fait et que sa date est dépassée. */
    public boolean estEnRetard() {
        return !realise && LocalDate.now().isAfter(datePrevue);
    }

    // --- Getters / Setters ---

    public String    getTypeEntretien()         { return typeEntretien; }
    public void      setTypeEntretien(String v) { this.typeEntretien = v; }

    public LocalDate getDatePrevue()            { return datePrevue; }
    public void      setDatePrevue(LocalDate v) { this.datePrevue = v; }

    public LocalDate getDateRealisee()          { return dateRealisee; }

    public double    getCout()                  { return cout; }
    public void      setCout(double v)          { this.cout = v; }

    public String    getDescription()           { return description; }
    public void      setDescription(String v)   { this.description = v; }

    public String    getVehiculeId()            { return vehiculeId; }

    public boolean   estRealise()               { return realise; }

    /** Retourne un libellé d'état lisible pour l'affichage dans la table. */
    public String getStatutLibelle() {
        if (realise)         return "Réalisé";
        if (estEnRetard())   return "EN RETARD";
        return "Planifié";
    }
}
