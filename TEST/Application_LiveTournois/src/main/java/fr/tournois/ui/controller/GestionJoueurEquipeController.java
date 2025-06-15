package fr.tournois.ui.controller;

import fr.tournois.dao.JoueurDAO;
import fr.tournois.model.Equipe;
import fr.tournois.model.Joueur;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class GestionJoueurEquipeController implements Initializable {

    @FXML private Label titleLabel;
    @FXML private Label equipeLabel;
    @FXML private TextField searchJoueursLibres;
    @FXML private TextField searchJoueursEquipe;
    @FXML private ListView<Joueur> joueursLibresListView;
    @FXML private ListView<Joueur> joueursEquipeListView;
    @FXML private Button ajouterJoueurBtn;
    @FXML private Button creerJoueurBtn;
    @FXML private Button retirerJoueurBtn;
    @FXML private Button modifierJoueurBtn;
    @FXML private Button fermerBtn;

    private Stage dialogStage;
    private Equipe equipe;
    private JoueurDAO joueurDAO;
    private Object parentController;

    private ObservableList<Joueur> joueursLibres = FXCollections.observableArrayList();
    private ObservableList<Joueur> joueursEquipe = FXCollections.observableArrayList();
    private FilteredList<Joueur> filteredJoueursLibres;
    private FilteredList<Joueur> filteredJoueursEquipe;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Configuration des ListView avec des CellFactory personnalisées
        configurerListViews();

        // Configuration des filtres de recherche
        configurerFiltres();

        // Configuration des listeners pour activer/désactiver les boutons
        configurerListeners();
    }

    private void configurerListViews() {
        // CellFactory pour afficher les joueurs avec leurs informations
        Callback<ListView<Joueur>, ListCell<Joueur>> cellFactory = listView -> new ListCell<Joueur>() {
            @Override
            protected void updateItem(Joueur joueur, boolean empty) {
                super.updateItem(joueur, empty);
                if (empty || joueur == null) {
                    setText(null);
                } else {
                    setText(String.format("%s %s (%s)",
                            joueur.getPrenom(),
                            joueur.getNom(),
                            joueur.getPseudo() != null ? joueur.getPseudo() : "Pas de pseudo"));
                }
            }
        };

        joueursLibresListView.setCellFactory(cellFactory);
        joueursEquipeListView.setCellFactory(cellFactory);
    }

    private void configurerFiltres() {
        filteredJoueursLibres = new FilteredList<>(joueursLibres, p -> true);
        filteredJoueursEquipe = new FilteredList<>(joueursEquipe, p -> true);

        joueursLibresListView.setItems(filteredJoueursLibres);
        joueursEquipeListView.setItems(filteredJoueursEquipe);

        // Filtre pour les joueurs libres
        searchJoueursLibres.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredJoueursLibres.setPredicate(joueur -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }
                String lowerCaseFilter = newValue.toLowerCase();
                return joueur.getNom().toLowerCase().contains(lowerCaseFilter) ||
                        joueur.getPrenom().toLowerCase().contains(lowerCaseFilter) ||
                        (joueur.getPseudo() != null && joueur.getPseudo().toLowerCase().contains(lowerCaseFilter));
            });
        });

        // Filtre pour les joueurs de l'équipe
        searchJoueursEquipe.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredJoueursEquipe.setPredicate(joueur -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }
                String lowerCaseFilter = newValue.toLowerCase();
                return joueur.getNom().toLowerCase().contains(lowerCaseFilter) ||
                        joueur.getPrenom().toLowerCase().contains(lowerCaseFilter) ||
                        (joueur.getPseudo() != null && joueur.getPseudo().toLowerCase().contains(lowerCaseFilter));
            });
        });
    }

    private void configurerListeners() {
        // Activer/désactiver le bouton "Ajouter à l'équipe"
        joueursLibresListView.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> ajouterJoueurBtn.setDisable(newValue == null)
        );

        // Activer/désactiver les boutons "Retirer" et "Modifier"
        joueursEquipeListView.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    boolean disable = newValue == null;
                    retirerJoueurBtn.setDisable(disable);
                    modifierJoueurBtn.setDisable(disable);
                }
        );
    }

    @FXML
    private void ajouterJoueurAEquipe() {
        Joueur joueurSelectionne = joueursLibresListView.getSelectionModel().getSelectedItem();
        if (joueurSelectionne != null) {
            try {
                // Affecter le joueur à l'équipe
                joueurSelectionne.setEquipe(equipe);
                joueurDAO.mettreAJourJoueur(joueurSelectionne);

                // Mettre à jour les listes
                joueursLibres.remove(joueurSelectionne);
                joueursEquipe.add(joueurSelectionne);

                showInfo("Succès", "Joueur ajouté à l'équipe avec succès.");
            } catch (SQLException e) {
                showError("Erreur", "Impossible d'ajouter le joueur à l'équipe :\n" + e.getMessage());
            }
        }
    }

    @FXML
    private void retirerJoueurDeEquipe() {
        Joueur joueurSelectionne = joueursEquipeListView.getSelectionModel().getSelectedItem();
        if (joueurSelectionne != null) {
            Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
            confirmation.setTitle("Confirmation");
            confirmation.setHeaderText("Retirer le joueur de l'équipe");
            confirmation.setContentText("Êtes-vous sûr de vouloir retirer " +
                    joueurSelectionne.getPrenom() + " " + joueurSelectionne.getNom() +
                    " de l'équipe ?");

            if (confirmation.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
                try {
                    // Retirer le joueur de l'équipe
                    joueurSelectionne.setEquipe(null);
                    joueurDAO.mettreAJourJoueur(joueurSelectionne);

                    // Mettre à jour les listes
                    joueursEquipe.remove(joueurSelectionne);
                    joueursLibres.add(joueurSelectionne);

                    showInfo("Succès", "Joueur retiré de l'équipe avec succès.");
                } catch (SQLException e) {
                    showError("Erreur", "Impossible de retirer le joueur de l'équipe :\n" + e.getMessage());
                }
            }
        }
    }

    @FXML
    private void fermerDialog() {
        dialogStage.close();
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public void setEquipe(Equipe equipe) {
        this.equipe = equipe;
        if (equipe != null) {
            equipeLabel.setText("Équipe : " + equipe.getNom());
            chargerJoueurs();
        }
    }

    public void setJoueurDAO(JoueurDAO joueurDAO) {
        this.joueurDAO = joueurDAO;
    }

    public void setParentController(Object parentController) {
        this.parentController = parentController;
    }

    private void chargerJoueurs() {
        if (joueurDAO == null || equipe == null) {
            System.out.println("ERREUR: joueurDAO ou equipe est null");
            return;
        }

        try {
            List<Joueur> tousLesJoueurs = joueurDAO.getTousLesJoueurs();
            System.out.println("Nombre total de joueurs récupérés: " + tousLesJoueurs.size());

            // Séparer les joueurs libres des joueurs de l'équipe
            List<Joueur> joueursLibresList = new ArrayList<>();
            List<Joueur> joueursEquipeList = new ArrayList<>();

            for (Joueur joueur : tousLesJoueurs) {
                if (joueur.getEquipe() == null) {
                    // Joueur libre (sans équipe)
                    joueursLibresList.add(joueur);
                } else if (joueur.getEquipe().getId().equals(equipe.getId())) {
                    // Joueur de l'équipe sélectionnée
                    joueursEquipeList.add(joueur);
                }
                // Les joueurs d'autres équipes ne sont ajoutés nulle part
            }

            System.out.println("Joueurs libres: " + joueursLibresList.size());
            System.out.println("Joueurs de l'équipe '" + equipe.getNom() + "': " + joueursEquipeList.size());

            // Mettre à jour les listes observables
            joueursLibres.setAll(joueursLibresList);
            joueursEquipe.setAll(joueursEquipeList);

        } catch (SQLException e) {
            System.err.println("Erreur lors du chargement des joueurs: " + e.getMessage());
            e.printStackTrace();
            showError("Erreur", "Impossible de charger les joueurs :\n" + e.getMessage());
        }
    }

    // Méthode pour configurer complètement le contrôleur
    public void configurerController(Stage dialogStage, Equipe equipe, JoueurDAO joueurDAO, Object parentController) {
        System.out.println("Configuration du contrôleur...");
        this.dialogStage = dialogStage;
        this.joueurDAO = joueurDAO;
        this.parentController = parentController;
        this.equipe = equipe;

        if (equipe != null) {
            equipeLabel.setText("Équipe : " + equipe.getNom());
            System.out.println("Équipe configurée: " + equipe.getNom() + " (ID: " + equipe.getId() + ")");
            chargerJoueurs();
        } else {
            System.out.println("ATTENTION: Équipe est null !");
        }
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}