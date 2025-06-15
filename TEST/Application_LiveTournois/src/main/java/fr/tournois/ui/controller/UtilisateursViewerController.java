package fr.tournois.ui.controller;

import fr.tournois.dao.DAOException;
import fr.tournois.dao.StaffDAO;
import fr.tournois.dao.UtilisateurDAO;
import fr.tournois.model.Staff;
import fr.tournois.model.Utilisateur;
import fr.tournois.ui.util.AlertUtils;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.stage.Stage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Contrôleur pour la fenêtre de visualisation des utilisateurs.
 * Cette classe permet d'afficher la liste des utilisateurs avec leurs staffs associés
 * dans une fenêtre modale.
 *
 * @author F. Pelleau &amp; A. Péninou
 */
public class UtilisateursViewerController {
    /** Liste des utilisateurs avec leurs staffs associés */
    @FXML
    private ListView<String> utilisateursListView;

    /** DAO pour accéder aux utilisateurs en base de données */
    private UtilisateurDAO utilisateurDAO;
    
    /** DAO pour accéder aux staffs en base de données */
    private StaffDAO staffDAO;
    
    /** Référence vers la fenêtre de dialogue */
    private Stage dialogStage;

    /**
     * Initialise les DAOs et charge la liste des utilisateurs.
     * Cette méthode doit être appelée avant l'affichage de la fenêtre.
     *
     * @param utilisateurDAO DAO pour accéder aux utilisateurs
     * @param staffDAO DAO pour accéder aux staffs
     */
    public void setDAOs(UtilisateurDAO utilisateurDAO, StaffDAO staffDAO) {
        this.utilisateurDAO = utilisateurDAO;
        this.staffDAO = staffDAO;
        chargerUtilisateurs();
    }

    /**
     * Définit la fenêtre de dialogue pour l'affichage des messages d'erreur.
     *
     * @param dialogStage La fenêtre de dialogue à utiliser
     */
    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    /**
     * Affiche une boîte de dialogue d'erreur.
     *
     * @param title Titre de la boîte de dialogue
     * @param message Message d'erreur à afficher
     */
    private void showError(String title, String message) {
        AlertUtils.showError(title, message, dialogStage);
    }

    /**
     * Charge la liste des utilisateurs depuis la base de données et les affiche
     * dans la ListView avec leurs staffs associés.
     * 
     * Le format d'affichage est :
     * "pseudo (rôle) - Staff : nom prénom" ou
     * "pseudo (rôle) - Pas de staff associé"
     */
    private void chargerUtilisateurs() {
        try {
            // Récupérer tous les utilisateurs
            List<Utilisateur> utilisateurs = utilisateurDAO.findAll();
            
            // Récupérer tous les staffs pour faire la correspondance
            List<Staff> staffs = staffDAO.findAll();
            Map<Integer, Staff> staffParUtilisateur = new HashMap<>();
            for (Staff staff : staffs) {
                if (staff.getUtilisateur() != null) {
                    staffParUtilisateur.put(staff.getUtilisateur().getId(), staff);
                }
            }
            
            // Remplir la liste avec les utilisateurs et leur staff associé
            utilisateursListView.getItems().clear();
            for (Utilisateur utilisateur : utilisateurs) {
                String ligne = String.format("%s (%s)", utilisateur.getPseudo(), utilisateur.getRole());
                Staff staffAssocie = staffParUtilisateur.get(utilisateur.getId());
                if (staffAssocie != null) {
                    ligne += String.format(" - Staff : %s %s", staffAssocie.getNom(), staffAssocie.getPrenom());
                } else {
                    ligne += " - Pas de staff associé";
                }
                utilisateursListView.getItems().add(ligne);
            }
        } catch (DAOException e) {
            showError("Erreur", "Impossible de charger la liste des utilisateurs\n" + e.getMessage());
        }
    }
}
