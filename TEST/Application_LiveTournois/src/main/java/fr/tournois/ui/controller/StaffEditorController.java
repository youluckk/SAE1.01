package fr.tournois.ui.controller;

import fr.tournois.dao.DAOException;
import fr.tournois.dao.AffectationDAO;
import fr.tournois.dao.StaffDAO;
import fr.tournois.dao.UtilisateurDAO;
import fr.tournois.security.SecurityContext;
import fr.tournois.model.Staff;
import fr.tournois.model.Utilisateur;
import fr.tournois.ui.util.AlertUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import javafx.scene.input.KeyCode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class StaffEditorController {

    @FXML private TextField nomField;
    @FXML private TextField prenomField;
    @FXML private TextField emailField;
    @FXML private TextField fonctionField;
    @FXML private TextField telephoneField;
    @FXML
    private ComboBox<Utilisateur> utilisateurComboBox;

    @FXML private Button btnVoirTous;
    @FXML private Button btnNouveau;
    @FXML private Button okButton;

    private StaffDAO staffDAO;
    private UtilisateurDAO utilisateurDAO;
    private AffectationDAO affectationDAO;
    private Staff staff;
    private boolean valide = false;
    private Stage dialogStage;

    public void initialize() {

        // Configuration de l'affichage des utilisateurs dans la ComboBox
        utilisateurComboBox.setConverter(new StringConverter<Utilisateur>() {
            @Override
            public String toString(Utilisateur utilisateur) {
                if (utilisateur == null) return "";
                return utilisateur.getPseudo() + " (" + utilisateur.getRole() + ")";
            }

            @Override
            public Utilisateur fromString(String string) {
                return null; // Non utilisé car la ComboBox n'est pas éditable
            }
        });

        // Contrôle accès bouton créer
        this.btnNouveau.setDisable(!SecurityContext.getInstance().isAdmin());
    }

    public void setDAOs(StaffDAO staffDAO, UtilisateurDAO utilisateurDAO, AffectationDAO affectationDAO) {
        this.staffDAO = staffDAO;
        this.utilisateurDAO = utilisateurDAO;
        this.affectationDAO = affectationDAO;
        chargerUtilisateurs();
    }

    public void setStaff(Staff staff) {
        this.staff = staff;
        if (staff.getId() != null) {
            // Mode édition
            nomField.setText(staff.getNom());
            prenomField.setText(staff.getPrenom());
            emailField.setText(staff.getEmail());
            fonctionField.setText(staff.getFonction());
            telephoneField.setText(staff.getTelephone());
            
            if (staff.getUtilisateur() != null) {
                utilisateurComboBox.setValue(staff.getUtilisateur());
            }
        }
    }

    private void chargerUtilisateurs() {
        try {
            // Récupérer tous les utilisateurs
            List<Utilisateur> utilisateurs = utilisateurDAO.findAll();
            
            // Récupérer tous les staffs pour identifier les utilisateurs déjà associés
            List<Staff> staffs = staffDAO.findAll();

            List<Utilisateur> utilisateursDisponibles = new ArrayList<>();
            boolean trouve;
            for (Utilisateur utilisateur : utilisateurs) {
                trouve = false;
                for (Staff staff : staffs) {
                    if (staff.getUtilisateur() != null && staff.getUtilisateur().getId().equals(utilisateur.getId())) {
                        trouve = true;
                    }
                }
                if (!trouve) {
                    utilisateursDisponibles.add(utilisateur);
                }
            }
            
            utilisateurComboBox.getItems().clear();
            utilisateurComboBox.getItems().add(null); // Option "Aucun utilisateur"
            utilisateurComboBox.getItems().addAll(utilisateursDisponibles);
            
            // Si on est en mode édition et qu'il y a un utilisateur associé
            if (staff != null && staff.getUtilisateur() != null) {
                // Vérifier si l'utilisateur actuel n'est pas déjà dans la liste
                Utilisateur utilisateurActuel = staff.getUtilisateur();
                boolean dejaPresent = utilisateurComboBox.getItems().stream()
                    .filter(u -> u != null)
                    .anyMatch(u -> u.getId().equals(utilisateurActuel.getId()));
                
                // L'ajouter uniquement s'il n'est pas déjà présent
                if (!dejaPresent) {
                    utilisateurComboBox.getItems().add(utilisateurActuel);
                }
                
                // Sélectionner l'utilisateur dans la combobox
                utilisateurComboBox.setValue(utilisateurActuel);
            }
        } catch (DAOException e) {
            showError("Erreur", "Impossible de charger la liste des utilisateurs\n" + e.getMessage());
        }
    }

    @FXML
    private void doValider() {
        if (!validateFields()) return;

        staff.setNom(nomField.getText().trim());
        staff.setPrenom(prenomField.getText().trim());
        staff.setEmail(emailField.getText().trim());
        staff.setFonction(fonctionField.getText().trim());
        staff.setTelephone(telephoneField.getText() != null ? telephoneField.getText().trim() : null);
        
        Utilisateur selectedUser = utilisateurComboBox.getValue();
        staff.setUtilisateur(selectedUser);

        try {
            if (staff.getId() == null) {
                staffDAO.create(staff);
            } else {
                staffDAO.update(staff);
            }
            valide = true;
            closeStage();
        } catch (DAOException e) {
            showError("Erreur", "Impossible de sauvegarder le membre du staff\n" + e.getMessage());
        }
    }

    private boolean validateFields() {
        String errorMessage = "";

        if (nomField.getText() == null || nomField.getText().trim().isEmpty()) {
            errorMessage += "Le nom est obligatoire\n";
        }
        if (prenomField.getText() == null || prenomField.getText().trim().isEmpty()) {
            errorMessage += "Le prénom est obligatoire\n";
        }
        if (emailField.getText() == null || emailField.getText().trim().isEmpty()) {
            errorMessage += "L'email est obligatoire\n";
        } else if (!emailField.getText().matches("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}")) {
            errorMessage += "L'email n'est pas valide\n";
        }
        if (fonctionField.getText() == null || fonctionField.getText().trim().isEmpty()) {
            errorMessage += "La fonction est obligatoire\n";
        }

        if (errorMessage.isEmpty()) {
            return true;
        } else {
            showError("Erreur de saisie", errorMessage);
            return false;
        }
    }

    @FXML
    private void doAnnuler() {
        closeStage();
    }

    private void closeStage() {
        if (dialogStage != null) {
            dialogStage.close();
        }
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
        
        // Ajouter un gestionnaire d'événements pour la touche Échap
        dialogStage.getScene().setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                // Appeler la même méthode que le bouton Annuler
                doAnnuler();
            }
        });
    }

    private void showError(String title, String message) {
        AlertUtils.showError(title, message, dialogStage);
    }

    @FXML
    private void doVoirUtilisateurs() {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/fr/tournois/ui/fxml/UtilisateursViewer.fxml"));
            BorderPane page = loader.load();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Liste des utilisateurs");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(this.dialogStage);
            Scene scene = new Scene(page);
            dialogStage.setScene(scene);

            UtilisateursViewerController controller = loader.getController();
            controller.setDialogStage(dialogStage);
            controller.setDAOs(utilisateurDAO, staffDAO);

            // Centrer la fenêtre par rapport à la fenêtre parente
            dialogStage.setOnShown(e -> {
                dialogStage.setX(this.dialogStage.getX() + (this.dialogStage.getWidth() - dialogStage.getWidth()) / 2);
                dialogStage.setY(this.dialogStage.getY() + (this.dialogStage.getHeight() - dialogStage.getHeight()) / 2);
            });

            dialogStage.showAndWait();
        } catch (IOException e) {
            showError("Erreur", "Impossible d'ouvrir la fenêtre de visualisation des utilisateurs\n" + e.getMessage());
        }
    }

    @FXML
    private void doNouvelUtilisateur() {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/fr/tournois/ui/fxml/UtilisateurEditor.fxml"));
            GridPane page = loader.load();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Nouvel utilisateur");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(this.dialogStage);
            Scene scene = new Scene(page);
            dialogStage.setScene(scene);

            UtilisateurEditorController controller = loader.getController();
            controller.setDialogStage(dialogStage);
            controller.setUtilisateurDAO(utilisateurDAO);
            
            // Créer un nouvel utilisateur
            Utilisateur nouvelUtilisateur = new Utilisateur();
            controller.setUtilisateur(nouvelUtilisateur);

            // Centrer la fenêtre par rapport à la fenêtre parente
            dialogStage.setOnShown(e -> {
                dialogStage.setX(this.dialogStage.getX() + (this.dialogStage.getWidth() - dialogStage.getWidth()) / 2);
                dialogStage.setY(this.dialogStage.getY() + (this.dialogStage.getHeight() - dialogStage.getHeight()) / 2);
            });

            dialogStage.showAndWait();

            // Si un utilisateur a été créé, le sélectionner dans la combobox
            if (controller.isOkClicked()) {
                // Récupérer l'id du nouvel utilisateur créé
                Integer nouvelUtilisateurId = nouvelUtilisateur.getId();
                
                // Recharger la liste des utilisateurs
                chargerUtilisateurs();
                
                // Trouver et sélectionner le nouvel utilisateur dans la liste rechargée
                if (nouvelUtilisateurId != null) {
                    // Chercher l'utilisateur nouvellement créé dans la liste
                    Utilisateur utilisateurCree = utilisateurComboBox.getItems().stream()
                        .filter(u -> u != null && nouvelUtilisateurId.equals(u.getId()))
                        .findFirst()
                        .orElse(null);
                    
                    // S'il est trouvé, le sélectionner
                    if (utilisateurCree != null) {
                        utilisateurComboBox.setValue(utilisateurCree);
                    }
                }
            }
        } catch (IOException e) {
            showError("Erreur", "Impossible d'ouvrir la fenêtre de création d'utilisateur\n" + e.getMessage());
        }
    }

    public boolean isValide() {
        return valide;
    }
}
