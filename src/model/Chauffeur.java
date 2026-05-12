package model;

import java.util.ArrayList;
import java.util.List;

/**
 * Représente un chauffeur de la flotte.
 *
 * Chaque chauffeur possède un ou plusieurs permis et un historique
 * de ses missions. Son flag 'disponible' indique s'il peut être
 * affecté à une nouvelle mission.
 */
public class Chauffeur extends Entite {

    private static final long serialVersionUID = 1L;

    private String           nom;
    private String           prenom;
    private String           telephone;
    private List<TypePermis> permis;
    private boolean          disponible;
    private int              nombreKmTotal;
    private final List<String> historiqueMissionsIds;  // IDs pour éviter références circulaires

    public Chauffeur(String nom, String prenom, String telephone, List<TypePermis> permis) {
        super();
        this.nom                  = nom;
        this.prenom               = prenom;
        this.telephone            = telephone;
        this.permis               = new ArrayList<>(permis);
        this.disponible           = true;
        this.nombreKmTotal        = 0;
        this.historiqueMissionsIds = new ArrayList<>();
    }

    /** Retourne true si le chauffeur possède ce type de permis. */
    public boolean aLePermis(TypePermis permisRequis) {
        return permis.contains(permisRequis);
    }

    /** Met le chauffeur en mission (le rend indisponible). */
    public void prendreEnCharge(Mission mission) {
        this.disponible = false;
        historiqueMissionsIds.add(mission.getId());
    }

    /** Libère le chauffeur à la fin de la mission et cumule les km. */
    public void finirMission(int kmEffectues) {
        this.disponible    = true;
        this.nombreKmTotal += kmEffectues;
    }

    public String getNomComplet()  { return prenom + " " + nom; }

    public int getNombreMissions() { return historiqueMissionsIds.size(); }

    // --- Getters / Setters ---

    public String getNom()            { return nom; }
    public void setNom(String v)      { this.nom = v; }

    public String getPrenom()         { return prenom; }
    public void setPrenom(String v)   { this.prenom = v; }

    public String getTelephone()      { return telephone; }
    public void setTelephone(String v){ this.telephone = v; }

    public List<TypePermis> getPermis()       { return new ArrayList<>(permis); }
    public void setPermis(List<TypePermis> v) { this.permis = new ArrayList<>(v); }

    public boolean isDisponible()     { return disponible; }
    public void setDisponible(boolean v) { this.disponible = v; }

    public int getNombreKmTotal()     { return nombreKmTotal; }
    public void setNombreKmTotal(int v) { this.nombreKmTotal = v; }

    @Override
    public String getDescription() {
        String listePermis = permis.stream()
            .map(TypePermis::name)
            .reduce((a, b) -> a + ", " + b)
            .orElse("Aucun");
        return String.format("%s (%s) — %s", getNomComplet(), listePermis,
            disponible ? "Disponible" : "En mission");
    }
}
