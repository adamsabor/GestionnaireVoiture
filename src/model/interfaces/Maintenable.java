package model.interfaces;

import model.Entretien;
import java.util.List;

/**
 * Interface pour les véhicules nécessitant un suivi de maintenance.
 *
 * Un Maintenable connaît son historique d'entretiens
 * et sait s'il a besoin d'une révision.
 */
public interface Maintenable {

    /** Enregistre un entretien (planifié ou déjà réalisé). */
    void ajouterEntretien(Entretien entretien);

    /**
     * Retourne le premier entretien futur non encore réalisé,
     * ou null s'il n'y en a pas.
     */
    Entretien getProchainEntretien();

    /** Retourne tous les entretiens liés à ce véhicule. */
    List<Entretien> getHistoriqueEntretiens();

    /**
     * Retourne true si le kilométrage depuis le dernier entretien
     * dépasse le seuil recommandé.
     */
    boolean necessiteEntretien();
}
