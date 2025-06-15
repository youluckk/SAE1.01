package fr.tournois.ui.controller;

import fr.tournois.dao.*;
import fr.tournois.model.*;
import fr.tournois.ui.util.DialogUtils;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class InscriptionIntegrationController implements Initializable {

    @FXML private TableView<Inscription> tableInscriptions;
    @FXML private TableColumn<Inscription, String> colEquipe;
    @FXML private TableColumn<Inscription, String> colDateInscription;
    @FXML private TableColumn<Inscription, String> colStatut;
    @FXML private Button btnInscrireEquipe;
    @FXML private Button btnModifierInscription;
    @FXML private Button btnSupprimerInscription;
    @FXML private Label labelPlacesRestantes;
    @FXML private Label labelNbInscrits;

    private InscriptionDAO inscriptionDAO;
    private EquipeDAO equipeDAO;
    private Tournoi tournoiSelectionne;
    private Stage parentStage;

    private ObservableList<Inscription> inscriptions = FXCollections.observableArrayList();

    /**
     * Initialise le contrôleur (appelé automatiquement par JavaFX)
     * Configure les colonnes et les événements de la table
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTableColumns();
        setupEventHandlers();
    }

    /**
     * Configure les colonnes de la table
     */
    private void setupTableColumns() {
        colEquipe.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getEquipe().getNom()));
        
        colDateInscription.setCellValueFactory(cellData -> {
            if (cellData.getValue().getDateInscription() != null) {
                return new SimpleStringProperty(
                    cellData.getValue().getDateInscription().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                );
            }
            return new SimpleStringProperty("");
        });
        
        colStatut.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getStatut()));

        tableInscriptions.setItems(inscriptions);
    }

    /**
     * Configure les gestionnaires d'événements
     */
    private void setupEventHandlers() {
        // Sélection dans la table
        tableInscriptions.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldSelection, newSelection) -> {
                boolean hasSelection = newSelection != null;
                btnModifierInscription.setDisable(!hasSelection);
                btnSupprimerInscription.setDisable(!hasSelection);
            });
    }

    /**
     * Définit les DAOs nécessaires pour l'intégration
     * @param inscriptionDAO DAO des inscriptions
     * @param equipeDAO DAO des équipes
     */
    public void setDAOs(InscriptionDAO inscriptionDAO, EquipeDAO equipeDAO) {
        this.inscriptionDAO = inscriptionDAO;
        this.equipeDAO = equipeDAO;
    }

    /**
     * Définit la fenêtre parente pour les dialogues modaux
     * @param parentStage Stage parent
     */
    public void setParentStage(Stage parentStage) {
        this.parentStage = parentStage;
    }

    /**
     * Définit le tournoi sélectionné et charge ses inscriptions
     * @param tournoi Le tournoi sélectionné
     */
    public void setTournoi(Tournoi tournoi) {
        this.tournoiSelectionne = tournoi;
        if (tournoi != null) {
            chargerInscriptions();
            mettreAJourStatistiques();
        } else {
            inscriptions.clear();
            mettreAJourStatistiques();
        }
    }

    /**
     * Charge les inscriptions du tournoi sélectionné
     */
    private void chargerInscriptions() {
        if (tournoiSelectionne == null || inscriptionDAO == null) {
            return;
        }

        try {
            List<Inscription> listeInscriptions = inscriptionDAO.findByTournoi(tournoiSelectionne.getId());
            inscriptions.setAll(listeInscriptions);
        } catch (DAOException e) {
            showError("Erreur", "Impossible de charger les inscriptions: " + e.getMessage());
        }
    }

    /**
     * Met à jour les statistiques affichées
     */
    private void mettreAJourStatistiques() {
        if (tournoiSelectionne == null) {
            labelNbInscrits.setText("Inscrits: 0");
            labelPlacesRestantes.setText("Places restantes: 0");
            btnInscrireEquipe.setDisable(true);
            return;
        }

        int nbInscrits = inscriptions.size();
        int placesRestantes = 16 - nbInscrits;
        
        labelNbInscrits.setText("Inscrits: " + nbInscrits + "/16");
        labelPlacesRestantes.setText("Places restantes: " + placesRestantes);
        
        btnInscrireEquipe.setDisable(placesRestantes <= 0);
        
        if (placesRestantes > 0) {
            labelPlacesRestantes.setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
        } else {
            labelPlacesRestantes.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
        }
    }

    /**
     * Ouvre la fenêtre pour inscrire une nouvelle équipe
     * Gère la création d'une inscription
     */
    @FXML
    private void doInscrireEquipe() {
        if (tournoiSelectionne == null) {
            showWarning("Aucun tournoi sélectionné", "Veuillez sélectionner un tournoi pour inscrire une équipe.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fr/tournois/ui/fxml/InscriptionEditor.fxml"));
            GridPane root = loader.load();
            
            InscriptionEditorController controller = loader.getController();
            controller.setDAOs(inscriptionDAO, null, equipeDAO);
            controller.setModeCreationAvecTournoi(tournoiSelectionne);
            
            Stage stage = new Stage();
            stage.setTitle("Inscrire une équipe");
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(parentStage);
            stage.setResizable(false);
            
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/fr/tournois/ui/css/style.css").toExternalForm());
            stage.setScene(scene);
            
            DialogUtils.centerDialog(stage, parentStage);
            
            stage.showAndWait();
            
            if (controller.isInscriptionCreee()) {
                chargerInscriptions();
                mettreAJourStatistiques();
            }
            
        } catch (IOException e) {
            showError("Erreur", "Impossible d'ouvrir la fenêtre d'inscription: " + e.getMessage());
        }
    }

    /**
     * Ouvre la fenêtre de modification de l'inscription sélectionnée
     * Gère la modification d'une inscription
     */
    @FXML
    private void doModifierInscription() {
        Inscription inscriptionSelectionnee = tableInscriptions.getSelectionModel().getSelectedItem();
        if (inscriptionSelectionnee == null) {
            showWarning("Aucune sélection", "Veuillez sélectionner une inscription à modifier.");
            return;
        }
        
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fr/tournois/ui/fxml/InscriptionEditor.fxml"));
            GridPane root = loader.load();
            
            InscriptionEditorController controller = loader.getController();
            controller.setDAOs(inscriptionDAO, null, equipeDAO);
            controller.setModeModification(inscriptionSelectionnee);
            
            Stage stage = new Stage();
            stage.setTitle("Modifier l'inscription");
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(parentStage);
            stage.setResizable(false);
            
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/fr/tournois/ui/css/style.css").toExternalForm());
            stage.setScene(scene);
            
            DialogUtils.centerDialog(stage, parentStage);
            
            stage.showAndWait();
            
            if (controller.isInscriptionModifiee()) {
                chargerInscriptions();
                mettreAJourStatistiques();
            }
            
        } catch (IOException e) {
            showError("Erreur", "Impossible d'ouvrir la fenêtre de modification: " + e.getMessage());
        }
    }

    /**
     * Supprime l'inscription sélectionnée après confirmation
     */
    @FXML
    private void doSupprimerInscription() {
        Inscription inscriptionSelectionnee = tableInscriptions.getSelectionModel().getSelectedItem();
        if (inscriptionSelectionnee == null) {
            showWarning("Aucune sélection", "Veuillez sélectionner une inscription à supprimer.");
            return;
        }
        
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation de suppression");
        alert.setHeaderText("Supprimer l'inscription");
        alert.setContentText("Êtes-vous sûr de vouloir supprimer l'inscription de l'équipe \"" + 
                           inscriptionSelectionnee.getEquipe().getNom() + "\" ?");
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                inscriptionDAO.delete(inscriptionSelectionnee.getTournoi().getId(), 
                                    inscriptionSelectionnee.getEquipe().getId());
                showSuccess("Suppression réussie", "L'inscription a été supprimée avec succès.");
                chargerInscriptions();
                mettreAJourStatistiques();
            } catch (DAOException e) {
                showError("Erreur de suppression", "Impossible de supprimer l'inscription: " + e.getMessage());
            }
        }
    }

    /**
     * Actualise les inscriptions et statistiques affichées
     */
    public void actualiser() {
        chargerInscriptions();
        mettreAJourStatistiques();
    }

    /**
     * Affiche un message d'erreur
     */
    private void showError(String titre, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Affiche un message d'avertissement
     */
    private void showWarning(String titre, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Affiche un message de succès
     */
    private void showSuccess(String titre, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}