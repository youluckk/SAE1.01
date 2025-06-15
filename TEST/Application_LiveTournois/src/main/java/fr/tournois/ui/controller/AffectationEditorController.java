package fr.tournois.ui.controller;

import fr.tournois.dao.DAOException;
import fr.tournois.dao.StaffDAO;
import fr.tournois.model.Affectation;
import fr.tournois.model.Staff;

import java.util.ArrayList;
import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.ListCell;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class AffectationEditorController {
    @FXML private ComboBox<Staff> staffComboBox;
    @FXML private TextField roleField;
    @FXML private DatePicker dateDebutPicker;
    @FXML private DatePicker dateFinPicker;
    @FXML private Button btnOk;
    @FXML private Button btnCancel;

    private Stage dialogStage;
    private Affectation affectation;
    private boolean okClicked = false;
    private StaffDAO staffDAO;

    @FXML
    private void initialize() {
        dateDebutPicker.setValue(LocalDate.now());
        dateFinPicker.setValue(LocalDate.now());
        
        // Configuration de l'affichage des staff dans la combobox
        staffComboBox.setCellFactory(lv -> new ListCell<Staff>() {
            @Override
            protected void updateItem(Staff staff, boolean empty) {
                super.updateItem(staff, empty);
                if (empty || staff == null) {
                    setText(null);
                } else {
                    setText(staff.getNom() + " (" + staff.getFonction() + ")");
                }
            }
        });
        staffComboBox.setButtonCell(staffComboBox.getCellFactory().call(null));
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public void setAffectation(Affectation affectation) {
        this.affectation = affectation;

        if (affectation != null) {
            staffComboBox.setValue(affectation.getStaff());
            roleField.setText(affectation.getRoleSpecifique());
            if (affectation.getDateDebut() != null) {
                dateDebutPicker.setValue(affectation.getDateDebut().toLocalDate());
            }
            if (affectation.getDateFin() != null) {
                dateFinPicker.setValue(affectation.getDateFin().toLocalDate());
            }
        }
    }

    public void setStaffDAO(StaffDAO staffDAO) {
        this.staffDAO = staffDAO;
        if (affectation != null && affectation.getTournoi() != null) {
            try {
                // Récupérer tous les staff
                List<Staff> allStaff = this.staffDAO.findAll();
                // Récupérer les staff déjà affectés au tournoi
                List<Staff> staffAffectes = this.staffDAO.findByTournoi(affectation.getTournoi());
                // Filtrer pour ne garder que les staff non affectés

                List<Staff> staffDisponibles = new ArrayList<>();
                boolean trouve;

                for (Staff staff : allStaff) {
                    trouve = false;
                    for (Staff staffAffecte : staffAffectes) {
                        if (staff.getId().equals(staffAffecte.getId())) {
                            trouve = true;
                        }
                    }
                    if (!trouve) {
                        staffDisponibles.add(staff);
                    }
                }
                // Mettre à jour la combobox
                staffComboBox.getItems().setAll(staffDisponibles);
            } catch (DAOException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Erreur");
                alert.setHeaderText("Impossible de charger la liste des staff");
                alert.setContentText(e.getMessage());
                alert.showAndWait();
            }
        }
    }

    public boolean isOkClicked() {
        return okClicked;
    }

    @FXML
    private void doOk() {
        if (isInputValid()) {
            affectation.setStaff(staffComboBox.getValue());
            affectation.setRoleSpecifique(roleField.getText());
            affectation.setDateDebut(LocalDateTime.of(dateDebutPicker.getValue(), LocalTime.MIDNIGHT));
            affectation.setDateFin(LocalDateTime.of(dateFinPicker.getValue(), LocalTime.MIDNIGHT));

            okClicked = true;
            dialogStage.close();
        }
    }

    @FXML
    private void doCancel() {
        dialogStage.close();
    }

    private boolean isInputValid() {
        String errorMessage = "";

        if (staffComboBox.getValue() == null) {
            errorMessage += "Veuillez sélectionner un staff !\n";
        }
        if (roleField.getText() == null || roleField.getText().trim().isEmpty()) {
            errorMessage += "Le rôle spécifique ne peut pas être vide !\n";
        }
        if (dateDebutPicker.getValue() == null) {
            errorMessage += "Veuillez sélectionner une date de début !\n";
        }
        if (dateFinPicker.getValue() == null) {
            errorMessage += "Veuillez sélectionner une date de fin !\n";
        }
        if (dateDebutPicker.getValue() != null && dateFinPicker.getValue() != null 
            && dateDebutPicker.getValue().isAfter(dateFinPicker.getValue())) {
            errorMessage += "La date de début doit être antérieure à la date de fin !\n";
        }

        if (errorMessage.isEmpty()) {
            return true;
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur de saisie");
            alert.setHeaderText("Veuillez corriger les erreurs suivantes :");
            alert.setContentText(errorMessage);
            alert.initOwner(dialogStage);
            alert.showAndWait();
            return false;
        }
    }
}
