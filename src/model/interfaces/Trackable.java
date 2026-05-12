package model.interfaces;

import java.util.List;

/**
 * Interface pour les missions nécessitant un suivi de position.
 *
 * Seules les missions longues (plusieurs jours) sont Trackable :
 * on enregistre chaque étape du trajet pour pouvoir retracer l'itinéraire.
 */
public interface Trackable {

    /** Met à jour la position actuelle du convoi. */
    void updatePosition(String position);

    /** Retourne la dernière position connue. */
    String getPositionActuelle();

    /** Retourne la liste de toutes les positions enregistrées depuis le départ. */
    List<String> getItineraire();
}
