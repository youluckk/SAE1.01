package fr.tournois.ui.controller;

import fr.tournois.dao.UtilisateurDAO;
import fr.tournois.model.Utilisateur;
import fr.tournois.model.Role;
import fr.tournois.security.PasswordHasher;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.scene.input.KeyCode;
import fr.tournois.ui.util.AlertUtils;

/**
 * Contrôleur pour l'interface d'édition d'un utilisateur.
 * Cette classe gère le dialogue permettant de créer ou modifier un utilisateur.
 * Elle fournit une interface utilisateur avec des champs pour le pseudo,
 * le mot de passe, le rôle et le statut actif/inactif.
 *
 * @author F. Pelleau &amp; A. Péninou
 * @since V0.0
 */
public class UtilisateurEditorController {
    @FXML private TextField pseudoField;
    @FXML private PasswordField passwordField;
    @FXML private ComboBox<Role> roleComboBox;
    @FXML private CheckBox actifCheckBox;
    @FXML private Button btnValider;
    @FXML private Button btnAnnuler;

    private Stage dialogStage;
    private Utilisateur utilisateur;
    private UtilisateurDAO utilisateurDAO;
    private boolean okClicked = false;

    /**
     * Initialise le contrôleur.
     * Cette méthode est appelée automatiquement après le chargement du fichier FXML.
     * Elle configure les valeurs initiales des composants :
     * - Remplit la ComboBox des rôles avec les valeurs de l'énumération Role
     * - Active par défaut la case à cocher 'actif'
     */
    @FXML
    public void initialize() {
        roleComboBox.getItems().setAll(Role.values());
        actifCheckBox.setSelected(true);
    }

    /**
     * Configure la fenêtre de dialogue.
     * @param dialogStage La fenêtre de dialogue à configurer
     * Configure également la touche Échap pour fermer le dialogue
     */
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

    /**
     * Définit le DAO utilisateur pour les opérations de persistance.
     * @param utilisateurDAO Le DAO utilisateur à utiliser
     */
    public void setUtilisateurDAO(UtilisateurDAO utilisateurDAO) {
        this.utilisateurDAO = utilisateurDAO;
    }

    /**
     * Configure l'utilisateur à éditer.
     * Remplit les champs avec les données de l'utilisateur.
     * En cas de modification, le pseudo n'est pas modifiable.
     * Le mot de passe n'est jamais pré-rempli pour des raisons de sécurité.
     * @param utilisateur L'utilisateur à éditer, peut être null pour un nouvel utilisateur
     */
    public void setUtilisateur(Utilisateur utilisateur) {
        this.utilisateur = utilisateur;
        if (utilisateur != null) {
            pseudoField.setText(utilisateur.getPseudo());
            // Ne pas remplir le mot de passe pour des raisons de sécurité
            roleComboBox.setValue(utilisateur.getRole());
            actifCheckBox.setSelected(utilisateur.isActif());
            
            // Si c'est une modification, le pseudo ne doit pas être modifiable
            if (utilisateur.getId() != null) {
                pseudoField.setDisable(true);
            }
        }
    }

    /**
     * Indique si l'utilisateur a validé le dialogue.
     * @return true si l'utilisateur a cliqué sur Valider, false sinon
     */
    public boolean isOkClicked() {
        return okClicked;
    }

    /**
     * Gère la validation du formulaire.
     * Vérifie la validité des données, crée ou met à jour l'utilisateur,
     * et ferme le dialogue si tout s'est bien passé.
     * Le mot de passe est haché avant d'être enregistré.
     */
    @FXML
    private void doValider() {
        if (isInputValid()) {
            if (utilisateur == null) {
                utilisateur = new Utilisateur();
            }
            
            utilisateur.setPseudo(pseudoField.getText().trim());
            if (passwordField.getText() != null && !passwordField.getText().trim().isEmpty()) {
                // Hacher le mot de passe avant de l'enregistrer
                String hashedPassword = PasswordHasher.hashPassword(passwordField.getText().trim());
                utilisateur.setPassword(hashedPassword);
            }
            utilisateur.setRole(roleComboBox.getValue());
            utilisateur.setActif(actifCheckBox.isSelected());

            try {
                if (utilisateur.getId() == null) {
                    utilisateurDAO.create(utilisateur);
                } else {
                    utilisateurDAO.update(utilisateur);
                }
                okClicked = true;
                dialogStage.close();
            } catch (Exception e) {
                AlertUtils.showError("Erreur lors de l'enregistrement", e.getMessage(), dialogStage);
            }
        }
    }

    /**
     * Gère l'annulation de l'édition.
     * Ferme simplement le dialogue sans sauvegarder.
     */
    @FXML
    private void doAnnuler() {
        dialogStage.close();
    }

    /**
     * Vérifie la validité des données saisies.
     * Contrôle que :
     * - Le pseudo n'est pas vide
     * - Le mot de passe est renseigné pour un nouvel utilisateur
     * - Un rôle est sélectionné
     * @return true si les données sont valides, false sinon
     */
    private boolean isInputValid() {
        String errorMessage = "";

        if (pseudoField.getText() == null || pseudoField.getText().trim().isEmpty()) {
            errorMessage += "Le pseudo est requis\n";
        }

        // Vérifier le mot de passe uniquement pour un nouvel utilisateur
        if ((utilisateur == null || utilisateur.getId() == null) && 
            (passwordField.getText() == null || passwordField.getText().trim().isEmpty())) {
            errorMessage += "Le mot de passe est requis pour un nouvel utilisateur\n";
        }

        if (roleComboBox.getValue() == null) {
            errorMessage += "Le rôle est requis\n";
        }

        if (errorMessage.isEmpty()) {
            return true;
        } else {
            AlertUtils.showError("Erreur de saisie", errorMessage, dialogStage);
            return false;
        }
    }
}
