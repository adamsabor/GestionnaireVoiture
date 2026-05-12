package util;

import controller.GestionnaireFlotte;
import exception.*;
import model.*;

import java.time.LocalDate;
import java.util.List;

/**
 * Injecte des données de démonstration au premier lancement.
 * Permet de tester toutes les fonctionnalités sans saisie manuelle.
 */
public class DonneesTest {

    public static void charger(GestionnaireFlotte g) {

        // ===== VÉHICULES =====
        VehiculeLeger v1 = new VehiculeLeger("AB-123-CD", "Renault", "Kangoo", 2021, 18_000);
        v1.setKilometrage(42_300);

        VehiculeLeger v2 = new VehiculeLeger("EF-456-GH", "Peugeot", "Partner", 2022, 20_500);
        v2.setKilometrage(18_700);

        VehiculeLourd v3 = new VehiculeLourd("IJ-789-KL", "Mercedes", "Actros", 2020, 95_000, 20.0);
        v3.setKilometrage(130_000);

        VehiculeLourd v4 = new VehiculeLourd("MN-012-OP", "Volvo", "FH16", 2019, 110_000, 25.0);
        v4.setKilometrage(210_500);

        VehiculeSpecial v5 = new VehiculeSpecial("QR-345-ST", "Ford", "Transit", 2023, 35_000, "Ambulance");
        v5.setKilometrage(8_200);

        g.ajouterVehicule(v1);
        g.ajouterVehicule(v2);
        g.ajouterVehicule(v3);
        g.ajouterVehicule(v4);
        g.ajouterVehicule(v5);

        // ===== CHAUFFEURS =====
        Chauffeur c1 = new Chauffeur("Behouch", "Youness", "06 11 22 33 44", List.of(TypePermis.B));
        Chauffeur c2 = new Chauffeur("Bouabizi", "Yazid", "06 55 66 77 88", List.of(TypePermis.B, TypePermis.C));
        Chauffeur c3 = new Chauffeur("Sabor", "Adam", "07 12 34 56 78", List.of(TypePermis.B, TypePermis.C, TypePermis.CE));
        c1.setNombreKmTotal(45_000);
        c2.setNombreKmTotal(82_000);
        c3.setNombreKmTotal(120_000);
       

        g.ajouterChauffeur(c1);
        g.ajouterChauffeur(c2);
        g.ajouterChauffeur(c3);
        

        // ===== ENTRETIENS =====
        LocalDate hier     = LocalDate.now().minusDays(1);
        LocalDate dans10j  = LocalDate.now().plusDays(10);
        LocalDate dansUnMois = LocalDate.now().plusDays(30);

        Entretien e1 = new Entretien("Vidange", hier, 85.0, "Vidange + filtre huile", v1.getId());
        e1.marquerRealise();
        g.ajouterEntretien(e1);

        Entretien e2 = new Entretien("Révision", dans10j, 320.0, "Révision 130 000 km", v3.getId());
        g.ajouterEntretien(e2);

        Entretien e3 = new Entretien("Pneus", dansUnMois, 600.0, "Remplacement 4 pneus", v4.getId());
        g.ajouterEntretien(e3);

        Entretien e4 = new Entretien("Freins", LocalDate.now().minusDays(5), 250.0, "Plaquettes avant", v2.getId());
        // Cet entretien est en retard — utile pour les statistiques
        g.ajouterEntretien(e4);

        // ===== MISSIONS (planifiées ou terminées) =====
        MissionCourte m1 = new MissionCourte("Livraison Paris-Lyon", "Paris", "Lyon",
            LocalDate.now().minusDays(3), 465.0);
        m1.setStatut(StatutMission.TERMINEE);
        m1.setVehiculeAssocie(v1);
        m1.setChauffeurAssocie(c1);
        g.ajouterMission(m1);

        MissionLongue m2 = new MissionLongue("Transport Bordeaux-Strasbourg", "Bordeaux", "Strasbourg",
            LocalDate.now().plusDays(2), LocalDate.now().plusDays(4), 870.0);
        g.ajouterMission(m2);

        MissionCourte m3 = new MissionCourte("Collecte Marseille", "Marseille", "Toulon",
            LocalDate.now(), 65.0);
        // Cette mission sera lancée via l'interface
        g.ajouterMission(m3);

        try {
            // Lance une mission pour montrer l'état EN_COURS dans l'interface
            // On utilise v1 car son entretien (e1) est déjà marqué réalisé → v1 est DISPONIBLE
            MissionCourte m4 = new MissionCourte("Livraison express Nantes", "Nantes", "Rennes",
                LocalDate.now(), 110.0);
            g.ajouterMission(m4);
            g.assignerMission(m4, v1, c3);
        } catch (VehiculeIndisponibleException | ChauffeurSansPermisException | MissionDejaAssigneeException ex) {
            System.err.println("Erreur données test : " + ex.getMessage());
        }
    }
}
