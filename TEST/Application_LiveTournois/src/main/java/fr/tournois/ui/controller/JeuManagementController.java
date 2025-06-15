package fr.tournois.ui.controller;

import fr.tournois.model.Jeu;
import fr.tournois.dao.JeuDAO;
import fr.tournois.dao.DAOException;
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

import java.util.List;

/**
 * Contrôleur pour la gestion des jeux.
 * Cette classe gère l'interface principale de gestion des jeux, permettant de :
 * - Lister tous les jeux
 * - Créer un nouveau jeu
 * - Modifier un jeu existant
 * - Supprimer un jeu
 * - Rechercher des jeux par nom ou genre
 */
public class JeuManagementController {
    @FXML private ListView<Jeu> jeuxListView;
    @FXML private Button btnNouveauJeu;
    @FXML private Button btnModifierJeu;
    @FXML private Button btnSupprimerJeu;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> genreFilterComboBox;

    private ObservableList<Jeu> jeux = FXCollections.observableArrayList();
    private JeuDAO jeuDAO;
    private Stage parentStage;

    /**
     * Définit la fenêtre parente pour les dialogues modaux
     * @param stage Stage parent
     */
    public void setParentStage(Stage stage) {
        this.parentStage = stage;
    }

    /**
     * Configure le DAO jeu et rafraîchit la liste
     * @param jeuDAO DAO à utiliser
     */
    public void setJeuDAO(JeuDAO jeuDAO) {
        this.jeuDAO = jeuDAO;
        rafraichirListe();
        loadGenres();
    }

