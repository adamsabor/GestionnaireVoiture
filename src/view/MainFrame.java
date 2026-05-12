package view;

import controller.GestionnaireFlotte;
import util.DonneesTest;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Fenêtre principale de l'application.
 *
 * Contient un JTabbedPane avec cinq onglets :
 *   Véhicules | Chauffeurs | Missions | Entretiens | Statistiques
 *
 * Lance le contrôleur, charge les données, et sauvegarde à la fermeture.
 */
public class MainFrame extends JFrame {

    private final GestionnaireFlotte gestionnaire;

    // Références pour pouvoir les rafraîchir depuis d'autres onglets
    private VehiculePanel     vehiculePanel;
    private ChauffeurPanel    chauffeurPanel;
    private MissionPanel      missionPanel;
    private EntretienPanel    entretienPanel;
    private StatistiquesPanel statistiquesPanel;

    public MainFrame() {
        this.gestionnaire = new GestionnaireFlotte();
        chargerDonnees();
        configurerApparence();
        construireUI();
        configurerFenetre();
    }

    private void chargerDonnees() {
        gestionnaire.charger();
        // Premier lancement : injecte des données de démonstration
        if (gestionnaire.getTousLesVehicules().isEmpty()) {
            DonneesTest.charger(gestionnaire);
        }
    }

    private void configurerApparence() {
        try {
            // Nimbus donne un rendu plus propre que le Look & Feel Metal par défaut
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (Exception ignored) {
            // Si Nimbus n'est pas disponible, on garde le LnF par défaut
        }
    }

    private void construireUI() {
        vehiculePanel     = new VehiculePanel(gestionnaire);
        chauffeurPanel    = new ChauffeurPanel(gestionnaire);
        missionPanel      = new MissionPanel(gestionnaire);
        entretienPanel    = new EntretienPanel(gestionnaire);
        statistiquesPanel = new StatistiquesPanel(gestionnaire);

        JTabbedPane onglets = new JTabbedPane(JTabbedPane.TOP);
        onglets.setFont(new Font("SansSerif", Font.BOLD, 13));

        onglets.addTab("  Véhicules  ",  vehiculePanel);
        onglets.addTab("  Chauffeurs ",  chauffeurPanel);
        onglets.addTab("  Missions   ",  missionPanel);
        onglets.addTab("  Entretiens ",  entretienPanel);
        onglets.addTab("  Statistiques", statistiquesPanel);

        // Quand on passe sur l'onglet Statistiques, on les recalcule
        onglets.addChangeListener(e -> {
            if (onglets.getSelectedComponent() == statistiquesPanel) {
                statistiquesPanel.rafraichir();
            }
        });

        add(onglets, BorderLayout.CENTER);

        // Barre de statut
        JLabel statusBar = new JLabel("  Gestionnaire de Flotte — Prêt  |  "
            + gestionnaire.getTousLesVehicules().size() + " véhicule(s)  |  "
            + gestionnaire.getTousLesChauffeurs().size() + " chauffeur(s)");
        statusBar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, Color.LIGHT_GRAY),
            BorderFactory.createEmptyBorder(4, 10, 4, 10)
        ));
        add(statusBar, BorderLayout.SOUTH);
    }

    private void configurerFenetre() {
        setTitle("Gestionnaire de Flotte de Véhicules");
        setSize(1250, 780);
        setMinimumSize(new Dimension(900, 600));
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE); // géré manuellement

        // Centrer la fenêtre sur l'écran
        setLocationRelativeTo(null);

        // Demander si on sauvegarde à la fermeture
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                demanderSauvegarde();
            }
        });
    }

    private void demanderSauvegarde() {
        int choix = JOptionPane.showConfirmDialog(
            this,
            "Sauvegarder les données avant de quitter ?",
            "Fermeture",
            JOptionPane.YES_NO_CANCEL_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );
        if (choix == JOptionPane.YES_OPTION) {
            gestionnaire.sauvegarder();
            dispose();
            System.exit(0);
        } else if (choix == JOptionPane.NO_OPTION) {
            dispose();
            System.exit(0);
        }
        // CANCEL → on reste dans l'application
    }
}
