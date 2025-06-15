package fr.tournois.ui.controller;

import fr.tournois.dao.JeuDAO;
import fr.tournois.model.Jeu;
import fr.tournois.dao.DAOException;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.scene.input.KeyCode;
import fr.tournois.ui.util.AlertUtils;

import java.time.Year;

/**
 * Contrôleur pour l'interface d'édition d'un jeu.
 * Cette classe gère le dialogue permettant de créer ou modifier un jeu.
 * Elle fournit une interface utilisateur avec des champs pour le nom,
 * l'éditeur, l'année de sortie, le genre et la description.
 */
public class JeuEditorController {
    @FXML private TextField nomField;
    @FXML private TextField editeurField;
    @FXML private Spinner<Integer> anneeSortieSpinner;
    @FXML private TextField genreField;
    @FXML private TextArea descriptionArea;
    @FXML private Button btnValider;
    @FXML private Button btnAnnuler;

    private Stage dialogStage;
    private Jeu jeu;
    private JeuDAO jeuDAO;
    private boolean okClicked = false;

    /**
     * Initialise le contrôleur (appelé automatiquement par JavaFX)
     * Configure les composants et les règles de saisie
     */
    @FXML
    public void initialize() {
        int currentYear = Year.now().getValue();
        SpinnerValueFactory<Integer> valueFactory = 
            new SpinnerValueFactory.IntegerSpinnerValueFactory(1970, currentYear + 5, currentYear);
        anneeSortieSpinner.setValueFactory(valueFactory);
        anneeSortieSpinner.setEditable(true);

        descriptionArea.setWrapText(true);

        nomField.textProperty().addListener((obs, oldText, newText) -> {
            if (newText != null && newText.length() > 100) {
                nomField.setText(oldText);
            }
        });

        editeurField.textProperty().addListener((obs, oldText, newText) -> {
            if (newText != null && newText.length() > 100) {
                editeurField.setText(oldText);
            }
        });

        genreField.textProperty().addListener((obs, oldText, newText) -> {
            if (newText != null && newText.length() > 50) {
                genreField.setText(oldText);
            }
        });

        descriptionArea.textProperty().addListener((obs, oldText, newText) -> {
            if (newText != null && newText.length() > 300) {
                descriptionArea.setText(oldText);
            }
        });
    }

    /**
     * Configure la fenêtre de dialogue pour l'édition
     * @param dialogStage Stage de la fenêtre de dialogue
     */
    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
        
        dialogStage.getScene().setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                doAnnuler();
            }
        });
    }

    /**
     * Définit le DAO jeu pour les opérations de persistance
     * @param jeuDAO DAO à utiliser
     */
    public void setJeuDAO(JeuDAO jeuDAO) {
        this.jeuDAO = jeuDAO;
    }

    /**
     * Configure le jeu à éditer (remplit les champs)
     * @param jeu Jeu à éditer (null pour création)
     */
    public void setJeu(Jeu jeu) {
        this.jeu = jeu;
        if (jeu != null) {
            nomField.setText(jeu.getNom());
            editeurField.setText(jeu.getEditeur());
            if (jeu.getAnneeSortie() > 0) {
                anneeSortieSpinner.getValueFactory().setValue(jeu.getAnneeSortie());
            }
            genreField.setText(jeu.getGenre());
            descriptionArea.setText(jeu.getDescription());
        } else {
            anneeSortieSpinner.getValueFactory().setValue(Year.now().getValue());
        }
    }

    /**
     * Indique si l'utilisateur a validé le dialogue
     * @return true si validé, false sinon
     */
    public boolean isOkClicked() {
        return okClicked;
    }

    /**
     * Gère la validation du formulaire et l'enregistrement du jeu
     */
    @FXML
    private void doValider() {
        if (isInputValid()) {
            if (jeu == null) {
                jeu = new Jeu();
            }
            
            jeu.setNom(nomField.getText().trim());
            jeu.setEditeur(editeurField.getText() != null ? editeurField.getText().trim() : "");
            jeu.setAnneeSortie(anneeSortieSpinner.getValue());
            jeu.setGenre(genreField.getText() != null ? genreField.getText().trim() : "");
            jeu.setDescription(descriptionArea.getText() != null ? descriptionArea.getText().trim() : "");

            try {
                if (jeu.getId() == null) {
                    jeuDAO.create(jeu);
                } else {
                    jeuDAO.update(jeu);
                }
                okClicked = true;
                dialogStage.close();
            } catch (DAOException e) {
                AlertUtils.showError("Erreur lors de l'enregistrement", e.getMessage(), dialogStage);
            }
        }
    }

    /**
     * Gère l'annulation de l'édition (fermeture du dialogue)
     */
    @FXML
    private void doAnnuler() {
        dialogStage.close();
    }

    /**
     * Vérifie la validité des données saisies.
     * Contrôle que :
     * - Le nom n'est pas vide
     * - L'année de sortie est dans une plage raisonnable
     * @return true si les données sont valides, false sinon
     */
    private boolean isInputValid() {
        StringBuilder errorMessage = new StringBuilder();

        // Vérification du nom 
        if (nomField.getText() == null || nomField.getText().trim().isEmpty()) {
            errorMessage.append("Le nom du jeu est requis.\n");
        }

        // Vérification de l'année de sortie
        Integer annee = anneeSortieSpinner.getValue();
        if (annee == null) {
            errorMessage.append("L'année de sortie est requise.\n");
        } else {
            int currentYear = Year.now().getValue();
            if (annee < 1970 || annee > currentYear + 5) {
                errorMessage.append("L'année de sortie doit être comprise entre 1970 et " + (currentYear + 5) + ".\n");
            }
        }

        if (nomField.getText() != null && nomField.getText().length() > 100) {
            errorMessage.append("Le nom ne peut pas dépasser 100 caractères.\n");
        }

        if (editeurField.getText() != null && editeurField.getText().length() > 100) {
            errorMessage.append("L'éditeur ne peut pas dépasser 100 caractères.\n");
        }

        if (genreField.getText() != null && genreField.getText().length() > 50) {
            errorMessage.append("Le genre ne peut pas dépasser 50 caractères.\n");
        }

        if (descriptionArea.getText() != null && descriptionArea.getText().length() > 300) {
            errorMessage.append("La description ne peut pas dépasser 300 caractères.\n");
        }

        if (errorMessage.length() > 0) {
            AlertUtils.showError("Erreur de saisie", errorMessage.toString(), dialogStage);
            return false;
        }

        return true;
    }
}