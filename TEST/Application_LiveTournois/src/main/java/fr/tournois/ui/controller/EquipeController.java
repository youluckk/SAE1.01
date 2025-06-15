package fr.tournois.ui.controller;

import fr.tournois.dao.EquipeDAO;
import fr.tournois.dao.JoueurDAO;
import fr.tournois.model.Equipe;
import fr.tournois.model.Joueur;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import javafx.scene.control.TextField;

public class EquipeController {
    @FXML
    private Button btnNouvelleEquipe;
    @FXML
    private Button btnGererJoueurs;
    @FXML
    private Button btnSupprimerEquipe;
    @FXML
    private TableView<Equipe> teamTable;
    @FXML
    private TableColumn<Equipe, String> nameColumn;
    @FXML
    private TableColumn<Equipe, String> membersColumn;
    @FXML
    private TextField searchField;
    @FXML
    private TextField nomEquipeField;

    private final ObservableList<Equipe> equipes = FXCollections.observableArrayList();
    private EquipeDAO equipeDAO;
    private JoueurDAO joueurDAO;
    private Stage parentStage;

    private AppMainFrameController appMainFrameController;

    public void setEquipeDAO(EquipeDAO equipeDAO) {
        this.equipeDAO = equipeDAO;
        rafraichirEquipes();
    }

    public void setJoueurDAO(JoueurDAO joueurDAO) {
        this.joueurDAO = joueurDAO;
    }

    @FXML
    public void initialize() {
        nameColumn.setCellValueFactory(
                cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getNom()));
        membersColumn.setCellValueFactory(
                cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getJoueursAsString()));
        teamTable.setItems(equipes);
    }

    public void rafraichirEquipes() {
        if (equipeDAO != null) {
            equipes.clear();
            equipes.addAll(this.getToutesLesEquipes());
        }
    }

    @FXML
    private void handleDeleteTeam() {
        Equipe selected = teamTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            this.doSupprimerEquipe(selected.getId());
            rafraichirEquipes();
        } else {
            showAlert("Sélectionnez une équipe à supprimer.");
        }
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void ajouterEquipe(String nom) {
        Equipe equipe = new Equipe();
        equipe.setNom(nom);
        equipeDAO.create(equipe);
    }

    @FXML
    public void ajouterEquipe() {
        String nom = nomEquipeField.getText();
        if (!nom.trim().isEmpty()) {
            ajouterEquipe(nom);
        }
    }

    public void modifierEquipe(Equipe equipe) {
        equipeDAO.update(equipe);
    }

    public void doSupprimerEquipe(int id) {
        try {
            equipeDAO.supprimerEquipe(id);
        } catch (SQLException e) {
            System.err.println("Erreur lors de la suppression de l'équipe : " + e.getMessage());
        }
    }

    public List<Equipe> getToutesLesEquipes() {
        if (equipeDAO == null) {
            return new java.util.ArrayList<>();
        }
        try {
            return equipeDAO.getToutesLesEquipes();
        } catch (SQLException e) {
            return new java.util.ArrayList<>();
        }
    }

    public void ajouterJoueurAEquipe(Equipe equipe, String nom, String prenom, String pseudo) {
        try {
            Equipe equipeFromDb = equipeDAO.getEquipeParId(equipe.getId());
            if (equipeFromDb != null) {
                Joueur joueur = new Joueur();
                joueur.setNom(nom);
                joueur.setPrenom(prenom);
                joueur.setPseudo(pseudo);
                joueur.setEquipe(equipeFromDb);
                joueurDAO.ajouterJoueur(joueur);
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de l'ajout du joueur : " + e.getMessage());
        }
    }

    public void afficherJoueursDeLEquipe(Equipe equipe) {
        try {
            List<Joueur> joueurs = joueurDAO.getJoueursParEquipeId(equipe.getId());
            for (Joueur joueur : joueurs) {
                System.out.println(joueur.getPrenom() + " " + joueur.getNom() + " (" + joueur.getPseudo() + ")");
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des joueurs : " + e.getMessage());
        }
    }

    public void supprimerJoueurDeLEquipe(Equipe equipe, int joueurId) {
        try {
            Joueur joueur = joueurDAO.getJoueurParId(joueurId);
            if (joueur != null && joueur.getEquipe() != null && joueur.getEquipe().getId().equals(equipe.getId())) {
                joueurDAO.supprimerJoueur(joueurId);
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la suppression du joueur : " + e.getMessage());
        }
    }

    @FXML
    public void showNouvelleEquipe() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fr/tournois/ui/fxml/AjoutEquipe.fxml"));
            Parent root = loader.load();
            root.getStylesheets().add(getClass().getResource("/fr/tournois/ui/css/style.css").toExternalForm());
            AjoutEquipeController controller = loader.getController();
            controller.setEquipeDAO(equipeDAO);
            controller.setParentController(this);
            Stage stage = new Stage();
            stage.setTitle("Ajouter une équipe");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();
            rafraichirEquipes();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void gererJoueur() {
        Equipe equipeSelectionnee = teamTable.getSelectionModel().getSelectedItem();
        if (equipeSelectionnee == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Aucune équipe sélectionnée");
            alert.setHeaderText(null);
            alert.setContentText("Veuillez sélectionner une équipe avant de gérer ses joueurs.");
            alert.showAndWait();
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fr/tournois/ui/fxml/GestionJoueurEquipe.fxml"));
            Scene scene = new Scene(loader.load());
            scene.getStylesheets().add(getClass().getResource("/fr/tournois/ui/css/style.css").toExternalForm());
            Stage stage = new Stage();
            stage.setTitle("Gestion des joueurs - " + equipeSelectionnee.getNom());
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(scene);
            GestionJoueurEquipeController controller = loader.getController();
            controller.configurerController(stage, equipeSelectionnee, joueurDAO, this);
            scene.setOnKeyPressed(event -> {
                if (event.getCode() == javafx.scene.input.KeyCode.ESCAPE) {
                    stage.close();
                }
            });
            stage.showAndWait();
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText("Impossible d'ouvrir la page");
            alert.setContentText("Une erreur est survenue lors du chargement de la gestion des joueurs.");
            alert.showAndWait();
            e.printStackTrace();
        }
    }

    @FXML
    private void doActualiser() {
        rafraichirEquipes();
    }

    @FXML
    private void doRechercheEquipe() {
        String filtre = searchField.getText().trim().toLowerCase();
        if (filtre.isEmpty()) {
            rafraichirEquipes();
        } else {
            try {
                List<Equipe> toutes = equipeDAO.getToutesLesEquipes();
                equipes.setAll(
                        toutes.stream()
                                .filter(eq -> eq.getNom().toLowerCase().contains(filtre))
                                .toList());
            } catch (SQLException e) {
                showAlert("Erreur lors de la recherche : " + e.getMessage());
            }
        }
    }

    @FXML
    private void doViderChamps() {
        if (nomEquipeField != null) {
            nomEquipeField.clear();
        }
    }

    @FXML
    private void doRetourAccueil() {
        Stage stage = (Stage) nomEquipeField.getScene().getWindow();
        stage.close();
    }

    public void setParentStage(Stage window) {
        this.parentStage = window;
    }

    public Stage getParentStage() {
        return parentStage;
    }
}
