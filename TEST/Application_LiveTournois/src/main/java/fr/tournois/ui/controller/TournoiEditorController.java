package fr.tournois.ui.controller;

import fr.tournois.model.Tournoi;
import fr.tournois.model.Jeu;
import fr.tournois.dao.DAOException;
import fr.tournois.dao.JeuDAO;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.scene.input.KeyCode;
import fr.tournois.ui.util.AlertUtils;
import java.sql.Connection;
import javafx.collections.FXCollections;

public class TournoiEditorController {
    @FXML private TextField nomField;
    @FXML private DatePicker dateDebutField;
    @FXML private DatePicker dateFinField;
    @FXML private TextField lieuField;
    @FXML private TextField formatField;
    @FXML private TextField nbEquipesMaxField;
    @FXML private TextField statutField;
    @FXML private TextField prixPoolField;
    @FXML private ComboBox<Jeu> jeuComboBox;
    @FXML private Button okButton;
    @FXML private Button cancelButton;

    private Connection connection;

    private Stage dialogStage;
    private Tournoi tournoi;
    private boolean okClicked = false;
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
    public void setTournoi(Tournoi tournoi) {
        this.tournoi = tournoi;
        if (tournoi != null) {
            // Si le tournoi existe déjà, on charge ses valeurs
            nomField.setText(tournoi.getNom());
            dateDebutField.setValue(tournoi.getDateDebut());
            dateFinField.setValue(tournoi.getDateFin());
            lieuField.setText(tournoi.getLieu());
            formatField.setText(tournoi.getFormat());
            if (tournoi.getNbEquipesMax() == 0) {
                // Tournoi nouveau
                nbEquipesMaxField.setText("1");
            } else {
                nbEquipesMaxField.setText(String.valueOf(tournoi.getNbEquipesMax()));
            }
            statutField.setText(tournoi.getStatut());
            prixPoolField.setText(String.valueOf(tournoi.getPrixPool()));
            jeuComboBox.setValue(tournoi.getJeu());
        } 
    }


    public boolean isOkClicked() {
        return okClicked;
    }
    @FXML
    private void doValider() {
        if (isInputValid()) {
            try {
                tournoi.setNom(nomField.getText().trim());
                tournoi.setDateDebut(dateDebutField.getValue());
                tournoi.setDateFin(dateFinField.getValue());
                tournoi.setLieu(lieuField.getText());
                tournoi.setFormat(formatField.getText());
                tournoi.setNbEquipesMax(Integer.parseInt(nbEquipesMaxField.getText()));
                tournoi.setStatut(statutField.getText());
                tournoi.setPrixPool(Double.parseDouble(prixPoolField.getText()));
                tournoi.setJeu(jeuComboBox.getValue());

                okClicked = true;
                dialogStage.close();
            } catch (DAOException e) {
                showError("Erreur lors de l'enregistrement", e.getMessage());
            }
        }
    }
    @FXML
    private void doAnnuler() {
        dialogStage.close();
    }
    private boolean isInputValid() {
        StringBuilder errorMessage = new StringBuilder();
        if (nomField.getText() == null || nomField.getText().trim().isEmpty()) {
            errorMessage.append("Le nom est obligatoire.\n");
        }
        if (dateDebutField.getValue() == null) {
            errorMessage.append("La date de début est obligatoire.\n");
        }
        if (dateFinField.getValue() == null) {
            errorMessage.append("La date de fin est obligatoire.\n");
        } else if (dateDebutField.getValue() != null && dateFinField.getValue().isBefore(dateDebutField.getValue())) {
            errorMessage.append("La date de fin doit être postérieure ou égale à la date de début.\n");
        }
        if (lieuField.getText() == null || lieuField.getText().trim().isEmpty()) {
            errorMessage.append("Le lieu est obligatoire.\n");
        }
        if (formatField.getText() == null || formatField.getText().trim().isEmpty()) {
            errorMessage.append("Le format est obligatoire.\n");
        }
        if (nbEquipesMaxField.getText() == null || nbEquipesMaxField.getText().trim().isEmpty()) {
            errorMessage.append("Le nombre d'équipes maximum est obligatoire.\n");
        } else {
            try {
                int nbEquipes = Integer.parseInt(nbEquipesMaxField.getText().trim());
                if (nbEquipes <= 0) {
                    errorMessage.append("Le nombre d'équipes maximum doit être supérieur à 0.\n");
                }
            } catch (NumberFormatException e) {
                errorMessage.append("Le nombre d'équipes maximum doit être un nombre entier.\n");
            }
        }
        if (statutField.getText() == null || statutField.getText().trim().isEmpty()) {
            errorMessage.append("Le statut est obligatoire.\n");
        }
        if (prixPoolField.getText() == null || prixPoolField.getText().trim().isEmpty()) {
            errorMessage.append("Le prix pool est obligatoire.\n");
        } else {
            try {
                double prixPool = Double.parseDouble(prixPoolField.getText().trim());
                if (prixPool < 0) {
                    errorMessage.append("Le prix pool doit être supérieur ou égal à 0.\n");
                }
            } catch (NumberFormatException e) {
                errorMessage.append("Le prix pool doit être un nombre.\n");
            }
        }
        if (jeuComboBox.getValue() == null) {
            errorMessage.append("Le jeu est obligatoire.\n");
        }

        if (errorMessage.length() > 0) {
            showError("Champs invalides", errorMessage.toString());
            return false;
        }
        return true;
    }

    private void showError(String title, String message) {
        AlertUtils.showError(title, message, dialogStage);
    }

    @FXML
    private void initialize() {
        // Ajoute le CSS si ce n'est pas déjà fait
        if (nomField.getScene() != null) {
            Scene scene = nomField.getScene();
            if (scene.getStylesheets().stream().noneMatch(s -> s.contains("css/style.css"))) {
                scene.getStylesheets().add(getClass().getResource("/fr/tournois/ui/css/style.css").toExternalForm());
            }
        }

        // Configuration de la ComboBox des jeux
        jeuComboBox.setConverter(new javafx.util.StringConverter<Jeu>() {
            @Override
            public String toString(Jeu jeu) {
                return jeu == null ? "" : jeu.getNom();
            }

            @Override
            public Jeu fromString(String string) {
                return null; // Pas nécessaire car la ComboBox n'est pas éditable
            }
        });
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
        loadJeux();
    }

    private void loadJeux() {
        try {
            JeuDAO jeuDAO = new JeuDAO(connection);
            jeuComboBox.setItems(FXCollections.observableArrayList(jeuDAO.findAll()));
        } catch (DAOException e) {
            showError("Erreur", "Impossible de charger la liste des jeux");
        }
    }
}
