package view;

import controller.GestionnaireFlotte;
import exception.*;
import model.*;

import javax.swing.*;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * Panneau de gestion des missions.
 *
 * Permet de créer, filtrer, lancer (assigner véhicule + chauffeur),
 * terminer ou annuler une mission.
 */
public class MissionPanel extends JPanel {

    private final GestionnaireFlotte gestionnaire;
    private final MissionTableModel  tableModel;
    private final JTable             table;

    private final JTextField         filtreNom   = new JTextField(12);
    private final JComboBox<Object>  filtreStatut = new JComboBox<>();
    private final JComboBox<String>  filtreType   = new JComboBox<>(new String[]{"Tous", "Courte", "Longue"});

    public MissionPanel(GestionnaireFlotte gestionnaire) {
        this.gestionnaire = gestionnaire;
        this.tableModel   = new MissionTableModel(gestionnaire.getToutesLesMissions());
        this.table        = new JTable(tableModel);

        filtreStatut.addItem(null); // "Tous" = null
        for (StatutMission s : StatutMission.values()) filtreStatut.addItem(s);

        table.setRowSorter(new TableRowSorter<>(tableModel));

        setLayout(new BorderLayout(5, 5));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(creerFiltres(),         BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(creerBoutons(),         BorderLayout.SOUTH);

        styleTable();
    }

    private JPanel creerFiltres() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        p.setBorder(BorderFactory.createTitledBorder("Filtres"));
        p.add(new JLabel("Lieu :"));   p.add(filtreNom);
        p.add(new JLabel("Statut :")); p.add(filtreStatut);
        p.add(new JLabel("Type :"));   p.add(filtreType);
        JButton btnF = new JButton("Filtrer");
        JButton btnR = new JButton("Réinitialiser");
        btnF.addActionListener(e -> appliquerFiltres());
        btnR.addActionListener(e -> reinitialiser());
        p.add(btnF); p.add(btnR);
        return p;
    }

    private JPanel creerBoutons() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        JButton btnA = new JButton("Ajouter");
        JButton btnL = new JButton("Lancer (assigner)");
        JButton btnT = new JButton("Terminer");
        JButton btnX = new JButton("Annuler");
        JButton btnS = new JButton("Supprimer");

        btnA.addActionListener(e -> actionAjouter());
        btnL.addActionListener(e -> actionAssigner());
        btnT.addActionListener(e -> actionTerminer());
        btnX.addActionListener(e -> actionAnnuler());
        btnS.addActionListener(e -> actionSupprimer());

