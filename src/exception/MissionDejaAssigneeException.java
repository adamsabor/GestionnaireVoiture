package exception;

/**
 * Lancée quand on tente d'assigner un chauffeur
 * qui est déjà occupé par une autre mission.
 */
public class MissionDejaAssigneeException extends Exception {

    public MissionDejaAssigneeException(String message) {
        super(message);
    }
}
