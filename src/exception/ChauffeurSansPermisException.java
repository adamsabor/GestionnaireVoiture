package exception;

import model.TypePermis;

/**
 * Lancée quand un chauffeur est affecté à un véhicule
 * sans posséder le permis requis pour ce gabarit.
 */
public class ChauffeurSansPermisException extends Exception {

    private final TypePermis permisRequis;

    public ChauffeurSansPermisException(String nomChauffeur, TypePermis permisRequis) {
        super("Le chauffeur " + nomChauffeur + " ne possède pas le permis "
              + permisRequis.name() + " requis pour ce véhicule.");
        this.permisRequis = permisRequis;
    }

    public TypePermis getPermisRequis() { return permisRequis; }
}
