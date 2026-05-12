package model;

/**
 * Cycle de vie d'une mission : de sa planification jusqu'à sa clôture.
 */
public enum StatutMission {
    PLANIFIEE("Planifiée"),
    EN_COURS("En cours"),
    TERMINEE("Terminée"),
    ANNULEE("Annulée");

    private final String libelle;

    StatutMission(String libelle) {
        this.libelle = libelle;
    }

    @Override
    public String toString() { return libelle; }
}
