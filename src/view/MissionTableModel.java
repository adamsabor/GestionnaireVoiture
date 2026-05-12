package view;

import model.Mission;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

/**
 * TableModel personnalisé pour la liste des missions.
 */
public class MissionTableModel extends AbstractTableModel {

    private static final String[] COLONNES = {
        "ID", "Type", "Titre", "Départ", "Arrivée", "Date début", "Date fin",
        "Distance", "Statut", "Véhicule", "Chauffeur"
    };

    private List<Mission> donnees;

    public MissionTableModel(List<Mission> missions) {
        this.donnees = new ArrayList<>(missions);
    }

    public void setDonnees(List<Mission> missions) {
        this.donnees = new ArrayList<>(missions);
        fireTableDataChanged();
    }

    public Mission getMission(int row) { return donnees.get(row); }

    @Override public int getRowCount()    { return donnees.size(); }
    @Override public int getColumnCount() { return COLONNES.length; }
    @Override public String getColumnName(int col) { return COLONNES[col]; }

    @Override
    public Object getValueAt(int row, int col) {
        Mission m = donnees.get(row);
        return switch (col) {
            case 0  -> m.getId();
            case 1  -> m.getTypeMission();
            case 2  -> m.getTitre();
            case 3  -> m.getLieuDepart();
            case 4  -> m.getLieuArrivee();
            case 5  -> m.getDateDebut().toString();
            case 6  -> m.getDateFin().toString();
            case 7  -> String.format("%.0f km", m.getDistanceKm());
            case 8  -> m.getStatut().toString();
            case 9  -> m.getVehiculeAssocie() != null ? m.getVehiculeAssocie().getImmatriculation() : "—";
            case 10 -> m.getChauffeurAssocie() != null ? m.getChauffeurAssocie().getNomComplet() : "—";
            default -> "";
        };
    }

    @Override public boolean isCellEditable(int r, int c) { return false; }
}
