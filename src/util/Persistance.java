package util;

import java.io.*;

/**
 * Gère la sauvegarde et le chargement des données via la sérialisation Java.
 *
 * Toute la flotte (véhicules, chauffeurs, missions, entretiens) est sérialisée
 * dans un seul fichier binaire. C'est simple et fiable pour des objets complexes.
 */
public class Persistance {

    private static final String FICHIER = "resources/flotte.ser";

    /** Sauvegarde toutes les données dans le fichier binaire. */
    public static void sauvegarder(DonneesFlotte donnees) {
        // Crée le dossier resources s'il n'existe pas
        new File("resources").mkdirs();
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(FICHIER))) {
            oos.writeObject(donnees);
        } catch (IOException e) {
            System.err.println("Erreur de sauvegarde : " + e.getMessage());
        }
    }

    /**
     * Charge les données depuis le fichier binaire.
     * Retourne un objet vide si le fichier n'existe pas encore.
     */
    public static DonneesFlotte charger() {
        File f = new File(FICHIER);
        if (!f.exists()) return new DonneesFlotte();
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f))) {
            return (DonneesFlotte) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Erreur de chargement : " + e.getMessage());
            return new DonneesFlotte();
        }
    }
}
