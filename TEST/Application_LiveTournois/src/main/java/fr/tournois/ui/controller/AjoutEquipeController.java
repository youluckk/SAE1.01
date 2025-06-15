package fr.tournois.ui.controller;

import fr.tournois.dao.EquipeDAO;
import fr.tournois.model.Equipe;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.TextField;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.Alert;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.event.ActionEvent;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class AjoutEquipeController {

    @FXML
    private TextField nomEquipeField;

    @FXML
    private TableView<Equipe> teamTable;
    
    @FXML
    private TableColumn<Equipe, String> nomColumn;
    
    @FXML
    private TableColumn<Equipe, String> tagColumn;
    
    @FXML
    private TableColumn<Equipe, String> paysColumn;
    
    @FXML
    private TableColumn<Equipe, LocalDate> dateCreationColumn;

    private EquipeDAO equipeDAO;
    private EquipeController parentController;
    private ObservableList<Equipe> equipesList = FXCollections.observableArrayList();

    /**
     * Injecte le DAO utilisé pour les opérations d'équipe.
     */
    public void setEquipeDAO(EquipeDAO equipeDAO) {
        this.equipeDAO = equipeDAO;
        // Charger les équipes dès que le DAO est disponible
        chargerEquipes();
    }

    /**
     * Permet de réutiliser les méthodes de EquipeController comme ajouterEquipe(String).
     */
    public void setParentController(EquipeController parentController) {
        this.parentController = parentController;
    }

    @FXML
    private void initialize() {
        // Configuration des colonnes de la TableView
        if (nomColumn != null) {
            nomColumn.setCellValueFactory(new PropertyValueFactory<>("nom"));
        }
        if (tagColumn != null) {
            tagColumn.setCellValueFactory(new PropertyValueFactory<>("tag"));
        }
        if (paysColumn != null) {
            paysColumn.setCellValueFactory(new PropertyValueFactory<>("pays"));
        }
        if (dateCreationColumn != null) {
            dateCreationColumn.setCellValueFactory(new PropertyValueFactory<>("dateCreation"));
        }
        
        // Associer la liste observable à la TableView
        if (teamTable != null) {
            teamTable.setItems(equipesList);
            
            // Double-clic pour modifier
            teamTable.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2) {
                    Equipe selected = teamTable.getSelectionModel().getSelectedItem();
                    if (selected != null) {
                        showModifierEquipe();
                    }
                }
            });
        }
    }

    /**
     * Charge toutes les équipes depuis la base de données
     */
    private void chargerEquipes() {
        if (equipeDAO == null) {
            return;
        }
        
        try {
            List<Equipe> equipes = equipeDAO.getToutesLesEquipes();
            equipesList.clear();
            equipesList.addAll(equipes);
        } catch (SQLException e) {
            showAlert("Erreur lors du chargement des équipes : " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Rafraîchit la liste des équipes
     */
    public void rafraichirEquipes() {
        chargerEquipes();
    }

    /**
     * Ajoute une équipe à la base via le DAO.
     */
    @FXML
    private void ajouterEquipe(ActionEvent event) {
        String nom = nomEquipeField.getText().trim();

        if (nom.isEmpty()) {
            showAlert("Veuillez saisir un nom d'équipe.");
            return;
        }

        try {
            // Utilisation de la méthode du contrôleur parent si possible
            if (parentController != null) {
                parentController.ajouterEquipe(nom);
                parentController.rafraichirEquipes();
            } else {
                // Fallback direct au DAO
                Equipe equipe = new Equipe();
                equipe.setNom(nom);
                equipeDAO.create(equipe); // Utiliser create au lieu d'update
            }
            
            // Rafraîchir la liste locale
            rafraichirEquipes();
            
            // Vider le champ après ajout réussi
            nomEquipeField.clear();
            
            showSuccessAlert("Équipe ajoutée avec succès !");
            
        } catch (Exception e) {
            showAlert("Erreur lors de l'ajout de l'équipe : " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Ferme la fenêtre sans ajouter.
     */
    @FXML
    private void annuler(ActionEvent event) {
        fermerFenetre(event);
    }

    @FXML
    public void showAjouterEquipe() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fr/tournois/ui/fxml/nouvelleEquipe.fxml"));
            Parent root = loader.load();

            nouvelleEquipeController controller = loader.getController();
        
            Stage stage = new Stage();
            stage.setTitle("Nouvelle Équipe");
            stage.initModality(Modality.APPLICATION_MODAL);
        
            // CONFIGURATION POUR CRÉATION
            controller.setEquipeDAO(equipeDAO);
            controller.setParentController(parentController);
            controller.setEquipe(null); // Pas d'équipe = création
            controller.setModeModification(false); // Mode création
        
            // Créer la Scene AVANT de configurer le DialogStage
            Scene scene = new Scene(root);
            stage.setScene(scene);
        
            // MAINTENANT on peut configurer le DialogStage et le KeyPressed
            controller.setDialogStage(stage);
        
            // Configurer la touche Échap manuellement ici
            scene.setOnKeyPressed(event -> {
                if (event.getCode() == javafx.scene.input.KeyCode.ESCAPE) {
                    stage.close();
                }
            });
        
            stage.showAndWait();

            // Rafraîchir après fermeture
            if (controller.isOkClicked()) {
                rafraichirEquipes();
                if (parentController != null) {
                    parentController.rafraichirEquipes();
                }
            }
        } catch (Exception e) {
            showAlert("Erreur lors de l'ouverture de la fenêtre d'ajout : " + e.getMessage());
            e.printStackTrace();
        }
    }


    @FXML
    public void showModifierEquipe() {
        Equipe selected = teamTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Veuillez sélectionner une équipe à modifier.");
            return;
        }
    
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fr/tournois/ui/fxml/nouvelleEquipe.fxml"));
            Parent root = loader.load();

            nouvelleEquipeController controller = loader.getController();

            Stage stage = new Stage();
            stage.setTitle("Modifier Équipe - " + selected.getNom());
            stage.initModality(Modality.APPLICATION_MODAL);

            // CONFIGURATION POUR MODIFICATION
            controller.setEquipeDAO(equipeDAO);
            controller.setParentController(parentController);
            controller.setEquipe(selected); // Équipe existante = modification
            controller.setModeModification(true); // Mode modification
        
            // Créer la Scene AVANT de configurer le DialogStage
            Scene scene = new Scene(root);
            stage.setScene(scene);
        
            // MAINTENANT on peut configurer le DialogStage et le KeyPressed
            controller.setDialogStage(stage);
        
            // Configurer la touche Échap manuellement ici
            scene.setOnKeyPressed(event -> {
                if (event.getCode() == javafx.scene.input.KeyCode.ESCAPE) {
                    stage.close();
                }
            });
        
            stage.showAndWait();
        
            // Actualiser les listes après modification
            if (controller.isOkClicked()) {
                rafraichirEquipes();
                if (parentController != null) {
                    parentController.rafraichirEquipes();
                
            }
        }
        } catch (Exception e) {
        showAlert("Erreur lors de l'ouverture de la fenêtre de modification : " + e.getMessage());
        e.printStackTrace();
    }
}

    @FXML
    private void supprimerEquipe() {
        Equipe selected = teamTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Veuillez sélectionner une équipe à supprimer.");
            return;
        }
        
        try {
            equipeDAO.supprimerEquipe(selected.getId());
            rafraichirEquipes();
            if (parentController != null) {
                parentController.rafraichirEquipes();
            }
            showSuccessAlert("Équipe supprimée avec succès !");
        } catch (SQLException e) {
            showAlert("Erreur lors de la suppression : " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void fermerFenetre(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private void showSuccessAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Succès");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void doViderChamps() {
        nomEquipeField.clear();
    }

    @FXML
    private void doRetourAccueil(ActionEvent event) {
        fermerFenetre(event);
    }
}