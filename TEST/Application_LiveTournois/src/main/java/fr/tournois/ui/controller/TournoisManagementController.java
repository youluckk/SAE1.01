package fr.tournois.ui.controller;

import fr.tournois.model.Tournoi;
import fr.tournois.dao.TournoiDAO;
import fr.tournois.dao.DAOException;
import fr.tournois.dao.StaffDAO;
import fr.tournois.dao.InscriptionDAO;
import fr.tournois.dao.EquipeDAO;
import fr.tournois.model.Staff;
import fr.tournois.model.Affectation;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import fr.tournois.ui.util.AlertUtils;
import fr.tournois.ui.util.DialogUtils;
import java.sql.Connection;

public class TournoisManagementController {
    // Composants FXML
    @FXML private ListView<Tournoi> tournoisListView;
    @FXML private Button btnNouveauTournoi;
    @FXML private Button btnModifierTournoi;
    @FXML private Button btnSupprimerTournoi;
    @FXML private Button btnGererInscriptions;
    @FXML private ListView<Staff> staffListView;
    @FXML private Button btnAjouterStaff;
    @FXML private Button btnRetirerStaff;
    @FXML private TabPane tabPane;
    @FXML private Tab tabInscriptions;
    @FXML private InscriptionIntegrationController inscriptionPaneController;

    // Autres attributs
    private ObservableList<Tournoi> tournois = FXCollections.observableArrayList();
    private TournoiDAO tournoiDAO;
    private StaffDAO staffDAO;
    private InscriptionDAO inscriptionDAO;
    private EquipeDAO equipeDAO;
    private Stage parentStage;
    private Connection connection;

    public void setDAOs(TournoiDAO tournoiDAO, StaffDAO staffDAO) {
        this.tournoiDAO = tournoiDAO;
        this.staffDAO = staffDAO;
        this.connection = tournoiDAO.getConnection();
        
        this.inscriptionDAO = new InscriptionDAO(connection);
        this.equipeDAO = new EquipeDAO(connection);
        
        if (inscriptionPaneController != null) {
            inscriptionPaneController.setDAOs(inscriptionDAO, equipeDAO);
            inscriptionPaneController.setParentStage(parentStage);
        }
        
        rafraichirListe();
    }
    
    public void setParentStage(Stage stage) {
        this.parentStage = stage;
        if (inscriptionPaneController != null) {
            inscriptionPaneController.setParentStage(stage);
        }
    }

    private void showError(String title, String message) {
        AlertUtils.showError(title, message, parentStage);
    }

