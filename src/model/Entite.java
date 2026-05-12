package model;

import java.io.Serializable;
import java.util.UUID;

/**
 * Classe abstraite racine de tout le modèle métier.
 *
 * Chaque entité possède un identifiant unique (ID) généré automatiquement.
 * L'implémentation de Serializable permet la sauvegarde/chargement sur disque.
 *
 * Toutes les classes concrètes doivent fournir une description courte via
 * getDescription(), utilisée dans les listes et les logs.
 */
public abstract class Entite implements Serializable {

    private static final long serialVersionUID = 1L;

    // ID court (8 hex) — lisible par un humain, unique dans la session
    private final String id;

    protected Entite() {
        this.id = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
    }

    public String getId() { return id; }

    /** Description courte affichée dans les listes et les ComboBox. */
    public abstract String getDescription();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Entite other)) return false;
        return id.equals(other.id);
    }

    @Override
    public int hashCode() { return id.hashCode(); }

    @Override
    public String toString() { return getDescription(); }
}
