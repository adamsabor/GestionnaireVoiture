package view;

import controller.GestionnaireFlotte;
import model.*;

import javax.swing.*;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

/**
 * Panneau de gestion des véhicules.
 *
 * Affiche la liste dans un JTable triable, propose un filtre multicritères
 * et des boutons CRUD + action "Mettre en maintenance".
 */
public class VehiculePanel extends JPanel {

    private final GestionnaireFlotte gestionnaire;
    private final VehiculeTableModel tableModel;
    private final JTable             table;
    private final TableRowSorter<VehiculeTableModel> sorter;

    // Champs de filtre
    private final JTextField    filtreMarque = new JTextField(10);
    private final JComboBox<String>      filtreType  = new JComboBox<>(new String[]{"Tous", "Léger", "Lourd", "Spécial"});
    private final JComboBox<EtatVehicule> filtreEtat  = new JComboBox<>();

    public VehiculePanel(GestionnaireFlotte gestionnaire) {
        this.gestionnaire = gestionnaire;
        this.tableModel   = new VehiculeTableModel(gestionnaire.getTousLesVehicules());
        this.table        = new JTable(tableModel);
        this.sorter       = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter); // Active le tri par clic sur les en-têtes

        setLayout(new BorderLayout(5, 5));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(creerPanneauFiltres(),  BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(creerPanneauBoutons(), BorderLayout.SOUTH);

        styleTable();
    }

    private JPanel creerPanneauFiltres() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        p.setBorder(BorderFactory.createTitledBorder("Filtres"));

        // Peuple le ComboBox des états (null = "Tous")
        filtreEtat.addItem(null);
        for (EtatVehicule e : EtatVehicule.values()) filtreEtat.addItem(e);

        p.add(new JLabel("Marque :"));  p.add(filtreMarque);
        p.add(new JLabel("Type :"));    p.add(filtreType);
        p.add(new JLabel("État :"));    p.add(filtreEtat);

        JButton btnFiltrer = new JButton("Filtrer");
        JButton btnReset   = new JButton("Réinitialiser");
        btnFiltrer.addActionListener(e -> appliquerFiltres());
        btnReset.addActionListener(e   -> reinitialiserFiltres());