        p.add(btnA); p.add(btnL); p.add(btnT); p.add(btnX); p.add(btnS);
        return p;
    }

    private void appliquerFiltres() {
        String        lieu   = filtreNom.getText().trim();
        StatutMission statut = (StatutMission) filtreStatut.getSelectedItem();
        String        type   = filtreType.getSelectedIndex() == 0 ? null : (String) filtreType.getSelectedItem();
        tableModel.setDonnees(gestionnaire.filtrerMissions(statut, lieu, type));
    }

    private void reinitialiser() {
        filtreNom.setText("");
        filtreStatut.setSelectedIndex(0);
        filtreType.setSelectedIndex(0);
        tableModel.setDonnees(gestionnaire.getToutesLesMissions());
    }

    /** Formulaire de création d'une nouvelle mission. */
    private void actionAjouter() {
        JTextField titreF   = new JTextField(15);
        JTextField departF  = new JTextField(12);
        JTextField arriveeF = new JTextField(12);
        JTextField dateDF   = new JTextField(LocalDate.now().toString(), 10);
        JTextField dateFin  = new JTextField(LocalDate.now().toString(), 10);
        JTextField distF    = new JTextField("100", 8);
        JComboBox<String> typeBox = new JComboBox<>(new String[]{"Courte", "Longue"});

        // Quand on choisit "Courte", la date de fin se désactive
        typeBox.addActionListener(e -> dateFin.setEnabled("Longue".equals(typeBox.getSelectedItem())));
        dateFin.setEnabled(false);

        JPanel form = new JPanel(new GridLayout(0, 2, 5, 5));
        form.add(new JLabel("Titre :"));      form.add(titreF);
        form.add(new JLabel("Départ :"));     form.add(departF);
        form.add(new JLabel("Arrivée :"));    form.add(arriveeF);
        form.add(new JLabel("Date début (AAAA-MM-JJ) :")); form.add(dateDF);
        form.add(new JLabel("Date fin :"));   form.add(dateFin);
        form.add(new JLabel("Distance km :")); form.add(distF);
        form.add(new JLabel("Type :"));       form.add(typeBox);

        int rep = JOptionPane.showConfirmDialog(this, form, "Nouvelle mission", JOptionPane.OK_CANCEL_OPTION);
        if (rep != JOptionPane.OK_OPTION) return;

        try {
            String titre   = titreF.getText().trim();
            String depart  = departF.getText().trim();
            String arrivee = arriveeF.getText().trim();
            if (titre.isBlank() || depart.isBlank() || arrivee.isBlank()) {
                erreur("Titre, départ et arrivée sont obligatoires."); return;
            }
            LocalDate dd   = LocalDate.parse(dateDF.getText().trim());
            LocalDate df   = LocalDate.parse(dateFin.getText().trim());
            double dist    = Double.parseDouble(distF.getText().trim());
            String type    = (String) typeBox.getSelectedItem();

            Mission m = "Longue".equals(type)
                ? new MissionLongue(titre, depart, arrivee, dd, df, dist)
                : new MissionCourte(titre, depart, arrivee, dd, dist);

            gestionnaire.ajouterMission(m);
            rafraichir();
        } catch (DateTimeParseException ex) {
            erreur("Date invalide. Format attendu : AAAA-MM-JJ");
        } catch (NumberFormatException ex) {
            erreur("Distance invalide.");
        }
    }

    /**
     * Lance une mission planifiée : choisit un véhicule et un chauffeur,
     * puis appelle le contrôleur qui vérifie toutes les règles métier.
     */
    private void actionAssigner() {
        int row = table.getSelectedRow();
        if (row < 0) { erreur("Sélectionnez une mission."); return; }
        Mission m = tableModel.getMission(table.convertRowIndexToModel(row));

        if (m.getStatut() != StatutMission.PLANIFIEE) {
            erreur("Seules les missions planifiées peuvent être lancées."); return;
        }

        // Listes des véhicules et chauffeurs disponibles uniquement
        List<Vehicule>  vehs = gestionnaire.filtrerVehicules(null, EtatVehicule.DISPONIBLE, null);
        List<Chauffeur> chfs = gestionnaire.filtrerChauffeurs(true, null, null);

        if (vehs.isEmpty()) { erreur("Aucun véhicule disponible."); return; }
        if (chfs.isEmpty()) { erreur("Aucun chauffeur disponible."); return; }

        JComboBox<Vehicule>  vehBox = new JComboBox<>(vehs.toArray(new Vehicule[0]));
        JComboBox<Chauffeur> chfBox = new JComboBox<>(chfs.toArray(new Chauffeur[0]));

        JPanel form = new JPanel(new GridLayout(0, 2, 5, 5));
        form.add(new JLabel("Véhicule :"));  form.add(vehBox);
        form.add(new JLabel("Chauffeur :")); form.add(chfBox);

        int rep = JOptionPane.showConfirmDialog(this, form, "Assigner la mission", JOptionPane.OK_CANCEL_OPTION);
        if (rep != JOptionPane.OK_OPTION) return;

        Vehicule  veh = (Vehicule)  vehBox.getSelectedItem();
        Chauffeur chf = (Chauffeur) chfBox.getSelectedItem();

        try {
            gestionnaire.assignerMission(m, veh, chf);
            rafraichir();
            JOptionPane.showMessageDialog(this, "Mission lancée avec succès !");
        } catch (VehiculeIndisponibleException | ChauffeurSansPermisException | MissionDejaAssigneeException ex) {
            erreur(ex.getMessage());
        }
    }

    private void actionTerminer() {
        int row = table.getSelectedRow();
        if (row < 0) { erreur("Sélectionnez une mission."); return; }
        Mission m = tableModel.getMission(table.convertRowIndexToModel(row));

        if (m.getStatut() != StatutMission.EN_COURS) {
            erreur("Seules les missions en cours peuvent être terminées."); return;
        }
        gestionnaire.terminerMission(m.getId());
        rafraichir();
    }

    private void actionAnnuler() {
        int row = table.getSelectedRow();
        if (row < 0) { erreur("Sélectionnez une mission."); return; }
        Mission m = tableModel.getMission(table.convertRowIndexToModel(row));

        if (m.getStatut() == StatutMission.TERMINEE) {
            erreur("Impossible d'annuler une mission déjà terminée."); return;
        }
        int conf = JOptionPane.showConfirmDialog(this, "Annuler la mission « " + m.getTitre() + " » ?",
            "Confirmation", JOptionPane.YES_NO_OPTION);
        if (conf == JOptionPane.YES_OPTION) { gestionnaire.annulerMission(m.getId()); rafraichir(); }
    }

    private void actionSupprimer() {
        int row = table.getSelectedRow();
        if (row < 0) { erreur("Sélectionnez une mission."); return; }
        Mission m = tableModel.getMission(table.convertRowIndexToModel(row));

        int conf = JOptionPane.showConfirmDialog(this, "Supprimer la mission « " + m.getTitre() + " » ?",
            "Confirmation", JOptionPane.YES_NO_OPTION);
        if (conf != JOptionPane.YES_OPTION) return;

        if (!gestionnaire.supprimerMission(m.getId())) {
            erreur("Impossible : la mission est en cours.");
        } else {
            rafraichir();
        }
    }

    public void rafraichir() { tableModel.setDonnees(gestionnaire.getToutesLesMissions()); }

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
