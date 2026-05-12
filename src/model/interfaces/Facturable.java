package model.interfaces;

/**
 * Interface pour les entités qui génèrent un coût facturable.
 *
 * Missions et entretiens sont Facturables :
 * on peut calculer leur coût et obtenir un résumé pour la facturation.
 */
public interface Facturable {

    /** Calcule et retourne le coût total en euros. */
    double calculerCout();

    /** Génère un résumé textuel pour la facturation. */
    String genererResume();
}
