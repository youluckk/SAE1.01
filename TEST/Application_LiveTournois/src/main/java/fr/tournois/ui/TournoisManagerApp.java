package fr.tournois.ui;

import fr.tournois.dao.DAOException;
import fr.tournois.dao.JoueurDAO;
import fr.tournois.dao.UtilisateurDAO;
import fr.tournois.model.Utilisateur;
import fr.tournois.security.PasswordHasher;
import fr.tournois.security.SecurityContext;
import fr.tournois.ui.controller.AppMainFrameController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;
import fr.tournois.dao.ConnectionManager;

public class TournoisManagerApp extends Application {

    private Stage primaryStage;
    private Connection connection;

    /* @author: Eliot 
     * pour après faire une injection de joueurDAO et établir une connexion
     */
    
    private JoueurDAO joueurDAO;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.primaryStage.setTitle("Gestionnaire de Tournois");

        // Initialisation de la connexion à la base de données
        try {
            connection = ConnectionManager.getInstance().getConnection();
            // Définition de du joueur DAO via la connection
            this.joueurDAO = new JoueurDAO(connection);
            /*
            // TODO: SUPPRIMER AVANT LIVRAISON - Début connexion automatique
            try {
                UtilisateurDAO utilisateurDAO = new UtilisateurDAO(connection);
                Optional<Utilisateur> adminOpt = utilisateurDAO.findByPseudo("admin");
                if (adminOpt.isPresent()) {
                    Utilisateur admin = adminOpt.get();
                    if (PasswordHasher.verifyPassword("admin123", admin.getPassword())) {
                        SecurityContext.getInstance().setCurrentUser(admin);
                    }
                }
            } catch (DAOException e) {
                e.printStackTrace();
            }
            // TODO: SUPPRIMER AVANT LIVRAISON - Fin connexion automatique
            */
            this.joueurDAO = new JoueurDAO(connection);

        } catch (SQLException e) {
            e.printStackTrace();
            return;
        }

        // Initialisation de la fenêtre principale
        try {
            // Chargement du style global
            URL cssUrl = TournoisManagerApp.class.getResource("css/style.css");
            if (cssUrl == null) {
                System.err.println("Le fichier de style n'a pas été trouvé !");
                return;
            }

            // Chargement de la main frame
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(TournoisManagerApp.class.getResource("fxml/AppMainFrame.fxml"));
            BorderPane mainFrame = loader.load();
            // Injection de la connexion dans le contrôleur principal
            AppMainFrameController mainFrameController = loader.getController();
            mainFrameController.setConnection(connection);

            /* @author: Eliot 
             * injection de Joueur DAO   
             */
            if (this.joueurDAO != null) { // Utilise this.joueurDAO car c'est un champ de la classe
                mainFrameController.setJoueurDAO(this.joueurDAO);
            } else {
                System.err.println("Erreur critique: JoueurDAO n'a pu être créé.");
                // Ce scénario ne devrait idéalement pas se produire si l'exception SQLException
                // ci-dessus est gérée,
                // mais c'est une vérification défensive.
            }

            Scene scene = new Scene(mainFrame, 800, 600);
            scene.getStylesheets().add(cssUrl.toExternalForm());

            primaryStage.setScene(scene);
            // primaryStage.show();
            mainFrameController.displayTemporaire(primaryStage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Lancement de l'application.
     */
    public static void runApp(String[] args) {
        launch(args);
    }

    /**
     * Retourne la fenêtre principale.
     */
    public Stage getPrimaryStage() {
        return primaryStage;
    }

    @Override
    public void stop() {
        // Fermer la connexion à la base de données
        try {
            ConnectionManager.getInstance().close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
