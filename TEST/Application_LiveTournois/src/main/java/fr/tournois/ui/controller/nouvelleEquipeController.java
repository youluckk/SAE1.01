package fr.tournois.ui.controller;


import fr.tournois.model.Equipe;

import fr.tournois.dao.DAOException;
import fr.tournois.dao.EquipeDAO;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import javafx.scene.input.KeyCode;
import fr.tournois.ui.util.AlertUtils;

import java.time.LocalDate;
import java.time.Year;

/**
 * Contrôleur pour l'interface d'édition d'un jeu.
 * Cette classe gère le dialogue permettant de créer ou modifier un jeu.
 * Elle fournit une interface utilisateur avec des champs pour le nom,
 * l'éditeur, l'année de sortie, le genre et la description.
 */
public class nouvelleEquipeController {
    @FXML private TextField nomField;
    @FXML private TextField tagField;
    @FXML private DatePicker dateCreationPicker;
    @FXML private TextField logoField;
    @FXML private TextArea descriptionArea;
    @FXML private TextField paysField;
    @FXML private Button btnValider;
    @FXML private Button btnAnnuler;
    @FXML private Label titleLabel;

    private Stage dialogStage;
    private Equipe equipe;
    private EquipeDAO equipeDAO;
    private boolean okClicked = false;
    private EquipeController parentController;

    /**
     * Initialise le contrôleur.
     * Cette méthode est appelée automatiquement après le chargement du fichier FXML.
     * Elle configure les composants de l'interface :
     * - Le spinner pour l'année de sortie avec des valeurs raisonnables
     * - La zone de texte pour la description avec un retour à la ligne automatique
     */
    @FXML
    public void initialize() {
        
        
        dateCreationPicker.setValue(LocalDate.now());

        descriptionArea.setWrapText(true);

        nomField.textProperty().addListener((obs, oldText, newText) -> {
            if (newText != null && newText.length() > 100) {
                nomField.setText(oldText);
            }
        });

        tagField.textProperty().addListener((obs, oldText, newText) -> {
            if (newText != null && newText.length() > 100) {
                tagField.setText(oldText);
            }
        });

        logoField.textProperty().addListener((obs, oldText, newText) -> {
            if (newText != null && newText.length() > 50) {
                logoField.setText(oldText);
            }
        });

        descriptionArea.textProperty().addListener((obs, oldText, newText) -> {
            if (newText != null && newText.length() > 300) {
                descriptionArea.setText(oldText);
            }
        });
    }

    /**
     * Configure la fenêtre de dialogue.
     * @param dialogStage La fenêtre de dialogue à configurer
     * Configure également la touche Échap pour fermer le dialogue
     */
    public void setDialogStage(Stage dialogStage) {
    this.dialogStage = dialogStage;
}

    /**
     * Définit le DAO jeu pour les opérations de persistance.
     * @param jeuDAO Le DAO jeu à utiliser
     */
    public void setEquipeDAO(EquipeDAO equipeDAO) {
        this.equipeDAO = equipeDAO;
    }

    public void setParentController(EquipeController parentController) {
    this.parentController = parentController;
}

    /**
     * Configure l'équipe à éditer.
     * Remplit les champs avec les données de l'équipe.
     * @param equipe L'équipe à éditer, peut être null pour une nouvelle équipe
     */
    public void setEquipe(Equipe equipe) {
        this.equipe = equipe;
        if (equipe != null) {
            nomField.setText(equipe.getNom());
            tagField.setText(equipe.getTag());
            dateCreationPicker.setValue(equipe.getDateCreation());
            logoField.setText(equipe.getLogo());
            descriptionArea.setText(equipe.getDescription());
            paysField.setText(equipe.getPays());
        } else {
            dateCreationPicker.setValue(LocalDate.now());
        }
    }

    
    public void setModeModification(boolean isModification) {
    if (isModification) {
        if (titleLabel != null) {
            titleLabel.setText("Modifier Équipe");
        }
        if (dialogStage != null) {
            dialogStage.setTitle("Modifier Équipe");
        }
        if (btnValider != null) {
            btnValider.setText("Modifier");
        }
    } else {
        if (titleLabel != null) {
            titleLabel.setText("Nouvelle Équipe");
        }
        if (dialogStage != null) {
            dialogStage.setTitle("Nouvelle Équipe");
        }
        if (btnValider != null) {
            btnValider.setText("Valider");
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
     * Vérifie la validité des données, crée ou met à jour le jeu,
     * et ferme le dialogue si tout s'est bien passé.
     */
    @FXML
    private void doValider() {
        if (isInputValid()) {
            if (equipe == null) {
                equipe = new Equipe();
            }
            equipe.setNom(nomField.getText().trim());
            equipe.setTag(tagField.getText() != null ? tagField.getText().trim() : "");
            equipe.setDateCreation(dateCreationPicker.getValue());
            equipe.setLogo(logoField.getText() != null ? logoField.getText().trim() : "");
            equipe.setDescription(descriptionArea.getText() != null ? descriptionArea.getText().trim() : "");
            equipe.setPays(paysField.getText() != null ? paysField.getText().trim() : "");

            try {
                if (equipe.getId() == null) {
                    equipeDAO.create(equipe);
                } else {
                    equipeDAO.update(equipe);
                }
                okClicked = true;
                dialogStage.close();
            } catch (DAOException e) {
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
     * - Le nom n'est pas vide
     * - L'année de sortie est dans une plage raisonnable
     * @return true si les données sont valides, false sinon
     */
    private boolean isInputValid() {
        StringBuilder errorMessage = new StringBuilder();

        // Vérification du nom 
        if (nomField.getText() == null || nomField.getText().trim().isEmpty()) {
            errorMessage.append("Le nom de l'équipe est requis.\n");
        }

        // Vérification de l'année de création
        Integer annee = dateCreationPicker.getValue().getYear();
        if (annee == null) {
            errorMessage.append("L'année de création est requise.\n");
        } else {
            int currentYear = Year.now().getValue();
            if (annee < 1970 || annee > currentYear + 5) {
                errorMessage.append("L'année de création doit être comprise entre 1970 et " + (currentYear + 5) + ".\n");
            }
        }

        if (nomField.getText() != null && nomField.getText().length() > 100) {
            errorMessage.append("Le nom ne peut pas dépasser 100 caractères.\n");
        }

        if (tagField.getText() != null && tagField.getText().length() > 10) {
            errorMessage.append("Le tag ne peut pas dépasser 10 caractères.\n");
        }

        if (logoField.getText() != null && logoField.getText().length() > 50) {
            errorMessage.append("Le logo ne peut pas dépasser 50 caractères.\n");
        }

        if (descriptionArea.getText() != null && descriptionArea.getText().length() > 300) {
            errorMessage.append("La description ne peut pas dépasser 300 caractères.\n");
        }

        if (errorMessage.length() > 0) {
            AlertUtils.showError("Erreur de saisie", errorMessage.toString(), dialogStage);
            return false;
        }
        if (paysField.getText() != null && paysField.getText().length() > 50) {
            errorMessage.append("Le pays ne peut pas dépasser 50 caractères.\n");
        }

        return true;
    }
}
    
