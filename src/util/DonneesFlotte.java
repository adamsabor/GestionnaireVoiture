package util;

import model.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Conteneur sérialisable qui regroupe toutes les listes de données.
 * C'est cet objet qui est écrit/lu sur le disque lors de la sauvegarde.
 */
public class DonneesFlotte implements Serializable {

    private static final long serialVersionUID = 1L;

    public List<Vehicule>   vehicules   = new ArrayList<>();
    public List<Chauffeur>  chauffeurs  = new ArrayList<>();
    public List<Mission>    missions    = new ArrayList<>();
    public List<Entretien>  entretiens  = new ArrayList<>();
}
