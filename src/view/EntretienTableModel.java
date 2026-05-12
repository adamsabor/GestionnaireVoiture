package view;

import model.Entretien;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

/**
 * TableModel personnalisé pour la liste des entretiens.
 */
public class EntretienTableModel extends AbstractTableModel {

    private static final String[] COLONNES = {
        "ID", "Type", "Véhicule ID", "Date prévue", "Coût", "Statut", "Description"
    };

    private List<Entretien> donnees;

    public EntretienTableModel(List<Entretien> entretiens) {
        this.donnees = new ArrayList<>(entretiens);
    }

    public void setDonnees(List<Entretien> entretiens) {
        this.donnees = new ArrayList<>(entretiens);
        fireTableDataChanged();
    }

    public Entretien getEntretien(int row) { return donnees.get(row); }

    @Override public int getRowCount()    { return donnees.size(); }
    @Override public int getColumnCount() { return COLONNES.length; }
    @Override public String getColumnName(int col) { return COLONNES[col]; }

    @Override
    public Object getValueAt(int row, int col) {
        Entretien e = donnees.get(row);
        return switch (col) {
            case 0 -> e.getId();
            case 1 -> e.getTypeEntretien();
            case 2 -> e.getVehiculeId();
            case 3 -> e.getDatePrevue().toString();
            case 4 -> String.format("%.2f €", e.getCout());
            case 5 -> e.getStatutLibelle();
            case 6 -> e.getDescription();
            default -> "";
        };
    }

    @Override public boolean isCellEditable(int r, int c) { return false; }
}
