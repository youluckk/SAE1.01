package fr.tournois.ui.util;

import javafx.stage.Stage;
import javafx.stage.Window;

/**
 * Classe utilitaire pour la gestion des dialogues JavaFX.
 *
 * @author F. Pelleau &amp; A. Péninou
 * @since V0.0
 */
public class DialogUtils {
    
    private DialogUtils() {
        // Constructeur privé pour empêcher l'instanciation
    }
    
    /**
     * Centre une fenêtre de dialogue par rapport à sa fenêtre parente.
     * Le centrage est effectué lors de l'affichage de la fenêtre.
     * 
     * @param dialog la fenêtre de dialogue à centrer
     * @param parent la fenêtre parente par rapport à laquelle centrer le dialogue
     */
    public static void centerDialog(Stage dialog, Window parent) {
        if (dialog == null || parent == null) {
            return;
        }
        
        dialog.setOnShown(e -> {
            dialog.setX(parent.getX() + (parent.getWidth() - dialog.getWidth()) / 2);
            dialog.setY(parent.getY() + (parent.getHeight() - dialog.getHeight()) / 2);
        });
    }
}