        p.add(btnFiltrer);
        p.add(btnReset);
        return p;
    }

    private JPanel creerPanneauBoutons() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));

        JButton btnAjouter      = new JButton("Ajouter");
        JButton btnModifier     = new JButton("Modifier");
        JButton btnSupprimer    = new JButton("Supprimer");
        JButton btnMaintenance  = new JButton("Mettre en maintenance");

        btnAjouter.addActionListener(e     -> actionAjouter());
        btnModifier.addActionListener(e    -> actionModifier());
        btnSupprimer.addActionListener(e   -> actionSupprimer());
        btnMaintenance.addActionListener(e -> actionMettreEnMaintenance());

        p.add(btnAjouter);
        p.add(btnModifier);
        p.add(btnSupprimer);
        p.add(btnMaintenance);
        return p;
    }

    /** Applique les filtres de la barre et rafraîchit la table. */
    private void appliquerFiltres() {
        String       marque = filtreMarque.getText().trim();
        String       type   = filtreType.getSelectedIndex() == 0 ? null : (String) filtreType.getSelectedItem();
        EtatVehicule etat   = (EtatVehicule) filtreEtat.getSelectedItem();

        List<Vehicule> resultats = gestionnaire.filtrerVehicules(type, etat, marque);
        tableModel.setDonnees(resultats);
    }

    private void reinitialiserFiltres() {
        filtreMarque.setText("");
        filtreType.setSelectedIndex(0);
        filtreEtat.setSelectedIndex(0);
        tableModel.setDonnees(gestionnaire.getTousLesVehicules());
    }

    /** Ouvre le dialog de création d'un nouveau véhicule. */
    private void actionAjouter() {
        // Champs du formulaire
        JTextField immatField   = new JTextField(12);
        JTextField marqueField  = new JTextField(10);
        JTextField modeleField  = new JTextField(10);
        JTextField anneeField   = new JTextField("2024", 6);
        JTextField coutField    = new JTextField("20000", 8);
        JComboBox<String> typeBox = new JComboBox<>(new String[]{"Léger", "Lourd", "Spécial"});
        JTextField extraField   = new JTextField(10); // charge (lourd) ou spécialisation (spécial)
        JLabel     extraLabel   = new JLabel("Charge max (t) :");

        typeBox.addActionListener(e -> {
            String t = (String) typeBox.getSelectedItem();
            if ("Lourd".equals(t))   { extraLabel.setText("Charge max (t) :"); extraField.setEnabled(true); }
            else if ("Spécial".equals(t)) { extraLabel.setText("Spécialisation :"); extraField.setEnabled(true); }
            else { extraLabel.setText("(option)"); extraField.setEnabled(false); }
        });
        extraField.setEnabled(false);

        JPanel form = new JPanel(new GridLayout(0, 2, 5, 5));
        form.add(new JLabel("Immatriculation :")); form.add(immatField);
        form.add(new JLabel("Marque :"));          form.add(marqueField);
        form.add(new JLabel("Modèle :"));          form.add(modeleField);
        form.add(new JLabel("Année :"));           form.add(anneeField);
        form.add(new JLabel("Coût acquisition :"));form.add(coutField);
        form.add(new JLabel("Type :"));            form.add(typeBox);
        form.add(extraLabel);                      form.add(extraField);

        int rep = JOptionPane.showConfirmDialog(this, form, "Nouveau véhicule", JOptionPane.OK_CANCEL_OPTION);
        if (rep != JOptionPane.OK_OPTION) return;

        try {
            String immat   = immatField.getText().trim().toUpperCase();
            String marque  = marqueField.getText().trim();
            String modele  = modeleField.getText().trim();
            int    annee   = Integer.parseInt(anneeField.getText().trim());
            double cout    = Double.parseDouble(coutField.getText().trim());
            String type    = (String) typeBox.getSelectedItem();

            if (immat.isBlank() || marque.isBlank()) { erreur("Immatriculation et marque obligatoires."); return; }

            Vehicule nouveau = switch (type) {
                case "Lourd" -> {
                    double charge = extraField.getText().isBlank() ? 10.0 : Double.parseDouble(extraField.getText());
                    yield new VehiculeLourd(immat, marque, modele, annee, cout, charge);
                }
                case "Spécial" -> {
                    String spec = extraField.getText().isBlank() ? "Générique" : extraField.getText();
                    yield new VehiculeSpecial(immat, marque, modele, annee, cout, spec);
                }
                default -> new VehiculeLeger(immat, marque, modele, annee, cout);
            };

            gestionnaire.ajouterVehicule(nouveau);
            rafraichir();
        } catch (NumberFormatException ex) {
            erreur("Valeur numérique invalide. Vérifiez l'année et le coût.");
        }
    }

    private void actionModifier() {
        int row = table.getSelectedRow();
        if (row < 0) { erreur("Sélectionnez un véhicule à modifier."); return; }
        Vehicule v = tableModel.getVehicule(table.convertRowIndexToModel(row));

        JTextField immatField  = new JTextField(v.getImmatriculation(), 12);
        JTextField marqueField = new JTextField(v.getMarque(), 10);
        JTextField modeleField = new JTextField(v.getModele(), 10);
        JTextField anneeField  = new JTextField(String.valueOf(v.getAnnee()), 6);
        JTextField kmField     = new JTextField(String.valueOf(v.getKilometrage()), 8);

        JPanel form = new JPanel(new GridLayout(0, 2, 5, 5));
        form.add(new JLabel("Immatriculation :")); form.add(immatField);
        form.add(new JLabel("Marque :"));          form.add(marqueField);
        form.add(new JLabel("Modèle :"));          form.add(modeleField);
        form.add(new JLabel("Année :"));           form.add(anneeField);
        form.add(new JLabel("Kilométrage :"));     form.add(kmField);

        int rep = JOptionPane.showConfirmDialog(this, form, "Modifier le véhicule", JOptionPane.OK_CANCEL_OPTION);
        if (rep != JOptionPane.OK_OPTION) return;

        try {
            v.setImmatriculation(immatField.getText().trim().toUpperCase());
            v.setMarque(marqueField.getText().trim());
            v.setModele(modeleField.getText().trim());
            v.setAnnee(Integer.parseInt(anneeField.getText().trim()));
            v.setKilometrage(Integer.parseInt(kmField.getText().trim()));
            rafraichir();
        } catch (NumberFormatException ex) {
            erreur("Valeur numérique invalide.");
        }
    }

    private void actionSupprimer() {
        int row = table.getSelectedRow();
        if (row < 0) { erreur("Sélectionnez un véhicule à supprimer."); return; }
        Vehicule v = tableModel.getVehicule(table.convertRowIndexToModel(row));

        int conf = JOptionPane.showConfirmDialog(this,
            "Supprimer le véhicule " + v.getImmatriculation() + " ?",
            "Confirmation", JOptionPane.YES_NO_OPTION);
        if (conf != JOptionPane.YES_OPTION) return;

        if (!gestionnaire.supprimerVehicule(v.getId())) {
            erreur("Impossible : ce véhicule est actuellement en mission.");
        } else {
            rafraichir();
        }
    }

    private void actionMettreEnMaintenance() {
        int row = table.getSelectedRow();
        if (row < 0) { erreur("Sélectionnez un véhicule."); return; }
        Vehicule v = tableModel.getVehicule(table.convertRowIndexToModel(row));

        if (v.getEtat() == EtatVehicule.EN_MISSION) {
            erreur("Impossible : le véhicule est en mission."); return;
        }
        v.setEtat(EtatVehicule.EN_MAINTENANCE);
        rafraichir();
        JOptionPane.showMessageDialog(this, v.getImmatriculation() + " mis en maintenance.");
    }

    /** Recharge les données depuis le contrôleur et met à jour la table. */
    public void rafraichir() {
        tableModel.setDonnees(gestionnaire.getTousLesVehicules());
    }

    private void erreur(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Erreur", JOptionPane.ERROR_MESSAGE);
    }

    private void styleTable() {
        table.setRowHeight(22);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getTableHeader().setReorderingAllowed(false);
        // Colonne ID plus étroite
        table.getColumnModel().getColumn(0).setPreferredWidth(70);
    }
}
