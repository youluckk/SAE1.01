package fr.tournois.ui.controller;

import fr.tournois.security.SecurityContext;
import fr.tournois.security.PasswordHasher;
import fr.tournois.dao.UtilisateurDAO;
import fr.tournois.model.Utilisateur;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class LoginController {
    @FXML private TextField pseudoField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private Label messageLabel;

    private UtilisateurDAO utilisateurDAO;
    private Stage dialogStage;
    private Utilisateur utilisateurConnecte;

    public void setUtilisateurDAO(UtilisateurDAO utilisateurDAO) {
        this.utilisateurDAO = utilisateurDAO;
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public Utilisateur getUtilisateurConnecte() {
        return utilisateurConnecte;
    }

    @FXML
    private void doConnexion() {
        String pseudo = pseudoField.getText().trim();
        String password = passwordField.getText();
        if (pseudo.isEmpty() || password.isEmpty()) {
            messageLabel.setText("Merci de remplir tous les champs obligatoires.");
            return;
        }
        try {
            Utilisateur u = utilisateurDAO.findByPseudo(pseudo).orElse(null);
            if (u != null && PasswordHasher.verifyPassword(password, u.getPassword())) {
                try {
                    utilisateurDAO.updateDerniereConnexion(u.getId());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                SecurityContext.getInstance().setCurrentUser(u);
                utilisateurConnecte = u;
                dialogStage.close();
            } else {
                messageLabel.setText("Pseudo ou mot de passe incorrect.");
            }
        } catch (Exception e) {
            messageLabel.setText("Erreur technique : " + e.getMessage());
        }
    }
}
