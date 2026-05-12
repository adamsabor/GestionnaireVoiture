package view;

import controller.GestionnaireFlotte;
import model.*;

import javax.swing.*;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * Panneau de gestion de la maintenance.
 *
 * Permet de planifier des entretiens, de les marquer comme réalisés
 * et de voir lesquels sont en retard.
 */
public class EntretienPanel extends JPanel {

    private final GestionnaireFlotte  gestionnaire;
    private final EntretienTableModel tableModel;
    private final JTable              table;

    private final JComboBox<Object>  filtreVehicule = new JComboBox<>();
    private final JComboBox<String>  filtreStatut   = new JComboBox<>(new String[]{"Tous", "Planifié", "Réalisé", "En retard"});

    public EntretienPanel(GestionnaireFlotte gestionnaire) {
        this.gestionnaire = gestionnaire;
        this.tableModel   = new EntretienTableModel(gestionnaire.getTousLesEntretiens());
        this.table        = new JTable(tableModel);
        table.setRowSorter(new TableRowSorter<>(tableModel));

        setLayout(new BorderLayout(5, 5));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(creerFiltres(),         BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(creerBoutons(),         BorderLayout.SOUTH);

        styleTable();
        peuplerFiltreVehicule();
    }

    private void peuplerFiltreVehicule() {
        filtreVehicule.addItem(null); // = "Tous"
        gestionnaire.getTousLesVehicules().forEach(v -> filtreVehicule.addItem(v));
    }

    private JPanel creerFiltres() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        p.setBorder(BorderFactory.createTitledBorder("Filtres"));
        p.add(new JLabel("Véhicule :"));        p.add(filtreVehicule);
        p.add(new JLabel("Statut :"));          p.add(filtreStatut);
        JButton btnF = new JButton("Filtrer");
        JButton btnR = new JButton("Réinitialiser");
        btnF.addActionListener(e -> appliquerFiltres());
        btnR.addActionListener(e -> reinitialiser());
        p.add(btnF); p.add(btnR);
        return p;
    }

    private JPanel creerBoutons() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        JButton btnA  = new JButton("Planifier entretien");
        JButton btnOK = new JButton("Marquer réalisé");
        JButton btnS  = new JButton("Supprimer");

        btnA.addActionListener(e  -> actionAjouter());
        btnOK.addActionListener(e -> actionMarquerRealise());
        btnS.addActionListener(e  -> actionSupprimer());

        p.add(btnA); p.add(btnOK); p.add(btnS);
        return p;
    }

    private void appliquerFiltres() {
        List<Entretien> tous = gestionnaire.getTousLesEntretiens();

        Object selVeh = filtreVehicule.getSelectedItem();
        String vidVeh = selVeh instanceof Vehicule v ? v.getId() : null;

        String statut = (String) filtreStatut.getSelectedItem();

        List<Entretien> filtres = tous.stream()
            .filter(e -> vidVeh == null || e.getVehiculeId().equals(vidVeh))
            .filter(e -> switch (statut) {
                case "Planifié"  -> !e.estRealise() && !e.estEnRetard();
                case "Réalisé"   -> e.estRealise();
                case "En retard" -> e.estEnRetard();
                default          -> true;
            })
            .toList();

        tableModel.setDonnees(filtres);
    }

    private void reinitialiser() {
        filtreVehicule.setSelectedIndex(0);
        filtreStatut.setSelectedIndex(0);
        tableModel.setDonnees(gestionnaire.getTousLesEntretiens());
    }

    private void actionAjouter() {
        List<Vehicule> vehs = gestionnaire.getTousLesVehicules();
        if (vehs.isEmpty()) { erreur("Aucun véhicule enregistré."); return; }

        JComboBox<Vehicule> vehBox = new JComboBox<>(vehs.toArray(new Vehicule[0]));
        JTextField typeF    = new JTextField(10);
        JTextField dateF    = new JTextField(LocalDate.now().plusDays(7).toString(), 10);
        JTextField coutF    = new JTextField("0.0", 8);
        JTextField descF    = new JTextField(20);

        // Si un véhicule manque d'entretien, on le présélectionne
        gestionnaire.getVehiculesSansEntretienRecent().stream().findFirst().ifPresent(vehBox::setSelectedItem);

        JPanel form = new JPanel(new GridLayout(0, 2, 5, 5));
        form.add(new JLabel("Véhicule :"));            form.add(vehBox);
        form.add(new JLabel("Type (Vidange…) :"));     form.add(typeF);
        form.add(new JLabel("Date prévue (AAAA-MM-JJ) :")); form.add(dateF);
        form.add(new JLabel("Coût estimé (€) :"));    form.add(coutF);
        form.add(new JLabel("Description :"));         form.add(descF);

        int rep = JOptionPane.showConfirmDialog(this, form, "Planifier un entretien", JOptionPane.OK_CANCEL_OPTION);
        if (rep != JOptionPane.OK_OPTION) return;

        try {
            Vehicule veh   = (Vehicule) vehBox.getSelectedItem();
            String   type  = typeF.getText().trim();
            LocalDate date = LocalDate.parse(dateF.getText().trim());
            double   cout  = Double.parseDouble(coutF.getText().trim());
            String   desc  = descF.getText().trim();

            if (type.isBlank()) { erreur("Le type d'entretien est obligatoire."); return; }

            Entretien e = new Entretien(type, date, cout, desc, veh.getId());
            gestionnaire.ajouterEntretien(e);

            // Recharge le filtre véhicule au cas où la liste a changé
            peuplerFiltreVehicule();
            rafraichir();
        } catch (DateTimeParseException ex) {
            erreur("Date invalide. Format : AAAA-MM-JJ");
        } catch (NumberFormatException ex) {
            erreur("Coût invalide.");
        }
    }

    private void actionMarquerRealise() {
        int row = table.getSelectedRow();
        if (row < 0) { erreur("Sélectionnez un entretien."); return; }
        Entretien e = tableModel.getEntretien(table.convertRowIndexToModel(row));

        if (e.estRealise()) { erreur("Cet entretien est déjà réalisé."); return; }

        e.marquerRealise();

        // Libère le véhicule s'il était bloqué en maintenance
        gestionnaire.trouverVehicule(e.getVehiculeId()).ifPresent(v -> {
            if (v.getEtat() == EtatVehicule.EN_MAINTENANCE) v.setEtat(EtatVehicule.DISPONIBLE);
        });
        rafraichir();
    }

    private void actionSupprimer() {
        int row = table.getSelectedRow();
        if (row < 0) { erreur("Sélectionnez un entretien."); return; }
        Entretien e = tableModel.getEntretien(table.convertRowIndexToModel(row));

        int conf = JOptionPane.showConfirmDialog(this,
            "Supprimer l'entretien « " + e.getTypeEntretien() + " » ?",
            "Confirmation", JOptionPane.YES_NO_OPTION);
        if (conf == JOptionPane.YES_OPTION) { gestionnaire.supprimerEntretien(e.getId()); rafraichir(); }
    }

    public void rafraichir() { tableModel.setDonnees(gestionnaire.getTousLesEntretiens()); }

    private void erreur(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Erreur", JOptionPane.ERROR_MESSAGE);
    }

    private void styleTable() {
        table.setRowHeight(22);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getTableHeader().setReorderingAllowed(false);
        table.getColumnModel().getColumn(0).setPreferredWidth(70);
    }
}
