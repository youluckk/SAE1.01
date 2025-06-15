package fr.tournois.ui.controller;

import fr.tournois.dao.UtilisateurDAO;
import fr.tournois.model.Utilisateur;
import fr.tournois.ui.util.DialogUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import fr.tournois.ui.util.AlertUtils;

import java.util.List;

/**
 * Contrôleur pour la gestion des utilisateurs.
 * Cette classe gère l'interface principale de gestion des utilisateurs, permettant de :
 * - Lister tous les utilisateurs
 * - Créer un nouvel utilisateur
 * - Modifier un utilisateur existant
 * - Supprimer un utilisateur
 * - Rechercher un utilisateur par son pseudo
 *
 * @author F. Pelleau &amp; A. Péninou
 * @since V0.0
 */
public class UtilisateursManagementController {
    @FXML private ListView<Utilisateur> utilisateursListView;
    @FXML private Button btnNouveau;
    @FXML private Button btnModifier;
    @FXML private Button btnSupprimer;
    @FXML private TextField searchField;


    private ObservableList<Utilisateur> utilisateurs = FXCollections.observableArrayList();
    private UtilisateurDAO utilisateurDAO;
    private Stage parentStage;


    /**
     * Définit la fenêtre parente.
     * Utilisée pour le positionnement des dialogues modaux.
     * @param stage La fenêtre parente
     */
    public void setParentStage(Stage stage) {
        this.parentStage = stage;
    }

    /**
     * Configure le DAO utilisateur et rafraîchit la liste.
     * @param utilisateurDAO Le DAO utilisateur à utiliser
     */
    public void setUtilisateurDAO(UtilisateurDAO utilisateurDAO) {
        this.utilisateurDAO = utilisateurDAO;
        rafraichirListe();
    }

