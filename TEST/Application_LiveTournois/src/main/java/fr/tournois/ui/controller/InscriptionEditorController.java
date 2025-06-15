package fr.tournois.ui.controller;

import fr.tournois.dao.*;
import fr.tournois.model.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Contrôleur pour l'édition d'une inscription
 */
public class InscriptionEditorController implements Initializable {

    @FXML private Label labelTitre;
    @FXML private Label labelTournoiInfo;
    @FXML private ComboBox<Equipe> comboEquipe;
    @FXML private ComboBox<String> comboStatut;
    @FXML private Label labelInfoTournoi;
    @FXML private Label labelPlacesDisponibles;
    @FXML private Label labelMessage;
    @FXML private Button btnAnnuler;
    @FXML private Button btnValider;

    private InscriptionDAO inscriptionDAO;
    private TournoiDAO tournoiDAO;
    private EquipeDAO equipeDAO;
    
    private boolean modeCreation = true;
    private Inscription inscriptionEnCours;
    private boolean inscriptionCreee = false;
    private boolean inscriptionModifiee = false;
    private boolean chargerDonneesTermine = false;
    
    private Stage dialogStage;

    /**
     * Initialise le contrôleur (appelé automatiquement par JavaFX)
     * Configure les contrôles et les événements
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupControls();
        setupEventHandlers();
    }

    /**
     * Configure les contrôles
     */
    private void setupControls() {
        // Configuration des statuts
        ObservableList<String> statuts = FXCollections.observableArrayList(
            "En attente", "Confirmé", "Annulé", "Refusé"
        );
        comboStatut.setItems(statuts);
        comboStatut.setValue("En attente");
        
        // Masquer le message initialement
        labelMessage.setVisible(false);
    }

    /**
     * Configure les gestionnaires d'événements
     */
    private void setupEventHandlers() {
        // Quand une équipe est sélectionnée, vérifier si elle n'est pas déjà inscrite
        comboEquipe.setOnAction(e -> verifierInscriptionExistante());
    }

    /**
     * Définit les DAOs nécessaires pour l'édition d'inscription
     * @param inscriptionDAO DAO des inscriptions
     * @param tournoiDAO DAO des tournois
     * @param equipeDAO DAO des équipes
     */
    public void setDAOs(InscriptionDAO inscriptionDAO, TournoiDAO tournoiDAO, EquipeDAO equipeDAO) {
        this.inscriptionDAO = inscriptionDAO;
        this.tournoiDAO = tournoiDAO;
        this.equipeDAO = equipeDAO;
        
        chargerDonnees();
    }

    /**
     * Configure le mode création (nouvelle inscription)
     */
    public void setModeCreation() {
        this.modeCreation = true;
        this.inscriptionEnCours = new Inscription();
        labelTitre.setText("Nouvelle Inscription");
        btnValider.setText("Créer");
    }

    /**
     * Configure le mode création avec tournoi pré-sélectionné
     * @param tournoi Tournoi pré-sélectionné
     */
    public void setModeCreationAvecTournoi(Tournoi tournoi) {
        this.modeCreation = true;
        this.inscriptionEnCours = new Inscription();
        this.inscriptionEnCours.setTournoi(tournoi);
        this.inscriptionEnCours.setSeed(0); // Pas de seed
        labelTitre.setText("Inscrire une équipe");
        btnValider.setText("Inscrire");
        
        // Afficher les informations du tournoi
        if (chargerDonneesTermine) {
            afficherInfoTournoi(tournoi);
        }
    }

    /**
     * Configure le mode modification (édition d'une inscription)
     * @param inscription Inscription à modifier
     */
    public void setModeModification(Inscription inscription) {
        this.modeCreation = false;
        this.inscriptionEnCours = inscription;
        labelTitre.setText("Modifier l'Inscription");
        btnValider.setText("Modifier");
        
        // Pré-remplir les champs une fois les données chargées
        if (chargerDonneesTermine) {
            remplirChamps();
        }
    }

