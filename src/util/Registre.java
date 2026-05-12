package util;

import model.Entite;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Registre générique pour gérer n'importe quelle collection d'entités métier.
 *
 * Le type T est borné par Entite (T extends Entite) :
 * cela garantit que tout élément stocké possède un ID unique.
 *
 * Exemples d'utilisation :
 *   Registre<Vehicule>  gereVehicules  = new Registre<>();
 *   Registre<Chauffeur> gereChauffeurs = new Registre<>();
 *
 * Les wildcards (? extends / ? super) sont utilisés dans les méthodes de tri
 * pour accepter des comparateurs plus généraux (ex. Comparator<Entite>).
 *
 * @param <T> Type d'entité, doit étendre Entite
 */
public class Registre<T extends Entite> {

    // TreeMap : accès rapide par ID (O(log n)) et ordre alphabétique garanti
    private final Map<String, T> stockage;

    public Registre() {
        this.stockage = new TreeMap<>();
    }

    /** Ajoute ou remplace une entité dans le registre. */
    public void ajouter(T entite) {
        stockage.put(entite.getId(), entite);
    }

    /** Supprime une entité par son ID. Retourne true si elle existait. */
    public boolean supprimer(String id) {
        return stockage.remove(id) != null;
    }

    /** Cherche une entité par son ID. Retourne un Optional vide si introuvable. */
    public Optional<T> trouverParId(String id) {
        return Optional.ofNullable(stockage.get(id));
    }

    /** Retourne toutes les entités sous forme de liste (copie défensive). */
    public List<T> getTous() {
        return new ArrayList<>(stockage.values());
    }

    /**
     * Filtre les entités selon un prédicat (lambda ou référence de méthode).
     *
     * Exemple : registre.filtrer(v -> v.getEtat() == EtatVehicule.DISPONIBLE)
     */
    public List<T> filtrer(Predicate<T> critere) {
        return stockage.values().stream()
            .filter(critere)
            .collect(Collectors.toList());
    }

    /**
     * Filtre ET trie en une seule passe — combine les deux opérations Stream.
     *
     * @param critere    Condition de sélection
     * @param comparateur Ordre de tri voulu
     */
    public List<T> filtrerEtTrier(Predicate<T> critere, Comparator<T> comparateur) {
        return stockage.values().stream()
            .filter(critere)
            .sorted(comparateur)
            .collect(Collectors.toList());
    }

    /**
     * Retourne toutes les entités triées.
     * Le wildcard (? super T) accepte un Comparator<Entite> en plus de Comparator<T>.
     */
    public List<T> getTousTries(Comparator<? super T> comparateur) {
        return stockage.values().stream()
            .sorted(comparateur)
            .collect(Collectors.toList());
    }

    /** Nombre d'entités stockées. */
    public int getNombre()  { return stockage.size(); }

    /** Vide totalement le registre. */
    public void vider()     { stockage.clear(); }

    public boolean estVide(){ return stockage.isEmpty(); }
}
