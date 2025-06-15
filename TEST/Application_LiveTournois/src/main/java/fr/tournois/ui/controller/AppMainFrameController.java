package fr.tournois.ui.controller;

import fr.tournois.dao.StaffDAO;
import fr.tournois.dao.TournoiDAO;
import fr.tournois.dao.UtilisateurDAO;
import fr.tournois.model.Equipe;
import fr.tournois.dao.AffectationDAO;
import fr.tournois.dao.EquipeDAO;
import fr.tournois.security.SecurityContext;
import fr.tournois.ui.util.DialogUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import fr.tournois.dao.JeuDAO;
import fr.tournois.dao.JoueurDAO;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import javax.print.DocFlavor.URL;

/**
 * Contrôleur principal de l'application.
 * Gère la fenêtre principale et la navigation entre les différentes
 * fonctionnalités.
 */
public class AppMainFrameController {
    @FXML
    private MenuBar menuBar;
    @FXML
    private MenuItem menuDeconnexion;
    @FXML
    private MenuItem menuQuitter;
    @FXML
    private MenuItem menuGestionTournois;
    @FXML
    private MenuItem menuStaff;
    @FXML
    private MenuItem menuConnexion;
    @FXML
    private Menu menuCompte;
    @FXML
    private Label labelUtilisateur;
    @FXML
    private StackPane mainContentPane;
    @FXML
    private MenuItem menuGestionJeux;
    @FXML
    private MenuItem menuUtilisateurs;
    @FXML
    private MenuItem menuGestionJoueurs;
    @FXML
    private MenuItem menuGestionEquipes;

    private TournoisManagementController tournoisManagementController;
    private StaffManagementController staffManagementController;
    private UtilisateursManagementController utilisateursManagementController;
    private EquipeController equipesManagementController;
    private JeuManagementController jeuxManagementController;

    private Connection connection;
    

    /*
     * @author: Eliot
     */
    private JoueurDAO joueurDAO;
    private Stage primaryStage;
    private TournoiDAO tournoiDAO;

    /**
     * Définit la connexion à la base de données et met à jour l'interface
     * 
     * @param connection la connexion à utiliser
     */
    public void setConnection(Connection connection) {
        this.connection = connection;
        // Mise à jour des menus car nouvelle connexion
        this.updateMenuCompte();
    }

    // Elle est essentielle pour le `stage.initOwner(this.primaryStage);` dans
    // `ouvrirGererJoueurs`
    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    /**
     * Met à jour l'affichage du menu Compte en fonction de l'état de connexion
     */
    private void updateMenuCompte() {
        SecurityContext securityContext = SecurityContext.getInstance();
        boolean connecte = securityContext.isAuthenticated();
        menuConnexion.setVisible(!connecte);
        menuDeconnexion.setVisible(connecte);
        if (labelUtilisateur != null) {
            if (connecte) {
                var u = securityContext.getCurrentUser();
                String nomComplet = u.getPseudo();
                labelUtilisateur.setText("Connecté : " + nomComplet + " (" + u.getRole().name() + ")");
            } else {
                labelUtilisateur.setText("Non connecté");
            }
        }
        updateMenuAccess();
    }

    /**
     * Met à jour l'accès aux menus en fonction du rôle de l'utilisateur.
     * - Non connecté : seuls les menus Fichier (Quitter) et Compte (Connexion) sont
     * accessibles
     * - ADMIN : accès complet à toutes les fonctionnalités
     * - ORGANISATEUR : accès restreint (pas de gestion des utilisateurs ni des
     * joueurs)
     */
    private void updateMenuAccess() {
        SecurityContext securityContext = SecurityContext.getInstance();
        boolean isAuthenticated = securityContext.isAuthenticated();
        boolean isAdmin = securityContext.isAdmin();
        boolean isOrganisateur = securityContext.isOrganisateur();

        // D'abord, désactiver tous les menus sauf les menus de base
        menuQuitter.setDisable(false);
        menuConnexion.setDisable(false);
        menuDeconnexion.setDisable(false);
        menuGestionTournois.setDisable(true);
        menuGestionJeux.setDisable(true);
        menuStaff.setDisable(true);
        menuUtilisateurs.setDisable(true);
        menuGestionJoueurs.setDisable(true);
        menuGestionEquipes.setDisable(true);

        // Si non connecté, on s'arrête là
        if (!isAuthenticated) {
            return;
        }

        // Si connecté, activer les menus de base
        menuGestionTournois.setDisable(false);
        menuGestionJeux.setDisable(false);
        menuStaff.setDisable(false);
        menuGestionEquipes.setDisable(false);

        // Si ORGANISATEUR, ajouter ses droits spécifiques
        if (isOrganisateur) {
            // L'organisateur a accès à la gestion des équipes
            menuGestionEquipes.setDisable(false);
        }

        // Si ADMIN, accès complet
        if (isAdmin) {
            menuUtilisateurs.setDisable(false);
            menuGestionJoueurs.setDisable(false);
            menuGestionEquipes.setDisable(false);
        }
    }

