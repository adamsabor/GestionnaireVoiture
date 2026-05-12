package model.interfaces;

import exception.VehiculeIndisponibleException;
import model.Mission;

/**
 * Interface pour les véhicules pouvant être affectés à une mission.
 *
 * Un Assignable sait recevoir une mission, la terminer,
 * et signaler s'il est libre ou non.
 */
public interface Assignable {

    /**
     * Affecte une mission au véhicule.
     * @throws VehiculeIndisponibleException si le véhicule n'est pas DISPONIBLE
     */
    void assignerMission(Mission mission) throws VehiculeIndisponibleException;

    /** Libère le véhicule une fois la mission terminée. */
    void terminerMission();

    /** Retourne true si le véhicule peut accepter une nouvelle mission. */
    boolean estDisponible();

    /** Retourne la mission en cours, ou null si le véhicule est libre. */
    Mission getMissionActuelle();
}
