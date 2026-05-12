package model;

/**
 * Types de permis de conduire reconnus dans la flotte.
 * Chaque véhicule exige un niveau de permis minimum.
 */
public enum TypePermis {
    B("Véhicule léger (< 3,5t)"),
    C("Poids lourd (> 3,5t)"),
    D("Transport en commun"),
    CE("Ensemble articulé");

    private final String description;

    TypePermis(String description) {
        this.description = description;
    }

    public String getDescription() { return description; }

    @Override
    public String toString() { return name() + " — " + description; }
}