    /**
     * Affiche la fenêtre de connexion
     */
    @FXML
    private void doConnexion() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fr/tournois/ui/fxml/Login.fxml"));
            GridPane loginPane = loader.load();
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Connexion");
            dialogStage.initOwner(menuBar.getScene().getWindow());
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.setResizable(false);
            Scene scene = new Scene(loginPane);
            scene.getStylesheets().add(getClass().getResource("/fr/tournois/ui/css/style.css").toExternalForm());
            dialogStage.setScene(scene);
            DialogUtils.centerDialog(dialogStage, (Stage) menuBar.getScene().getWindow());
            LoginController controller = loader.getController();
            controller.setUtilisateurDAO(new UtilisateurDAO(connection));
            controller.setDialogStage(dialogStage);
            dialogStage.showAndWait();
            updateMenuCompte();
        } catch (Exception e) {
            showError("Erreur", "Impossible d'ouvrir la fenêtre de connexion :\n" + e.getMessage());
        }
    }

    /**
     * Déconnecte l'utilisateur et réinitialise l'interface
     */
    @FXML
    private void doDeconnexion() {
        SecurityContext.getInstance().logout();
        updateMenuCompte();
        mainContentPane.getChildren().clear();
    }

    /**
     * Ferme l'application
     */
    @FXML
    private void doQuitter() {
        Stage stage = (Stage) menuBar.getScene().getWindow();
        stage.close();
    }

    /**
     * Affiche l'interface de gestion des tournois
     */
    @FXML
    private void doAfficherGestionTournois() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fr/tournois/ui/fxml/TournoisManagement.fxml"));
            BorderPane tournoisManagement = loader.load();
            mainContentPane.getChildren().setAll(tournoisManagement);
            tournoisManagementController = loader.getController();
            tournoisManagementController.setDAOs(new TournoiDAO(connection), new StaffDAO(connection));
            tournoisManagementController.setParentStage((Stage) mainContentPane.getScene().getWindow());
        } catch (Exception e) {
            showError("Erreur", "Impossible d'ouvrir la gestion des tournois :\n" + e.getMessage());
        }
    }

    /**
     * Affiche l'interface de gestion des jeux
     */
    @FXML
    private void doAfficherGestionJeux() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fr/tournois/ui/fxml/JeuManagement.fxml"));
            BorderPane jeuxManagement = loader.load();
            mainContentPane.getChildren().setAll(jeuxManagement);
            jeuxManagementController = loader.getController();
            jeuxManagementController.setJeuDAO(new JeuDAO(connection));
            jeuxManagementController.setParentStage((Stage) mainContentPane.getScene().getWindow());
        } catch (Exception e) {
            showError("Erreur", "Impossible d'ouvrir la gestion des jeux :\n" + e.getMessage());
        }
    }

    /**
     * Affiche l'interface de gestion du staff
     */
    @FXML
    private void doAfficherStaff() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fr/tournois/ui/fxml/StaffManagement.fxml"));
            BorderPane staffManagement = loader.load();
            mainContentPane.getChildren().setAll(staffManagement);
            staffManagementController = loader.getController();
            staffManagementController.setDAOs(
                    new StaffDAO(connection),
                    new UtilisateurDAO(connection),
                    new AffectationDAO(connection));
        } catch (Exception e) {
            showError("Erreur", "Impossible d'ouvrir la gestion du staff :\n" + e.getMessage());
        }
    }

    /**
     * Affiche l'interface de gestion des utilisateurs
     */
    @FXML
    private void doAfficherUtilisateurs() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fr/tournois/ui/fxml/UtilisateursManagement.fxml"));
            BorderPane utilisateursManagement = loader.load();
            mainContentPane.getChildren().setAll(utilisateursManagement);
            utilisateursManagementController = loader.getController();
            utilisateursManagementController.setUtilisateurDAO(new UtilisateurDAO(connection));
            utilisateursManagementController.setParentStage((Stage) mainContentPane.getScene().getWindow());
        } catch (Exception e) {
            showError("Erreur", "Impossible d'ouvrir la gestion des utilisateurs :\n" + e.getMessage());
        }
    }

    @FXML
    private void doAfficherGestionEquipes() {
        try {

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fr/tournois/ui/fxml/equipeAccueil.fxml"));
            BorderPane equipesManagement = loader.load();
            mainContentPane.getChildren().setAll(equipesManagement);
            equipesManagementController = loader.getController();
            equipesManagementController.setEquipeDAO(new EquipeDAO(connection));
            equipesManagementController.setParentStage((Stage) mainContentPane.getScene().getWindow());
        } catch (Exception e) {
            showError("Erreur", "Impossible d'ouvrir la gestion des équipes :\n" + e.getMessage());
        }
    }

    @FXML
    private void doAfficherGestionJoueursGlobal() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fr/tournois/ui/fxml/GererJoueurs.fxml")); // Charge le FXML de la gestion des joueurs
            Parent root = loader.load();

            GererJoueursController gererJoueursController = loader.getController();
            gererJoueursController.setJoueurDAO(this.joueurDAO);
            gererJoueursController.setEquipe(null); // Indique au contrôleur de gérer tous les joueurs

            // Créer une nouvelle scène avec la vue de gestion des joueurs
            Scene scene = new Scene(root);

            // Créer une nouvelle fenêtre (Stage)
            Stage gestionJoueursStage = new Stage();
            gestionJoueursStage.setTitle("Gestion des Joueurs"); // Titre de la nouvelle fenêtre
            gestionJoueursStage.setScene(scene);

            // Optionnel : Définir le comportement de la nouvelle fenêtre
            // gestionJoueursStage.initModality(Modality.APPLICATION_MODAL); // Rend la fenêtre modale (bloque les interactions avec la fenêtre parente)
            // gestionJoueursStage.initOwner(primaryStage); // Lie la nouvelle fenêtre à la fenêtre principale

            // Afficher la nouvelle fenêtre
            gestionJoueursStage.show();

            // Si vous voulez fermer la fenêtre principale ou masquer son contenu, vous pouvez le faire ici:
            // mainContentPane.getChildren().clear(); // Efface le contenu de la fenêtre principale si vous ne voulez pas de superposition.
            // ((Stage) mainContentPane.getScene().getWindow()).hide(); // Cache la fenêtre principale (pas recommandé si elle contient le menu)

        } catch (IOException e) {
            showError("Erreur de chargement", "Impossible de charger la page de gestion des joueurs : " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            showError("Erreur inattendue", "Une erreur inattendue est survenue : " + e.getMessage());
            e.printStackTrace();
        }
    }


    @FXML
    public void ouvrirGestionEquipes() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fr/tournois/ui/fxml/equipeAccueil.fxml"));
            Parent root = loader.load();

            EquipeController controller = loader.getController();
            controller.setEquipeDAO(new EquipeDAO(connection));
            controller.setJoueurDAO(new JoueurDAO(connection));

            Stage stage = new Stage();
            stage.setTitle("Gestion des Équipes");
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            System.err.println("Erreur lors du chargement : " + e.getMessage());
            e.printStackTrace();

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText("Impossible d'ouvrir la gestion des équipes");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

    // Assurez-vous que votre méthode ouvrirGererJoueurs(Equipe equipeSelectionnee)
    // fait la même chose si vous voulez aussi ouvrir une nouvelle fenêtre pour la gestion
    // des joueurs par équipe.
    public void ouvrirGererJoueurs(Equipe equipeSelectionnee) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fr/tournois/ui/fxml/GererJoueurs.fxml")); // Charge le FXML
            Parent root = loader.load();

            GererJoueursController controller = loader.getController();
            controller.setJoueurDAO(this.joueurDAO);
            controller.setEquipe(equipeSelectionnee); // Passe l'équipe spécifique

            Scene scene = new Scene(root);
            Stage gestionJoueursEquipeStage = new Stage();
            gestionJoueursEquipeStage.setTitle("Gestion des Joueurs de l'équipe : " + equipeSelectionnee.getNom()); // Titre de la fenêtre par équipe
            gestionJoueursEquipeStage.setScene(scene);
            gestionJoueursEquipeStage.show();

        } catch (IOException e) {
            showError("Erreur de chargement", "Impossible de charger la page de gestion des joueurs de l'équipe : " + e.getMessage());
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            showError("Erreur d'arguments", "Argument invalide pour la gestion des joueurs : " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            showError("Erreur inattendue", "Une erreur inattendue est survenue : " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Affiche la fenêtre "À propos"
     */
    @FXML
    private void doAfficherAPropos() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("À propos de LiveTournois");
        alert.setHeaderText("LiveTournois - Version 2");
        alert.setContentText("Auteurs : V. Veron - E. Olivencia - T. Larrose");
        DialogUtils.centerDialog((Stage) alert.getDialogPane().getScene().getWindow(),
                (Stage) mainContentPane.getScene().getWindow());
        alert.showAndWait();
    }

   @FXML 
private void doExportTournoi() {
    String fxmlPath = "/fr/tournois/ui/fxml/ExportTournois.fxml";
    
    try {
        // Vérification de l'existence de la ressource FXML
        java.net.URL fxmlUrl = getClass().getResource(fxmlPath);
        if (fxmlUrl == null) {
            showError("Erreur de ressource", 
                     "Le fichier FXML est introuvable :\n" + fxmlPath + 
                     "\n\nVérifiez que le fichier existe dans les ressources du projet.");
            return;
        }
        
        System.out.println("DEBUG: Chargement du fichier FXML depuis : " + fxmlUrl);
        
        // Chargement du FXML
        FXMLLoader loader = new FXMLLoader(fxmlUrl);
        AnchorPane exportTournoi;
        
        try {
            exportTournoi = loader.load();
        } catch (IOException e) {
            showError("Erreur de chargement FXML", 
                     "Impossible de charger le fichier FXML :\n" + fxmlPath + 
                     "\n\nErreur : " + e.getMessage() +
                     "\n\nVérifiez la syntaxe du fichier FXML.");
            e.printStackTrace(); // Pour voir la stack trace complète dans la console
            return;
        }
        
        // Vérification du contenu principal
        if (mainContentPane == null) {
            showError("Erreur d'interface", 
                     "Le conteneur principal (mainContentPane) n'est pas initialisé.");
            return;
        }
        
        // Mise à jour de l'interface
        try {
            mainContentPane.getChildren().setAll(exportTournoi);
        } catch (Exception e) {
            showError("Erreur d'affichage", 
                     "Impossible de mettre à jour l'interface :\n" + e.getMessage());
            e.printStackTrace();
            return;
        }
        
        // Récupération et configuration du contrôleur
        try {
            ExportTournoiController exportTournoiController = loader.getController();
            
            if (exportTournoiController == null) {
                showError("Erreur de contrôleur", 
                         "Le contrôleur n'a pas pu être récupéré.\n" + 
                         "Vérifiez l'attribut fx:controller dans le fichier FXML.");
                return;
            }
            
            // Vérification de la connexion base de données
            if (connection == null) {
                showError("Erreur de base de données", 
                         "La connexion à la base de données n'est pas établie.");
                return;
            }
            
            // Configuration du contrôleur
            exportTournoiController.setDAOs(new TournoiDAO(connection));
            
            // Configuration de la fenêtre parente
            try {
                Stage parentStage = (Stage) mainContentPane.getScene().getWindow();
                if (parentStage != null) {
                    exportTournoiController.setParentStage(parentStage);
                } else {
                    System.out.println("WARNING: Impossible de récupérer la fenêtre parente");
                }
            } catch (Exception e) {
                System.out.println("WARNING: Erreur lors de la configuration de la fenêtre parente : " + e.getMessage());
                // Non critique, on continue
            }
            
            System.out.println("SUCCESS: Export tournoi chargé avec succès");
            
        } catch (Exception e) {
            showError("Erreur de configuration", 
                     "Erreur lors de la configuration du contrôleur :\n" + e.getMessage());
            e.printStackTrace();
        }
        
    } catch (Exception e) {
        // Catch-all pour toute autre erreur non prévue
        showError("Erreur inattendue", 
                 "Une erreur inattendue s'est produite :\n" + 
                 e.getClass().getSimpleName() + ": " + e.getMessage() +
                 "\n\nConsultez la console pour plus de détails.");
        e.printStackTrace();
    }
}

    /**
     * Affiche une boîte de dialogue d'erreur
     * 
     * @param titre   titre de la fenêtre d'erreur
     * @param message message d'erreur à afficher
     */
    private void showError(String titre, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Affiche une fenêtre temporaire
     * 
     * @param stage la fenêtre à afficher
     */
    public void displayTemporaire(Stage stage) {
        this.updateMenuCompte();
        stage.show();
    }

    public void setJoueurDAO(JoueurDAO joueurDAO) {
        this.joueurDAO = joueurDAO;
    }

}