    /**
     * Initialise le contrôleur.
     * Cette méthode est appelée automatiquement après le chargement du fichier FXML.
     * Elle configure :
     * - L'affichage des utilisateurs dans la ListView
     * - L'état des boutons en fonction de la sélection
     * - La recherche en temps réel
     */
    @FXML
    public void initialize() {

        utilisateursListView.setItems(utilisateurs);
        utilisateursListView.setCellFactory(list -> new ListCell<Utilisateur>() {
            @Override
            protected void updateItem(Utilisateur utilisateur, boolean empty) {
                super.updateItem(utilisateur, empty);
                if (empty || utilisateur == null) {
                    setText(null);
                } else {
                    setText(utilisateur.getPseudo() + " (" + utilisateur.getRole() + ", " + 
                          (utilisateur.isActif() ? "Actif" : "Inactif") + ")");
                }
            }
        });


        btnModifier.setDisable(true);
        btnSupprimer.setDisable(true);

        utilisateursListView.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            btnModifier.setDisable(newSel == null);
            btnSupprimer.setDisable(newSel == null);
        });

        searchField.textProperty().addListener((obs, oldText, newText) -> {
            if (utilisateurDAO != null) {
                if (newText == null || newText.trim().isEmpty()) {
                    rafraichirListe();
                } else {
                    utilisateurs.setAll(utilisateurDAO.findByPseudo(newText.trim()).map(List::of).orElse(List.of()));
                }
            }
        });
    }

    /**
     * Rafraîchit la liste des utilisateurs.
     * Récupère tous les utilisateurs depuis la base de données
     * et met à jour l'affichage.
     */
    private void rafraichirListe() {
        if (utilisateurDAO != null) {
            utilisateurs.setAll(utilisateurDAO.findAll());
        }
    }

    /**
     * Ouvre le dialogue de création d'un nouvel utilisateur.
     * Crée une nouvelle instance d'Utilisateur et ouvre l'éditeur.
     * Si l'utilisateur valide, rafraîchit la liste.
     */
    @FXML
    private void doNouvelUtilisateur() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fr/tournois/ui/fxml/UtilisateurEditor.fxml"));
            Scene scene = new Scene(loader.load());
            scene.getStylesheets().add(getClass().getResource("/fr/tournois/ui/css/style.css").toExternalForm());

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Nouvel Utilisateur");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(parentStage);
            dialogStage.setScene(scene);

            // Centrage sur la fenêtre principale
            DialogUtils.centerDialog(dialogStage, parentStage);

            UtilisateurEditorController controller = loader.getController();
            controller.setDialogStage(dialogStage);
            controller.setUtilisateurDAO(utilisateurDAO);
            controller.setUtilisateur(new Utilisateur());

            dialogStage.showAndWait();
            if (controller.isOkClicked()) {
                rafraichirListe();
            }
        } catch (Exception e) {
            showError("Erreur", "Impossible d'ouvrir l'éditeur d'utilisateur :\n" + e.getMessage());
        }
    }

    /**
     * Ouvre le dialogue de modification d'un utilisateur existant.
     * Récupère l'utilisateur sélectionné et ouvre l'éditeur.
     * Si l'utilisateur valide, rafraîchit la liste.
     */
    @FXML
    private void doModifierUtilisateur() {
        Utilisateur selected = utilisateursListView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fr/tournois/ui/fxml/UtilisateurEditor.fxml"));
                Scene scene = new Scene(loader.load());
                scene.getStylesheets().add(getClass().getResource("/fr/tournois/ui/css/style.css").toExternalForm());

                Stage dialogStage = new Stage();
                dialogStage.setTitle("Modifier Utilisateur");
                dialogStage.initModality(Modality.WINDOW_MODAL);
                dialogStage.initOwner(parentStage);
                dialogStage.setScene(scene);

                // Centrage sur la fenêtre principale
                dialogStage.setOnShown(e -> {
                    dialogStage.setX(parentStage.getX() + (parentStage.getWidth() - dialogStage.getWidth()) / 2);
                    dialogStage.setY(parentStage.getY() + (parentStage.getHeight() - dialogStage.getHeight()) / 2);
                });

                UtilisateurEditorController controller = loader.getController();
                controller.setDialogStage(dialogStage);
                controller.setUtilisateurDAO(utilisateurDAO);
                controller.setUtilisateur(selected);

                dialogStage.showAndWait();
                if (controller.isOkClicked()) {
                    rafraichirListe();
                }
            } catch (Exception e) {
                showError("Erreur", "Impossible d'ouvrir l'éditeur d'utilisateur :\n" + e.getMessage());
            }
        }
    }

    /**
     * Gère la suppression d'un utilisateur.
     * Demande confirmation avant la suppression.
     * Si confirmé, supprime l'utilisateur et rafraîchit la liste.
     */
    @FXML
    private void doSupprimerUtilisateur() {
        Utilisateur selected = utilisateursListView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Confirmation de suppression");
            confirm.setHeaderText(null);
            confirm.setContentText("Voulez-vous vraiment supprimer cet utilisateur ?");
            
            if (parentStage != null) {
                confirm.initOwner(parentStage);
            }
            
            if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
                try {
                    utilisateurDAO.delete(selected.getId());
                    rafraichirListe();
                } catch (Exception e) {
                    showError("Erreur", "Impossible de supprimer l'utilisateur :\n" + e.getMessage());
                }
            }
        }
    }

    /**
     * Effectue une recherche d'utilisateur par pseudo.
     * Si le champ de recherche est vide, affiche tous les utilisateurs.
     * Sinon, filtre les utilisateurs par pseudo.
     */
    @FXML
    private void doRechercheUtilisateur() {
        String searchText = searchField.getText().trim();
        if (utilisateurDAO != null) {
            if (searchText.isEmpty()) {
                rafraichirListe();
            } else {
                utilisateurs.setAll(utilisateurDAO.findByPseudo(searchText).map(List::of).orElse(List.of()));
            }
        }
    }

    /**
     * Affiche une boîte de dialogue d'erreur.
     * @param title Le titre de la boîte de dialogue
     * @param message Le message d'erreur à afficher
     */
    private void showError(String title, String message) {
        AlertUtils.showError(title, message, parentStage);
    }
}
