package model;

/**
 * Les quatre états possibles d'un véhicule dans la flotte.
 * Un véhicule passe d'un état à l'autre selon les opérations.
 */
public enum EtatVehicule {
    DISPONIBLE("Disponible"),
    EN_MISSION("En mission"),
    EN_MAINTENANCE("En maintenance"),
    HORS_SERVICE("Hors service");

    private final String libelle;

    EtatVehicule(String libelle) {
        this.libelle = libelle;
    }

    public String getLibelle() { return libelle; }

    @Override
    public String toString() { return libelle; }
}
