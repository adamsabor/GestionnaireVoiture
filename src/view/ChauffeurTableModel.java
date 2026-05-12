package view;

import model.Chauffeur;
import model.TypePermis;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * TableModel personnalisé pour la liste des chauffeurs.
 */
public class ChauffeurTableModel extends AbstractTableModel {

    private static final String[] COLONNES = {
        "ID", "Nom", "Prénom", "Téléphone", "Permis", "Disponible", "Km total", "Missions"
    };

    private List<Chauffeur> donnees;

    public ChauffeurTableModel(List<Chauffeur> chauffeurs) {
        this.donnees = new ArrayList<>(chauffeurs);
    }

    public void setDonnees(List<Chauffeur> chauffeurs) {
        this.donnees = new ArrayList<>(chauffeurs);
        fireTableDataChanged();
    }

    public Chauffeur getChauffeur(int row) { return donnees.get(row); }

    @Override public int getRowCount()    { return donnees.size(); }
    @Override public int getColumnCount() { return COLONNES.length; }
    @Override public String getColumnName(int col) { return COLONNES[col]; }

    @Override
    public Object getValueAt(int row, int col) {
        Chauffeur c = donnees.get(row);
        return switch (col) {
            case 0 -> c.getId();
            case 1 -> c.getNom();
            case 2 -> c.getPrenom();
            case 3 -> c.getTelephone();
            case 4 -> c.getPermis().stream().map(TypePermis::name).collect(Collectors.joining(", "));
            case 5 -> c.isDisponible() ? "Oui" : "Non";
            case 6 -> c.getNombreKmTotal() + " km";
            case 7 -> c.getNombreMissions();
            default -> "";
        };
    }

    @Override public boolean isCellEditable(int r, int c) { return false; }
}