    /**
     * Charge les données depuis la base
     */
    private void chargerDonnees() {
        try {            
            // Charger les équipes
            List<Equipe> equipes = equipeDAO.getToutesLesEquipes();
            ObservableList<Equipe> equipeItems = FXCollections.observableArrayList(equipes);
            comboEquipe.setItems(equipeItems);
            
            // Configurer l'affichage de la ComboBox équipes
            comboEquipe.setConverter(new javafx.util.StringConverter<Equipe>() {
                @Override
                public String toString(Equipe equipe) {
                    return equipe == null ? "" : equipe.getNom() + " (" + equipe.getTag() + ")";
                }
                
                @Override
                public Equipe fromString(String string) {
                    return null;
                }
            });
            
            chargerDonneesTermine = true;
            
            // Si en mode modification, remplir les champs
            if (!modeCreation && inscriptionEnCours != null) {
                remplirChamps();
            }
            
            // Si en mode création avec tournoi, afficher les infos
            if (modeCreation && inscriptionEnCours != null && inscriptionEnCours.getTournoi() != null) {
                afficherInfoTournoi(inscriptionEnCours.getTournoi());
            }
            
        } catch (Exception e) {
            showError("Erreur", "Impossible de charger les données: " + e.getMessage());
        }
    }

    /**
     * Affiche les informations du tournoi
     */
    private void afficherInfoTournoi(Tournoi tournoi) {
        if (tournoi == null) {
            labelTournoiInfo.setText("Aucun tournoi sélectionné");
            labelInfoTournoi.setText("");
            labelPlacesDisponibles.setText("");
            return;
        }
        
        // Nom du tournoi
        labelTournoiInfo.setText(tournoi.getNom());
        
        try {
            // Informations générales du tournoi (places fixes à 16)
            String info = String.format("%s - %s\nLieu: %s\nFormat: %s\nNombre max d'équipes: 16",
                tournoi.getDateDebut(),
                tournoi.getDateFin(),
                tournoi.getLieu(),
                tournoi.getFormat());
            labelInfoTournoi.setText(info);
            
            // Places disponibles (calcul avec 16 places max)
            int placesRestantes = calculerPlacesRestantes(tournoi);
            String textePlaces;
            if (placesRestantes > 0) {
                textePlaces = String.format("Places disponibles: %d/16", placesRestantes);
                labelPlacesDisponibles.setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
            } else {
                textePlaces = "TOURNOI COMPLET (16/16)";
                labelPlacesDisponibles.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
            }
            labelPlacesDisponibles.setText(textePlaces);
            
        } catch (Exception e) {
            labelInfoTournoi.setText("Erreur lors du chargement des informations");
            labelPlacesDisponibles.setText("");
        }
    }

    /**
     * Calcule les places restantes (16 max)
     */
    private int calculerPlacesRestantes(Tournoi tournoi) throws DAOException {
        int nbInscrits = 0;
        String sql = "SELECT COUNT(*) FROM Inscription WHERE id_tournoi = ?";
        
        try (PreparedStatement pst = inscriptionDAO.getConnection().prepareStatement(sql)) {
            pst.setInt(1, tournoi.getId());
            
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    nbInscrits = rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            throw new DAOException("Erreur lors du calcul des places restantes: " + e.getMessage());
        }
        
        return 16 - nbInscrits; // Places fixes à 16
    }

    /**
     * Remplit les champs en mode modification
     */
    private void remplirChamps() {
        if (inscriptionEnCours == null) return;
        
        // Afficher les informations du tournoi
        afficherInfoTournoi(inscriptionEnCours.getTournoi());
        
        // Sélectionner l'équipe
        for (Equipe equipe : comboEquipe.getItems()) {
            if (equipe.getId().equals(inscriptionEnCours.getEquipe().getId())) {
                comboEquipe.setValue(equipe);
                break;
            }
        }
        
        // Statut
        comboStatut.setValue(inscriptionEnCours.getStatut());
        
        // Désactiver la sélection de l'équipe en mode modification
        comboEquipe.setDisable(true);
    }

    /**
     * Vérifie si l'équipe sélectionnée n'est pas déjà inscrite au tournoi
     */
    private void verifierInscriptionExistante() {
        if (!modeCreation) return; // Pas de vérification en mode modification
        
        Tournoi tournoiSelectionne = inscriptionEnCours != null ? inscriptionEnCours.getTournoi() : null;
        Equipe equipeSelectionnee = comboEquipe.getValue();
        
        if (tournoiSelectionne == null || equipeSelectionnee == null) {
            masquerMessage();
            return;
        }
        
        try {
            boolean dejaInscrite = inscriptionDAO.isEquipeInscrite(tournoiSelectionne.getId(), equipeSelectionnee.getId());
            if (dejaInscrite) {
                afficherMessage("Cette équipe est déjà inscrite à ce tournoi !", "error-message");
                btnValider.setDisable(true);
            } else {
                // Vérifier aussi les places restantes
                int placesRestantes = calculerPlacesRestantes(tournoiSelectionne);
                if (placesRestantes <= 0) {
                    afficherMessage("Le tournoi est complet (16 équipes maximum) !", "error-message");
                    btnValider.setDisable(true);
                } else {
                    masquerMessage();
                    btnValider.setDisable(false);
                }
            }
        } catch (DAOException e) {
            afficherMessage("Erreur lors de la vérification: " + e.getMessage(), "error-message");
        }
    }

