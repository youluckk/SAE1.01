package fr.tournois.ui.controller;

import java.io.File;
import java.io.IOException;
import java.util.List;

import fr.tournois.dao.TournoiDAO;
import fr.tournois.model.Tournoi;
import fr.tournois.ui.util.PdfTournoiGenerator;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class ExportTournoiController {
    
    @FXML
    private ComboBox<Tournoi> comboTournois;
    
    private TournoiDAO tournoiDAO;
    private Stage parentStage;
    
    // Méthode pour recevoir les DAOs (même pattern que les autres contrôleurs)
    public void setDAOs(TournoiDAO tournoiDAO) {
        this.tournoiDAO = tournoiDAO;
        loadTournois();
    }
    
    // Méthode pour recevoir le stage parent (même pattern)
    public void setParentStage(Stage parentStage) {
        this.parentStage = parentStage;
    }
    
    @FXML
    public void initialize() {
        // NE RIEN FAIRE ICI avec tournoiDAO car il est encore null
        System.out.println("DEBUG: Controller initialisé, en attente des DAOs...");
    }
    
    private void loadTournois() {
        if (tournoiDAO == null) {
            System.err.println("ERREUR: tournoiDAO est null dans loadTournois()");
            return;
        }
        
        try {
            System.out.println("DEBUG: Chargement des tournois...");
            List<Tournoi> tournoisList = tournoiDAO.findAll();
            comboTournois.getItems().clear();
            comboTournois.getItems().addAll(tournoisList);
            System.out.println("DEBUG: " + tournoisList.size() + " tournois chargés");
        } catch (Exception e) {
            System.err.println("ERREUR lors du chargement: " + e.getMessage());
            e.printStackTrace();
            showAlert("Erreur lors du chargement des tournois : " + e.getMessage());
        }
    }
    
    @FXML
    public void handleExporterPDF() {
        Tournoi tournoi = comboTournois.getValue();
        if (tournoi == null) {
            showAlert("Veuillez sélectionner un tournoi.");
            return;
        }
        
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Enregistrer le PDF");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF files", "*.pdf"));
        fileChooser.setInitialFileName("tournoi_" + tournoi.getNom().replaceAll("\\s+", "_") + ".pdf");
        
        File file = fileChooser.showSaveDialog(parentStage);
        
        if (file != null) {
            try {
                PdfTournoiGenerator.genererPDFTournoi(tournoi, file.getAbsolutePath());
                showAlert("PDF généré avec succès !");
            } catch (IOException | com.itextpdf.text.DocumentException e) {
                e.printStackTrace();
                showAlert("Erreur lors de la génération du PDF : " + e.getMessage());
            }
        }
    }
    
    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}