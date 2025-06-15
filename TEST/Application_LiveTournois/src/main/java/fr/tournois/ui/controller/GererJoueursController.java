package fr.tournois.ui.controller;

import fr.tournois.dao.JoueurDAO;
import fr.tournois.model.Equipe;
import fr.tournois.model.Joueur;


import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.cell.PropertyValueFactory;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.sql.SQLException;
import java.util.List;
import fr.tournois.ui.controller.GenererPDF;


public class GererJoueursController {

    @FXML
    private TextField idField;
    @FXML
    private TextField pseudoField;
    @FXML
    private TextField nomField;
    @FXML
    private TextField prenomField;
    @FXML
    private DatePicker dateNaissancePicker;
    @FXML
    private Label equipeLabel; 
    @FXML
    private TextField rechercheField;

    @FXML
    private TableView<Joueur> joueursTable;
    @FXML
    private TableColumn<Joueur, Integer> idColumn;
    @FXML
    private TableColumn<Joueur, String> pseudoColumn;
    @FXML
    private TableColumn<Joueur, String> nomColumn;
    @FXML
    private TableColumn<Joueur, String> prenomColumn;
    @FXML
    private TableColumn<Joueur, Date> dateNaissanceColumn;

    private ObservableList<Joueur> joueurs = FXCollections.observableArrayList();
    private ObservableList<Joueur> filteredJoueurs = FXCollections.observableArrayList();
    private JoueurDAO joueurDAO;
    private Equipe equipeActuelle;
    

