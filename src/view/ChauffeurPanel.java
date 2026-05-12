package view;

import controller.GestionnaireFlotte;
import model.Chauffeur;
import model.TypePermis;

import javax.swing.*;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Panneau de gestion des chauffeurs.
 * CRUD complet + filtre par disponibilité, permis et nom.
 */
public class ChauffeurPanel extends JPanel {

    private final GestionnaireFlotte   gestionnaire;
    private final ChauffeurTableModel  tableModel;
    private final JTable               table;

    private final JTextField        filtreNom   = new JTextField(12);
    private final JComboBox<String>  filtreDisp  = new JComboBox<>(new String[]{"Tous", "Disponible", "En mission"});
    private final JComboBox<Object>  filtrePermis;

    public ChauffeurPanel(GestionnaireFlotte gestionnaire) {
        this.gestionnaire = gestionnaire;
        this.tableModel   = new ChauffeurTableModel(gestionnaire.getTousLesChauffeurs());
        this.table        = new JTable(tableModel);

        // Peuple le filtre permis
        Object[] permisItems = new Object[TypePermis.values().length + 1];
        permisItems[0] = "Tous";
        System.arraycopy(TypePermis.values(), 0, permisItems, 1, TypePermis.values().length);
        filtrePermis = new JComboBox<>(permisItems);

        TableRowSorter<ChauffeurTableModel> sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);

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

        p.add(new JLabel("Nom :")); p.add(filtreNom);
        p.add(new JLabel("Disponibilité :")); p.add(filtreDisp);
        p.add(new JLabel("Permis :")); p.add(filtrePermis);

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
        JButton btnM = new JButton("Modifier");
        JButton btnS = new JButton("Supprimer");
        btnA.addActionListener(e -> actionAjouter());
        btnM.addActionListener(e -> actionModifier());
        btnS.addActionListener(e -> actionSupprimer());
        p.add(btnA); p.add(btnM); p.add(btnS);
        return p;
    }

    private void appliquerFiltres() {
        String nom      = filtreNom.getText().trim();
        Boolean dispon  = switch (filtreDisp.getSelectedIndex()) {
            case 1 -> true;
            case 2 -> false;
            default -> null;
        };
        Object sel = filtrePermis.getSelectedItem();
        TypePermis permis = sel instanceof TypePermis tp ? tp : null;

        tableModel.setDonnees(gestionnaire.filtrerChauffeurs(dispon, permis, nom));
    }

    private void reinitialiser() {
        filtreNom.setText("");
        filtreDisp.setSelectedIndex(0);
        filtrePermis.setSelectedIndex(0);
        tableModel.setDonnees(gestionnaire.getTousLesChauffeurs());
    }

    private void actionAjouter() {
        JTextField nomF    = new JTextField(12);
        JTextField prenomF = new JTextField(12);
        JTextField telF    = new JTextField(12);

        // Cases à cocher pour les permis
        JCheckBox[] casesPermis = new JCheckBox[TypePermis.values().length];
        JPanel casesPanel = new JPanel(new GridLayout(1, 0, 4, 0));
        for (int i = 0; i < TypePermis.values().length; i++) {
            casesPermis[i] = new JCheckBox(TypePermis.values()[i].name());
            casesPanel.add(casesPermis[i]);
        }
        casesPermis[0].setSelected(true); // Permis B par défaut

        JPanel form = new JPanel(new GridLayout(0, 2, 5, 5));
        form.add(new JLabel("Nom :"));    form.add(nomF);
        form.add(new JLabel("Prénom :")); form.add(prenomF);
        form.add(new JLabel("Tél. :"));   form.add(telF);
        form.add(new JLabel("Permis :")); form.add(casesPanel);

        int rep = JOptionPane.showConfirmDialog(this, form, "Nouveau chauffeur", JOptionPane.OK_CANCEL_OPTION);
        if (rep != JOptionPane.OK_OPTION) return;

        String nom    = nomF.getText().trim();
        String prenom = prenomF.getText().trim();
        if (nom.isBlank() || prenom.isBlank()) { erreur("Nom et prénom obligatoires."); return; }

        List<TypePermis> permis = new ArrayList<>();
        for (int i = 0; i < casesPermis.length; i++) {
            if (casesPermis[i].isSelected()) permis.add(TypePermis.values()[i]);
        }
        if (permis.isEmpty()) { erreur("Le chauffeur doit avoir au moins un permis."); return; }

        gestionnaire.ajouterChauffeur(new Chauffeur(nom, prenom, telF.getText().trim(), permis));
        rafraichir();
    }

    private void actionModifier() {
        int row = table.getSelectedRow();
        if (row < 0) { erreur("Sélectionnez un chauffeur."); return; }
        Chauffeur c = tableModel.getChauffeur(table.convertRowIndexToModel(row));

        JTextField nomF    = new JTextField(c.getNom(), 12);
        JTextField prenomF = new JTextField(c.getPrenom(), 12);
        JTextField telF    = new JTextField(c.getTelephone(), 12);

        JCheckBox[] casesPermis = new JCheckBox[TypePermis.values().length];
        JPanel casesPanel = new JPanel(new GridLayout(1, 0, 4, 0));
        for (int i = 0; i < TypePermis.values().length; i++) {
            casesPermis[i] = new JCheckBox(TypePermis.values()[i].name(), c.aLePermis(TypePermis.values()[i]));
            casesPanel.add(casesPermis[i]);
        }

        JPanel form = new JPanel(new GridLayout(0, 2, 5, 5));
        form.add(new JLabel("Nom :"));    form.add(nomF);
        form.add(new JLabel("Prénom :")); form.add(prenomF);
        form.add(new JLabel("Tél. :"));   form.add(telF);
        form.add(new JLabel("Permis :")); form.add(casesPanel);

        int rep = JOptionPane.showConfirmDialog(this, form, "Modifier le chauffeur", JOptionPane.OK_CANCEL_OPTION);
        if (rep != JOptionPane.OK_OPTION) return;

        c.setNom(nomF.getText().trim());
        c.setPrenom(prenomF.getText().trim());
        c.setTelephone(telF.getText().trim());

        List<TypePermis> permis = new ArrayList<>();
        for (int i = 0; i < casesPermis.length; i++) {
            if (casesPermis[i].isSelected()) permis.add(TypePermis.values()[i]);
        }
        if (!permis.isEmpty()) c.setPermis(permis);
        rafraichir();
    }

    private void actionSupprimer() {
        int row = table.getSelectedRow();
        if (row < 0) { erreur("Sélectionnez un chauffeur."); return; }
        Chauffeur c = tableModel.getChauffeur(table.convertRowIndexToModel(row));

        int conf = JOptionPane.showConfirmDialog(this,
            "Supprimer " + c.getNomComplet() + " ?", "Confirmation", JOptionPane.YES_NO_OPTION);
        if (conf != JOptionPane.YES_OPTION) return;

        if (!gestionnaire.supprimerChauffeur(c.getId())) {
            erreur("Impossible : ce chauffeur est en mission.");
        } else {
            rafraichir();
        }
    }

    public void rafraichir() { tableModel.setDonnees(gestionnaire.getTousLesChauffeurs()); }

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
