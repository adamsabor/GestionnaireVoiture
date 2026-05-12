package view;

import model.Vehicule;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

/**
 * TableModel personnalisé pour afficher les véhicules dans un JTable.
 *
 * AbstractTableModel gère les listeners automatiquement ;
 * il suffit d'implémenter getRowCount(), getColumnCount() et getValueAt().
 */
public class VehiculeTableModel extends AbstractTableModel {

    private static final String[] COLONNES = {
        "ID", "Immatriculation", "Marque", "Modèle", "Type", "Année", "Kilométrage", "État"
    };

    private List<Vehicule> donnees;

    public VehiculeTableModel(List<Vehicule> vehicules) {
        this.donnees = new ArrayList<>(vehicules);
    }

    /** Remplace les données et rafraîchit la table. */
    public void setDonnees(List<Vehicule> vehicules) {
        this.donnees = new ArrayList<>(vehicules);
        fireTableDataChanged();
    }

    /** Retourne le véhicule à la ligne indiquée. */
    public Vehicule getVehicule(int row) { return donnees.get(row); }

    @Override public int getRowCount()    { return donnees.size(); }
    @Override public int getColumnCount() { return COLONNES.length; }
    @Override public String getColumnName(int col) { return COLONNES[col]; }

    @Override
    public Object getValueAt(int row, int col) {
        Vehicule v = donnees.get(row);
        return switch (col) {
            case 0 -> v.getId();
            case 1 -> v.getImmatriculation();
            case 2 -> v.getMarque();
            case 3 -> v.getModele();
            case 4 -> v.getTypeVehicule();
            case 5 -> v.getAnnee();
            case 6 -> v.getKilometrage() + " km";
            case 7 -> v.getEtat().getLibelle();
            default -> "";
        };
    }

    /** Rend la colonne "État" éditable si besoin (non utilisé ici). */
    @Override public boolean isCellEditable(int row, int col) { return false; }

    @Override
    public Class<?> getColumnClass(int col) {
        return switch (col) {
            case 5 -> Integer.class;
            default -> String.class;
        };
    }
}
