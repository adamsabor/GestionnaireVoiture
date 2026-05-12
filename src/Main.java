import view.MainFrame;

import javax.swing.*;

/**
 * Point d'entrée de l'application.
 *
 * SwingUtilities.invokeLater() garantit que la création de l'interface
 * se fait dans l'Event Dispatch Thread (EDT), comme l'exige Swing.
 * On ne crée JAMAIS de JFrame directement depuis le thread main.
 */
public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MainFrame fenetre = new MainFrame();
            fenetre.setVisible(true);
        });
    }
}
