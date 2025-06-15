package fr.tournois.ui.controller;

import fr.tournois.dao.AffectationDAO;
import fr.tournois.dao.DAOException;
import fr.tournois.dao.StaffDAO;
import fr.tournois.dao.UtilisateurDAO;
import fr.tournois.model.Affectation;
import fr.tournois.model.Staff;
import fr.tournois.model.Utilisateur;
import fr.tournois.ui.TournoisManagerApp;
import fr.tournois.ui.util.AlertUtils;
import fr.tournois.ui.util.DialogUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.util.List;

public class StaffManagementController {

    @FXML private ListView<Staff> staffListView;
    @FXML private Button btnNouveau;
    @FXML private Button btnModifier;
    @FXML private Button btnSupprimer;
    @FXML private TextField searchField;
    @FXML private Label nomLabel;
    @FXML private Label prenomLabel;
    @FXML private Label emailLabel;
    @FXML private Label fonctionLabel;
    @FXML private Label telephoneLabel;
    @FXML private Label utilisateurLabel;
    @FXML private ListView<String> affectationsListView;

    private StaffDAO staffDAO;
    private UtilisateurDAO utilisateurDAO;
    private AffectationDAO affectationDAO;
    private ObservableList<Staff> staffList;

    public void initialize() {
        staffList = FXCollections.observableArrayList();
        staffListView.setItems(staffList);

        // Mise à jour des détails quand un staff est sélectionné
        staffListView.getSelectionModel().selectedItemProperty().addListener(
            (observable, oldValue, newValue) -> afficherDetailsStaff(newValue)
        );

        // Désactiver les boutons si aucun staff n'est sélectionné
        btnModifier.disableProperty().bind(staffListView.getSelectionModel().selectedItemProperty().isNull());
        btnSupprimer.disableProperty().bind(staffListView.getSelectionModel().selectedItemProperty().isNull());

        // Configuration de la recherche
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null || newValue.isEmpty()) {
                chargerStaff();
            } else {
                rechercherStaff(newValue);
            }
        });

        // Le bouton Nouveau est toujours actif car tous les utilisateurs connectés peuvent créer du staff
    }

    public void setDAOs(StaffDAO staffDAO, UtilisateurDAO utilisateurDAO, AffectationDAO affectationDAO) {
        this.staffDAO = staffDAO;
        this.utilisateurDAO = utilisateurDAO;
        this.affectationDAO = affectationDAO;
        chargerStaff();
    }

    private void chargerStaff() {
        try {
            List<Staff> staff = staffDAO.findAll();
            staffList.setAll(staff);
        } catch (DAOException e) {
            showError("Erreur", "Impossible de charger la liste du staff\n" + e.getMessage());
        }
    }

    private void rechercherStaff(String critere) {
        try {
            List<Staff> resultats = staffDAO.rechercher(critere);
            staffList.setAll(resultats);
        } catch (DAOException e) {
            showError("Erreur", "Erreur lors de la recherche\n" + e.getMessage());
        }
    }

    private void afficherDetailsStaff(Staff staff) {
        if (staff == null) {
            nomLabel.setText("");
            prenomLabel.setText("");
            emailLabel.setText("");
            fonctionLabel.setText("");
            telephoneLabel.setText("");
            utilisateurLabel.setText("");
            affectationsListView.getItems().clear();
            return;
        }

        nomLabel.setText(staff.getNom());
        prenomLabel.setText(staff.getPrenom());
        emailLabel.setText(staff.getEmail());
        fonctionLabel.setText(staff.getFonction());
        telephoneLabel.setText(staff.getTelephone());

        try {
            Utilisateur utilisateur = staff.getUtilisateur();
            utilisateurLabel.setText(utilisateur != null ? utilisateur.getPseudo() + " (" + utilisateur.getRole() + ")" : "Non associé");
            
            // Charger les affectations du staff
            List<Affectation> affectations = affectationDAO.findByStaff(staff);
            ObservableList<String> affectationItems = FXCollections.observableArrayList();
            for (Affectation affectation : affectations) {
                affectationItems.add(affectation.getTournoi().getNom() + " - " + affectation.getRoleSpecifique());
            }
            affectationsListView.setItems(affectationItems);
        } catch (DAOException e) {
            utilisateurLabel.setText("Erreur de chargement");
            affectationsListView.setItems(FXCollections.observableArrayList("Erreur de chargement des affectations"));
        }
    }

    @FXML
    private void doNouveauStaff() {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(TournoisManagerApp.class.getResource("fxml/StaffEditor.fxml"));
            Scene scene = new Scene(loader.load());
            scene.getStylesheets().add(TournoisManagerApp.class.getResource("css/style.css").toExternalForm());
            
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Nouveau membre du staff");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            Window parentStage = staffListView.getScene().getWindow();
            dialogStage.initOwner(parentStage);
            dialogStage.setScene(scene);
            
            DialogUtils.centerDialog(dialogStage, parentStage);
            
            StaffEditorController controller = loader.getController();
            controller.setDialogStage(dialogStage);
            controller.setDAOs(staffDAO, utilisateurDAO, affectationDAO);
            controller.setStaff(new Staff());
            
            dialogStage.showAndWait();
            if (controller.isValide()) {
                chargerStaff();
            }
        } catch (Exception e) {
            showError("Erreur", "Impossible d'ouvrir l'éditeur de staff :\n" + e.getMessage());
        }
    }

    @FXML
    private void doModifierStaff() {
        Staff selected = staffListView.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(TournoisManagerApp.class.getResource("fxml/StaffEditor.fxml"));
            Scene scene = new Scene(loader.load());
            scene.getStylesheets().add(TournoisManagerApp.class.getResource("css/style.css").toExternalForm());
            
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Modifier un membre du staff");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            Window parentStage = staffListView.getScene().getWindow();
            dialogStage.initOwner(parentStage);
            dialogStage.setScene(scene);
            
            DialogUtils.centerDialog(dialogStage, parentStage);
            
            StaffEditorController controller = loader.getController();
            controller.setDialogStage(dialogStage);
            controller.setDAOs(staffDAO, utilisateurDAO, affectationDAO);
            controller.setStaff(selected);
            
            dialogStage.showAndWait();
            if (controller.isValide()) {
                chargerStaff();
            }
        } catch (Exception e) {
            showError("Erreur", "Impossible d'ouvrir l'éditeur de staff :\n" + e.getMessage());
        }
    }

    @FXML
    private void doSupprimerStaff() {
        Staff selected = staffListView.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Supprimer ce membre du staff ?");
        alert.setContentText("Êtes-vous sûr de vouloir supprimer " + selected.getNom() + " " + selected.getPrenom() + " ?");
        
        Window parentStage = staffListView.getScene().getWindow();
        alert.initOwner(parentStage);
        DialogUtils.centerDialog((Stage) alert.getDialogPane().getScene().getWindow(), parentStage);
        
        if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try {
                staffDAO.delete(selected.getId());
                chargerStaff();
            } catch (DAOException e) {
                showError("Erreur", "Impossible de supprimer le membre du staff\n" + e.getMessage());
            }
        }
    }

    @FXML
    private void doRechercheStaff() {
        String critere = searchField.getText();
        if (critere == null || critere.isEmpty()) {
            chargerStaff();
        } else {
            rechercherStaff(critere);
        }
    }

    private void showError(String title, String message) {
        Window owner = staffListView != null ? staffListView.getScene().getWindow() : null;
        AlertUtils.showError(title, message, owner);
    }
}