    @FXML
    public void initialize() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        pseudoColumn.setCellValueFactory(new PropertyValueFactory<>("pseudo"));
        nomColumn.setCellValueFactory(new PropertyValueFactory<>("nom"));
        prenomColumn.setCellValueFactory(new PropertyValueFactory<>("prenom"));
        dateNaissanceColumn.setCellValueFactory(cellData -> {
            if (cellData.getValue().getDateNaissance() != null) {
                return new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getDateNaissance());
            }
            return null;
        });

        joueursTable.setItems(filteredJoueurs);

        joueursTable.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    if (newValue != null) {
                        afficherDetailsJoueur(newValue);
                    } else {
                        viderChamps();
                    }
                });

        rechercheField.textProperty().addListener((observable, oldValue, newValue) -> filterJoueurs(newValue));
            }

        

    /**
     * Définit l'équipe actuelle dont les joueurs sont gérés.
     * Appelé depuis AppMainFrameController.
     *
     * @param equipe L'objet Equipe à gérer, ou null pour la gestion globale de tous les joueurs.
     */
    public void setEquipe(Equipe equipe) { // <<< RENOMMÉ EN setEquipe
        this.equipeActuelle = equipe; // On stocke toujours dans equipeActuelle
        if (equipeLabel != null) {
            if (equipe != null) {
                equipeLabel.setText("Gestion des joueurs de l'équipe : " + equipe.getNom());
                rafraichirJoueursParEquipe(); // Appelle la méthode pour rafraîchir les joueurs de l'équipe
            } else {
                rafraichirTousLesJoueurs(); // Appelle la méthode pour rafraîchir tous les joueurs
            }
        }
    }

    
    private void rafraichirTousLesJoueurs() {
        if (joueurDAO == null) {
            System.err.println("Le DAO Joueur n'est pas configuré. Impossible de rafraîchir tous les joueurs.");
            return;
        }
        try {
            // Assurez-vous que JoueurDAO a une méthode getTousLesJoueurs()
            List<Joueur> listeJoueurs = joueurDAO.getTousLesJoueurs();
            joueurs.setAll(listeJoueurs);
        } catch (SQLException e) {
            showError("Erreur de rafraîchissement",
                    "Erreur lors du chargement de tous les joueurs : " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void rafraichirJoueursParEquipe() {
        if (joueurDAO == null || equipeActuelle == null) {
            System.err.println("Le DAO Joueur ou l'équipe actuelle n'est pas configuré. Impossible de rafraîchir.");
            return;
        }
        try {
            List<Joueur> listeJoueurs = joueurDAO.getJoueursParEquipeId(equipeActuelle.getId());
            joueurs.setAll(listeJoueurs);
        } catch (SQLException e) {
            showError("Erreur de rafraîchissement", "Erreur lors du chargement des joueurs : " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Définit l'instance de JoueurDAO à utiliser.
     * 
     * @param joueurDAO L'instance de JoueurDAO.
     */
    public void setJoueurDAO(JoueurDAO joueurDAO) {
        this.joueurDAO = joueurDAO;
        rafraichirJoueurs(); // Charger les joueurs dès que le DAO est configuré
    }

    

    /**
     * Rafraîchit la liste des joueurs affichée.
     * Récupère TOUS les joueurs de la base de données.
     */
    private void rafraichirJoueurs() {
        if (joueurDAO == null) {
            showError("Erreur", "Le DAO Joueur n'est pas configuré. Impossible de rafraîchir.");
            return;
        }
        try {
            List<Joueur> allJoueurs = joueurDAO.getTousLesJoueurs(); // Appel correct pour tous les joueurs
            joueurs.setAll(allJoueurs);
            filterJoueurs(rechercheField.getText());
        } catch (SQLException e) {
            showError("Erreur de rafraîchissement", "Erreur lors du chargement des joueurs : " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void filterJoueurs(String searchText) {
        if (searchText == null || searchText.isEmpty()) {
            filteredJoueurs.setAll(joueurs);
        } else {
            String lowerCaseFilter = searchText.toLowerCase();
            filteredJoueurs.setAll(joueurs.stream()
                    .filter(joueur -> joueur.getPseudo().toLowerCase().contains(lowerCaseFilter) ||
                            joueur.getNom().toLowerCase().contains(lowerCaseFilter) ||
                            joueur.getPrenom().toLowerCase().contains(lowerCaseFilter))
                    .toList());
        }
    }

    private void afficherDetailsJoueur(Joueur joueur) {
        idField.setText(joueur.getId() != null ? String.valueOf(joueur.getId()) : "");
        pseudoField.setText(joueur.getPseudo());
        nomField.setText(joueur.getNom());
        prenomField.setText(joueur.getPrenom());
        if (joueur.getDateNaissance() != null) {
            dateNaissancePicker.setValue(convertToLocalDate((java.sql.Date) joueur.getDateNaissance()));
        } else {
            dateNaissancePicker.setValue(null);
        }
    }

    @FXML
    private void viderChamps() {
        idField.clear();
        pseudoField.clear();
        nomField.clear();
        prenomField.clear();
        dateNaissancePicker.setValue(null);
        joueursTable.getSelectionModel().clearSelection();
    }

    @FXML
    private void creerJoueur() {
        if (!validateFields()) {
            return;
        }
        if (joueurDAO == null) {
            showError("Erreur", "Le DAO Joueur n'est pas configuré. Impossible de créer le joueur.");
            return;
        }

        try {
            Joueur nouveauJoueur = new Joueur();
            nouveauJoueur.setPseudo(pseudoField.getText());
            nouveauJoueur.setNom(nomField.getText());
            nouveauJoueur.setPrenom(prenomField.getText());
            nouveauJoueur.setDateNaissance(convertToDate(dateNaissancePicker.getValue()));
            // L'affectation à une équipe est ignorée pour cette interface globale
            nouveauJoueur.setEquipe(null); // IMPORTANT : Définit l'équipe à null pour la création globale

            joueurDAO.ajouterJoueur(nouveauJoueur); // La méthode ajouterJoueur dans JoueurDAO gérera le NULL
            showAlert("Succès", "Joueur créé avec l'ID: " + nouveauJoueur.getId());
            rafraichirJoueurs();
            viderChamps();
        } catch (SQLException e) {
            showError("Erreur création", "Problème DB lors de la création du joueur : " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void modifierJoueur() {
        if (!validateFieldsForUpdate()) {
            return;
        }
        if (joueurDAO == null) {
            showError("Erreur", "Le DAO Joueur n'est pas configuré. Impossible de modifier le joueur.");
            return;
        }

        Joueur joueurSelectionne = joueursTable.getSelectionModel().getSelectedItem();
        if (joueurSelectionne != null) {
            try {
                joueurSelectionne.setPseudo(pseudoField.getText());
                joueurSelectionne.setNom(nomField.getText());
                joueurSelectionne.setPrenom(prenomField.getText());
                joueurSelectionne.setDateNaissance(convertToDate(dateNaissancePicker.getValue()));

                // IMPORTANT : Pour la modification en mode global, on ne change PAS l'équipe.
                // L'équipe du joueur reste celle qu'il a déjà, ou null si il n'en a pas.
                // Le JoueurDAO.mettreAJourJoueur DOIT prendre en compte cet aspect.
                // Si votre JoueurDAO est bien fait, il ne changera pas l'ID équipe si vous ne
                // le modifiez pas ici.
                // Si la colonne équipe est toujours mise à jour par setEquipe,
                // il faudrait récupérer l'équipe AVANT de modifier, ou modifier le DAO.
                // Étant donné que le JoueurDAO.mettreAJourJoueur prend un objet Joueur entier,
                // l'équipe actuelle du joueur est conservée si elle n'est pas modifiée
                // explicitement ici.

                joueurDAO.mettreAJourJoueur(joueurSelectionne);
                showAlert("Succès", "Joueur modifié avec l'ID: " + joueurSelectionne.getId());
                rafraichirJoueurs();
                viderChamps();
            } catch (SQLException e) {
                showError("Erreur modification", "Problème DB lors de la modification du joueur : " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            showError("Sélection", "Veuillez sélectionner un joueur à modifier.");
        }
    }

    @FXML
    private void supprimerJoueur() {
        if (joueurDAO == null) {
            showError("Erreur", "Le DAO Joueur n'est pas configuré. Impossible de supprimer le joueur.");
            return;
        }
        Joueur joueurSelectionne = joueursTable.getSelectionModel().getSelectedItem();
        if (joueurSelectionne != null) {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Confirmer suppression");
            confirm.setHeaderText(null);
            confirm.setContentText("Supprimer le joueur " + joueurSelectionne.getPseudo() + " ?");

            if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
                try {
                    joueurDAO.supprimerJoueur(joueurSelectionne.getId());
                    showAlert("Succès", "Joueur supprimé.");
                    rafraichirJoueurs();
                    viderChamps();
                } catch (SQLException e) {
                    showError("Erreur suppression", "Problème DB lors de la suppression du joueur : " + e.getMessage());
                    e.printStackTrace();
                }
            }
        } else {
            showError("Sélection", "Veuillez sélectionner un joueur à supprimer.");
        }
    }

    private boolean validateFields() {
        if (pseudoField.getText().trim().isEmpty() ||
                nomField.getText().trim().isEmpty() ||
                prenomField.getText().trim().isEmpty() ||
                dateNaissancePicker.getValue() == null) {
            showAlert("Validation", "Tous les champs sont obligatoires!");
            return false;
        }
        return true;
    }

    private boolean validateFieldsForUpdate() {
        if (joueursTable.getSelectionModel().getSelectedItem() == null) {
            showError("Modification", "Veuillez sélectionner un joueur à modifier.");
            return false;
        }
        return validateFields();
    }

    private void showError(String title, String message) {
        new Alert(Alert.AlertType.ERROR, message).showAndWait();
    }

    private void showAlert(String title, String message) {
        new Alert(Alert.AlertType.INFORMATION, message).showAndWait();
    }

    private Date convertToDate(LocalDate localDate) {
        if (localDate == null)
            return null;
        return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    public LocalDate convertToLocalDate(java.sql.Date date) {
        if (date == null)
            return null;
        return date.toLocalDate();
    }

    @FXML
    private void genererPDFJoueurSelectionne() {
        Joueur joueurPDF = joueursTable.getSelectionModel().getSelectedItem();

        if (joueurPDF == null) {
            showAlert("Erreur", "Veuillez sélectionner un joueur pour générer le PDF.");
            return;
        }

        try {
            GenererPDF.genererPdfPourJoueur(joueurPDF); 
            // La classe GenererPDF gère elle-même le nom du fichier.
            showAlert("Succès", "Le PDF du joueur '" + joueurPDF.getPseudo() + "' a été généré avec succès.");

        } catch (IllegalArgumentException e) {
            showError("Erreur de données", e.getMessage());
        } catch (Exception e) {
            showError("Erreur PDF", "Une erreur est survenue lors de la génération du PDF : " + e.getMessage());
            e.printStackTrace(); // Garde le printStackTrace pour le débogage
        }
    }

    @FXML
    private void genererPDFListeJoueurs() {
        List<Joueur> joueursAGenerer;
        String titrePDF;
        String nomFichierPDF;

        // Détermine la liste et les noms en fonction du contexte
        if (equipeActuelle != null) {
            joueursAGenerer = joueursTable.getItems(); // Déjà filtré par équipe
            titrePDF = "Liste des Joueurs de l'équipe : " + equipeActuelle.getNom();
            nomFichierPDF = "liste_joueurs_equipe_" + equipeActuelle.getNom();
        } else if (rechercheField != null && !rechercheField.getText().trim().isEmpty()) {
            joueursAGenerer = joueursTable.getItems(); // Déjà filtré par recherche
            titrePDF = "Liste des Joueurs Filtrés";
            nomFichierPDF = "liste_joueurs_filtres";
        } else {
            joueursAGenerer = joueurs; // Tous les joueurs
            titrePDF = "Liste de Tous les Joueurs";
            nomFichierPDF = "liste_de_tous_les_joueurs";
        }
        
        if (joueursAGenerer.isEmpty()) {
            showAlert("Information", "Aucun joueur à générer dans la liste.");
            return;
        }

        try {
            // GenererPDF gère l'ajout du ".pdf" et la propreté du nom.
            GenererPDF.genererPdfListeJoueurs(joueursAGenerer, titrePDF, nomFichierPDF); 
            showAlert("Succès", "Le PDF '" + nomFichierPDF + ".pdf' a été généré avec succès.");
        } catch (IllegalArgumentException e) {
            showError("Erreur de données", e.getMessage());
        } catch (Exception e) {
            showError("Erreur PDF", "Une erreur est survenue lors de la génération du PDF de la liste : " + e.getMessage());
            e.printStackTrace();
        }
    }

}
