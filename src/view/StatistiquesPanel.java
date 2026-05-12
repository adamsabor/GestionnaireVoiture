package view;

import controller.GestionnaireFlotte;
import model.*;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Panneau de statistiques.
 *
 * Affiche des indicateurs calculés dynamiquement via Stream :
 *   - Répartition de la flotte par état
 *   - Kilométrage total
 *   - Coûts (missions + entretiens)
 *   - Top chauffeur
 *   - Véhicules nécessitant un entretien
 */
public class StatistiquesPanel extends JPanel {

    private final GestionnaireFlotte gestionnaire;
    private final JPanel             contenu;

    public StatistiquesPanel(GestionnaireFlotte gestionnaire) {
        this.gestionnaire = gestionnaire;
        this.contenu      = new JPanel();
        contenu.setLayout(new BoxLayout(contenu, BoxLayout.Y_AXIS));

        setLayout(new BorderLayout(5, 5));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JButton btnRafraichir = new JButton("Actualiser les statistiques");
        btnRafraichir.addActionListener(e -> rafraichir());
        btnRafraichir.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(btnRafraichir);

        add(top, BorderLayout.NORTH);
        add(new JScrollPane(contenu), BorderLayout.CENTER);

        rafraichir();
    }

    /** Recalcule et réaffiche toutes les statistiques. */
    public void rafraichir() {
        contenu.removeAll();

        contenu.add(creerSection("Répartition de la flotte par état", buildRepartition()));
        contenu.add(Box.createVerticalStrut(15));
        contenu.add(creerSection("Indicateurs globaux", buildIndicateurs()));
        contenu.add(Box.createVerticalStrut(15));
        contenu.add(creerSection("Véhicules nécessitant un entretien", buildAlertesEntretien()));
        contenu.add(Box.createVerticalStrut(15));
        contenu.add(creerSection("Top 3 chauffeurs (km parcourus)", buildTopChauffeurs()));
        contenu.add(Box.createVerticalStrut(15));
        contenu.add(creerSection("Missions du mois en cours", buildMissionsMois()));

        contenu.revalidate();
        contenu.repaint();
    }

    /** Répartition des véhicules par état sous forme de texte. */
    private String buildRepartition() {
        Map<EtatVehicule, Long> stats = gestionnaire.getStatsParEtat();
        int total = gestionnaire.getTousLesVehicules().size();

        StringBuilder sb = new StringBuilder();
        sb.append("  Flotte totale : ").append(total).append(" véhicule(s)\n\n");
        for (EtatVehicule etat : EtatVehicule.values()) {
            long n = stats.getOrDefault(etat, 0L);
            int pct = total == 0 ? 0 : (int) (n * 100 / total);
            sb.append("  ").append(etat.getLibelle())
              .append(" : ").append(n)
              .append(" (").append(pct).append("%)\n");
        }
        return sb.toString();
    }

    /** Indicateurs financiers et kilométriques. */
    private String buildIndicateurs() {
        int    kmTotal         = gestionnaire.getKilometrageTotal();
        double coutMissions    = gestionnaire.getCoutTotalMissions();
        double coutEntretiens  = gestionnaire.getCoutTotalEntretiens();
        double coutTotal       = coutMissions + coutEntretiens;
        double moyenneMissions = gestionnaire.getMoyenneMissionsParChauffeur();
        int    enRetard        = gestionnaire.getEntretiensEnRetard().size();

        return String.format(
            "  Kilométrage total flotte : %,d km%n" +
            "  Coût total missions terminées : %.2f €%n" +
            "  Coût total entretiens réalisés : %.2f €%n" +
            "  Coût total global : %.2f €%n" +
            "  Moyenne missions/chauffeur : %.1f%n" +
            "  Entretiens en retard : %d",
            kmTotal, coutMissions, coutEntretiens, coutTotal, moyenneMissions, enRetard
        );
    }

    /** Liste des véhicules dont le kilométrage dépasse le seuil d'entretien. */
    private String buildAlertesEntretien() {
        List<Vehicule> alertes = gestionnaire.getVehiculesSansEntretienRecent();
        if (alertes.isEmpty()) return "  Aucun véhicule en alerte — entretiens à jour !";

        return alertes.stream()
            .map(v -> "  ⚠  " + v.getImmatriculation() + " — " + v.getMarque() + " " + v.getModele()
                + " (" + v.getKilometrage() + " km)")
            .collect(Collectors.joining("\n"));
    }

    /** Les 3 meilleurs chauffeurs en kilométrage cumulé. */
    private String buildTopChauffeurs() {
        List<Chauffeur> top = gestionnaire.getTousLesChauffeurs().stream()
            .sorted((a, b) -> Integer.compare(b.getNombreKmTotal(), a.getNombreKmTotal()))
            .limit(3)
            .toList();

        if (top.isEmpty()) return "  Aucun chauffeur enregistré.";

        StringBuilder sb = new StringBuilder();
        int rang = 1;
        for (Chauffeur c : top) {
            sb.append("  ").append(rang++).append(". ")
              .append(c.getNomComplet())
              .append(" — ").append(String.format("%,d", c.getNombreKmTotal())).append(" km")
              .append(" — ").append(c.getNombreMissions()).append(" mission(s)\n");
        }
        return sb.toString();
    }

    /** Résumé des missions planifiées ce mois-ci. */
    private String buildMissionsMois() {
        List<Mission> missions = gestionnaire.getMissionsDuMois();
        if (missions.isEmpty()) return "  Aucune mission ce mois-ci.";

        return missions.stream()
            .map(m -> "  " + m.getDateDebut() + "  " + m.getTitre()
                + " (" + m.getLieuDepart() + " → " + m.getLieuArrivee() + ")"
                + " — " + m.getStatut())
            .collect(Collectors.joining("\n"));
    }

    /** Crée un panneau titré avec un bloc de texte monospace. */
    private JPanel creerSection(String titre, String texte) {
        JPanel section = new JPanel(new BorderLayout(5, 5));
        section.setBorder(BorderFactory.createTitledBorder(titre));
        section.setAlignmentX(Component.LEFT_ALIGNMENT);
        section.setMaximumSize(new Dimension(Integer.MAX_VALUE, section.getPreferredSize().height + 200));

        JTextArea area = new JTextArea(texte);
        area.setEditable(false);
        area.setFont(new Font("Monospaced", Font.PLAIN, 13));
        area.setBackground(section.getBackground());
        area.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        section.add(area, BorderLayout.CENTER);
        return section;
    }
}
