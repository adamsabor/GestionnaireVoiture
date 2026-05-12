package exception;

/**
 * Lancée quand on tente d'assigner un véhicule qui n'est pas en état DISPONIBLE.
 * Force l'appelant à gérer ce cas explicitement (checked exception).
 */
public class VehiculeIndisponibleException extends Exception {

    public VehiculeIndisponibleException(String message) {
        super(message);
    }
}