    /**
     * Initialise le contrôleur (appelé automatiquement par JavaFX)
     * Configure l'affichage, les boutons et les filtres
     */
    @FXML
    public void initialize() {
        jeuxListView.setItems(jeux);
        jeuxListView.setCellFactory(list -> new ListCell<Jeu>() {
            @Override
            protected void updateItem(Jeu jeu, boolean empty) {
                super.updateItem(jeu, empty);
                if (empty || jeu == null) {
                    setText(null);
                } else {
                    setText(String.format("%s (%s, %d) - %s", 
                           jeu.getNom(), 
                           jeu.getEditeur(), 
                           jeu.getAnneeSortie(),
                           jeu.getGenre()));
                }
            }
        });

        btnModifierJeu.setDisable(true);
        btnSupprimerJeu.setDisable(true);

        jeuxListView.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            btnModifierJeu.setDisable(newSel == null);
            btnSupprimerJeu.setDisable(newSel == null);
        });

        searchField.textProperty().addListener((obs, oldText, newText) -> {
            if (jeuDAO != null) {
                filtrerJeux();
            }
        });

        genreFilterComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (jeuDAO != null) {
                filtrerJeux();
            }
        });

        genreFilterComboBox.getItems().add("Tous les genres");
        genreFilterComboBox.setValue("Tous les genres");
    }

    /**
     * Rafraîchit la liste des jeux.
     * Récupère tous les jeux depuis la base de données
     * et met à jour l'affichage.
     */
    private void rafraichirListe() {
        if (jeuDAO != null) {
            try {
                jeux.setAll(jeuDAO.findAll());
            } catch (DAOException e) {
                showError("Erreur", "Erreur lors du chargement des jeux : " + e.getMessage());
            }
        }
    }

    /**
     * Charge la liste des genres disponibles
     */
    private void loadGenres() {
        if (jeuDAO != null) {
            try {
                List<String> genres = jeuDAO.findAllGenres();
                genreFilterComboBox.getItems().clear();
                genreFilterComboBox.getItems().add("Tous les genres");
                genreFilterComboBox.getItems().addAll(genres);
                genreFilterComboBox.setValue("Tous les genres");
            } catch (DAOException e) {
                showError("Erreur", "Erreur lors du chargement des genres : " + e.getMessage());
            }
        }
    }

    /**
     * Filtre les jeux selon la recherche et le genre sélectionné
     */
    private void filtrerJeux() {
        if (jeuDAO == null) return;

        try {
            String searchText = searchField.getText();
            String selectedGenre = genreFilterComboBox.getValue();
            
            List<Jeu> resultats;
            
            if (searchText != null && !searchText.trim().isEmpty()) {
                resultats = jeuDAO.findByNom(searchText.trim());
            } else {
                resultats = jeuDAO.findAll();
            }
            
            // Filtrer par genre si nécessaire
            if (selectedGenre != null && !"Tous les genres".equals(selectedGenre)) {
                resultats = resultats.stream()
                    .filter(jeu -> selectedGenre.equals(jeu.getGenre()))
                    .toList();
            }
            
            jeux.setAll(resultats);
            
        } catch (DAOException e) {
            showError("Erreur", "Erreur lors de la recherche : " + e.getMessage());
        }
    }

    /**
     * Ouvre le dialogue de création d'un nouveau jeu
     * Gère la création et l'affichage de l'éditeur
     */
    @FXML
    private void doNouveauJeu() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fr/tournois/ui/fxml/JeuEditor.fxml"));
            Scene scene = new Scene(loader.load());
            scene.getStylesheets().add(getClass().getResource("/fr/tournois/ui/css/style.css").toExternalForm());

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Nouveau Jeu");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(parentStage);
            dialogStage.setScene(scene);

            // Centrage sur la fenêtre principale
            DialogUtils.centerDialog(dialogStage, parentStage);

            JeuEditorController controller = loader.getController();
            controller.setDialogStage(dialogStage);
            controller.setJeuDAO(jeuDAO);
            controller.setJeu(new Jeu());

            dialogStage.showAndWait();
            if (controller.isOkClicked()) {
                rafraichirListe();
                loadGenres(); 
            }
        } catch (Exception e) {
            showError("Erreur", "Impossible d'ouvrir l'éditeur de jeu :\n" + e.getMessage());
        }
    }

    /**
     * Ouvre le dialogue de modification d'un jeu existant
     * Gère la modification et l'affichage de l'éditeur
     */
    @FXML
    private void doModifierJeu() {
        Jeu selected = jeuxListView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fr/tournois/ui/fxml/JeuEditor.fxml"));
                Scene scene = new Scene(loader.load());
                scene.getStylesheets().add(getClass().getResource("/fr/tournois/ui/css/style.css").toExternalForm());

                Stage dialogStage = new Stage();
                dialogStage.setTitle("Modifier Jeu");
                dialogStage.initModality(Modality.WINDOW_MODAL);
                dialogStage.initOwner(parentStage);
                dialogStage.setScene(scene);

                DialogUtils.centerDialog(dialogStage, parentStage);

                JeuEditorController controller = loader.getController();
                controller.setDialogStage(dialogStage);
                controller.setJeuDAO(jeuDAO);
                controller.setJeu(selected);

                dialogStage.showAndWait();
                if (controller.isOkClicked()) {
                    rafraichirListe();
                    loadGenres(); 
                }
            } catch (Exception e) {
                showError("Erreur", "Impossible d'ouvrir l'éditeur de jeu :\n" + e.getMessage());
            }
        }
    }

    /**
     * Gère la suppression d'un jeu après confirmation
     */
    @FXML
    private void doSupprimerJeu() {
        Jeu selected = jeuxListView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Confirmation de suppression");
            confirm.setHeaderText(null);
            confirm.setContentText("Voulez-vous vraiment supprimer ce jeu ?\n\n" + 
                                 "Nom : " + selected.getNom() + "\n" +
                                 "Éditeur : " + selected.getEditeur() + "\n" +
                                 "Année : " + selected.getAnneeSortie());
            
            if (parentStage != null) {
                confirm.initOwner(parentStage);
                DialogUtils.centerDialog((Stage) confirm.getDialogPane().getScene().getWindow(), parentStage);
            }
            
            if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
                try {
                    jeuDAO.delete(selected);
                    rafraichirListe();
                    loadGenres(); 
                } catch (DAOException e) {
                    showError("Erreur", "Impossible de supprimer le jeu :\n" + e.getMessage());
                }
            }
        }
    }

    /**
     * Effectue une recherche de jeu par nom (champ recherche)
     */
    @FXML
    private void doRechercheJeu() {
        filtrerJeux();
    }

    /**
     * Réinitialise les filtres et affiche tous les jeux
     */
    @FXML
    private void doResetFilters() {
        searchField.clear();
        genreFilterComboBox.setValue("Tous les genres");
        rafraichirListe();
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