    @FXML
    public void initialize() {
        tournoisListView.setItems(tournois);
        tournoisListView.setCellFactory(list -> new ListCell<Tournoi>() {
            @Override
            protected void updateItem(Tournoi item, boolean empty) {
                super.updateItem(item, empty);
                setText((item == null || empty) ? null : item.getNom());
            }
        });
        
        // Configuration de la liste des staff
        staffListView.setCellFactory(list -> new ListCell<Staff>() {
            @Override
            protected void updateItem(Staff staff, boolean empty) {
                super.updateItem(staff, empty);
                if (empty || staff == null) {
                    setText(null);
                } else {
                    try {
                        Tournoi tournoi = tournoisListView.getSelectionModel().getSelectedItem();
                        if (tournoi != null) {
                            Affectation affectation = staffDAO.getAffectation(staff, tournoi);
                            String dateDebut = affectation.getDateDebut().toLocalDate().toString();
                            String dateFin = affectation.getDateFin().toLocalDate().toString();
                            setText(String.format("%s (%s - %s : du %s au %s)", 
                                staff.getNom(),
                                staff.getFonction(),
                                affectation.getRoleSpecifique(),
                                dateDebut,
                                dateFin));
                        } else {
                            setText(staff.getNom() + " (" + staff.getFonction() + ")");
                        }
                    } catch (DAOException e) {
                        setText(staff.getNom() + " (" + staff.getFonction() + ")");
                    }
                }
            }
        });
        
        // Désactiver tous les boutons par défaut
        btnModifierTournoi.setDisable(true);
        btnSupprimerTournoi.setDisable(true);
        btnGererInscriptions.setDisable(true);
        btnAjouterStaff.setDisable(true);
        btnRetirerStaff.setDisable(true);
        
        tabInscriptions.setDisable(true);
        
        // Gestion des boutons selon la sélection du tournoi
        tournoisListView.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            boolean tournoisSelected = (newSel != null);
            btnModifierTournoi.setDisable(!tournoisSelected);
            btnSupprimerTournoi.setDisable(!tournoisSelected);
            btnGererInscriptions.setDisable(!tournoisSelected);
            btnAjouterStaff.setDisable(!tournoisSelected);
            tabInscriptions.setDisable(!tournoisSelected);
            
            // Réinitialiser la sélection du staff et désactiver le bouton Retirer
            staffListView.getSelectionModel().clearSelection();
            btnRetirerStaff.setDisable(true);
            
            // Afficher les staff du tournoi sélectionné
            afficherStaffTournoi(newSel);
            
            if (inscriptionPaneController != null) {
                inscriptionPaneController.setTournoi(newSel);
            }
        });
        
        // Gestion du bouton Retirer selon la sélection du staff
        staffListView.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            boolean staffSelected = (newSel != null);
            boolean tournoisSelected = (tournoisListView.getSelectionModel().getSelectedItem() != null);
            btnRetirerStaff.setDisable(!staffSelected || !tournoisSelected);
        });
        
        // Initialisation : désélectionner tout
        tournoisListView.getSelectionModel().clearSelection();
        staffListView.getSelectionModel().clearSelection();
        
        configurerControleurInscriptions();
    }

    private void configurerControleurInscriptions() {
        if (inscriptionPaneController != null && inscriptionDAO != null && equipeDAO != null) {
            inscriptionPaneController.setDAOs(inscriptionDAO, equipeDAO);
            inscriptionPaneController.setParentStage(parentStage);
        }
    }

    private void rafraichirListe() {
        if (tournoiDAO != null) {
            try {
                tournois.setAll(tournoiDAO.findAll());
            } catch (DAOException e) {
                showError("Erreur", "Impossible de charger la liste des tournois : " + e.getMessage());
            }
        }
    }

    @FXML
    private void doNouveauTournoi() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fr/tournois/ui/fxml/TournoiEditor.fxml"));
            Scene scene = new Scene(loader.load());
            scene.getStylesheets().add(getClass().getResource("/fr/tournois/ui/css/style.css").toExternalForm());
            
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Nouveau tournoi");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(parentStage);
            dialogStage.setScene(scene);
            
            TournoiEditorController controller = loader.getController();
            controller.setDialogStage(dialogStage);
            controller.setConnection(connection);
            
            Tournoi tournoi = new Tournoi();
            controller.setTournoi(tournoi);
            
            dialogStage.showAndWait();
            
            if (controller.isOkClicked()) {
                tournoiDAO.create(tournoi);
                rafraichirListe();
            }
        } catch (Exception e) {
            showError("Erreur", "Impossible d'ouvrir l'éditeur de tournoi : " + e.getMessage());
        }
    }
    
    @FXML
    private void doModifierTournoi() {
        Tournoi tournoi = tournoisListView.getSelectionModel().getSelectedItem();
        if (tournoi != null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fr/tournois/ui/fxml/TournoiEditor.fxml"));
                Scene scene = new Scene(loader.load());
                scene.getStylesheets().add(getClass().getResource("/fr/tournois/ui/css/style.css").toExternalForm());

                Stage dialogStage = new Stage();
                dialogStage.setTitle("Modifier tournoi");
                dialogStage.initModality(Modality.WINDOW_MODAL);
                dialogStage.initOwner(parentStage);
                DialogUtils.centerDialog(dialogStage, parentStage);
                dialogStage.setScene(scene);

                TournoiEditorController controller = loader.getController();
                controller.setDialogStage(dialogStage);
                controller.setConnection(connection);
                controller.setTournoi(tournoi);

                dialogStage.showAndWait();

                if (controller.isOkClicked()) {
                    tournoiDAO.update(tournoi);
                    rafraichirListe();
                    
                    if (inscriptionPaneController != null && tournoi.equals(tournoisListView.getSelectionModel().getSelectedItem())) {
                        inscriptionPaneController.actualiser();
                    }
                }
            } catch (Exception e) {
                AlertUtils.showError("Erreur", "Impossible d'ouvrir l'éditeur de tournoi :\n" + e.getMessage(), parentStage);
            }
        } else {
            AlertUtils.showError("Erreur", "Veuillez sélectionner un tournoi à modifier.", parentStage);
        }
    }

    @FXML
    private void doSupprimerTournoi() {
        Tournoi selected = tournoisListView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Confirmation de suppression");
            confirm.setHeaderText(null);
            confirm.setContentText("Voulez-vous vraiment supprimer ce tournoi ?\n\nATTENTION : Toutes les inscriptions et affectations seront également supprimées !");
            if (parentStage != null) {
                confirm.initOwner(parentStage);
                DialogUtils.centerDialog((Stage) confirm.getDialogPane().getScene().getWindow(), parentStage);
            }
            if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
                try {
                    tournoiDAO.delete(selected);
                    rafraichirListe();
                    
                    if (inscriptionPaneController != null) {
                        inscriptionPaneController.setTournoi(null);
                    }
                } catch (DAOException e) {
                    showError("Erreur", "Impossible de supprimer le tournoi : " + e.getMessage());
                }
            }
        }
    }

    private void afficherStaffTournoi(Tournoi tournoi) {
        if (tournoi == null || staffDAO == null) {
            staffListView.setItems(FXCollections.observableArrayList());
            return;
        }
        try {
            staffListView.setItems(FXCollections.observableArrayList(staffDAO.findByTournoi(tournoi)));
        } catch (Exception e) {
            showError("Erreur", "Impossible de charger la liste des staff : " + e.getMessage());
        }
    }

    @FXML
    private void doAjouterStaff() {
        Tournoi tournoi = tournoisListView.getSelectionModel().getSelectedItem();
        if (tournoi == null) return;

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fr/tournois/ui/fxml/AffectationEditor.fxml"));
            Scene scene = new Scene(loader.load());
            scene.getStylesheets().add(getClass().getResource("/fr/tournois/ui/css/style.css").toExternalForm());
            
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Ajouter un staff au tournoi");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(parentStage);
            dialogStage.setScene(scene);
            
            AffectationEditorController controller = loader.getController();
            controller.setDialogStage(dialogStage);
            
            Affectation affectation = new Affectation();
            affectation.setTournoi(tournoi);
            controller.setAffectation(affectation);
            controller.setStaffDAO(staffDAO);
            
            dialogStage.showAndWait();
            
            if (controller.isOkClicked()) {
                staffDAO.addStaffToTournoi(affectation);
                afficherStaffTournoi(tournoi);
            }
        } catch (Exception e) {
            showError("Erreur", "Impossible d'ouvrir l'éditeur d'affectation : " + e.getMessage());
        }
    }

    @FXML
    private void doRetirerStaff() {
        Tournoi tournoi = tournoisListView.getSelectionModel().getSelectedItem();
        Staff staff = staffListView.getSelectionModel().getSelectedItem();
        
        if (tournoi == null || staff == null) return;

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText(null);
        confirm.setContentText("Voulez-vous vraiment retirer ce staff du tournoi ?");
        
        if (parentStage != null) {
            confirm.initOwner(parentStage);
        }
        DialogUtils.centerDialog((Stage) confirm.getDialogPane().getScene().getWindow(), parentStage);
        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try {
                staffDAO.removeStaffFromTournoi(staff, tournoi);
                afficherStaffTournoi(tournoi);
            } catch (DAOException e) {
                showError("Erreur", "Impossible de retirer le staff : " + e.getMessage());
            }
        }
    }

    /**
     * Gère les inscriptions - bascule vers l'onglet inscriptions
     */
    @FXML
    private void doGererInscriptions() {
        Tournoi tournoi = tournoisListView.getSelectionModel().getSelectedItem();
        if (tournoi == null) {
            AlertUtils.showWarning("Aucune sélection", "Veuillez sélectionner un tournoi pour gérer les inscriptions.", parentStage);
            return;
        }

        tabPane.getSelectionModel().select(tabInscriptions);
        
        if (inscriptionPaneController != null) {
            inscriptionPaneController.setTournoi(tournoi);
            inscriptionPaneController.actualiser();
        }
    }
}