    /**
     * Valide et sauvegarde l'inscription
     */
    @FXML
    private void doValider() {
        if (!validerSaisie()) {
            return;
        }
        
        try {
            Tournoi tournoi = inscriptionEnCours.getTournoi(); 
            Equipe equipe = comboEquipe.getValue();
            String statut = comboStatut.getValue();
            
            // Créer ou mettre à jour l'inscription
            inscriptionEnCours.setTournoi(tournoi);
            inscriptionEnCours.setEquipe(equipe);
            inscriptionEnCours.setStatut(statut);
            inscriptionEnCours.setSeed(0); 
            
            if (modeCreation) {
                inscriptionDAO.create(inscriptionEnCours);
                inscriptionCreee = true;
                afficherMessage("Inscription créée avec succès !", "success-message");
            } else {
                inscriptionDAO.update(inscriptionEnCours);
                inscriptionModifiee = true;
                afficherMessage("Inscription modifiée avec succès !", "success-message");
            }
            
            javafx.animation.Timeline timeline = new javafx.animation.Timeline(
                new javafx.animation.KeyFrame(javafx.util.Duration.seconds(1.5), e -> fermerFenetre())
            );
            timeline.play();
            
        } catch (DAOException e) {
            afficherMessage("Erreur lors de la sauvegarde: " + e.getMessage(), "error-message");
        }
    }

    /**
     * Annule et ferme la fenêtre
     */
    @FXML
    private void doAnnuler() {
        fermerFenetre();
    }

    /**
     * Valide la saisie des champs
     */
    private boolean validerSaisie() {
        StringBuilder erreurs = new StringBuilder();
        
        if (inscriptionEnCours == null || inscriptionEnCours.getTournoi() == null) {
            erreurs.append("- Aucun tournoi sélectionné\n");
        }
        
        if (comboEquipe.getValue() == null) {
            erreurs.append("- Veuillez sélectionner une équipe\n");
        }
        
        if (comboStatut.getValue() == null || comboStatut.getValue().trim().isEmpty()) {
            erreurs.append("- Veuillez sélectionner un statut\n");
        }
        
        if (modeCreation && inscriptionEnCours != null && inscriptionEnCours.getTournoi() != null) {
            try {
                int placesRestantes = calculerPlacesRestantes(inscriptionEnCours.getTournoi());
                if (placesRestantes <= 0) {
                    erreurs.append("- Le tournoi sélectionné est complet (16 équipes maximum)\n");
                }
            } catch (DAOException e) {
                erreurs.append("- Impossible de vérifier les places disponibles\n");
            }
        }
        
        if (erreurs.length() > 0) {
            afficherMessage("Veuillez corriger les erreurs suivantes:\n" + erreurs.toString(), "error-message");
            return false;
        }
        
        return true;
    }

    /**
     * Affiche un message
     */
    private void afficherMessage(String message, String styleClass) {
        labelMessage.setText(message);
        labelMessage.getStyleClass().clear();
        labelMessage.getStyleClass().add(styleClass);
        labelMessage.setVisible(true);
    }

    /**
     * Masque le message
     */
    private void masquerMessage() {
        labelMessage.setVisible(false);
    }

    /**
     * Ferme la fenêtre
     */
    private void fermerFenetre() {
        if (dialogStage == null) {
            dialogStage = (Stage) btnAnnuler.getScene().getWindow();
        }
        dialogStage.close();
    }

    /**
     * Définit la fenêtre de dialogue pour l'édition
     * @param dialogStage Stage de la fenêtre de dialogue
     */
    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    /**
     * Indique si une inscription a été créée
     * @return true si créée, false sinon
     */
    public boolean isInscriptionCreee() {
        return inscriptionCreee;
    }

    /**
     * Indique si une inscription a été modifiée
     * @return true si modifiée, false sinon
     */
    public boolean isInscriptionModifiee() {
        return inscriptionModifiee;
    }

    /**
     * Affiche un message d'erreur
     */
    private void showError(String titre, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Méthode pour récupérer la connexion depuis le DAO (pour calculerPlacesRestantes)
     */
    private java.sql.Connection getConnection() {
        if (inscriptionDAO != null) {
            try {
                return ((InscriptionDAO) inscriptionDAO).getConnection();
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }
}