package fr.tournois;

import fr.tournois.ui.TournoisManagerApp;

/**
 * Point d'entrée principal de l'application de gestion des tournois.
 * Cette classe lance l'interface graphique de l'application.
 *
 * @author V. Veron - E. Olivencia - T. Larrose
 * @version 2
 * @since V0.0
 */
public class LiveTournois {

    /**
     * Méthode principale qui démarre l'application.
     * 
     * @param args arguments de la ligne de commande (non utilisés)
     */
    public static void main(String[] args) {
        TournoisManagerApp.runApp(args);
    }
}
